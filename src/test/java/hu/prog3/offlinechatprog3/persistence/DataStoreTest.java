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

    //regisztráció és hitelesítés tesztelése
    @Test
    void registerAndAuthenticate() {
        assertTrue(store.registerUser("TesztElek", "tesztElek"));
        assertFalse(store.registerUser("TesztElek", "password")); // mivel duplán lenne
        assertTrue(store.authenticateUser("TesztElek", "tesztElek")); //sikeres bejelentkezés
        assertFalse(store.authenticateUser("TesztElek", "roszzJelszo")); // sikertelen bejelentkezés
    }

    //barát műveletek tesztelése
    @Test
    void friendOperations() {
        store.registerUser("a", "1"); //két felhasználó regisztrálása
        store.registerUser("b", "2");
        assertTrue(store.addFriend("a","b")); //a bejelöli b-t barátnak
        assertTrue(store.areFriends("a","b")); //ekkor már barátok
        assertTrue(store.removeFriend("a","b")); //a eltávolítja b-t a barátok közül
        assertFalse(store.areFriends("a","b")); //már nem barátok
    }

    //barátkérés tesztelése
    @Test
    void testFriendRequest() {
        
        store.registerUser("TesztElek", "tesztElek");
        store.registerUser("bob", "BOB");
        // TesztElek bob-nak küld barátkérést
        assertTrue(store.sendFriendRequest("TesztElek", "bob"));
        // bobnak egy bejövő kérése van
        java.util.Set<String> incoming = store.getIncomingFriendRequests("bob");
        assertEquals(1, incoming.size());
        assertTrue(incoming.contains("TesztElek")); //ellenerőzzük hogy a kérelem tényleg TesztElektől jött
        // kérés elfogadása
        assertTrue(store.acceptFriendRequest("bob", "TesztElek"));
        // most már barátok
        assertTrue(store.areFriends("TesztElek", "bob"));
        assertTrue(store.getIncomingFriendRequests("bob").isEmpty()); // nem maradt több kérelem
    }


    // Csoport létrehozás és tagság tesztelése
    @Test
    void testGroupCreationAndMembership() {
        store.registerUser("owner", "owner"); //két felhasználó regisztrálása
        store.registerUser("member", "member");
        java.util.UUID id = store.createGroup("Group1", "owner"); //csoport létrehozása
        assertNotNull(id);
        assertTrue(store.addGroupMember(id, "member", "Résztvevő")); //tag hozzáadása
        java.util.Set<String> members = store.getGroupMembers(id); //csoport tagjai
        assertTrue(members.contains("member"));
    }

    // barátkérés visszavonásának tesztelése
    @Test
    void testCancelOutgoingRequest() {
        store.registerUser("a", "p"); // két felhasználó regisztrálása
        store.registerUser("b", "p"); 
        assertTrue(store.sendFriendRequest("a", "b")); // a barátkérelmet küldd b-nek
        assertFalse(store.getOutgoingFriendRequests("a").isEmpty()); // a-nak itt már lesz egy kimenő kérése
        assertTrue(store.cancelOutgoingFriendRequest("a", "b")); //visszavonjuk a barátkérelmet
        assertTrue(store.getOutgoingFriendRequests("a").isEmpty()); //a-nak és b-nek sincs már több kérése
        assertTrue(store.getIncomingFriendRequests("b").isEmpty());
    }

    // privát üzenetküldésnek a tesztelése
    @Test
    void privateMessaging() {
        store.registerUser("a","p");
        store.registerUser("b","p");
        store.sendPrivateMessage("a","b","Szép napot kollega!");
        List<Message> message = store.getPrivateMessages("a","b");
        assertEquals(1, message.size());
        assertEquals("Szép napot kollega!", message.get(0).getContent());
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
