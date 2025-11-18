package hu.prog3.offlinechatprog3.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testMessageCreation() {
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String content = "hello bob";

        Message message = new Message(senderId, conversationId, content);

        assertNotNull(message.getId());
        assertEquals(senderId, message.getSenderId());
        assertEquals(conversationId, message.getConversationId());
        assertEquals(content, message.getContent());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testEmptyConstructor() {
        Message message = new Message();
        assertNotNull(message.getId());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testSetSenderId() {
        Message message = new Message();
        UUID newSenderId = UUID.randomUUID();
        message.setSenderId(newSenderId);
        assertEquals(newSenderId, message.getSenderId());
    }

    @Test
    void testSetConversationId() {
        Message message = new Message();
        UUID newConvId = UUID.randomUUID();
        message.setConversationId(newConvId);
        assertEquals(newConvId, message.getConversationId());
    }

    @Test
    void testSetContent() {
        Message message = new Message(UUID.randomUUID(), UUID.randomUUID(), "eredeti");
        message.setContent("modositott");
        assertEquals("modositott", message.getContent());
    }

    @Test
    void testSetTimestamp() {
        Message message = new Message(UUID.randomUUID(), UUID.randomUUID(), "uzenet");
        Instant newTime = Instant.now().plusSeconds(100);
        message.setTimestamp(newTime);
        assertEquals(newTime, message.getTimestamp());
    }

    @Test
    void testEquals() {
        Message msg1 = new Message();
        Message msg2 = new Message();
        
        assertNotEquals(msg2, msg1);
        assertEquals(msg1, msg1);
        assertNotEquals(null, msg1);
        assertNotEquals("nem uzenet", msg1);
    }

    @Test
    void testHashCode() {
        Message msg1 = new Message();
        Message msg2 = new Message();
        
        assertNotEquals(msg1.hashCode(), msg2.hashCode());
        assertEquals(msg1.hashCode(), msg1.hashCode());
    }
}
