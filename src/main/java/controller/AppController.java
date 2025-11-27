package controller;

import model.Permissions;
import persistence.DataStore;
import persistence.FileManager;

import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * MVC Controller réteg - üzleti logika és adatkezelés.
 */
public class AppController {
    private static final String DATA_FILE_PATH = "data/offline-chat.dat";
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_GROUP_NAME_LENGTH = 30;

    private DataStore store;
    private final File dataFile;
    private long lastLoadedTimestamp = 0;

    /**
     * Controller inicializálása - adatok betöltése vagy új DataStore létrehozása.
     */
    public AppController() {
        this.dataFile = new File(DATA_FILE_PATH);
        this.dataFile.getParentFile().mkdirs();
        
        DataStore loaded = FileManager.load(dataFile);
        this.store = (loaded != null) ? loaded : new DataStore();
        updateTimestamp();
    }

    
    /**
     * Adattár elérése.
     * @return DataStore instance
     */
    public DataStore getDataStore() {
        return store;
    }

    private void updateTimestamp() {
        if (dataFile.exists()) {
            lastLoadedTimestamp = dataFile.lastModified();
        }
    }

    /**
     * Adattár újratöltése fájlból, ha módosult.
     */
    public void reloadStore() {
        if (!dataFile.exists()) return;
        
        long currentFileTime = dataFile.lastModified();
        //csak akkor töltünk újra, ha a fájl módosult az utolsó betöltés óta
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

    /**
     * Adatok mentése fájlba.
     * @return true ha sikeres
     */
    public boolean saveStore() {
        try {
            boolean saved = FileManager.save(store, dataFile);
            if (saved) {
                updateTimestamp();
            }
            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Új felhasználó regisztrálása.
     * @param username felhasználónév
     * @param passwordHash bcrypt hash
     * @return regisztráció eredménye
     */
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

    /**
     * Felhasználó hitelesítése.
     * @param username felhasználónév
     * @param plainPassword jelszó
     * @return true ha sikeres
     */
    public boolean authenticateUser(String username, String plainPassword) {
        var user = store.getUserByName(username);
        if (user == null) return false;
        try {
            return util.PasswordUtil.checkPassword(plainPassword, user.getPasswordHash());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Csoport létrehozása.
     * @param name csoport neve
     * @param creatorUsername létrehozó
     * @return csoport UUID vagy null
     */
    public UUID createGroup(String name, String creatorUsername) {
        if (!isValidGroupName(name)) return null;
        UUID id = store.createGroup(name, creatorUsername);
        saveStore();
        return id;
    }

    /**
     * Csoport tagjainak lekérdezése.
     * @param groupId csoport UUID
     * @return felhasználónevek halmaza
     */
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

    /**
     * Tag hozzáadása csoporthoz.
     * @param groupId csoport UUID
     * @param username felhasználónév
     * @param role szerep
     * @return true ha sikeres
     */
    public boolean addGroupMember(UUID groupId, String username, String role) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        group.addMember(user.getId(), role);
        return saveStore();
    }

    /**
     * Ellenőrzi, hogy a felhasználó admin-e.
     * @param groupId csoport UUID
     * @param username felhasználónév
     * @return true ha admin
     */
    public boolean isGroupAdmin(UUID groupId, String username) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        return group.isAdmin(user.getId());
    }

    /**
     * Tag eltávolítása csoportból.
     * @param groupId csoport UUID
     * @param username felhasználónév
     * @return true ha sikeres
     */
    public boolean removeGroupMember(UUID groupId, String username) {
        var group = store.getGroup(groupId);
        var user = store.getUserByName(username);
        if (group == null || user == null) return false;
        group.removeMember(user.getId());
        return saveStore();
    }

    /**
     * Egyéni szerep hozzáadása.
     * @param groupId csoport UUID
     * @param role szerep neve
     * @return true ha sikeres
     */
    public boolean addCustomRole(UUID groupId, String role) {
        var group = store.getGroup(groupId);
        if (group == null) return false;
        group.addRole(role);
        return saveStore();
    }

    /**
     * Tag szerepének megváltoztatása.
     * @param groupId csoport UUID
     * @param username felhasználónév
     * @param role új szerep
     * @return true ha sikeres
     */
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

    /**
     * Szerep jogosultságainak beállítása.
     * @param groupId csoport UUID
     * @param role szerep neve
     * @param perms jogosultságok
     * @return true ha sikeres
     */
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

    /**
     * Jogosultság ellenőrzése csoportban.
     * @param groupId csoport UUID
     * @param username felhasználónév
     * @param permission jogosultság
     * @return true ha van jogosultsága
     */
    public boolean hasGroupPermission(UUID groupId, String username, String permission) {
        return checkPermission(groupId, username, permission);
    }

    /**
     * Üzenet küldése csoportba.
     * @param groupId csoport UUID
     * @param from küldő
     * @param content tartalom
     * @return true ha sikeres
     */
    public boolean sendGroupMessage(UUID groupId, String from, String content) {
        if (!isValidMessage(content)) return false;
        if (!checkPermission(groupId, from, Permissions.GROUP_SEND_MESSAGE)) return false;
        var user = store.getUserByName(from);
        if (user == null) return false;
        store.sendGroupMessage(user.getId(), groupId, content);
        return saveStore();
    }

    /**
     * Csoport üzenet törlése.
     * @param groupId csoport UUID
     * @param messageId üzenet UUID
     * @param requester kérelmő
     * @return true ha sikeres
     */
    public boolean deleteGroupMessage(UUID groupId, UUID messageId, String requester) {
        if (!checkPermission(groupId, requester, Permissions.GROUP_DELETE_MESSAGES)) return false;
        store.deleteGroupMessage(groupId, messageId);
        return saveStore();
    }

    /**
     * Csoport törlése.
     * @param groupId csoport UUID
     * @param requester kérelmő
     * @return true ha sikeres
     */
    public boolean deleteGroup(UUID groupId, String requester) {
        if (!checkPermission(groupId, requester, Permissions.GROUP_DELETE_GROUP)) return false;
        store.deleteGroup(groupId);
        return saveStore();
    }

    /**
     * Privát üzenet küldése.
     * @param from küldő
     * @param to címzett
     * @param content tartalom
     * @return true ha sikeres
     */
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
