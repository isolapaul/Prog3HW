package model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Üzenetet reprezentáló modell osztály.
 * Használható privát üzenetekhez és csoportos üzenetekhez egyaránt.
 * Minden üzenet tartalmaz egyedi azonosítót, küldő ID-t, beszélgetés/csoport ID-t,
 * tartalmat és időbélyeget.
 */
public class Message implements Serializable {

    /** Verziószám a szerializációhoz */
    private static final long serialVersionUID = 1L;
    
    /** Egyedi azonosító - minden üzenetnek külön ID-ja van */
    private UUID id;
    
    /** Küldő felhasználó ID-ja */
    private UUID senderId;
    
    /** Csoport ID vagy beszélgetés ID (privát üzeneteknél a privateKey(a, b) eredménye UUID formában) */
    private UUID conversationId;
    
    /** Üzenet szövege */
    private String content;
    
    /** Időbélyeg - az üzenet létrehozásának pontos időpontja */
    private Instant timestamp;
    
    /**
     * Létrehoz egy új üzenetet a megadott küldővel, beszélgetés azonosítóval és tartalommal.
     * Automatikusan generál egy egyedi UUID azonosítót és időbélyeget.
     * 
     * @param senderId a küldő felhasználó UUID-ja (nem lehet null)
     * @param conversationId a beszélgetés vagy csoport UUID-ja (nem lehet null)
     * @param content az üzenet szöveges tartalma (nem lehet null)
     */
    public Message(UUID senderId, UUID conversationId, String content) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.content = content;
        this.timestamp = Instant.now();
    }
    
    /**
     * Visszaadja az üzenet egyedi azonosítóját.
     * 
     * @return az üzenet UUID azonosítója
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Visszaadja a küldő felhasználó azonosítóját.
     * 
     * @return a küldő UUID azonosítója
     */
    public UUID getSenderId() {
        return senderId;
    }
    
    /**
     * Visszaadja a beszélgetés vagy csoport azonosítóját.
     * 
     * @return a conversationId UUID
     */
    public UUID getConversationId() {
        return conversationId;
    }
    
    /**
     * Visszaadja az üzenet szöveges tartalmát.
     * 
     * @return az üzenet tartalma
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Visszaadja az üzenet időbélyegét.
     * 
     * @return az üzenet létrehozásának időpontja (Instant)
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Két üzenet akkor egyenlő, ha az azonosítójuk (id) megegyezik.
     * 
     * @param o az összehasonlítandó objektum
     * @return true ha az objektumok egyenlőek, egyébként false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; 
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o; 
        return Objects.equals(id, message.id); 
    }

    /**
     * Hash kód generálása az üzenet azonosítója alapján.
     * 
     * @return az üzenet hash kódja
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
