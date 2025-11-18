package hu.prog3.offlinechatprog3.controller;

import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.PermissionChecker;
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
 * ALKALMAZÁS CONTROLLER OSZTÁLY
 * 
 * Ez az osztály a KÖZÉPSŐ RÉT AZ MVC ARCHITEKTÚRÁBAN:
 * - VIEW (UI) <-> CONTROLLER <-> MODEL (DataStore)
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * 1. ELVÁLASZTJA az UI-t az adatoktól
 *    - Az UI nem tud a DataStore-ról, csak a Controllerről
 *    - Ha változik az adatstruktúra, csak a Controller-t kell átírni
 * 
 * 2. KEZELI A FÁJL MŰVELETETKET
 *    - Betöltés alkalmazás indulásakor
 *    - Mentés minden módosítás után
 *    - Újratöltés, ha a fájl megváltozott (multi-instance support!)
 * 
 * 3. EGYSZERŰ API-T BIZTOSÍT az UI számára
 *    - registerUser(), sendPrivateMessage(), createGroup(), stb.
 *    - Minden metódus automatikusan ment és újratölt
 * 
 * HOGYAN MŰKÖDIK A MULTI-INSTANCE TÁMOGATÁS?
 * Ha két példány fut ugyanabból az alkalmazásból:
 * 1. Példány A üzenetet küld -> menti a fájlba
 * 2. Példány B timer-e (1.5 mp-enként) újratölti az adatokat a fájlból
 * 3. Példány B frissíti a UI-t
 */
public class AppController {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "offline-chat.dat";

    // MEZŐK (instance változók)
    
    /** Az összes adatot tartalmazó DataStore objektum */
    private DataStore store;
    
    /** Központi jogosultság-ellenőrző */
    private PermissionChecker permissionChecker;
    
    /** A fájl objektum (data/offline-chat.dat) */
    private final java.io.File dataFile;

    /**
     * KONSTRUKTOR
     * Amikor létrehozunk egy új AppController-t, automatikusan:
     * 1. Létrehozza/megkeresi a data mappát és fájlt
     * 2. Betölti az adatokat a fájlból (vagy üres DataStore-t hoz létre)
     */
    public AppController() {
        this.dataFile = ensureDataFile(); // Fájl előkészítése
        this.store = loadStore();         // Adatok betöltése
        this.permissionChecker = new PermissionChecker(store); // Jogosultság-ellenőrző
    }

    /**
     * ADATOK BETÖLTÉSE FÁJLBÓL
     * 
     * LÉPÉSEK:
     * 1. Megpróbáljuk betölteni a fájlt (FileManager.load)
     * 2. Ha sikeres (nem null), eltároljuk a módosítási időt és visszaadjuk
     * 3. Ha nem sikerül (null vagy hiba), ÚJ ÜRES DataStore-t hozunk létre
     * 
     * MIÉRT JÓ EZ?
     * - Az első alkalommal, amikor nincs még fájl, automatikusan üres DataStore jön létre
     * - Ha van fájl, betöltjük a korábbi adatokat
     * 
     * @return DataStore objektum (betöltött vagy új üres)
     */
    private DataStore loadStore() {
        try {
            // Próbáljuk betölteni a fájlt
            DataStore ds = FileManager.load(dataFile);
            
            if (ds != null) {
                return ds;
            }
        } catch (Exception e) {
            // Ha hiba van, kiírjuk debug-hoz
            e.printStackTrace();
        }
        
        // Ha nem sikerült betölteni, új üres DataStore-t hozunk létre
        return new DataStore();
    }

    /**
     * FÁJL ÉS MAPPA ELŐKÉSZÍTÉSE
     * 
     * LÉPÉSEK:
     * 1. Létrehozzuk a "data" mappát, ha nem létezik
     * 2. Visszaadjuk a "data/offline-chat.dat" fájl objektumát
     * 3. Ha hiba van, a projekt gyökerében próbáljuk létrehozni
     * 
     * @return File objektum a data/offline-chat.dat fájlra
     */
    private File ensureDataFile() {
        try {
            // Path objektum a "data" mappához
            Path dir = Paths.get(DATA_DIR);
            
            // Ha nem létezik a mappa, létrehozzuk (mkdir -p)
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            
            // Visszaadjuk a teljes útvonalat: data/offline-chat.dat
            return dir.resolve(DATA_FILE).toFile();
        } catch (Exception e) {
            // Ha hiba van (pl. nincs írási jog), a projekt gyökerében próbálkozunk
            return new File(DATA_FILE);
        }
    }

