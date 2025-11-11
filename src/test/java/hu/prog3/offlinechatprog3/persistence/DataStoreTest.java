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
    void registerAndAuthenticate() {
        assertTrue(store.registerUser("alice", "pw1"));
        assertFalse(store.registerUser("alice", "pw2")); // duplicate
        assertTrue(store.authenticateUser("alice", "pw1"));
        assertFalse(store.authenticateUser("alice", "wrong"));
    }

    @Test
    void friendOperations() {
        store.registerUser("a", "1");
        store.registerUser("b", "2");
        assertTrue(store.addFriend("a","b"));
        assertTrue(store.areFriends("a","b"));
        assertTrue(store.removeFriend("a","b"));
        assertFalse(store.areFriends("a","b"));
    }

    @org.junit.jupiter.api.Test
    void testFriendRequestLifecycle() {
        DataStore ds = new DataStore();
        ds.registerUser("alice", "pw");
        ds.registerUser("bob", "pw");
        // send request from alice to bob
        assertTrue(ds.sendFriendRequest("alice", "bob"));
        // bob has one incoming request
        java.util.Set<String> incoming = ds.getIncomingFriendRequests("bob");
        assertEquals(1, incoming.size());
        assertTrue(incoming.contains("alice"));
        // accept request
        assertTrue(ds.acceptFriendRequest("bob", "alice"));
        // now they should be friends and no incoming
        assertTrue(ds.areFriends("alice", "bob"));
        assertTrue(ds.getIncomingFriendRequests("bob").isEmpty());
    }

    @org.junit.jupiter.api.Test
    void testGroupCreationAndMembership() {
        DataStore ds = new DataStore();
        ds.registerUser("owner", "pw");
        ds.registerUser("member", "pw");
        java.util.UUID id = ds.createGroup("G1", "owner");
        assertNotNull(id);
        assertEquals(1, ds.groupCount());
        assertTrue(ds.addGroupMember(id, "member", "Résztvevő"));
        java.util.Set<String> members = ds.getGroupMembers(id);
        assertTrue(members.contains("member"));
    }

    @org.junit.jupiter.api.Test
    void testCancelOutgoingRequest() {
        DataStore ds = new DataStore();
        ds.registerUser("u1", "p");
        ds.registerUser("u2", "p");
        assertTrue(ds.sendFriendRequest("u1", "u2"));
        assertFalse(ds.getOutgoingFriendRequests("u1").isEmpty());
        assertTrue(ds.cancelOutgoingFriendRequest("u1", "u2"));
        assertTrue(ds.getOutgoingFriendRequests("u1").isEmpty());
        assertTrue(ds.getIncomingFriendRequests("u2").isEmpty());
    }

    @Test
    void privateMessaging() {
        store.registerUser("u1","p");
        store.registerUser("u2","p");
        store.sendPrivateMessage("u1","u2","hello");
        List<Message> msgs = store.getPrivateMessages("u1","u2");
        assertEquals(1, msgs.size());
        assertEquals("hello", msgs.get(0).getContent());
    }

    @Test
    void groupAndMessages() {
        store.registerUser("owner","x");
        UUID gid = store.createGroup("g1","owner");
        assertNotNull(gid);
        store.registerUser("m","p");
        assertTrue(store.addGroupMember(gid,"m","Résztvevő"));
        store.sendGroupMessage(gid,"owner","hi group");
        assertEquals(1, store.getGroupMessages(gid).size());
    }
}
