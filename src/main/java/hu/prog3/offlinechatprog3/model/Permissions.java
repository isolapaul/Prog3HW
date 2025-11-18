package hu.prog3.offlinechatprog3.model;

/**
 * Csoport jogosultságok központi osztálya.
 * Minden jogosultság egy helyen van definiálva, átlátható és könnyen karbantartható.
 * 
 * Ez az osztály tartalmazza:
 * 1. A jogosultság konstansokat (String-ként, szerializációs kompatibilitás miatt)
 * 2. Emberi olvasható leírásokat minden jogosultsághoz
 * 3. Helper metódust a jogosultságok ellenőrzéséhez
 */
public final class Permissions {

    private Permissions() {} // Utility class, ne lehessen példányosítani
    
    // ========== JOGOSULTSÁG KONSTANSOK ==========
    
    /** 
     * Teljes jogosultság - minden műveletre jogot ad.
     * Általában Admin szerephez tartozik.
     */
    public static final String ALL = "ALL";
    
    /** 
     * Üzenet küldési jog a csoportban.
     * Nélküle csak olvasni lehet az üzeneteket.
     */
    public static final String GROUP_SEND_MESSAGE = "GROUP_SEND_MESSAGE";
    
    /** 
     * Tag hozzáadási jog - új tagok meghívása a csoportba.
     */
    public static final String GROUP_ADD_MEMBER = "GROUP_ADD_MEMBER";
    
    /** 
     * Tag eltávolítási jog - tagok kirúgása a csoportból.
     */
    public static final String GROUP_REMOVE_MEMBER = "GROUP_REMOVE_MEMBER";
    
    /** 
     * Üzenet törlési jog - mások üzeneteinek törlése.
     * Moderátori jogosultság.
     */
    public static final String GROUP_DELETE_MESSAGES = "GROUP_DELETE_MESSAGES";
    
    /** 
     * Csoport törlési jog - az egész csoport törlése.
     * Nagyon erős jogosultság, általában csak Admin-nak van.
     */
    public static final String GROUP_DELETE_GROUP = "GROUP_DELETE_GROUP";
    
    /** 
     * Olvasási jog - jelenleg minden szerepnek alapértelmezett.
     * Jövőbeli privát csoportokhoz hasznos lehet.
     */
    public static final String GROUP_READ = "GROUP_READ";
    
    // ========== LEÍRÁSOK ==========
    
    /**
     * Emberi olvasható leírást ad egy jogosultságról.
     * Hasznos UI-ban való megjelenítéshez.
     */
    public static String getDescription(String permission) {
        return switch (permission) {
            case ALL -> "Minden jogosultság (teljes hozzáférés)";
            case GROUP_SEND_MESSAGE -> "Üzenet küldése a csoportban";
            case GROUP_ADD_MEMBER -> "Új tag hozzáadása a csoporthoz";
            case GROUP_REMOVE_MEMBER -> "Tag eltávolítása a csoportból";
            case GROUP_DELETE_MESSAGES -> "Üzenetek törlése a csoportban";
            case GROUP_DELETE_GROUP -> "Csoport törlése";
            case GROUP_READ -> "Üzenetek olvasása";
            default -> "Ismeretlen jogosultság";
        };
    }
}
