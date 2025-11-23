package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.*;

public class Group implements Serializable {

    //fájl mentéshez szükséges verzió azonosító
    private static final long serialVersionUID = 1L;
    //egyedi azonosító
    private UUID id;
    //csoport neve
    private String name;
    //UUID a felhasználó, String a szerepe neve
    private Map<UUID, String> memberRoles = new HashMap<>();
    //elérhető szerepek
    private Set<String> roles = new HashSet<>();
    //szerepekhez tartozó jogosultságok
    private Map<String, Set<String>> rolePermissions = new HashMap<>();
    
    //alapértelmezett szerepek
    private static final String ROLE_ADMIN = "Adminisztrátor";
    private static final String ROLE_PARTICIPANT = "Résztvevő";
    private static final String ROLE_READER = "Olvasó";

    public Group(String name) {
        this.id = UUID.randomUUID(); //egyedi ID
        this.name = name;
        
        //alapértelmezett szerepek hozzáadása
        roles.add(ROLE_ADMIN);        
        roles.add(ROLE_PARTICIPANT);  
        roles.add(ROLE_READER);
        rolePermissions.put(ROLE_ADMIN, new HashSet<>(Arrays.asList(Permissions.ALL)));
        rolePermissions.put(ROLE_PARTICIPANT, new HashSet<>(Arrays.asList(Permissions.GROUP_SEND_MESSAGE)));
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
        roles.add(role); // Szerep hozzáadása
        rolePermissions.putIfAbsent(role, new HashSet<>()); // Üres jogosultság halmaz létrehozása
    }

    public void setRolePermissions(String role, Set<String> perms) {
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("Ismeretlen szerep: " + role);
        }
        // Új halmazt hozunk létre (defensive copy), hogy a külső módosítások ne érintsék
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
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("Ismeretlen szerep: " + role);
        }
        memberRoles.put(userId, role);
    }

    public boolean isAdmin(UUID userId) {
        String role = memberRoles.get(userId); // Lekérdezzük a szerepét
        return ROLE_ADMIN.equals(role); // Összehasonlítjuk "Adminisztrátor"-ral
    }

   
    public boolean hasPermission(UUID userId, String permission) {
        // 1. Lekérdezzük a felhasználó szerepét
        String role = memberRoles.get(userId);
        
        // 2. Ha nincs szerepe (nem tagja a csoportnak), nincs joga
        if (role == null) return false;
        
        // 3. Lekérdezzük a szerep jogosultságait
        //    Ha a szerep nem létezik, üres halmazt adunk vissza
        Set<String> perms = rolePermissions.getOrDefault(role, java.util.Collections.emptySet());
        
        // 4. Ellenőrzés: van-e "ALL" joga VAGY van-e a konkrét joga
        //    Az || (OR) operátor azt jelenti: "vagy az egyik vagy a másik igaz"
        return perms.contains(Permissions.ALL) || perms.contains(permission);
    }
}
