package hu.prog3.offlinechatprog3.model;

import hu.prog3.offlinechatprog3.persistence.DataStore;

import java.util.UUID;

/**
 * Központi jogosultság-ellenőrző osztály.
 * 
 * Ez az osztály felelős MINDEN jogosultság ellenőrzésért a rendszerben.
 * Kifejező metódusnevekkel teszi érthetővé, hogy ki mit tehet.
 * 
 * Előnyök:
 * - Egy helyen van az összes engedély-ellenőrzés logika
 * - Kifejező metódusnevek (canSendMessage, canDeleteGroup, stb.)
 * - Könnyen bővíthető új jogosultságokkal
 * - Az AppController nem tartalmaz jogosultság-logikát
 */
public class PermissionChecker {
    
    private final DataStore store;
    
    public PermissionChecker(DataStore store) {
        this.store = store;
    }
    
    // ========== CSOPORT ÜZENET JOGOSULTSÁGOK ==========
    
    /**
     * Ellenőrzi, hogy a felhasználó küldhet-e üzenetet a csoportba.
     */
    public boolean canSendMessage(UUID groupId, String username) {
        return store.hasGroupPermission(groupId, username, Permissions.GROUP_SEND_MESSAGE);
    }
    
    /**
     * Ellenőrzi, hogy a felhasználó törölhet-e üzeneteket a csoportban.
     */
    public boolean canDeleteMessages(UUID groupId, String username) {
        return store.hasGroupPermission(groupId, username, Permissions.GROUP_DELETE_MESSAGES);
    }
    
    // ========== CSOPORT TAG JOGOSULTSÁGOK ==========
    
    /**
     * Ellenőrzi, hogy a felhasználó hozzáadhat-e új tagot a csoporthoz.
     */
    public boolean canAddMember(UUID groupId, String username) {
        return store.hasGroupPermission(groupId, username, Permissions.GROUP_ADD_MEMBER);
    }
    
    /**
     * Ellenőrzi, hogy a felhasználó eltávolíthat-e tagot a csoportból.
     */
    public boolean canRemoveMember(UUID groupId, String username) {
        return store.hasGroupPermission(groupId, username, Permissions.GROUP_REMOVE_MEMBER);
    }
    
    // ========== CSOPORT KEZELÉS JOGOSULTSÁGOK ==========
    
    /**
     * Ellenőrzi, hogy a felhasználó törölheti-e az egész csoportot.
     * Ez a legerősebb jogosultság, általában csak Admin-nak van.
     */
    public boolean canDeleteGroup(UUID groupId, String username) {
        return store.hasGroupPermission(groupId, username, Permissions.GROUP_DELETE_GROUP);
    }
    
    /**
     * Ellenőrzi, hogy a felhasználónak van-e bármilyen adminisztrátori joga.
     * (ALL permission = minden jogosultság)
     */
    public boolean isAdmin(UUID groupId, String username) {
        return store.hasGroupPermission(groupId, username, Permissions.ALL);
    }
    
    // ========== ÁLTALÁNOS PERMISSION ELLENŐRZÉS ==========
    
    /**
     * Általános permission ellenőrzés - ha speciális metódus nem létezik.
     * Használd inkább a kifejező metódusokat (canSendMessage, stb.)!
     */
    public boolean hasPermission(UUID groupId, String username, String permission) {
        return store.hasGroupPermission(groupId, username, permission);
    }
}
