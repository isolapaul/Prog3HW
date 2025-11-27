package hu.prog3.offlinechatprog3.ui;

/**
 * UI üzenet konstansok.
 */
public final class UiMessages {
    
    private UiMessages() {}
    
    /** Hiba ablak címe */
    public static final String ERR_TITLE = "Hiba";
    /** Figyelmeztetés ablak címe */
    public static final String WARN_TITLE = "Figyelmeztetés";
    /** Információ ablak címe */
    public static final String INFO_TITLE = "Info";
    /** Barát kiválasztására szólít fel */
    public static final String SELECT_FRIEND = "Válassz egy barátot a listából.";
    /** Csoport kiválasztására szólít fel */
    public static final String SELECT_GROUP = "Válassz egy csoportot.";
    /** Küldés sikertelen */
    public static final String SEND_FAILED = "Küldés sikertelen.";
    /** Nincs jogosultság csoport üzenet küldésre */
    public static final String NO_PERM_SEND_GROUP = "Nincs jogosultságod üzenetet küldeni ebbe a csoportba.";
    /** Nincs jogosultság tag eltávolítására */
    public static final String NO_PERM_REMOVE = "Nincs jogosultságod tag eltávolítására.";
    /** Nincs jogosultság tag hozzáadására */
    public static final String NO_PERM_ADD = "Nincs jogosultságod tag hozzáadására.";
    /** Nincs jogosultság üzenetek törlésére */
    public static final String NO_PERM_DELETE_MSG = "Nincs jogosultságod üzenetek törlésére ebben a csoportban.";
    /** Nincs jogosultság csoport törlésére */
    public static final String NO_PERM_DELETE_GROUP = "Nincs jogosultságod a törléshez vagy hiba történt.";
}
