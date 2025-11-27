package model;

/**
 * Csoportos jogosultságokat definiáló konstans osztály.
 * Tartalmazza az összes elérhető jogosultság string azonosítóját.
 * Utility osztály - nem példányosítható.
 */
public final class Permissions {

    private Permissions() {} 
    
    /** Teljes jogosultság - minden műveletet engedélyez */
    public static final String ALL = "ALL";
    
    /** Jogosultság: üzenet küldése a csoportba */
    public static final String GROUP_SEND_MESSAGE = "GROUP_SEND_MESSAGE";
    
    /** Jogosultság: tag hozzáadása a csoporthoz */
    public static final String GROUP_ADD_MEMBER = "GROUP_ADD_MEMBER";
    
    /** Jogosultság: tag eltávolítása a csoportból */
    public static final String GROUP_REMOVE_MEMBER = "GROUP_REMOVE_MEMBER";
    
    /** Jogosultság: üzenetek törlése a csoportból */
    public static final String GROUP_DELETE_MESSAGES = "GROUP_DELETE_MESSAGES";
    
    /** Jogosultság: csoport törlése */
    public static final String GROUP_DELETE_GROUP = "GROUP_DELETE_GROUP";
    
    /** Jogosultság: csoport üzeneteinek olvasása */
    public static final String GROUP_READ = "GROUP_READ";

}
