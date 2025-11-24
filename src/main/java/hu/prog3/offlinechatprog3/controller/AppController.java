package hu.prog3.offlinechatprog3.controller;

import hu.prog3.offlinechatprog3.model.Permissions;
import hu.prog3.offlinechatprog3.persistence.DataStore;
import hu.prog3.offlinechatprog3.persistence.FileManager;

import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;

public class AppController {
    private static final String DATA_FILE_PATH = "data/offline-chat.dat";
    //korlátozások
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_GROUP_NAME_LENGTH = 30;

    private DataStore store;  // nem final, mert reloadStore() újra kell állítsa
    private final File dataFile;
    private long lastLoadedTimestamp = 0;  // Utolsó betöltés/mentés időpontja

    public AppController() {
        this.dataFile = new File(DATA_FILE_PATH);
        this.dataFile.getParentFile().mkdirs();
        
        DataStore loaded = FileManager.load(dataFile);
        this.store = (loaded != null) ? loaded : new DataStore();
        updateTimestamp();
    }

    // UI-nak kell a DataStore referencia, hogy közvetlenül hívhassa ahol nincs business logic
    public DataStore getDataStore() {
        return store;
    }

    // Frissíti a timestamp-et (mentés vagy betöltés után)
    private void updateTimestamp() {
        if (dataFile.exists()) {
            lastLoadedTimestamp = dataFile.lastModified();
        }
    }

    // Újratölti a DataStore-t a fájlból CSAK ha más user módosította
    public void reloadStore() {
        if (!dataFile.exists()) return;
        
        long currentFileTime = dataFile.lastModified();
        // Csak akkor töltsünk újra ha a fájl újabb mint amit mi utoljára láttunk
        if (currentFileTime > lastLoadedTimestamp) {
            DataStore loaded = FileManager.load(dataFile);
            if (loaded != null) {
                this.store = loaded;
                updateTimestamp();
            }
        }
    }

    //segéd metódus - művelet végrehajtása és mentés
    private boolean executeAndSave(BooleanSupplier operation) {
        boolean ok = operation.getAsBoolean();
        if (ok) saveStore();
        return ok;
    }

    //validálás
    private boolean isValidMessage(String content) {
        return content != null 
            && !content.isBlank() 
            && content.length() <= MAX_MESSAGE_LENGTH;
    }

    private boolean isValidGroupName(String name) {
        return name != null 
            && !name.isBlank()
            && name.length() <= MAX_GROUP_NAME_LENGTH;
    }

    //jogosultság ellenőrzés
    private boolean checkPermission(UUID groupId, String username, String permission) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        return group.hasPermission(user.getId(), permission);
    }

    //adatok mentése
    public boolean saveStore() {
        try {
            boolean saved = FileManager.save(store, dataFile);
            if (saved) {
                updateTimestamp();  //mentés után frissítjük az időbélyeget
            }
            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //felhasználó regisztrációja
    public RegistrationResult registerUser(String username, String passwordHash) {
        if (username == null || username.isBlank()) {
            return RegistrationResult.USERNAME_TOO_SHORT;
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            return RegistrationResult.USERNAME_TOO_SHORT;
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return RegistrationResult.USERNAME_TOO_LONG;
        }
        
        boolean created = executeAndSave(() -> store.registerUser(username, passwordHash));
        return created ? RegistrationResult.SUCCESS : RegistrationResult.USERNAME_ALREADY_TAKEN;
    }

    //felhasználó hitelesítése
    public boolean authenticateUser(String username, String plainPassword) {
        var user = store.getUserByName(username);
        if (user == null) return false;
        try {
            return hu.prog3.offlinechatprog3.util.PasswordUtil.checkPassword(plainPassword, user.getPasswordHash());
        } catch (Exception e) {
            return false;
        }
    }
    //Csoport létrehozása
    public UUID createGroup(String name, String creatorUsername) {
        if (!isValidGroupName(name)) return null;
        UUID id = store.createGroup(name, creatorUsername);
        saveStore();
        return id;
    }

    public Set<String> getGroupMembers(UUID groupId) {
        var group = store.getGroup(groupId);
        if (group == null) return Collections.emptySet();
        
        Set<String> usernames = new HashSet<>();
        for (UUID userId : group.getMemberRoles().keySet()) {
            String username = store.getUsernameById(userId);
            if (username != null) {
                usernames.add(username);
            }
        }
        return usernames;
    }

    public boolean addGroupMember(UUID groupId, String username, String role) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        group.addMember(user.getId(), role);
        return saveStore();
    }

    public boolean isGroupAdmin(UUID groupId, String username) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        return group.isAdmin(user.getId());
    }

    public boolean removeGroupMember(UUID groupId, String username) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        group.removeMember(user.getId());
        return saveStore();
    }

    public boolean addCustomRole(UUID groupId, String role) {
        var group = store.getGroup(groupId);
        if (group == null) return false;
        group.addRole(role);
        return saveStore();
    }

    public boolean setGroupMemberRole(UUID groupId, String username, String role) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        try {
            group.setMemberRole(user.getId(), role);
            return saveStore();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean setRolePermissions(UUID groupId, String role, Set<String> perms) {
        var group = store.getGroup(groupId);
        if (group == null) return false;
        try {
            group.setRolePermissions(role, perms);
            return saveStore();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasGroupPermission(UUID groupId, String username, String permission) {
        return checkPermission(groupId, username, permission);
    }

    public boolean sendGroupMessage(UUID groupId, String from, String content) {
        if (!isValidMessage(content)) return false;
        if (!checkPermission(groupId, from, Permissions.GROUP_SEND_MESSAGE)) return false;
        var user = store.getUserByName(from);
        if (user == null) return false;
        store.sendGroupMessage(user.getId(), groupId, content);
        saveStore();
        return saveStore();
    }

    public boolean deleteGroupMessage(UUID groupId, UUID messageId, String requester) {
        if (!checkPermission(groupId, requester, Permissions.GROUP_DELETE_MESSAGES)) return false;
        store.deleteGroupMessage(groupId, messageId);
        return saveStore();
    }

    public boolean deleteGroup(UUID groupId, String requester) {
        if (!checkPermission(groupId, requester, Permissions.GROUP_DELETE_GROUP)) return false;
        store.deleteGroup(groupId);
        return saveStore();
    }

    public boolean sendPrivateMessage(String from, String to, String content) {
        if (!isValidMessage(content)) return false;
        if (!store.areFriends(from, to)) return false;
        var user = store.getUserByName(from);
        if (user == null) return false;
        store.sendPrivateMessage(user.getId(), from, to, content);
        saveStore();
        return true;
    }
}
