package hu.prog3.offlinechatprog3.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    private Group group;
    private UUID adminId;
    private UUID participantId;
    private UUID readerId;

    @BeforeEach
    void setUp() {
        group = new Group("Test Group");
        adminId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        readerId = UUID.randomUUID();
        
        group.addMember(adminId, "Adminisztrátor");
        group.addMember(participantId, "Résztvevő");
        group.addMember(readerId, "Olvasó");
    }

    @Test
    void testGroupCreation() {
        assertNotNull(group.getId());
        assertEquals("Test Group", group.getName());
        assertTrue(group.getRoles().contains("Adminisztrátor"));
        assertTrue(group.getRoles().contains("Résztvevő"));
        assertTrue(group.getRoles().contains("Olvasó"));
    }

    @Test
    void testSetName() {
        group.setName("UjNev");
        assertEquals("UjNev", group.getName());
    }

    @Test
    void testAdminPermissions() {
        assertTrue(group.hasPermission(adminId, Permissions.GROUP_SEND_MESSAGE));
        assertTrue(group.hasPermission(adminId, Permissions.GROUP_DELETE_MESSAGES));
        assertTrue(group.hasPermission(adminId, Permissions.GROUP_ADD_MEMBER));
        assertTrue(group.isAdmin(adminId));
    }

    @Test
    void testParticipantPermissions() {
        assertTrue(group.hasPermission(participantId, Permissions.GROUP_SEND_MESSAGE));
        assertFalse(group.hasPermission(participantId, Permissions.GROUP_DELETE_MESSAGES));
        assertFalse(group.isAdmin(participantId));
    }

    @Test
    void testReaderPermissions() {
        assertFalse(group.hasPermission(readerId, Permissions.GROUP_SEND_MESSAGE));
        assertFalse(group.hasPermission(readerId, Permissions.GROUP_DELETE_MESSAGES));
        assertFalse(group.isAdmin(readerId));
    }

    @Test
    void testAddRole() {
        group.addRole("Mod");
        assertTrue(group.getRoles().contains("Mod"));
    }

    @Test
    void testSetRolePermissions() {
        group.addRole("Mod");
        Set<String> perms = new HashSet<>();
        perms.add(Permissions.GROUP_SEND_MESSAGE);
        perms.add(Permissions.GROUP_DELETE_MESSAGES);
        
        group.setRolePermissions("Mod", perms);
        Set<String> retrieved = group.getRolePermissions("Mod");
        
        assertTrue(retrieved.contains(Permissions.GROUP_SEND_MESSAGE));
        assertTrue(retrieved.contains(Permissions.GROUP_DELETE_MESSAGES));
    }

    @Test
    void testSetRolePermissionsInvalidRole() {
        Set<String> perms = new HashSet<>();
        assertThrows(IllegalArgumentException.class, () -> {
            group.setRolePermissions("NemLetezik", perms);
        });
    }

    @Test
    void testAddMember() {
        UUID newMember = UUID.randomUUID();
        group.addMember(newMember, "Résztvevő");
        assertTrue(group.getMemberRoles().containsKey(newMember));
    }

    @Test
    void testRemoveMember() {
        group.removeMember(participantId);
        assertFalse(group.getMemberRoles().containsKey(participantId));
    }

    @Test
    void testSetMemberRole() {
        group.setMemberRole(participantId, "Adminisztrátor");
        assertTrue(group.isAdmin(participantId));
    }

    @Test
    void testSetMemberRoleInvalidRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            group.setMemberRole(participantId, "NincsIlyenSzerepkor");
        });
    }

    @Test
    void testNonMemberHasNoPermission() {
        UUID nonMember = UUID.randomUUID();
        assertFalse(group.hasPermission(nonMember, Permissions.GROUP_SEND_MESSAGE));
        assertFalse(group.isAdmin(nonMember));
    }

    @Test
    void testEmptyConstructor() {
        Group emptyGroup = new Group();
        assertNotNull(emptyGroup.getId());
    }
}
