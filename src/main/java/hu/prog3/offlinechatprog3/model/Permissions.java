package hu.prog3.offlinechatprog3.model;

/**
 * JOGOSULTSÁG KONSTANSOK OSZTÁLYA
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * Ahelyett, hogy mindenhol kézzel írnánk be a jogosultság neveket (pl. "GROUP_SEND_MESSAGE"),
 * itt egy helyen definiáljuk őket. Ez azért jó, mert:
 * - Ha elírunk valamit, fordítási hibát kapunk (nem futásidejűt)
 * - Egy helyen lehet módosítani, ha változik a jogosultság neve
 * - Átláthatóbb a kód
 * 
 * HOGYAN HASZNÁLJUK?
 * Permissions.GROUP_SEND_MESSAGE helyett "GROUP_SEND_MESSAGE" írása
 * 
 * MIÉRT FINAL ÉS PRIVATE KONSTRUKTOR?
 * - final = nem lehet leszármaztatni (nem kell)
 * - private konstruktor = nem lehet példányosítani (csak a konstansokat használjuk)
 * Ez egy úgynevezett "utility class" - csak konstansokat tárol.
 */
public final class Permissions {
    
    /**
     * PRIVATE KONSTRUKTOR
     * Megakadályozza, hogy valaki new Permissions()-t írjon.
     * Nem kell példány, csak a konstansok kellnek.
     */
    private Permissions() {}

    /**
     * MINDEN JOGOSULTSÁG
     * Ha valakinek "ALL" joga van, akkor MINDEN műveletre joga van.
     * Általában az Admin szerepnek van ilyen joga.
     */
    public static final String ALL = "ALL";

    /**
     * ÜZENET KÜLDÉSI JOG
     * Lehetővé teszi, hogy üzenetet küldjön a csoportba.
     * Nélküle csak olvasni lehet az üzeneteket.
     */
    public static final String GROUP_SEND_MESSAGE = "GROUP_SEND_MESSAGE";
    
    /**
     * TAG HOZZÁADÁSI JOG
     * Lehetővé teszi új tagok meghívását a csoportba.
     */
    public static final String GROUP_ADD_MEMBER = "GROUP_ADD_MEMBER";
    
    /**
     * TAG ELTÁVOLÍTÁSI JOG
     * Lehetővé teszi tagok kirúgását a csoportból.
     */
    public static final String GROUP_REMOVE_MEMBER = "GROUP_REMOVE_MEMBER";
    
    /**
     * ÜZENET TÖRLÉSI JOG
     * Lehetővé teszi mások üzeneteinek törlését.
     * Fontos moderátori jogosultság.
     */
    public static final String GROUP_DELETE_MESSAGES = "GROUP_DELETE_MESSAGES";
    
    /**
     * CSOPORT TÖRLÉSI JOG
     * Lehetővé teszi az egész csoport törlését.
     * Nagyon erős jogosultság, általában csak az Admin-nak van.
     */
    public static final String GROUP_DELETE_GROUP = "GROUP_DELETE_GROUP";
    
    /**
     * OLVASÁSI JOG
     * Jelenleg nem használt - mindenki olvashat alapértelmezetten.
     * Jövőbeli bővítéshez lehet hasznos (privát csoportok).
     */
    public static final String GROUP_READ = "GROUP_READ";
}
