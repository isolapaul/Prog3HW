package hu.prog3.offlinechatprog3.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionsTest {

    @Test
    void testAllPermissionConstant() {
        assertEquals("ALL", Permissions.ALL);
    }

    @Test
    void testGroupPermissionConstants() {
        assertEquals("GROUP_SEND_MESSAGE", Permissions.GROUP_SEND_MESSAGE);
        assertEquals("GROUP_DELETE_MESSAGES", Permissions.GROUP_DELETE_MESSAGES);
        assertEquals("GROUP_ADD_MEMBER", Permissions.GROUP_ADD_MEMBER);
        assertEquals("GROUP_REMOVE_MEMBER", Permissions.GROUP_REMOVE_MEMBER);
        assertEquals("GROUP_DELETE_GROUP", Permissions.GROUP_DELETE_GROUP);
        assertEquals("GROUP_READ", Permissions.GROUP_READ);
    }

    @Test
    void testGetDescription() {
        assertEquals("Minden jogosultság (teljes hozzáférés)", Permissions.getDescription(Permissions.ALL));
        assertEquals("Üzenet küldése a csoportban", Permissions.getDescription(Permissions.GROUP_SEND_MESSAGE));
        assertEquals("Üzenetek törlése a csoportban", Permissions.getDescription(Permissions.GROUP_DELETE_MESSAGES));
        assertEquals("Új tag hozzáadása a csoporthoz", Permissions.getDescription(Permissions.GROUP_ADD_MEMBER));
        assertEquals("Tag eltávolítása a csoportból", Permissions.getDescription(Permissions.GROUP_REMOVE_MEMBER));
        assertEquals("Csoport törlése", Permissions.getDescription(Permissions.GROUP_DELETE_GROUP));
        assertEquals("Üzenetek olvasása", Permissions.getDescription(Permissions.GROUP_READ));
        assertEquals("Ismeretlen jogosultság", Permissions.getDescription("UNKNOWN_PERMISSION"));
    }
}
