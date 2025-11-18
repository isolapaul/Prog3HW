package hu.prog3.offlinechatprog3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User u = new User("tesztElek","jelszo");
        assertEquals("tesztElek", u.getUsername());
        assertEquals("jelszo", u.getPasswordHash());
        assertNotNull(u.getId());
    }
}
