package hu.prog3.offlinechatprog3.persistence;

import hu.prog3.offlinechatprog3.model.Group;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.User;

import java.io.Serializable;
import java.util.*;

/**
 * In-memory adattár felhasználók, barátok, csoportok és üzenetek tárolására.
 */
public class DataStore implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, User> usersByName = new HashMap<>();
    private final Map<UUID, User> usersById = new HashMap<>();
    private final Map<String, Set<String>> friends = new HashMap<>();
    private final Map<String, Set<String>> incomingFriendRequests = new HashMap<>();
    private final Map<String, Set<String>> outgoingFriendRequests = new HashMap<>();
    private final Map<UUID, Group> groups = new HashMap<>();
    private final Map<String, List<Message>> privateMessages = new HashMap<>();
    private final Map<UUID, List<Message>> groupMessages = new HashMap<>();

    public DataStore() {
    }
    
    /**
     * Új felhasználó regisztrálása.
     * @param username felhasználónév
     * @param passwordHash bcrypt hash
     * @return true ha sikeres
     */
    public boolean registerUser(String username, String passwordHash) {
        if (username == null || username.isBlank() || usersByName.containsKey(username)) {
            return false;
        }
        
        User u = new User(username, passwordHash);
        usersByName.put(username, u);
        usersById.put(u.getId(), u);
        
        friends.put(username, new HashSet<>());
        incomingFriendRequests.put(username, new HashSet<>());
        outgoingFriendRequests.put(username, new HashSet<>());
        
        return true;
    }

    /**
     * Felhasználó lekérdezése név alapján.
     * @param username felhasználónév
     * @return User vagy null
     */
    public User getUserByName(String username) {
        return usersByName.get(username);
    }

    /**
     * Csoport lekérdezése.
     * @param groupId csoport UUID
     * @return Group vagy null
     */
    public Group getGroup(UUID groupId) {
        return groups.get(groupId);
    }

    /**
     * Barátkérelem küldése.
     * @param from küldő
     * @param to címzett
     * @return true ha sikeres
     */
    public boolean sendFriendRequest(String from, String to) {
        if (!usersByName.containsKey(from) || !usersByName.containsKey(to)) return false;
        if (areFriends(from, to)) return false;
        Set<String> incoming = incomingFriendRequests.computeIfAbsent(to, k -> new HashSet<>());
        Set<String> outgoing = outgoingFriendRequests.computeIfAbsent(from, k -> new HashSet<>());
        
        if (incoming.contains(from) || outgoing.contains(to)) return false;
        incoming.add(from);
        outgoing.add(to);
        return true;
    }

    /**
     * Bejövő barátkérelmek lekérdezése.
     * @param username felhasználónév
     * @return felhasználónevek halmaza
     */
    public Set<String> getIncomingFriendRequests(String username) {
        return new HashSet<>(incomingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    /**
     * Kimenő barátkérelmek lekérdezése.
     * @param username felhasználónév
     * @return felhasználónevek halmaza
     */
    public Set<String> getOutgoingFriendRequests(String username) {
        return new HashSet<>(outgoingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    /**
     * Barátkérelem elfogadása.
     * @param username elfogadó
     * @param from küldő
     * @return true ha sikeres
     */
    public boolean acceptFriendRequest(String username, String from) {
        if (!usersByName.containsKey(username) || !usersByName.containsKey(from)) return false;
        Set<String> incoming = incomingFriendRequests.get(username);
        if (incoming == null) return false;
        if (!incoming.remove(from)) return false;
        // add friendship
        friends.get(username).add(from);
        friends.get(from).add(username);
        // remove outgoing entry for the requester
        Set<String> outgoing = outgoingFriendRequests.get(from);
        if (outgoing != null) outgoing.remove(username);
        return true;
    }

    /**
     * Barátkérelem elutasítása.
     * @param username elutasító
     * @param from küldő
     * @return true ha sikeres
     */
    public boolean rejectFriendRequest(String username, String from) {
        if (!usersByName.containsKey(username) || !usersByName.containsKey(from)) return false;
        Set<String> incoming = incomingFriendRequests.get(username);
        if (incoming == null) return false;
        boolean removed = incoming.remove(from);
        Set<String> outgoing = outgoingFriendRequests.get(from);
        if (outgoing != null) outgoing.remove(username);
        return removed;
    }

    /**
     * Kimenő barátkérelem visszavonása.
     * @param from küldő
     * @param to címzett
     * @return true ha sikeres
     */
    public boolean cancelOutgoingFriendRequest(String from, String to) {
        if (!usersByName.containsKey(from) || !usersByName.containsKey(to)) return false;
        Set<String> outgoing = outgoingFriendRequests.get(from);
        Set<String> incoming = incomingFriendRequests.get(to);
        boolean removedOut = false;
        boolean removedIn = false;
        if (outgoing != null) removedOut = outgoing.remove(to);
        if (incoming != null) removedIn = incoming.remove(from);
        return removedOut || removedIn;
    }
    
    /**
     * Barát eltávolítása.
     * @param a első felhasználó
     * @param b második felhasználó
     * @return true ha sikeres
     */
    public boolean removeFriend(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        boolean ra = friends.get(a).remove(b);
        boolean rb = friends.get(b).remove(a);
        return ra || rb;
    }
    
    /**
     * Barátság ellenőrzése.
     * @param a első felhasználó
     * @param b második felhasználó
     * @return true ha barátok
     */
    public boolean areFriends(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        return friends.get(a).contains(b);
    }

    /**
     * Csoport létrehozása.
     * @param name csoport neve
     * @param creatorUsername létrehozó
     * @return csoport UUID
     */
    public UUID createGroup(String name, String creatorUsername) {
        Group g = new Group(name);
        groups.put(g.getId(), g);
        
        User creator = usersByName.get(creatorUsername);
        if (creator != null) {
            g.addMember(creator.getId(), "Adminisztrátor");
        }
        
        return g.getId();
    }

    private String privateKey(String a, String b) {
        List<String> l = Arrays.asList(a, b);
        Collections.sort(l);
        return String.join("#", l);
    }
    
    /**
     * Privát üzenet küldése.
     * @param senderId küldő UUID
     * @param username1 első felhasználó
     * @param username2 második felhasználó
     * @param content tartalom
     */
    public void sendPrivateMessage(UUID senderId, String username1, String username2, String content) {
        String key = privateKey(username1, username2);
        Message m = new Message(senderId, null, content);
        privateMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
    }
    
    /**
     * Privát üzenetek lekérdezése.
     * @param a első felhasználó
     * @param b második felhasználó
     * @return üzenetek listája
     */
    public List<Message> getPrivateMessages(String a, String b) {
        String key = privateKey(a, b);
        return privateMessages.getOrDefault(key, Collections.emptyList());
    }
    /**
     * Csoport üzenet küldése.
     * @param senderId küldő UUID
     * @param groupId csoport UUID
     * @param content tartalom
     */
    public void sendGroupMessage(UUID senderId, UUID groupId, String content) {
        Message m = new Message(senderId, groupId, content);
        groupMessages.computeIfAbsent(groupId, k -> new ArrayList<>()).add(m);
    }
    
    /**
     * Csoport üzenetek lekérdezése.
     * @param groupId csoport UUID
     * @return üzenetek listája
     */
    public List<Message> getGroupMessages(UUID groupId) {
        return groupMessages.getOrDefault(groupId, Collections.emptyList());
    }
    
    /**
     * Csoport üzenet törlése.
     * @param groupId csoport UUID
     * @param messageId üzenet UUID
     */
    public void deleteGroupMessage(UUID groupId, UUID messageId) {
        List<Message> list = groupMessages.get(groupId);
        if (list != null) {
            list.removeIf(msg -> Objects.equals(msg.getId(), messageId));
        }
    }
    
    /**
     * Csoport törlése.
     * @param groupId csoport UUID
     */
    public void deleteGroup(UUID groupId) {
        groups.remove(groupId);
        groupMessages.remove(groupId);
    }

    /**
     * Összes csoport lekérdezése.
     * @return map UUID → név
     */
    public Map<java.util.UUID, String> getAllGroups() {
        Map<java.util.UUID, String> m = new HashMap<>();
        for (Map.Entry<java.util.UUID, Group> e : groups.entrySet()) {
            m.put(e.getKey(), e.getValue().getName());
        }
        return m;
    }
    
    /**
     * Felhasználó barátainak lekérdezése.
     * @param username felhasználónév
     * @return barátok halmaza
     */
    public java.util.Set<String> getFriends(String username) {
        return new java.util.HashSet<>(friends.getOrDefault(username, java.util.Collections.emptySet()));
    }
    
    /**
     * Felhasználónév lekérdezése UUID alapján.
     * @param id felhasználó UUID
     * @return felhasználónév vagy null
     */
    public String getUsernameById(UUID id) {
        User u = usersById.get(id);
        return u == null ? null : u.getUsername();
    }
    
    /**
     * Összes felhasználónév lekérdezése.
     * @return felhasználónevek halmaza
     */
    public java.util.Set<String> getAllUsernames() {
        return new java.util.HashSet<>(usersByName.keySet());
    }
}
