package hu.prog3.offlinechatprog3.controller;

import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.Permissions;
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
        if (f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    @AfterEach
    void tearDown() {
        cleanupDataFile();
    }

    @Test
    void friendRequestLifecycle_and_privateMessaging() {
        cleanupDataFile();
        AppController c = new AppController();
        assertTrue(c.registerUser("alice", "p"));
        assertTrue(c.registerUser("bob", "p"));

        assertTrue(c.sendFriendRequest("alice", "bob"));
        assertTrue(c.acceptFriendRequest("bob", "alice"));

        // Now friends; private messaging should work
        assertTrue(c.sendPrivateMessage("alice", "bob", "hello"));
        List<Message> msgs = c.getPrivateMessages("alice", "bob");
        assertEquals(1, msgs.size());
        assertEquals("hello", msgs.get(0).getContent());
        assertEquals("alice", c.getUsernameForId(msgs.get(0).getSenderId()));
    }

    @Test
    void groupMessaging_permissions() {
        cleanupDataFile();
        AppController c = new AppController();
        assertTrue(c.registerUser("alice", "p"));
        assertTrue(c.registerUser("bob", "p"));

        UUID gid = c.createGroup("g1", "alice");
        assertNotNull(gid);

        // Create a role that allows sending and assign to bob
        assertTrue(c.addCustomRole(gid, "Sender"));
        assertTrue(c.setRolePermissions(gid, "Sender", Set.of(Permissions.GROUP_SEND_MESSAGE)));
        assertTrue(c.addGroupMember(gid, "bob", "Sender"));

        // Bob can send now
        assertTrue(c.sendGroupMessage(gid, "bob", "hi"));
        List<Message> gmsgs = c.getGroupMessages(gid);
        assertEquals(1, gmsgs.size());
        assertEquals("hi", gmsgs.get(0).getContent());
        assertEquals("bob", c.getUsernameForId(gmsgs.get(0).getSenderId()));
    }
}
