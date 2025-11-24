package hu.prog3.offlinechatprog3.persistence;

import hu.prog3.offlinechatprog3.model.Group;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.User;

import java.io.Serializable;
import java.util.*;

public class DataStore implements Serializable {

    private static final long serialVersionUID = 1L;

    //felhasználó név és ID szerint
    private final Map<String, User> usersByName = new HashMap<>();
    private final Map<UUID, User> usersById = new HashMap<>();
    
    //barátság kezelés
    private final Map<String, Set<String>> friends = new HashMap<>();
    //bejövő barátkérelmek
    private final Map<String, Set<String>> incomingFriendRequests = new HashMap<>();
    //kimenő barátkérelmek
    private final Map<String, Set<String>> outgoingFriendRequests = new HashMap<>();
    //csoportok tárolása
    private final Map<UUID, Group> groups = new HashMap<>();
    //üzenet tárolás
    private final Map<String, List<Message>> privateMessages = new HashMap<>();
    //csoport üzenetek tárolása
    private final Map<UUID, List<Message>> groupMessages = new HashMap<>();

    public DataStore() {
        //az összes Map létrejött az inicializálásakor
    }
    //regisztráció
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

    public User getUserByName(String username) {
        return usersByName.get(username);
    }

    public Group getGroup(UUID groupId) {
        return groups.get(groupId);
    }

    //barát kérés küldése
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

    //bejövő barátkérelmek lekérdezése
    public Set<String> getIncomingFriendRequests(String username) {
        return new HashSet<>(incomingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    //kimenő barátkérelmek lekérdezése
    public Set<String> getOutgoingFriendRequests(String username) {
        return new HashSet<>(outgoingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    //barát kérés elfogadása
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

    //barát kérés elutasítása
    public boolean rejectFriendRequest(String username, String from) {
        if (!usersByName.containsKey(username) || !usersByName.containsKey(from)) return false;
        Set<String> incoming = incomingFriendRequests.get(username);
        if (incoming == null) return false;
        boolean removed = incoming.remove(from);
        // also remove outgoing entry
        Set<String> outgoing = outgoingFriendRequests.get(from);
        if (outgoing != null) outgoing.remove(username);
        return removed;
    }

    //kimenő barát kérés visszavonása
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
    //barát eltávolítása
    public boolean removeFriend(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        boolean ra = friends.get(a).remove(b);
        boolean rb = friends.get(b).remove(a);
        return ra || rb;
    }
    //barátság ellenőrzése
    public boolean areFriends(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        return friends.get(a).contains(b);
    }

    //csoport kezelés
    public UUID createGroup(String name, String creatorUsername) {
        Group g = new Group(name);
        groups.put(g.getId(), g);
        
        //létrehozó hozzáadása Adminisztrátor szerepkörrel
        User creator = usersByName.get(creatorUsername);
        if (creator != null) {
            g.addMember(creator.getId(), "Adminisztrátor");
        }
        
        return g.getId();
    }

    //privát kulcs generálása két felhasználó között
    private String privateKey(String a, String b) {
        List<String> l = Arrays.asList(a, b);
        Collections.sort(l);
        return String.join("#", l);
    }
    //privát üzenet küldése
    public void sendPrivateMessage(UUID senderId, String username1, String username2, String content) {
        String key = privateKey(username1, username2);
        Message m = new Message(senderId, null, content);
        privateMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
    }
    //privát üzenetek lekérdezése
    public List<Message> getPrivateMessages(String a, String b) {
        String key = privateKey(a, b);
        return privateMessages.getOrDefault(key, Collections.emptyList());
    }
    //csoport üzenet küldése
    public void sendGroupMessage(UUID senderId, UUID groupId, String content) {
        Message m = new Message(senderId, groupId, content);
        groupMessages.computeIfAbsent(groupId, k -> new ArrayList<>()).add(m);
    }
    //csoport üzenetek lekérdezése
    public List<Message> getGroupMessages(UUID groupId) {
        return groupMessages.getOrDefault(groupId, Collections.emptyList());
    }
    //csoport üzenet törlése
    public void deleteGroupMessage(UUID groupId, UUID messageId) {
        List<Message> list = groupMessages.get(groupId);
        if (list != null) {
            list.removeIf(msg -> Objects.equals(msg.getId(), messageId));
        }
    }
    //csoport törlése
    public void deleteGroup(UUID groupId) {
        groups.remove(groupId);
        groupMessages.remove(groupId);
    }

    //összes csoport lekérdezése
    public Map<java.util.UUID, String> getAllGroups() {
        Map<java.util.UUID, String> m = new HashMap<>();
        for (Map.Entry<java.util.UUID, Group> e : groups.entrySet()) {
            m.put(e.getKey(), e.getValue().getName());
        }
        return m;
    }
    //felhasználó barátainak lekérdezése
    public java.util.Set<String> getFriends(String username) {
        return new java.util.HashSet<>(friends.getOrDefault(username, java.util.Collections.emptySet()));
    }
    //felhasználó nevének lekérdezése azonosító alapján
    public String getUsernameById(UUID id) {
        User u = usersById.get(id);
        return u == null ? null : u.getUsername();
    }
    //összes felhasználónév lekérdezése
    public java.util.Set<String> getAllUsernames() {
        return new java.util.HashSet<>(usersByName.keySet());
    }
}
