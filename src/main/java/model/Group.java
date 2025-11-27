package model;

import java.io.Serializable;
import java.util.*;

/**
 * Csoportos beszélgetést reprezentáló modell osztály.
 * Támogatja a szerepkör alapú jogosultságkezelést.
 * Alapértelmezett szerepek: Adminisztrátor, Résztvevő, Olvasó. 
 */
public class Group implements Serializable {

    /** Verziószám a szerializációhoz */
    private static final long serialVersionUID = 1L;
    
    /** Egyedi azonosító */
    private UUID id;
    
    /** Csoport neve */
    private String name;
    
    /** Csoport tagok és szerepeik */
    private Map<UUID, String> memberRoles = new HashMap<>();
    
    /* Elérhető szerepek a csoportban */
    private Set<String> roles = new HashSet<>();
    
    /** Szerepekhez tartozó jogosultságok */
    private Map<String, Set<String>> rolePermissions = new HashMap<>();
    
    /** Alapértelmezett szerep: Adminisztrátor - teljes jogosultság */
    private static final String ROLE_ADMIN = "Adminisztrátor";
    
    /** Alapértelmezett szerep: Résztvevő - csak üzenetküldési jog */
    private static final String ROLE_PARTICIPANT = "Résztvevő";
    
    /** Alapértelmezett szerep: Olvasó - csak olvasási jog */
    private static final String ROLE_READER = "Olvasó";

    /**
     * Létrehoz egy új csoportot a megadott névvel.
     * Automatikusan létrehozza az alapértelmezett szerepeket
     * és beállítja a hozzájuk tartozó jogosultságokat.
     * 
     * @param name a csoport neve
     */
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

    /** 
     * Visszaadja a csoport egyedi azonosítóját.
     *  @return a csoport UUID azonosítója
     */
    public UUID getId() {
        return id;
    }

    /**
     * Visszaadja a csoport nevét.
     * @return a csoport neve
     */
    public String getName() {
        return name;
    }

    /**
     * Beállítja a csoport új nevét.
     * @param name az új név
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Visszaadja a csoport tagjainak szerepeit tartalmazó map-et.
     * @return map, ahol a kulcs a felhasználó UUID-ja, az érték pedig a szerepnév
     */
    public Map<UUID, String> getMemberRoles() {
        return memberRoles;
    }

    /**
     * Visszaadja a csoportban elérhető összes szerepet.
     * @return a szerepnevek halmaza
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Hozzáad egy új egyedi szerepet a csoporthoz.
     * Ha a szerep már létezik, nincs hatása.
     * Alapértelmezés szerint üres jogosultságokkal rendelkezik.
     * 
     * @param role az új szerepnév
     */
    public void addRole(String role) {
        roles.add(role);
        rolePermissions.putIfAbsent(role, new HashSet<>()); 
    }

    /**
     * Beállítja egy adott szerep jogosultságait.
     * 
     * @param role a szerepnév
     * @param perms a jogosultságok halmaza 
     * @throws IllegalArgumentException ha a szerep nem létezik a csoportban
     */
    public void setRolePermissions(String role, Set<String> perms) {
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("Ismeretlen szerep: " + role);
        }
        rolePermissions.put(role, new HashSet<>(perms));
    }

    /**
     * Visszaadja egy adott szerep jogosultságait.
     * 
     * @param role a szerepnév
     * @return a jogosultságok halmaza 
     */
    public Set<String> getRolePermissions(String role) {
        return new HashSet<>(rolePermissions.getOrDefault(role, Collections.emptySet()));
    }

    /**
     * Hozzáad egy új tagot a csoporthoz a megadott szereppel.
     * 
     * @param userId a felhasználó UUID azonosítója
     * @param role a tag szerepe 
     */
    public void addMember(UUID userId, String role) {
        memberRoles.put(userId, role);
    }

    /**
     * Eltávolít egy tagot a csoportból.
     * 
     * @param userId az eltávolítandó felhasználó UUID azonosítója
     */
    public void removeMember(UUID userId) {
        memberRoles.remove(userId);
    }

    /**
     * Módosítja egy tag szerepét a csoportban.
     * 
     * @param userId a felhasználó UUID azonosítója
     * @param role az új szerepnév
     * @throws IllegalArgumentException ha a szerep nem létezik a csoportban
     */
    public void setMemberRole(UUID userId, String role) {
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("Ismeretlen szerep: " + role);
        }
        memberRoles.put(userId, role);
    }

    /**
     * Ellenőrzi, hogy egy adott felhasználó adminisztrátor jogosultsággal rendelkezik-e.
     * 
     * @param userId a felhasználó UUID azonosítója
     * @return true ha a felhasználó "Adminisztrátor" szerepkörrel rendelkezik, egyébként false
     */
    public boolean isAdmin(UUID userId) {
        String role = memberRoles.get(userId);
        return ROLE_ADMIN.equals(role);
    }

    /**
     * Ellenőrzi, hogy egy felhasználó rendelkezik-e egy adott jogosultsággal a csoportban.
     * 
     * @param userId a felhasználó UUID azonosítója
     * @param permission a vizsgálandó jogosultság (pl. Permissions.GROUP_SEND_MESSAGE)
     * @return true ha a felhasználó rendelkezik a jogosultsággal, egyébként false
     */
    public boolean hasPermission(UUID userId, String permission) {
        String role = memberRoles.get(userId);
        if (role == null) return false;
        
        Set<String> perms = rolePermissions.getOrDefault(role, Collections.emptySet());
        
        return perms.contains(Permissions.ALL) || perms.contains(permission);
    }
}
