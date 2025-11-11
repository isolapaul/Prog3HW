package hu.prog3.offlinechatprog3.controller;

import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.persistence.DataStore;
import hu.prog3.offlinechatprog3.persistence.FileManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Application-level controller connecting UI and persistence/model.
 *
 * Responsibilities:
 * - load/save the DataStore from/to a single .dat file (Java serialization)
 * - expose simple operations for the UI (register/login/send message/create group)
 *
 * This class is intentionally simple for the assignment. In a larger app,
 * you'd want to split responsibilities and add better error handling.
 */
public class AppController {

    private static final String DATA_DIR = "data"; // relative to project root
    private static final String DATA_FILE = "offline-chat.dat";

    private DataStore store;
    private final java.io.File dataFile;
    private long lastLoadedModified = 0L;

    public AppController() {
        this.dataFile = ensureDataFile();
        this.store = loadStore();
    }

    /**
     * Loads the store from disk if exists, otherwise returns a new DataStore.
     */
    private DataStore loadStore() {
        try {
            DataStore ds = FileManager.load(dataFile);
            if (ds != null) {
                this.lastLoadedModified = dataFile.lastModified();
                return ds;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.lastLoadedModified = dataFile.lastModified();
        return new DataStore();
    }

    private File ensureDataFile() {
        try {
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            return dir.resolve(DATA_FILE).toFile();
        } catch (Exception e) {
            return new File(DATA_FILE);
        }
    }

    private void reloadIfChanged() {
        try {
            long mod = dataFile.lastModified();
            if (mod > lastLoadedModified) {
                DataStore ds = FileManager.load(dataFile);
                if (ds != null) {
                    this.store = ds;
                    this.lastLoadedModified = mod;
                }
            }
        } catch (Exception ignored) {
            // Intentionally ignore transient reload errors (file may be mid-write by another instance).
            // Next tick/read will attempt again.
        }
    }

    /**
     * Persist the current store to disk. Returns true on success.
     */
    public boolean saveStore() {
        try {
            boolean ok = FileManager.save(store, dataFile);
            if (ok) this.lastLoadedModified = dataFile.lastModified();
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // UI-facing API methods - thin wrappers over DataStore
    public boolean registerUser(String username, String passwordHash) {
        reloadIfChanged();
        boolean ok = store.registerUser(username, passwordHash);
        if (ok) saveStore();
        return ok;
    }

    public boolean authenticateUser(String username, String passwordHash) {
        reloadIfChanged();
        return store.authenticateUser(username, passwordHash);
    }

    public boolean addFriend(String me, String friend) {
        reloadIfChanged();
        boolean ok = store.addFriend(me, friend);
        if (ok) saveStore();
        return ok;
    }

    /**
     * Send a friend request (does not directly create friendship).
     */
    public boolean sendFriendRequest(String from, String to) {
        reloadIfChanged();
        boolean ok = store.sendFriendRequest(from, to);
        if (ok) saveStore();
        return ok;
    }

    public java.util.Set<String> getOutgoingFriendRequests(String username) {
        reloadIfChanged();
        return store.getOutgoingFriendRequests(username);
    }

    public java.util.Set<String> getIncomingFriendRequests(String username) {
        reloadIfChanged();
        return store.getIncomingFriendRequests(username);
    }

    public boolean acceptFriendRequest(String username, String from) {
        reloadIfChanged();
        boolean ok = store.acceptFriendRequest(username, from);
        if (ok) saveStore();
        return ok;
    }

    public boolean rejectFriendRequest(String username, String from) {
        reloadIfChanged();
        boolean ok = store.rejectFriendRequest(username, from);
        if (ok) saveStore();
        return ok;
    }

    public boolean cancelOutgoingFriendRequest(String from, String to) {
        reloadIfChanged();
        boolean ok = store.cancelOutgoingFriendRequest(from, to);
        if (ok) saveStore();
        return ok;
    }

    // Group management wrappers
    public java.util.UUID createGroup(String name, String creatorUsername) {
        reloadIfChanged();
        java.util.UUID id = store.createGroup(name, creatorUsername);
        saveStore();
        return id;
    }

    public java.util.Map<java.util.UUID, String> getAllGroups() {
        reloadIfChanged();
        return store.getAllGroups();
    }

    public java.util.Set<String> getGroupMembers(java.util.UUID groupId) {
        reloadIfChanged();
        return store.getGroupMembers(groupId);
    }

    public boolean addGroupMember(java.util.UUID groupId, String username, String role) {
        reloadIfChanged();
        boolean ok = store.addGroupMember(groupId, username, role);
        if (ok) saveStore();
        return ok;
    }

    public boolean isGroupAdmin(java.util.UUID groupId, String username) {
        reloadIfChanged();
        return store.isGroupAdmin(groupId, username);
    }

    public boolean removeGroupMember(java.util.UUID groupId, String username) {
        reloadIfChanged();
        boolean ok = store.removeGroupMember(groupId, username);
        if (ok) saveStore();
        return ok;
    }

    public boolean addCustomRole(java.util.UUID groupId, String role) {
        reloadIfChanged();
        boolean ok = store.addCustomRole(groupId, role);
        if (ok) saveStore();
        return ok;
    }

    // Group role/permission helpers
    public java.util.Map<String, String> getGroupMembersWithRoles(java.util.UUID groupId) {
        reloadIfChanged();
        return store.getGroupMembersWithRoles(groupId);
    }

    public java.util.Set<String> getGroupAvailableRoles(java.util.UUID groupId) {
        reloadIfChanged();
        return store.getAvailableRoles(groupId);
    }

    public boolean setGroupMemberRole(java.util.UUID groupId, String username, String role) {
        reloadIfChanged();
        boolean ok = store.setGroupMemberRole(groupId, username, role);
        if (ok) saveStore();
        return ok;
    }

    public java.util.Set<String> getRolePermissions(java.util.UUID groupId, String role) {
        reloadIfChanged();
        return store.getRolePermissions(groupId, role);
    }

    public boolean setRolePermissions(java.util.UUID groupId, String role, java.util.Set<String> perms) {
        reloadIfChanged();
        boolean ok = store.setRolePermissions(groupId, role, perms);
        if (ok) saveStore();
        return ok;
    }

    public boolean hasGroupPermission(java.util.UUID groupId, String username, String permission) {
        reloadIfChanged();
        return store.hasGroupPermission(groupId, username, permission);
    }

    public boolean sendGroupMessage(java.util.UUID groupId, String from, String content) {
        reloadIfChanged();
    if (!store.hasGroupPermission(groupId, from, hu.prog3.offlinechatprog3.model.Permissions.GROUP_SEND_MESSAGE)) return false;
        store.sendGroupMessage(groupId, from, content);
        saveStore();
        return true;
    }

    public java.util.List<Message> getGroupMessages(java.util.UUID groupId) {
        reloadIfChanged();
        return store.getGroupMessages(groupId);
    }

    public boolean deleteGroupMessage(java.util.UUID groupId, java.util.UUID messageId, String requester) {
        reloadIfChanged();
        if (!store.hasGroupPermission(groupId, requester, hu.prog3.offlinechatprog3.model.Permissions.GROUP_DELETE_MESSAGES)) return false;
        boolean ok = store.deleteGroupMessage(groupId, messageId);
        if (ok) saveStore();
        return ok;
    }

    public boolean deleteGroup(java.util.UUID groupId, String requester) {
        reloadIfChanged();
        // Need permission GROUP_DELETE_GROUP
    if (!store.hasGroupPermission(groupId, requester, hu.prog3.offlinechatprog3.model.Permissions.GROUP_DELETE_GROUP)) return false;
        boolean ok = store.deleteGroup(groupId);
        if (ok) saveStore();
        return ok;
    }

    public boolean removeFriend(String me, String friend) {
        reloadIfChanged();
        boolean ok = store.removeFriend(me, friend);
        if (ok) saveStore();
        return ok;
    }

    public boolean sendPrivateMessage(String from, String to, String content) {
        reloadIfChanged();
        // send only if both users exist and are friends (per spec)
        if (!store.areFriends(from, to)) return false;
        store.sendPrivateMessage(from, to, content);
        saveStore();
        return true;
    }

    public List<Message> getPrivateMessages(String a, String b) {
        reloadIfChanged();
        return store.getPrivateMessages(a, b);
    }

    public Set<String> getFriendsOf(String username) {
        reloadIfChanged();
        return store.getFriends(username);
    }

    /**
     * Return all registered usernames.
     */
    public java.util.Set<String> getAllUsernames() {
        reloadIfChanged();
        return store.getAllUsernames();
    }

    public DataStore getStore() { return store; }

    /**
     * Resolve a user's display name (username) from their UUID. Returns null if not found.
     */
    public String getUsernameForId(UUID id) {
        if (id == null) return null;
        reloadIfChanged();
        return store.getUsernameById(id);
    }
}
