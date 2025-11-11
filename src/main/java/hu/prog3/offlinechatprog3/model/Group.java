package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.*;

/**
 * Simple Group model which keeps track of members and roles.
 */
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    // map userId -> roleName
    private Map<UUID, String> memberRoles = new HashMap<>();
    // available roles and their permissions are simplified as strings for now
    private Set<String> roles = new HashSet<>();
    // role -> set of permission keys (e.g., GROUP_SEND_MESSAGE, GROUP_ADD_MEMBER)
    private Map<String, Set<String>> rolePermissions = new HashMap<>();
    private static final String ROLE_ADMIN = "Adminisztrátor";
    private static final String ROLE_PARTICIPANT = "Résztvevő";
    private static final String ROLE_READER = "Olvasó";

    public Group() {
        this.id = UUID.randomUUID();
    }

    public Group(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        // default roles
    roles.add(ROLE_ADMIN);
    roles.add(ROLE_PARTICIPANT);
    roles.add(ROLE_READER);
        // default permissions
    rolePermissions.put(ROLE_ADMIN, new HashSet<>(java.util.Arrays.asList(Permissions.ALL)));
    rolePermissions.put(ROLE_PARTICIPANT, new HashSet<>(java.util.Arrays.asList(Permissions.GROUP_SEND_MESSAGE)));
    rolePermissions.put(ROLE_READER, new HashSet<>());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<UUID, String> getMemberRoles() {
        return memberRoles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        roles.add(role);
        rolePermissions.putIfAbsent(role, new HashSet<>());
    }

    public void setRolePermissions(String role, Set<String> perms) {
        if (!roles.contains(role)) throw new IllegalArgumentException("Unknown role: " + role);
        rolePermissions.put(role, new HashSet<>(perms));
    }

    public Set<String> getRolePermissions(String role) {
        return new HashSet<>(rolePermissions.getOrDefault(role, java.util.Collections.emptySet()));
    }

    public void addMember(UUID userId, String role) {
        memberRoles.put(userId, role);
    }

    public void removeMember(UUID userId) {
        memberRoles.remove(userId);
    }

    public void setMemberRole(UUID userId, String role) {
        if (!roles.contains(role)) throw new IllegalArgumentException("Unknown role: " + role);
        memberRoles.put(userId, role);
    }

    /**
     * Return true if the given userId has the 'Adminisztrátor' role in this group.
     */
    public boolean isAdmin(UUID userId) {
        String role = memberRoles.get(userId);
        return ROLE_ADMIN.equals(role);
    }

    /**
     * Check whether the given user (by id) has the specified permission in this group.
    * Permission keys are strings; role can have 'ALL' to mean all permissions.
     */
    public boolean hasPermission(UUID userId, String permission) {
        String role = memberRoles.get(userId);
        if (role == null) return false;
        Set<String> perms = rolePermissions.getOrDefault(role, java.util.Collections.emptySet());
        return perms.contains(Permissions.ALL) || perms.contains(permission);
    }
}
