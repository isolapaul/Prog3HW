package hu.prog3.offlinechatprog3;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.controller.RegistrationResult;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.Permissions;
import hu.prog3.offlinechatprog3.persistence.DataStore;
import hu.prog3.offlinechatprog3.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    private static final File DATA_FILE = new File("data/offline-chat.dat");

    private void cleanup() {
        if (DATA_FILE.exists()) DATA_FILE.delete();
    }

    private String hash(String pw) {
        return PasswordUtil.hashPassword(pw);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void testRegisterAndAuthenticate() {
        cleanup();
        AppController c = new AppController();
        String hashedPw = hash("pw123");
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hashedPw));
        assertTrue(c.authenticateUser("alice", "pw123"));
        assertFalse(c.authenticateUser("alice", "wrongpw"));
    }

    @Test
    void testRegistrationValidation() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.USERNAME_TOO_SHORT, c.registerUser("ab", hash("pw")));
        assertEquals(RegistrationResult.USERNAME_TOO_LONG, c.registerUser("verylongusernamemorethan20chars", hash("pw")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("pw")));
        assertEquals(RegistrationResult.USERNAME_ALREADY_TAKEN, c.registerUser("alice", hash("pw2")));
    }

    @Test
    void testFriendRequestWorkflow() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        DataStore store = c.getDataStore();

        assertTrue(store.sendFriendRequest("alice", "bob"));
        c.saveStore();
        Set<String> incoming = store.getIncomingFriendRequests("bob");
        assertTrue(incoming.contains("alice"));
        
        assertTrue(store.acceptFriendRequest("bob", "alice"));
        c.saveStore();
        assertTrue(store.getFriends("alice").contains("bob"));
        assertTrue(store.getFriends("bob").contains("alice"));
    }

    @Test
    void testPrivateMessaging() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        DataStore store = c.getDataStore();

        store.sendFriendRequest("alice", "bob");
        store.acceptFriendRequest("bob", "alice");
        c.saveStore();
        
        assertTrue(c.sendPrivateMessage("alice", "bob", "hello"));
        assertTrue(c.sendPrivateMessage("bob", "alice", "hi there"));
        
        List<Message> msgs = store.getPrivateMessages("alice", "bob");
        assertEquals(2, msgs.size());
        assertEquals("hello", msgs.get(0).getContent());
        assertEquals("hi there", msgs.get(1).getContent());
    }

    @Test
    void testCannotMessageNonFriends() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));

        assertFalse(c.sendPrivateMessage("alice", "bob", "yo"));
    }

    @Test
    void testGroupCreation() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("pw")));
        
        UUID gid = c.createGroup("MyGroup", "alice");
        DataStore store = c.getDataStore();
        assertNotNull(gid);
        assertTrue(store.getAllGroups().containsKey(gid));
        assertEquals("MyGroup", store.getAllGroups().get(gid));
        assertTrue(c.isGroupAdmin(gid, "alice"));
    }

    @Test
    void testGroupMembers() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        
        UUID gid = c.createGroup("Team", "alice");
        assertTrue(c.addGroupMember(gid, "bob", "Résztvevő"));
        
        Set<String> members = c.getGroupMembers(gid);
        assertTrue(members.contains("alice"));
        assertTrue(members.contains("bob"));
        assertEquals(2, members.size());
    }

    @Test
    void testRemoveGroupMember() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        
        UUID gid = c.createGroup("Team", "alice");
        c.addGroupMember(gid, "bob", "Résztvevő");
        
        assertTrue(c.removeGroupMember(gid, "bob"));
        assertFalse(c.getGroupMembers(gid).contains("bob"));
    }

    @Test
    void testGroupMessaging() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        
        UUID gid = c.createGroup("Chat", "alice");
        assertTrue(c.sendGroupMessage(gid, "alice", "hello everyone"));
        DataStore store = c.getDataStore();
        
        List<Message> msgs = store.getGroupMessages(gid);
        assertEquals(1, msgs.size());
        assertEquals("hello everyone", msgs.get(0).getContent());
    }

    @Test
    void testGroupPermissions() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        
        UUID gid = c.createGroup("Restricted", "alice");
        c.addGroupMember(gid, "bob", "Olvasó");
        
        // Admin has all permissions
        assertTrue(c.hasGroupPermission(gid, "alice", Permissions.GROUP_SEND_MESSAGE));
        assertTrue(c.hasGroupPermission(gid, "alice", Permissions.GROUP_DELETE_MESSAGES));
        
        // Olvasó (Reader) cannot send or delete
        assertFalse(c.hasGroupPermission(gid, "bob", Permissions.GROUP_SEND_MESSAGE));
        assertFalse(c.sendGroupMessage(gid, "bob", "test"));
    }

    @Test
    void testDeleteGroupMessage() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        
        UUID gid = c.createGroup("Chat", "alice");
        c.sendGroupMessage(gid, "alice", "test message");
        DataStore store = c.getDataStore();
        
        List<Message> msgs = store.getGroupMessages(gid);
        UUID msgId = msgs.get(0).getId();
        
        assertTrue(c.deleteGroupMessage(gid, msgId, "alice"));
        assertEquals(0, store.getGroupMessages(gid).size());
    }

    @Test
    void testDeleteGroup() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        
        UUID gid = c.createGroup("TempGroup", "alice");
        DataStore store = c.getDataStore();
        assertTrue(c.deleteGroup(gid, "alice"));
        assertFalse(store.getAllGroups().containsKey(gid));
    }

    @Test
    void testSetGroupMemberRole() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        
        UUID gid = c.createGroup("Team", "alice");
        c.addGroupMember(gid, "bob", "Résztvevő");
        
        assertFalse(c.isGroupAdmin(gid, "bob"));
        assertTrue(c.setGroupMemberRole(gid, "bob", "Adminisztrátor"));
        assertTrue(c.isGroupAdmin(gid, "bob"));
    }

    @Test
    void testCustomRoleWithPermissions() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));
        
        UUID gid = c.createGroup("CustomRoles", "alice");
        assertTrue(c.addCustomRole(gid, "Moderator"));
        assertTrue(c.setRolePermissions(gid, "Moderator", Set.of(
            Permissions.GROUP_SEND_MESSAGE,
            Permissions.GROUP_DELETE_MESSAGES
        )));
        assertTrue(c.addGroupMember(gid, "bob", "Moderator"));
        
        // Moderator can send and delete messages
        assertTrue(c.hasGroupPermission(gid, "bob", Permissions.GROUP_SEND_MESSAGE));
        assertTrue(c.hasGroupPermission(gid, "bob", Permissions.GROUP_DELETE_MESSAGES));
        // But cannot delete the group
        assertFalse(c.hasGroupPermission(gid, "bob", Permissions.GROUP_DELETE_GROUP));
    }

    @Test
    void testRemoveFriend() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("alice", hash("p")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("p")));

        DataStore store = c.getDataStore();
        store.sendFriendRequest("alice", "bob");
        store.acceptFriendRequest("bob", "alice");
        c.saveStore();
        assertTrue(store.getFriends("alice").contains("bob"));
        
        assertTrue(store.removeFriend("alice", "bob"));
        c.saveStore();
        assertFalse(store.getFriends("alice").contains("bob"));
        assertFalse(store.getFriends("bob").contains("alice"));
    }

    @Test
    void testPersistence() {
        cleanup();
        
        // Create data
        AppController c1 = new AppController();
        String hashedPw = hash("test123");
        assertEquals(RegistrationResult.SUCCESS, c1.registerUser("alice", hashedPw));
        UUID gid = c1.createGroup("SavedGroup", "alice");
        c1.sendGroupMessage(gid, "alice", "persistent message");
        assertTrue(c1.saveStore());
        
        // Load data in new controller
        AppController c2 = new AppController();
        DataStore store2 = c2.getDataStore();
        assertTrue(c2.authenticateUser("alice", "test123"));
        assertTrue(store2.getAllGroups().containsKey(gid));
        List<Message> msgs = store2.getGroupMessages(gid);
        assertEquals(1, msgs.size());
        assertEquals("persistent message", msgs.get(0).getContent());
    }
}
