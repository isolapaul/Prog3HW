package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Message model used for both private and group messages.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID senderId;
    private UUID conversationId; // could be private conversation id or group id
    private String content;
    private Instant timestamp;

    public Message() {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    public Message(UUID senderId, UUID conversationId, String content) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.content = content;
        this.timestamp = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
