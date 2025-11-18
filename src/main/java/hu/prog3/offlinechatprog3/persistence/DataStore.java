package hu.prog3.offlinechatprog3.persistence;

import hu.prog3.offlinechatprog3.model.Group;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.User;
import hu.prog3.offlinechatprog3.util.PasswordUtil;

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
        // Az összes Map létrejött az inicializálásakor
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

    //bejelentkezés - passwordHash már BCrypt hash
    public boolean authenticateUser(String username, String passwordHash) {
        User u = usersByName.get(username);
        if (u == null) return false;
        try {
            return PasswordUtil.checkPassword(passwordHash, u.getPasswordHash());
        } catch (Exception e) {
            return false;
        }
    }

    /** Felhasználó keresése név szerint. */
    public Optional<User> findUserByName(String username) {
        return Optional.ofNullable(usersByName.get(username));
    }

    // Friend management
    public boolean addFriend(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        friends.get(a).add(b);
        friends.get(b).add(a);
        return true;
    }

    /**
     * Send a friend request from 'from' to 'to'. Returns false if users don't exist,
     * if already friends, or if a request already exists.
     */
    public boolean sendFriendRequest(String from, String to) {
        if (!usersByName.containsKey(from) || !usersByName.containsKey(to)) return false;
        if (areFriends(from, to)) return false;
        Set<String> incoming = incomingFriendRequests.computeIfAbsent(to, k -> new HashSet<>());
        Set<String> outgoing = outgoingFriendRequests.computeIfAbsent(from, k -> new HashSet<>());
        // if already requested (either direction) don't add
        if (incoming.contains(from) || outgoing.contains(to)) return false;
        incoming.add(from);
        outgoing.add(to);
        return true;
    }

    /**
     * Get a defensive copy of pending incoming friend requests for a user.
     */
    public Set<String> getIncomingFriendRequests(String username) {
        return new HashSet<>(incomingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    /**
     * Return outgoing friend requests sent by the given user.
     */
    public Set<String> getOutgoingFriendRequests(String username) {
        return new HashSet<>(outgoingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    /**
     * Accept a friend request where 'username' accepts a request from 'from'.
     */
    public boolean acceptFriendRequest(String username, String from) {
        if (!usersByName.containsKey(username) || !usersByName.containsKey(from)) return false;
        Set<String> incoming = incomingFriendRequests.get(username);
        if (incoming == null || !incoming.remove(from)) return false;
        // add friendship
        addFriend(username, from);
        // remove outgoing entry for the requester
        Set<String> outgoing = outgoingFriendRequests.get(from);
        if (outgoing != null) outgoing.remove(username);
        return true;
    }

    /**
     * Reject (remove) a friend request without adding friendship.
     */
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

    /**
     * Cancel an outgoing friend request: 'from' cancels a previously sent request to 'to'.
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

    public boolean removeFriend(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        boolean ra = friends.get(a).remove(b);
        boolean rb = friends.get(b).remove(a);
        return ra || rb;
    }

    public boolean areFriends(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        return friends.get(a).contains(b);
    }

    // Group management
    public UUID createGroup(String name, String creatorUsername) {
        Group g = new Group(name);
        groups.put(g.getId(), g);
        
        // Létrehozó hozzáadása Adminisztrátor szerepkörrel
        User creator = usersByName.get(creatorUsername);
        if (creator != null) {
            g.addMember(creator.getId(), "Adminisztrátor");
        }
        
        return g.getId();
    }

    public boolean addGroupMember(UUID groupId, String username, String role) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        g.addMember(u.getId(), role);
        return true;
    }

    /** Return mapping of username -> role for a given group. */
    public Map<String, String> getGroupMembersWithRoles(UUID groupId) {
        Group g = groups.get(groupId);
        if (g == null) return java.util.Collections.emptyMap();
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<UUID, String> e : g.getMemberRoles().entrySet()) {
            User u = usersById.get(e.getKey());
            if (u != null) res.put(u.getUsername(), e.getValue());
        }
        return res;
    }

    /** Get the role name of a user in the group, or null if not a member. */
    public String getGroupRole(UUID groupId, String username) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return null;
        return g.getMemberRoles().get(u.getId());
    }

    /** Change a member's role in the group. */
    public boolean setGroupMemberRole(UUID groupId, String username, String role) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        try {
            g.setMemberRole(u.getId(), role);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Available role names in the group. */
    public java.util.Set<String> getAvailableRoles(UUID groupId) {
        Group g = groups.get(groupId);
        if (g == null) return java.util.Collections.emptySet();
        return new java.util.HashSet<>(g.getRoles());
    }

    public boolean setRolePermissions(UUID groupId, String role, java.util.Set<String> perms) {
        Group g = groups.get(groupId);
        if (g == null) return false;
        try {
            g.setRolePermissions(role, perms);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public java.util.Set<String> getRolePermissions(UUID groupId, String role) {
        Group g = groups.get(groupId);
        if (g == null) return java.util.Collections.emptySet();
        return g.getRolePermissions(role);
    }

    /**
     * Return true if the given username is an administrator in the group.
     */
    public boolean isGroupAdmin(UUID groupId, String username) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        return g.isAdmin(u.getId());
    }

    public boolean removeGroupMember(UUID groupId, String username) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        g.removeMember(u.getId());
        return true;
    }

    public boolean addCustomRole(UUID groupId, String role) {
        Group g = groups.get(groupId);
        if (g == null) return false;
        g.addRole(role);
        return true;
    }

    // Messaging (private: key is sorted username pair joined by '#')
    private String privateKey(String a, String b) {
        if (a == null) {
            throw new IllegalArgumentException("First username (a) cannot be null");
        }
        if (b == null) {
            throw new IllegalArgumentException("Second username (b) cannot be null");
        }
        List<String> l = Arrays.asList(a, b);
        Collections.sort(l);
        return String.join("#", l);
    }

    public void sendPrivateMessage(String fromUsername, String toUsername, String content) {
        String key = privateKey(fromUsername, toUsername);
        User sender = usersByName.get(fromUsername);
        Message m = new Message(sender.getId(), null, content);
        
        // computeIfAbsent: ha nincs lista, létrehozza és beteszi, egyébként visszaadja a meglevőt
        privateMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
    }

    public List<Message> getPrivateMessages(String a, String b) {
        String key = privateKey(a, b);
        return privateMessages.getOrDefault(key, Collections.emptyList());
    }

    public void sendGroupMessage(UUID groupId, String fromUsername, String content) {
        User sender = usersByName.get(fromUsername);
        Message m = new Message(sender.getId(), groupId, content);
        groupMessages.computeIfAbsent(groupId, k -> new ArrayList<>()).add(m);
    }

    public List<Message> getGroupMessages(UUID groupId) {
        return groupMessages.getOrDefault(groupId, Collections.emptyList());
    }

    /** Delete a single message from a group's message list by message id. */
    public boolean deleteGroupMessage(UUID groupId, UUID messageId) {
        List<Message> list = groupMessages.get(groupId);
        if (list == null) return false;
        return list.removeIf(msg -> Objects.equals(msg.getId(), messageId));
    }

    /** Delete a group and its messages. */
    public boolean deleteGroup(UUID groupId) {
        Group g = groups.remove(groupId);
        groupMessages.remove(groupId);
        return g != null;
    }

    /** Permission check wrapper for group actions. */
    public boolean hasGroupPermission(UUID groupId, String username, String permission) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        return g.hasPermission(u.getId(), permission);
    }

    // Some utility/test helpers
    public int userCount() { return usersByName.size(); }

    public int groupCount() { return groups.size(); }

    public void clearAll() {
        usersByName.clear();
        usersById.clear();
        friends.clear();
        groups.clear();
        privateMessages.clear();
        groupMessages.clear();
        incomingFriendRequests.clear();
        outgoingFriendRequests.clear();
    }

    /**
     * Return a map of group id -> group name for all groups. Defensive copy.
     */
    public Map<java.util.UUID, String> getAllGroups() {
        Map<java.util.UUID, String> m = new HashMap<>();
        for (Map.Entry<java.util.UUID, Group> e : groups.entrySet()) {
            m.put(e.getKey(), e.getValue().getName());
        }
        return m;
    }

    /**
     * Return usernames of members for a given group id.
     */
    public Set<String> getGroupMembers(java.util.UUID groupId) {
        Group g = groups.get(groupId);
        if (g == null) return Collections.emptySet();
        Set<String> res = new HashSet<>();
        for (java.util.UUID uid : g.getMemberRoles().keySet()) {
            User u = usersById.get(uid);
            if (u != null) res.add(u.getUsername());
        }
        return res;
    }

    /**
     * Return a defensive copy of the friend usernames for the given user.
     */
    public java.util.Set<String> getFriends(String username) {
        return new java.util.HashSet<>(friends.getOrDefault(username, java.util.Collections.emptySet()));
    }

    /**
     * Resolve username for a given user UUID. Returns null if not found.
     */
    public String getUsernameById(UUID id) {
        User u = usersById.get(id);
        return u == null ? null : u.getUsername();
    }

    /**
     * Return all registered usernames (defensive copy).
     */
    public java.util.Set<String> getAllUsernames() {
        return new java.util.HashSet<>(usersByName.keySet());
    }
}