    /**
     * ADATOK MENTÉSE FÁJLBA
     * 
     * @return true = sikeres mentés, false = hiba történt
     */
    public boolean saveStore() {
        try {
            return FileManager.save(store, dataFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // UI-FACING API METÓDUSOK
    // Ezeket hívja meg az UI (LoginFrame, MainFrame, stb.)
    // ============================================================
    
    /**
     * FELHASZNÁLÓ REGISZTRÁLÁSA
     * 
     * @param username Felhasználónév
     * @param passwordHash Jelszó
     * @return true = sikeres regisztráció, false = foglalt név vagy hiba
     */
    public boolean registerUser(String username, String passwordHash) {
        boolean ok = store.registerUser(username, passwordHash);
        if (ok) saveStore();
        return ok;
    }

    /**
     * BEJELENTKEZÉS ELLENŐRZÉSE
     * 
     * @param username Felhasználónév
     * @param passwordHash Jelszó
     * @return true = helyes jelszó, false = hibás adatok
     */
    public boolean authenticateUser(String username, String passwordHash) {
        return store.authenticateUser(username, passwordHash);
    }

    public boolean addFriend(String me, String friend) {
        boolean ok = store.addFriend(me, friend);
        if (ok) saveStore();
        return ok;
    }

    /**
     * Send a friend request (does not directly create friendship).
     */
    public boolean sendFriendRequest(String from, String to) {
        boolean ok = store.sendFriendRequest(from, to);
        if (ok) saveStore();
        return ok;
    }

    public java.util.Set<String> getOutgoingFriendRequests(String username) {
        return store.getOutgoingFriendRequests(username);
    }

    public java.util.Set<String> getIncomingFriendRequests(String username) {
        return store.getIncomingFriendRequests(username);
    }

    public boolean acceptFriendRequest(String username, String from) {
        boolean ok = store.acceptFriendRequest(username, from);
        if (ok) saveStore();
        return ok;
    }

    public boolean rejectFriendRequest(String username, String from) {
        boolean ok = store.rejectFriendRequest(username, from);
        if (ok) saveStore();
        return ok;
    }

    public boolean cancelOutgoingFriendRequest(String from, String to) {
        boolean ok = store.cancelOutgoingFriendRequest(from, to);
        if (ok) saveStore();
        return ok;
    }

    // Group management wrappers
    public java.util.UUID createGroup(String name, String creatorUsername) {
        java.util.UUID id = store.createGroup(name, creatorUsername);
        saveStore();
        return id;
    }

    public java.util.Map<java.util.UUID, String> getAllGroups() {
        return store.getAllGroups();
    }

    public java.util.Set<String> getGroupMembers(java.util.UUID groupId) {
        return store.getGroupMembers(groupId);
    }

    public boolean addGroupMember(java.util.UUID groupId, String username, String role) {
        boolean ok = store.addGroupMember(groupId, username, role);
        if (ok) saveStore();
        return ok;
    }

    public boolean isGroupAdmin(java.util.UUID groupId, String username) {
        return store.isGroupAdmin(groupId, username);
    }

    public boolean removeGroupMember(java.util.UUID groupId, String username) {
        boolean ok = store.removeGroupMember(groupId, username);
        if (ok) saveStore();
        return ok;
    }

    public boolean addCustomRole(java.util.UUID groupId, String role) {
        boolean ok = store.addCustomRole(groupId, role);
        if (ok) saveStore();
        return ok;
    }

    // Group role/permission helpers
    public java.util.Map<String, String> getGroupMembersWithRoles(java.util.UUID groupId) {
        return store.getGroupMembersWithRoles(groupId);
    }

    public java.util.Set<String> getGroupAvailableRoles(java.util.UUID groupId) {
        return store.getAvailableRoles(groupId);
    }

    public boolean setGroupMemberRole(java.util.UUID groupId, String username, String role) {
        boolean ok = store.setGroupMemberRole(groupId, username, role);
        if (ok) saveStore();
        return ok;
    }

    public java.util.Set<String> getRolePermissions(java.util.UUID groupId, String role) {
        return store.getRolePermissions(groupId, role);
    }

    public boolean setRolePermissions(java.util.UUID groupId, String role, java.util.Set<String> perms) {
        boolean ok = store.setRolePermissions(groupId, role, perms);
        if (ok) saveStore();
        return ok;
    }

    public boolean hasGroupPermission(java.util.UUID groupId, String username, String permission) {
        return store.hasGroupPermission(groupId, username, permission);
    }

    public boolean sendGroupMessage(java.util.UUID groupId, String from, String content) {
        if (!permissionChecker.canSendMessage(groupId, from)) return false;
        store.sendGroupMessage(groupId, from, content);
        saveStore();
        return true;
    }

    public java.util.List<Message> getGroupMessages(java.util.UUID groupId) {
        return store.getGroupMessages(groupId);
    }

    public boolean deleteGroupMessage(java.util.UUID groupId, java.util.UUID messageId, String requester) {
        if (!permissionChecker.canDeleteMessages(groupId, requester)) return false;
        boolean ok = store.deleteGroupMessage(groupId, messageId);
        if (ok) saveStore();
        return ok;
    }

    public boolean deleteGroup(java.util.UUID groupId, String requester) {
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
        // send only if both users exist and are friends (per spec)
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
    public java.util.Set<String> getAllUsernames() {
        return store.getAllUsernames();
    }

    public DataStore getStore() { return store; }

    /**
     * Resolve a user's display name (username) from their UUID. Returns null if not found.
     */
    public String getUsernameForId(UUID id) {
        if (id == null) return null;
        return store.getUsernameById(id);
    }
}
