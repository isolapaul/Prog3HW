package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Message implements Serializable {

    //fájl mentéshez szükséges verzió azonosító
    private static final long serialVersionUID = 1L;
    //egyedi azonosító - minden üzenetnek külön ID-ja van
    private UUID id;
    //küldő felhasználó ID-ja
    private UUID senderId;
    //csoport ID vagy beszélgetés ID
    private UUID conversationId;
    //üzenet szövege
    private String content;
    //időpont tárolása
    private Instant timestamp;
    //konstruktor
    public Message(UUID senderId, UUID conversationId, String content) {
        this.id = UUID.randomUUID(); //egyedi id generálása
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.content = content;
        this.timestamp = Instant.now(); //jelenlegi időpont
    }
    //üzenet id lekérdezése
    public UUID getId() {
        return id;
    }
    //küldő id lekérdezése
    public UUID getSenderId() {
        return senderId;
    }
    //csoport/beszelgetés id lekérdezése
    public UUID getConversationId() {
        return conversationId;
    }
    //üzenet tartalmának lekérdezése
    public String getContent() {
        return content;
    }
    //időpont lekérdezése
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; 
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o; 
        return Objects.equals(id, message.id); 
    }

    //hash kód generálása
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
