package hu.prog3.offlinechatprog3.controller;

import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.Permissions;
import hu.prog3.offlinechatprog3.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppControllerTest {

    private void cleanupDataFile() {
        File f = new File("data/offline-chat.dat");
        if (f.exists()) f.delete();
    }

    private String hash(String pw) {
        return PasswordUtil.hashPassword(pw);
    }

    @AfterEach
    void tearDown() {
        cleanupDataFile();
    }

    @Test
    void friendRequestLifecycle_and_privateMessaging() {
        
        AppController c = new AppController();
        assertTrue(c.registerUser("tesztElek", hash("p")));
        assertTrue(c.registerUser("bob", hash("p")));

        assertTrue(c.sendFriendRequest("tesztElek", "bob"));
        assertTrue(c.acceptFriendRequest("bob", "tesztElek"));
        assertTrue(c.sendPrivateMessage("tesztElek", "bob", "hello"));
        List<Message> msgs = c.getPrivateMessages("tesztElek", "bob");
        assertEquals(1, msgs.size());
        assertEquals("hello", msgs.get(0).getContent());
        assertEquals("tesztElek", c.getUsernameForId(msgs.get(0).getSenderId()));
    }

    @Test
    void groupMessaging_permissions() {
        cleanupDataFile();
        AppController c = new AppController();
        assertTrue(c.registerUser("isolapaul", "jelszo"));
        assertTrue(c.registerUser("bob", "pass"));

        UUID gid = c.createGroup("csoportom", "isolapaul");
        assertNotNull(gid);

        assertTrue(c.addCustomRole(gid, "Kuldo"));
        assertTrue(c.setRolePermissions(gid, "Kuldo", Set.of(Permissions.GROUP_SEND_MESSAGE)));
        assertTrue(c.addGroupMember(gid, "bob", "Kuldo"));

        assertTrue(c.sendGroupMessage(gid, "bob", "szia"));
        List<Message> gmsgs = c.getGroupMessages(gid);
        assertEquals(1, gmsgs.size());
        assertEquals("szia", gmsgs.get(0).getContent());
        assertEquals("bob", c.getUsernameForId(gmsgs.get(0).getSenderId()));
    }

    @Test
    void testDeleteGroupMessage() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("tesztElek", "pass1");
        
        UUID gid = c.createGroup("Teszt", "tesztElek");
        c.sendGroupMessage(gid, "tesztElek", "ez egy uzenet");
        
        List<Message> msgs = c.getGroupMessages(gid);
        assertEquals(1, msgs.size());
        
        UUID msgId = msgs.get(0).getId();
        assertTrue(c.deleteGroupMessage(gid, msgId, "tesztElek"));
        
        msgs = c.getGroupMessages(gid);
        assertEquals(0, msgs.size());
    }

    @Test
    void testDeleteGroup() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("isolapaul", "pw");
        
        UUID gid = c.createGroup("CsoportX", "isolapaul");
        assertNotNull(gid);
        
        assertTrue(c.deleteGroup(gid, "isolapaul"));
        assertFalse(c.getAllGroups().containsKey(gid));
    }

    @Test
    void testGetGroupMembers() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("DondiDuo", "duo123");
        c.registerUser("bob", "bob456");
        
        UUID gid = c.createGroup("MyCrew", "DondiDuo");
        c.addGroupMember(gid, "bob", "Résztvevő");
        
        Set<String> members = c.getGroupMembers(gid);
        assertTrue(members.contains("DondiDuo"));
        assertTrue(members.contains("bob"));
    }

    @Test
    void testRemoveGroupMember() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("isolapaul", "12345");
        c.registerUser("tesztElek", "67890");
        
        UUID gid = c.createGroup("ProbaG", "isolapaul");
        c.addGroupMember(gid, "tesztElek", "Résztvevő");
        
        assertTrue(c.removeGroupMember(gid, "tesztElek"));
        assertFalse(c.getGroupMembers(gid).contains("tesztElek"));
    }

    @Test
    void testSetGroupMemberRole() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("bob", "bobpass");
        c.registerUser("DondiDuo", "duopass");
        
        UUID gid = c.createGroup("Squad", "bob");
        c.addGroupMember(gid, "DondiDuo", "Résztvevő");
        
        assertTrue(c.setGroupMemberRole(gid, "DondiDuo", "Adminisztrátor"));
        assertTrue(c.isGroupAdmin(gid, "DondiDuo"));
    }

    @Test
    void testGetAllUsernames() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("tesztElek", "1234");
        c.registerUser("isolapaul", "5678");
        
        Set<String> usernames = c.getAllUsernames();
        assertTrue(usernames.contains("tesztElek"));
        assertTrue(usernames.contains("isolapaul"));
    }

    @Test
    void testAuthenticationFailure() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("DondiDuo", "correctpw");
        
        assertFalse(c.authenticateUser("DondiDuo", "wrongpw"));
    }

    @Test
    void testRegisterDuplicateUser() {
        cleanupDataFile();
        AppController c = new AppController();
        assertTrue(c.registerUser("bob", "pw1"));
        assertFalse(c.registerUser("bob", "pw2"));
    }

    @Test
    void testSendFriendRequestToNonExistent() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("tesztElek", "xyz");
        
        assertFalse(c.sendFriendRequest("tesztElek", "nemletezik"));
    }

    @Test
    void testAcceptNonExistentFriendRequest() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("isolapaul", "abc");
        
        assertFalse(c.acceptFriendRequest("isolapaul", "nincskerelem"));
    }

    @Test
    void testGetGroupAvailableRoles() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("bob", "hello");
        
        UUID gid = c.createGroup("Roles", "bob");
        Set<String> roles = c.getGroupAvailableRoles(gid);
        
        assertTrue(roles.contains("Adminisztrátor"));
        assertTrue(roles.contains("Résztvevő"));
        assertTrue(roles.contains("Olvasó"));
    }

    @Test
    void testGetGroupMembersWithRoles() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("DondiDuo", "qwerty");
        c.registerUser("tesztElek", "asdfgh");
        
        UUID gid = c.createGroup("Team", "DondiDuo");
        c.addGroupMember(gid, "tesztElek", "Résztvevő");
        
        var membersWithRoles = c.getGroupMembersWithRoles(gid);
        assertEquals("Adminisztrátor", membersWithRoles.get("DondiDuo"));
        assertEquals("Résztvevő", membersWithRoles.get("tesztElek"));
    }

    @Test
    void testSendPrivateMessageWithoutFriendship() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("isolapaul", "pw1");
        c.registerUser("bob", "pw2");
        
        assertFalse(c.sendPrivateMessage("isolapaul", "bob", "yo"));
    }

    @Test
    void testMultipleFriendRequests() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("tesztElek", "aaa");
        c.registerUser("bob", "bbb");
        c.registerUser("DondiDuo", "ccc");
        
        c.sendFriendRequest("tesztElek", "bob");
        c.sendFriendRequest("tesztElek", "DondiDuo");
        c.acceptFriendRequest("bob", "tesztElek");
        
        assertTrue(c.sendPrivateMessage("tesztElek", "bob", "szia bob"));
    }

    @Test
    void testHasGroupPermission() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("isolapaul", "mypass");
        
        UUID gid = c.createGroup("PermTest", "isolapaul");
        
        assertTrue(c.hasGroupPermission(gid, "isolapaul", Permissions.GROUP_SEND_MESSAGE));
        assertTrue(c.hasGroupPermission(gid, "isolapaul", Permissions.GROUP_DELETE_MESSAGES));
    }

    @Test
    void testSendGroupMessageWithoutPermission() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("DondiDuo", "x1");
        c.registerUser("tesztElek", "x2");
        
        UUID gid = c.createGroup("NoSend", "DondiDuo");
        c.addGroupMember(gid, "tesztElek", "Olvasó");
        
        assertFalse(c.sendGroupMessage(gid, "tesztElek", "cant send this"));
    }

    @Test
    void testDeleteGroupMessageWithoutPermission() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("bob", "p1");
        c.registerUser("isolapaul", "p2");
        
        UUID gid = c.createGroup("DelTest", "bob");
        c.sendGroupMessage(gid, "bob", "msg");
        c.addGroupMember(gid, "isolapaul", "Résztvevő");
        
        List<Message> msgs = c.getGroupMessages(gid);
        UUID msgId = msgs.get(0).getId();
        
        assertFalse(c.deleteGroupMessage(gid, msgId, "isolapaul"));
    }

    @Test
    void testDeleteGroupWithoutPermission() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("tesztElek", "zxc");
        c.registerUser("DondiDuo", "vbn");
        
        UUID gid = c.createGroup("NoDel", "tesztElek");
        c.addGroupMember(gid, "DondiDuo", "Résztvevő");
        
        assertFalse(c.deleteGroup(gid, "DondiDuo"));
    }

    @Test
    void testGetUsernameForNonExistentId() {
        cleanupDataFile();
        AppController c = new AppController();
        
        UUID randomId = UUID.randomUUID();
        assertNull(c.getUsernameForId(randomId));
    }

    @Test
    void testInvalidOperationsWithNullGroup() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("bob", "passw");
        
        assertFalse(c.sendGroupMessage(null, "bob", "null group"));
        assertFalse(c.deleteGroup(null, "bob"));
    }

    @Test
    void testMultipleMessagesInPrivateChat() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("isolapaul", "1");
        c.registerUser("bob", "2");
        
        c.sendFriendRequest("isolapaul", "bob");
        c.acceptFriendRequest("bob", "isolapaul");
        
        c.sendPrivateMessage("isolapaul", "bob", "szia");
        c.sendPrivateMessage("bob", "isolapaul", "hello");
        c.sendPrivateMessage("isolapaul", "bob", "hogy vagy?");
        
        List<Message> msgs = c.getPrivateMessages("isolapaul", "bob");
        assertEquals(3, msgs.size());
    }

    @Test
    void testAddGroupMemberMultipleRoles() {
        cleanupDataFile();
        AppController c = new AppController();
        c.registerUser("DondiDuo", "dd");
        c.registerUser("tesztElek", "te");
        
        UUID gid = c.createGroup("MultiRole", "DondiDuo");
        c.addCustomRole(gid, "Mod");
        c.setRolePermissions(gid, "Mod", Set.of(Permissions.GROUP_SEND_MESSAGE, Permissions.GROUP_DELETE_MESSAGES));
        
        assertTrue(c.addGroupMember(gid, "tesztElek", "Mod"));
        assertTrue(c.hasGroupPermission(gid, "tesztElek", Permissions.GROUP_SEND_MESSAGE));
        assertTrue(c.hasGroupPermission(gid, "tesztElek", Permissions.GROUP_DELETE_MESSAGES));
    }
}
