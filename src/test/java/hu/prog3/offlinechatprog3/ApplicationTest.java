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
        String hashedPw = hash("jelszo123");
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hashedPw));
        assertTrue(c.authenticateUser("tesztElek", "jelszo123"));
        assertFalse(c.authenticateUser("tesztElek", "rosszjelszo"));
    }

    @Test
    void testRegistrationValidation() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.USERNAME_TOO_SHORT, c.registerUser("xy", hash("jelszo123")));
        assertEquals(RegistrationResult.USERNAME_TOO_LONG, c.registerUser("nagyonhosszufelhasznalonevnemleszjo", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.USERNAME_ALREADY_TAKEN, c.registerUser("tesztElek", hash("jelszo123")));
    }

    @Test
    void testFriendRequestWorkflow() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        DataStore store = c.getDataStore();

        assertTrue(store.sendFriendRequest("tesztElek", "bob"));
        c.saveStore();
        Set<String> incoming = store.getIncomingFriendRequests("bob");
        assertTrue(incoming.contains("tesztElek"));
        
        assertTrue(store.acceptFriendRequest("bob", "tesztElek"));
        c.saveStore();
        assertTrue(store.getFriends("tesztElek").contains("bob"));
        assertTrue(store.getFriends("bob").contains("tesztElek"));
    }

    @Test
    void testPrivateMessaging() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        DataStore store = c.getDataStore();

        store.sendFriendRequest("tesztElek", "bob");
        store.acceptFriendRequest("bob", "tesztElek");
        c.saveStore();
        
        assertTrue(c.sendPrivateMessage("tesztElek", "bob", "sziahalo"));
        assertTrue(c.sendPrivateMessage("bob", "tesztElek", "helobelo"));
        
        List<Message> msgs = store.getPrivateMessages("tesztElek", "bob");
        assertEquals(2, msgs.size());
        assertEquals("sziahalo", msgs.get(0).getContent());
        assertEquals("helobelo", msgs.get(1).getContent());
    }

    @Test
    void testCannotMessageNonFriends() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));

        assertFalse(c.sendPrivateMessage("tesztElek", "bob", "szia"));
    }

    @Test
    void testGroupCreation() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        
        UUID gid = c.createGroup("SajatCsoport", "tesztElek");
        DataStore store = c.getDataStore();
        assertNotNull(gid);
        assertTrue(store.getAllGroups().containsKey(gid));
        assertEquals("SajatCsoport", store.getAllGroups().get(gid));
        assertTrue(c.isGroupAdmin(gid, "tesztElek"));
    }

    @Test
    void testGroupMembers() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        
        UUID gid = c.createGroup("Csapat", "tesztElek");
        assertTrue(c.addGroupMember(gid, "bob", "Résztvevő"));
        
        Set<String> members = c.getGroupMembers(gid);
        assertTrue(members.contains("tesztElek"));
        assertTrue(members.contains("bob"));
        assertEquals(2, members.size());
    }

    @Test
    void testRemoveGroupMember() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        
        UUID gid = c.createGroup("Csapat", "tesztElek");
        c.addGroupMember(gid, "bob", "Résztvevő");
        
        assertTrue(c.removeGroupMember(gid, "bob"));
        assertFalse(c.getGroupMembers(gid).contains("bob"));
    }

    @Test
    void testGroupMessaging() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        
        UUID gid = c.createGroup("Beszelgetes", "tesztElek");
        assertTrue(c.sendGroupMessage(gid, "tesztElek", "sziasztok"));
        DataStore store = c.getDataStore();
        
        List<Message> msgs = store.getGroupMessages(gid);
        assertEquals(1, msgs.size());
        assertEquals("sziasztok", msgs.get(0).getContent());
    }

    @Test
    void testGroupPermissions() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        
        UUID gid = c.createGroup("Korlatozott", "tesztElek");
        c.addGroupMember(gid, "bob", "Olvasó");
        
        //admin minden jogosultsággal rendelkezik
        assertTrue(c.hasGroupPermission(gid, "tesztElek", Permissions.GROUP_SEND_MESSAGE));
        assertTrue(c.hasGroupPermission(gid, "tesztElek", Permissions.GROUP_DELETE_MESSAGES));
        
        //olvasó nem küldhet és nem törölhet
        assertFalse(c.hasGroupPermission(gid, "bob", Permissions.GROUP_SEND_MESSAGE));
        assertFalse(c.sendGroupMessage(gid, "bob", "teszt"));
    }

    @Test
    void testDeleteGroupMessage() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        
        UUID gid = c.createGroup("Beszelgetes", "tesztElek");
        c.sendGroupMessage(gid, "tesztElek", "torles");
        DataStore store = c.getDataStore();
        
        List<Message> msgs = store.getGroupMessages(gid);
        UUID msgId = msgs.get(0).getId();
        
        assertTrue(c.deleteGroupMessage(gid, msgId, "tesztElek"));
        assertEquals(0, store.getGroupMessages(gid).size());
    }

    @Test
    void testDeleteGroup() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        
        UUID gid = c.createGroup("Csoport", "tesztElek");
        DataStore store = c.getDataStore();
        assertTrue(c.deleteGroup(gid, "tesztElek"));
        assertFalse(store.getAllGroups().containsKey(gid));
    }

    @Test
    void testSetGroupMemberRole() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        
        UUID gid = c.createGroup("Csapat", "tesztElek");
        c.addGroupMember(gid, "bob", "Résztvevő");
        
        assertFalse(c.isGroupAdmin(gid, "bob"));
        assertTrue(c.setGroupMemberRole(gid, "bob", "Adminisztrátor"));
        assertTrue(c.isGroupAdmin(gid, "bob"));
    }

    @Test
    void testCustomRoleWithPermissions() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));
        
        UUID gid = c.createGroup("Csoport", "tesztElek");
        assertTrue(c.addCustomRole(gid, "Moderátor"));
        assertTrue(c.setRolePermissions(gid, "Moderátor", Set.of(
            Permissions.GROUP_SEND_MESSAGE,
            Permissions.GROUP_DELETE_MESSAGES
        )));
        assertTrue(c.addGroupMember(gid, "bob", "Moderátor"));
        
        //moderátor küldhet és törölhet üzeneteket
        assertTrue(c.hasGroupPermission(gid, "bob", Permissions.GROUP_SEND_MESSAGE));
        assertTrue(c.hasGroupPermission(gid, "bob", Permissions.GROUP_DELETE_MESSAGES));
        //de nem törölheti a csoportot
        assertFalse(c.hasGroupPermission(gid, "bob", Permissions.GROUP_DELETE_GROUP));
    }

    @Test
    void testRemoveFriend() {
        cleanup();
        AppController c = new AppController();
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("tesztElek", hash("jelszo123")));
        assertEquals(RegistrationResult.SUCCESS, c.registerUser("bob", hash("jelszo123")));

        DataStore store = c.getDataStore();
        store.sendFriendRequest("tesztElek", "bob");
        store.acceptFriendRequest("bob", "tesztElek");
        c.saveStore();
        assertTrue(store.getFriends("tesztElek").contains("bob"));
        
        assertTrue(store.removeFriend("tesztElek", "bob"));
        c.saveStore();
        assertFalse(store.getFriends("tesztElek").contains("bob"));
        assertFalse(store.getFriends("bob").contains("tesztElek"));
    }

    @Test
    void testPersistence() {
        cleanup();
        
        //adatok létrehozása
        AppController c1 = new AppController();
        String hashedPw = hash("jelszo123");
        assertEquals(RegistrationResult.SUCCESS, c1.registerUser("tesztElek", hashedPw));
        UUID gid = c1.createGroup("MentettCsoport", "tesztElek");
        c1.sendGroupMessage(gid, "tesztElek", "tartos uzenet");
        assertTrue(c1.saveStore());
        
        //ddatok betöltése új controllerben
        AppController c2 = new AppController();
        DataStore store2 = c2.getDataStore();
        assertTrue(c2.authenticateUser("tesztElek", "jelszo123"));
        assertTrue(store2.getAllGroups().containsKey(gid));
        List<Message> msgs = store2.getGroupMessages(gid);
        assertEquals(1, msgs.size());
        assertEquals("tartos uzenet", msgs.get(0).getContent());
    }
}
