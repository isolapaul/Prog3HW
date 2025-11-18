package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * ÜZENET MODELL OSZTÁLY
 * 
 * Ez az osztály egy chat üzenetet reprezentál.
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * - Tárolni kell, hogy KI küldte az üzenetet (senderId)
 * - Tárolni kell, hogy HOL lett küldve (conversationId - lehet privát beszélgetés vagy csoport)
 * - Tárolni kell MIKOR küldték (timestamp)
 * - Tárolni kell, hogy MI a tartalma (content)
 * 
 * HOGYAN MŰKÖDIK?
 * Ez az osztály UNIVERZÁLIS - ugyanazt az osztályt használjuk:
 * - Privát üzeneteknél (két felhasználó között)
 * - Csoport üzeneteknél (csoportban)
 * 
 * A conversationId határozza meg, hogy hova tartozik:
 * - Privát üzenetnél: null vagy egy generált conversation ID
 * - Csoport üzenetnél: a csoport ID-ja
 */
public class Message implements Serializable {

    // Fájl mentéshez szükséges verzió azonosító
    private static final long serialVersionUID = 1L;

    // MEZŐK (az üzenet adatai):
    
    /** Egyedi azonosító - minden üzenetnek külön ID-ja van */
    private UUID id;
    
    /** Ki küldte az üzenetet? A küldő felhasználó ID-ja */
    private UUID senderId;
    
    /** Hova lett küldve? Csoport ID vagy beszélgetés ID */
    private UUID conversationId;
    
    /** Mi az üzenet szövege? Pl. "Szia, hogy vagy?" */
    private String content;
    
    /** Mikor lett küldve? Időpont tárolása */
    private Instant timestamp;

    /**
     * KONSTRUKTOR
     * Létrehoz egy új üzenetet minden szükséges adattal.
     * 
     * @param senderId Ki küldi az üzenetet (felhasználó ID)
     * @param conversationId Hova megy az üzenet (beszélgetés vagy csoport ID)
     * @param content Az üzenet szövege
     */
    public Message(UUID senderId, UUID conversationId, String content) {
        this.id = UUID.randomUUID(); // Egyedi ID generálás
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.content = content;
        this.timestamp = Instant.now(); // Jelenlegi időpont automatikus rögzítése
    }

    // GETTER ÉS SETTER METÓDUSOK
    // Ezekkel tudjuk lekérdezni és módosítani az üzenet adatait
    
    /**
     * ÜZENET ID LEKÉRDEZÉSE
     * @return Az üzenet egyedi azonosítója
     */
    public UUID getId() {
        return id;
    }

    /**
     * KÜLDŐ ID LEKÉRDEZÉSE
     * @return Ki küldte az üzenetet (felhasználó ID)
     */
    public UUID getSenderId() {
        return senderId;
    }

    /**
     * KÜLDŐ ID BEÁLLÍTÁSA
     * @param senderId Az új küldő ID
     */
    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    /**
     * BESZÉLGETÉS/CSOPORT ID LEKÉRDEZÉSE
     * @return Hova tartozik ez az üzenet (conversation vagy group ID)
     */
    public UUID getConversationId() {
        return conversationId;
    }

    /**
     * BESZÉLGETÉS/CSOPORT ID BEÁLLÍTÁSA
     * @param conversationId Az új conversation/group ID
     */
    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * ÜZENET TARTALOM LEKÉRDEZÉSE
     * @return Az üzenet szövege
     */
    public String getContent() {
        return content;
    }

    /**
     * ÜZENET TARTALOM BEÁLLÍTÁSA
     * @param content Az új üzenet szöveg
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * KÜLDÉSI IDŐPONT LEKÉRDEZÉSE
     * @return Mikor lett elküldve ez az üzenet (Instant = pontos időpont)
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * KÜLDÉSI IDŐPONT BEÁLLÍTÁSA
     * @param timestamp Az új időpont
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * EGYENLŐSÉG ELLENŐRZÉS
     * Két üzenet akkor egyenlő, ha az ID-juk megegyezik.
     * 
     * MIÉRT AZ ID ALAPJÁN?
     * Mert két különböző üzenet lehet ugyanaz a szöveg ugyanattól a felhasználótól,
     * de a küldési időpont és ID mindig egyedi.
     * 
     * @param o A másik objektum
     * @return true ha azonos üzenet, false ha nem
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Ugyanaz az objektum
        if (o == null || getClass() != o.getClass()) return false; // null vagy nem Message
        Message message = (Message) o; // Átkasztolás Message típusra
        return Objects.equals(id, message.id); // ID összehasonlítás
    }

    /**
     * HASH KÓD GENERÁLÁS
     * Az ID alapján számolt hash kód gyors kereséshez.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
