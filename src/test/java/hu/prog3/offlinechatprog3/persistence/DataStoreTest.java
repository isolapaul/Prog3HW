package hu.prog3.offlinechatprog3.persistence;

import hu.prog3.offlinechatprog3.model.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    private final DataStore store = new DataStore();

    @AfterEach
    void cleanup() {
        store.clearAll();
    }

    @Test
    void registerUser() {
        assertTrue(store.registerUser("tesztElek", "jelszo123"));
        assertFalse(store.registerUser("tesztElek", "masikjelszo"));
        assertNotNull(store.getUserByName("tesztElek"));
        assertEquals("tesztElek", store.getUserByName("tesztElek").getUsername());
    }

    @Test
    void friendOperations() {
        store.registerUser("bob", "pw1");
        store.registerUser("isolapaul", "pw2");
        assertTrue(store.addFriend("bob","isolapaul"));
        assertTrue(store.areFriends("bob","isolapaul"));
        assertTrue(store.removeFriend("bob","isolapaul"));
        assertFalse(store.areFriends("bob","isolapaul"));
    }

    @Test
    void testFriendRequest() {
        store.registerUser("tesztElek", "pass");
        store.registerUser("bob", "pass2");
        assertTrue(store.sendFriendRequest("tesztElek", "bob"));
        java.util.Set<String> incoming = store.getIncomingFriendRequests("bob");
        assertEquals(1, incoming.size());
        assertTrue(incoming.contains("tesztElek"));
        assertTrue(store.acceptFriendRequest("bob", "tesztElek"));
        assertTrue(store.areFriends("tesztElek", "bob"));
        assertTrue(store.getIncomingFriendRequests("bob").isEmpty());
    }

    @Test
    void testGroupCreationAndMembership() {
        store.registerUser("DondiDuo", "duo");
        store.registerUser("isolapaul", "paul");
        UUID groupId = store.createGroup("MyGroup", "DondiDuo");
        assertNotNull(groupId);
        
        UUID isolapaulId = store.getUserByName("isolapaul").getId();
        var group = store.getGroup(groupId);
        assertNotNull(group);
        group.addMember(isolapaulId, "Résztvevő");
        
        assertTrue(group.getMemberRoles().containsKey(isolapaulId));
    }

    @Test
    void testCancelOutgoingRequest() {
        store.registerUser("tesztElek", "x");
        store.registerUser("DondiDuo", "y");
        assertTrue(store.sendFriendRequest("tesztElek", "DondiDuo"));
        assertFalse(store.getOutgoingFriendRequests("tesztElek").isEmpty());
        assertTrue(store.cancelOutgoingFriendRequest("tesztElek", "DondiDuo"));
        assertTrue(store.getOutgoingFriendRequests("tesztElek").isEmpty());
        assertTrue(store.getIncomingFriendRequests("DondiDuo").isEmpty());
    }

    @Test
    void privateMessaging() {
        store.registerUser("bob","a");
        store.registerUser("isolapaul","b");
        UUID bobId = store.getUserByName("bob").getId();
        store.sendPrivateMessage(bobId, "bob","isolapaul","szia paul!");
        List<Message> message = store.getPrivateMessages("bob","isolapaul");
        assertEquals(1, message.size());
        assertEquals("szia paul!", message.get(0).getContent());
    }

    @Test
    void groupAndMessages() {
        store.registerUser("tesztElek","pw");
        UUID gid = store.createGroup("csapat","tesztElek");
        assertNotNull(gid);
        store.registerUser("bob","pw2");
        
        UUID bobId = store.getUserByName("bob").getId();
        var group = store.getGroup(gid);
        group.addMember(bobId, "Résztvevő");
        
        UUID tesztElekId = store.getUserByName("tesztElek").getId();
        store.sendGroupMessage(tesztElekId, gid, "sziasztok");
        assertEquals(1, store.getGroupMessages(gid).size());
    }
}
