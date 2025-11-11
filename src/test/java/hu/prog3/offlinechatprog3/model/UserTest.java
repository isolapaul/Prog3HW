package hu.prog3.offlinechatprog3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void basicPropertiesAndEquality() {
        User u1 = new User("alice","pw");
        User u2 = new User("alice","pw");
        assertEquals(u1.getUsername(), u2.getUsername());
        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotNull(u1.toString());
    }
}
