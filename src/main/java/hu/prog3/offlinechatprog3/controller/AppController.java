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
 * 2. Fájl módosítási ideje (lastModified) megváltozik
 * 3. Példány B timer-e meghívja reloadIfChanged()-t
 * 4. Példány B észreveszi a változást (lastModified újabb)
 * 5. Példány B újratölti az adatokat a fájlból
 * 6. Példány B frissíti a UI-t
 * 
 * FONTOS MINTA: Minden publikus metódus
 * 1. Meghívja reloadIfChanged() - friss adatokat használunk
 * 2. Meghívja a DataStore megfelelő metódusát
 * 3. Ha módosítás történt, meghívja saveStore() - perzisztálás
 * 4. Visszaadja az eredményt
 */
public class AppController {

    // KONSTANSOK - fájl elérési útvonal
    /** Az "data" mappa neve a projekt gyökerében */
    private static final String DATA_DIR = "data";
    
    /** Az adatfájl neve (data/offline-chat.dat) */
    private static final String DATA_FILE = "offline-chat.dat";

    // MEZŐK (instance változók)
    
    /** Az összes adatot tartalmazó DataStore objektum */
    private DataStore store;
    
    /** A fájl objektum (data/offline-chat.dat) */
    private final java.io.File dataFile;
    
    /**
     * A fájl utolsó módosítási ideje (timestamp milliszekundumban)
     * Ezt használjuk annak ellenőrzésére, hogy megváltozott-e a fájl.
     */
    private long lastLoadedModified = 0L;

    /**
     * KONSTRUKTOR
     * Amikor létrehozunk egy új AppController-t, automatikusan:
     * 1. Létrehozza/megkeresi a data mappát és fájlt
     * 2. Betölti az adatokat a fájlból (vagy üres DataStore-t hoz létre)
     */
    public AppController() {
        this.dataFile = ensureDataFile(); // Fájl előkészítése
        this.store = loadStore();         // Adatok betöltése
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
                // Sikeres betöltés - eltároljuk a fájl módosítási időpontját
                this.lastLoadedModified = dataFile.lastModified();
                return ds;
            }
        } catch (Exception e) {
            // Ha hiba van, kiírjuk debug-hoz
            e.printStackTrace();
        }
        
        // Ha nem sikerült betölteni, új üres DataStore-t hozunk létre
        this.lastLoadedModified = dataFile.lastModified();
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
     * ADATOK ÚJRATÖLTÉSE, HA A FÁJL MEGVÁLTOZOTT
     * 
     * Ez a KULCSFONTOSSÁGÚ METÓDUS a multi-instance támogatáshoz!
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Lekérdezzük a fájl jelenlegi módosítási idejét (timestamp)
     * 2. Összehasonlítjuk az előző módosítási idővel (lastLoadedModified)
     * 3. Ha ÚJABB (mod > lastLoadedModified), akkor:
     *    - Valaki más (másik példány) módosította a fájlt
     *    - Újratöltjük az adatokat
     *    - Frissítjük a lastLoadedModified-t
     * 4. Ha NINCS változás, nem csinálunk semmit (gyors!)
     * 
     * MIÉRT CATCH (EXCEPTION IGNORED)?
     * Ha pont abban a pillanatban olvassuk a fájlt, amikor más írja,
     * előfordulhat átmeneti hiba. Ilyenkor egyszerűen nem töltünk újra,
     * a következő meghívásnál újra próbálkozunk.
     * 
     * PÉLDA HASZNÁLAT:
     * Minden publikus metódus elején meghívjuk, hogy friss adatokat használjunk.
     */
    private void reloadIfChanged() {
        try {
            // Jelenlegi fájl módosítási idő
            long mod = dataFile.lastModified();
            
            // Csak akkor töltünk újra, ha változott a fájl
            if (mod > lastLoadedModified) {
                // Újratöltés
                DataStore ds = FileManager.load(dataFile);
                
                if (ds != null) {
                    // Sikeres újratöltés
                    this.store = ds; // Cseréljük le a régi store-t az újra
                    this.lastLoadedModified = mod; // Frissítjük a timestamp-et
                }
            }
        } catch (Exception ignored) {
            // Átmeneti hibát ignorálunk (pl. fájl éppen írás alatt van)
            // A következő meghívásnál újra próbálkozunk
        }
    }

    /**
     * ADATOK MENTÉSE FÁJLBA
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Meghívjuk a FileManager.save() metódust
     * 2. Ha sikeres, frissítjük a lastLoadedModified-t
     * 3. Visszaadjuk, hogy sikeres volt-e
     * 
     * MIÉRT KELL FRISSÍTENI A lastLoadedModified-t?
     * Azért, hogy ne töltsük újra a saját mentésünket!
     * Ha mentés után nem frissítenénk, a következő reloadIfChanged()
     * újratöltené a fájlt (feleslegesen).
     * 
     * @return true = sikeres mentés, false = hiba történt
     */
    public boolean saveStore() {
        try {
            // Mentés fájlba
            boolean ok = FileManager.save(store, dataFile);
            
            if (ok) {
                // Sikeres mentés - frissítjük a timestamp-et
                // hogy ne töltsük újra a saját mentésünket
                this.lastLoadedModified = dataFile.lastModified();
            }
            
            return ok;
        } catch (Exception e) {
            // Hiba esetén kiírjuk debug-hoz és false-t adunk vissza
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
     * LÉPÉSEK:
     * 1. Újratöltjük az adatokat, ha változtak (multi-instance)
     * 2. Meghívjuk a DataStore.registerUser() metódusát
     * 3. Ha sikeres, elmentjük a fájlba
     * 4. Visszaadjuk az eredményt
     * 
     * EZ A MINTA MINDEN MÓDOSÍTÓ METÓDUSNÁL HASZNÁLT!
     * 
     * @param username Felhasználónév
     * @param passwordHash Jelszó
     * @return true = sikeres regisztráció, false = foglalt név vagy hiba
     */
    public boolean registerUser(String username, String passwordHash) {
        reloadIfChanged();  // 1. Friss adatok
        boolean ok = store.registerUser(username, passwordHash); // 2. Művelet
        if (ok) saveStore(); // 3. Mentés, ha sikeres volt
        return ok;           // 4. Eredmény visszaadása
    }

    /**
     * BEJELENTKEZÉS ELLENŐRZÉSE
     * 
     * Ez egy LEKÉRDEZŐ metódus (nem módosít), ezért:
     * - Meghívjuk reloadIfChanged()-t (friss adatok)
     * - NEM hívjuk meg saveStore()-t (nincs változás)
     * 
     * @param username Felhasználónév
     * @param passwordHash Jelszó
     * @return true = helyes jelszó, false = hibás adatok
     */
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
