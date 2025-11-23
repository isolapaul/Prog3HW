package hu.prog3.offlinechatprog3.ui;

//UI üzenetek egy helyen
public final class UiMessages {
    
    private UiMessages() {}
    
    //hiba üzenet ablak címe
    public static final String ERR_TITLE = "Hiba";
    
    //figyelmeztetéses ablak címe
    public static final String WARN_TITLE = "Figyelmeztetés";
    
    //információ ablak címe
    public static final String INFO_TITLE = "Info";
    //hibaüzenetek
    public static final String SELECT_FRIEND = "Válassz egy barátot a listából.";
    public static final String SELECT_GROUP = "Válassz egy csoportot.";
    public static final String SEND_FAILED = "Küldés sikertelen.";
    public static final String NO_PERM_SEND_GROUP = "Nincs jogosultságod üzenetet küldeni ebbe a csoportba.";
    public static final String NO_PERM_REMOVE = "Nincs jogosultságod tag eltávolítására.";
    public static final String NO_PERM_ADD = "Nincs jogosultságod tag hozzáadására.";
    public static final String NO_PERM_DELETE_MSG = "Nincs jogosultságod üzenetek törlésére ebben a csoportban.";
    public static final String NO_PERM_DELETE_GROUP = "Nincs jogosultságod a törléshez vagy hiba történt.";
}
