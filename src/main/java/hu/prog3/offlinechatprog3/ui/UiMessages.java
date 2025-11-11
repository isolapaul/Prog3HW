package hu.prog3.offlinechatprog3.ui;

/**
 * UI SZÖVEGEK GYŰJTEMÉNYE (UiMessages)
 * 
 * Ez egy UTILITY osztály, ami KONSTANS SZÖVEGEKET tárol.
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * - Sok helyen ugyanazokat a hibaüzeneteket használjuk
 * - Ha egy helyen módosítjuk, mindenhol változik
 * - Könnyebb lefordítani más nyelvre (minden szöveg egy helyen van)
 * 
 * MIÉRT FINAL?
 * Nem lehet örökölni belőle.
 * 
 * MIÉRT PRIVATE KONSTRUKTOR?
 * Nem lehet példányosítani (new UiMessages() nem működik).
 * 
 * HASZNÁLAT:
 * JOptionPane.showMessageDialog(this, UiMessages.SELECT_FRIEND, ...);
 */
public final class UiMessages {
    
    /**
     * Private konstruktor - megakadályozza a példányosítást
     */
    private UiMessages() {}

    // ============================================================
    // ABLAK CÍMEK (popup ablakok title-je)
    // ============================================================
    
    /** Hiba üzenet ablak címe */
    public static final String ERR_TITLE = "Hiba";
    
    /** Figyelmeztetés ablak címe */
    public static final String WARN_TITLE = "Figyelmeztetés";
    
    /** Információs ablak címe */
    public static final String INFO_TITLE = "Info";

    // ============================================================
    // ÁLTALÁNOS HIBAÜZENETEK
    // ============================================================
    
    /** Amikor nem választott ki barátot a listából */
    public static final String SELECT_FRIEND = "Válassz egy barátot a listából.";
    
    /** Amikor nem választott ki csoportot a listából */
    public static final String SELECT_GROUP = "Válassz egy csoportot.";
    
    /** Amikor az üzenet küldése sikertelen */
    public static final String SEND_FAILED = "Küldés sikertelen.";

    // ============================================================
    // JOGOSULTSÁG HIBAÜZENETEK (csoportokban)
    // ============================================================
    
    /** Amikor nincs joga üzenetet küldeni a csoportba */
    public static final String NO_PERM_SEND_GROUP = "Nincs jogosultságod üzenetet küldeni ebbe a csoportba.";
    
    /** Amikor nincs joga tagot eltávolítani */
    public static final String NO_PERM_REMOVE = "Nincs jogosultságod tag eltávolítására.";
    
    /** Amikor nincs joga tagot hozzáadni */
    public static final String NO_PERM_ADD = "Nincs jogosultságod tag hozzáadására.";
    
    /** Amikor nincs joga üzenetet törölni */
    public static final String NO_PERM_DELETE_MSG = "Nincs jogosultságod üzenetek törlésére ebben a csoportban.";
    
    /** Amikor nincs joga csoportot törölni */
    public static final String NO_PERM_DELETE_GROUP = "Nincs jogosultságod a törléshez vagy hiba történt.";
}
