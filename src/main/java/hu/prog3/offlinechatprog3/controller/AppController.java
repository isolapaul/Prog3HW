package hu.prog3.offlinechatprog3.controller;

import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.PermissionChecker;
import hu.prog3.offlinechatprog3.persistence.DataStore;
import hu.prog3.offlinechatprog3.persistence.FileManager;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AppController {
    private static final String DATA_FILE_PATH = "data/offline-chat.dat";

    private DataStore store;
    private PermissionChecker permissionChecker;
    private final File dataFile;

    public AppController() {
        this.dataFile = new File(DATA_FILE_PATH);
        this.dataFile.getParentFile().mkdirs();
        
        DataStore loaded = FileManager.load(dataFile);
        this.store = (loaded != null) ? loaded : new DataStore();
        this.permissionChecker = new PermissionChecker(store);
    }

    //adatok mentése
    public boolean saveStore() {
        try {
            return FileManager.save(store, dataFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //felhasználó regisztrációja
    public boolean registerUser(String username, String passwordHash) {
        boolean ok = store.registerUser(username, passwordHash);
        if (ok) saveStore();
        return ok;
    }

    //bejelentkezés ellenőrzése
    public boolean authenticateUser(String username, String passwordHash) {
        return store.authenticateUser(username, passwordHash);
    }
    //barát hozzáadása
    public boolean addFriend(String me, String friend) {
        boolean ok = store.addFriend(me, friend);
        if (ok) saveStore();
        return ok;
    }
    //barátkérelem küldése
    public boolean sendFriendRequest(String from, String to) {
        boolean ok = store.sendFriendRequest(from, to);
        if (ok) saveStore();
        return ok;
    }
    //kimenő barátkérelmek lekérdezése
    public Set<String> getOutgoingFriendRequests(String username) {
        return store.getOutgoingFriendRequests(username);
    }
    //bejövő barátkérelmek lekérdezése
    public Set<String> getIncomingFriendRequests(String username) {
        return store.getIncomingFriendRequests(username);
    }
    //barátkérelem elfogadása
    public boolean acceptFriendRequest(String username, String from) {
        boolean ok = store.acceptFriendRequest(username, from);
        if (ok) saveStore();
        return ok;
    }
    //barátkérelem elutasítása
    public boolean rejectFriendRequest(String username, String from) {
        boolean ok = store.rejectFriendRequest(username, from);
        if (ok) saveStore();
        return ok;
    }
    //barátkérelem visszavonása
    public boolean cancelOutgoingFriendRequest(String from, String to) {
        boolean ok = store.cancelOutgoingFriendRequest(from, to);
        if (ok) saveStore();
        return ok;
    }
    //Csoport létrehozása
    public UUID createGroup(String name, String creatorUsername) {
        UUID id = store.createGroup(name, creatorUsername);
        saveStore();
        return id;
    }

    public java.util.Map<UUID, String> getAllGroups() {
        return store.getAllGroups();
    }

    public Set<String> getGroupMembers(UUID groupId) {
        return store.getGroupMembers(groupId);
    }

    public boolean addGroupMember(UUID groupId, String username, String role) {
        boolean ok = store.addGroupMember(groupId, username, role);
        if (ok) saveStore();
        return ok;
    }

    public boolean isGroupAdmin(UUID groupId, String username) {
        return store.isGroupAdmin(groupId, username);
    }

    public boolean removeGroupMember(UUID groupId, String username) {
        boolean ok = store.removeGroupMember(groupId, username);
        if (ok) saveStore();
        return ok;
    }

    public boolean addCustomRole(UUID groupId, String role) {
        boolean ok = store.addCustomRole(groupId, role);
        if (ok) saveStore();
        return ok;
    }

    // Group role/permission helpers
    public Map<String, String> getGroupMembersWithRoles(UUID groupId) {
        return store.getGroupMembersWithRoles(groupId);
    }

    public Set<String> getGroupAvailableRoles(UUID groupId) {
        return store.getAvailableRoles(groupId);
    }

    public boolean setGroupMemberRole(UUID groupId, String username, String role) {
        boolean ok = store.setGroupMemberRole(groupId, username, role);
        if (ok) saveStore();
        return ok;
    }

    public Set<String> getRolePermissions(UUID groupId, String role) {
        return store.getRolePermissions(groupId, role);
    }

    public boolean setRolePermissions(UUID groupId, String role, Set<String> perms) {
        boolean ok = store.setRolePermissions(groupId, role, perms);
        if (ok) saveStore();
        return ok;
    }

    public boolean hasGroupPermission(UUID groupId, String username, String permission) {
        return store.hasGroupPermission(groupId, username, permission);
    }

    public boolean sendGroupMessage(UUID groupId, String from, String content) {
        if (!permissionChecker.canSendMessage(groupId, from)) return false;
        store.sendGroupMessage(groupId, from, content);
        saveStore();
        return true;
    }

    public List<Message> getGroupMessages(UUID groupId) {
        return store.getGroupMessages(groupId);
    }

    public boolean deleteGroupMessage(UUID groupId, UUID messageId, String requester) {
        if (!permissionChecker.canDeleteMessages(groupId, requester)) return false;
        boolean ok = store.deleteGroupMessage(groupId, messageId);
        if (ok) saveStore();
        return ok;
    }

    public boolean deleteGroup(UUID groupId, String requester) {
        if (!permissionChecker.canDeleteGroup(groupId, requester)) return false;
        boolean ok = store.deleteGroup(groupId);
        if (ok) saveStore();
        return ok;
    }

    public boolean removeFriend(String me, String friend) {
        boolean ok = store.removeFriend(me, friend);
        if (ok) saveStore();
        return ok;
    }

    public boolean sendPrivateMessage(String from, String to, String content) {
        if (!store.areFriends(from, to)) return false;
        store.sendPrivateMessage(from, to, content);
        saveStore();
        return true;
    }

    public List<Message> getPrivateMessages(String a, String b) {
        return store.getPrivateMessages(a, b);
    }

    public Set<String> getFriendsOf(String username) {
        return store.getFriends(username);
    }

    /**
     * Return all registered usernames.
     */
    public Set<String> getAllUsernames() {
        return store.getAllUsernames();
    }

    public DataStore getStore() { return store; }
    
    public String getUsernameForId(UUID id) {
        if (id == null) return null;
        return store.getUsernameById(id);
    }
}
