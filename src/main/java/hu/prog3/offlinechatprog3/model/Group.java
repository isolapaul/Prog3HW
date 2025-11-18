package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.*;

/**
 * CSOPORT MODELL OSZTÁLY
 * 
 * Ez az osztály egy chat csoportot reprezentál, ahol több felhasználó beszélgethet együtt.
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * - Tárolni kell, hogy KINEK milyen SZEREPE van a csoportban (admin, résztvevő, olvasó)
 * - Tárolni kell, hogy az egyes SZEREPEKNEK milyen JOGOSULTSÁGAI vannak
 * - Ez teszi lehetővé, hogy ne mindenki törölhessen üzeneteket vagy tagokat
 * 
 * HOGYAN MŰKÖDIK A SZEREPKÖR RENDSZER?
 * 1. Minden csoportnak vannak SZEREPEI (pl. "Adminisztrátor", "Résztvevő", "Olvasó")
 * 2. Minden SZEREPHEZ tartoznak JOGOSULTSÁGOK (pl. üzenet küldés, tag hozzáadás)
 * 3. Minden felhasználóhoz hozzá van rendelve EGY SZEREP a csoportban
 * 
 * PÉLDA:
 * - "Adminisztrátor" szerepnek MINDEN joga van (ALL)
 * - "Résztvevő" szerepnek csak üzenet küldési joga van
 * - "Olvasó" szerepnek semmilyen joga nincs, csak olvashat
 */
public class Group implements Serializable {

    // Fájl mentéshez szükséges verzió azonosító, mindenhol ugyanez
    private static final long serialVersionUID = 1L;
    // Egyedi azonosító
    private UUID id;
    // Csoport neve
    private String name;
    //UUID-felhasználó, String a szerepe neve
    private Map<UUID, String> memberRoles = new HashMap<>();
    // Elérhető szerepek,minden csak egyszer szerepelhet
    private Set<String> roles = new HashSet<>();
    // Szerepekhez tartozó jogosultságok
    private Map<String, Set<String>> rolePermissions = new HashMap<>();
    
    // Alapértelmezett szerepek
    private static final String ROLE_ADMIN = "Adminisztrátor";
    private static final String ROLE_PARTICIPANT = "Résztvevő";
    private static final String ROLE_READER = "Olvasó";
    // Üres konstruktor(teszthez)
    public Group() {
        this.id = UUID.randomUUID();
    }

    public Group(String name) {
        this.id = UUID.randomUUID(); // Egyedi ID
        this.name = name;
        
        // Alapértelmezett szerepek hozzáadása
        roles.add(ROLE_ADMIN);        // Adminisztrátor
        roles.add(ROLE_PARTICIPANT);  // Résztvevő
        roles.add(ROLE_READER);       // Olvasó
        
        // ALAPÉRTELMEZETT JOGOSULTSÁGOK BEÁLLÍTÁSA
        // Admin: minden jogosultság ("ALL" = mindent csinálhat)
        rolePermissions.put(ROLE_ADMIN, new HashSet<>(java.util.Arrays.asList(Permissions.ALL)));
        
        // Résztvevő: csak üzenet küldési jog
        rolePermissions.put(ROLE_PARTICIPANT, new HashSet<>(java.util.Arrays.asList(Permissions.GROUP_SEND_MESSAGE)));
        
        // Olvasó: üres halmaz = nincs semmilyen jogosultsága (csak olvashat)
        rolePermissions.put(ROLE_READER, new HashSet<>());
    }

    // ALAP GETTER ÉS SETTER METÓDUSOK
    
    /**
     * CSOPORT ID LEKÉRDEZÉSE
     * @return A csoport egyedi azonosítója
     */
    public UUID getId() {
        return id;
    }

    /**
     * CSOPORT NÉV LEKÉRDEZÉSE
     * @return A csoport neve (pl. "Prog3 csoport")
     */
    public String getName() {
        return name;
    }

    /**
     * CSOPORT NÉV BEÁLLÍTÁSA
     * @param name Az új csoport név
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * ÖSSZES TAG ÉS SZEREPÜK LEKÉRDEZÉSE
     * @return Map: felhasználó ID -> szerep név
     */
    public Map<UUID, String> getMemberRoles() {
        return memberRoles;
    }

    /**
     * ÖSSZES ELÉRHETŐ SZEREP LEKÉRDEZÉSE
     * @return A csoportban létező szerepek neve (pl. {"Adminisztrátor", "Résztvevő"})
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * ÚJ SZEREP HOZZÁADÁSA A CSOPORTHOZ
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Hozzáadjuk a szerepet a roles halmazhoz
     * 2. Létrehozunk neki egy üres jogosultság halmazt (putIfAbsent = ha még nincs)
     * 
     * PÉLDA HASZNÁLAT:
     * group.addRole("Moderátor");
     * // Most már létezik a "Moderátor" szerep, de még nincsenek jogosultságai
     * 
     * @param role Az új szerep neve
     */
    public void addRole(String role) {
        roles.add(role); // Szerep hozzáadása
        rolePermissions.putIfAbsent(role, new HashSet<>()); // Üres jogosultság halmaz létrehozása
    }

    /**
     * SZEREP JOGOSULTSÁGAINAK BEÁLLÍTÁSA
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Ellenőrizzük, hogy létezik-e a szerep
     * 2. Ha létezik, beállítjuk a jogosultságait
     * 3. Ha nem létezik, hibát dobunk
     * 
     * PÉLDA HASZNÁLAT:
     * Set<String> perms = new HashSet<>();
     * perms.add(Permissions.GROUP_SEND_MESSAGE);
     * perms.add(Permissions.GROUP_DELETE_MESSAGES);
     * group.setRolePermissions("Moderátor", perms);
     * 
     * @param role A szerep neve
     * @param perms A jogosultságok halmaza
     * @throws IllegalArgumentException Ha a szerep nem létezik
     */
    public void setRolePermissions(String role, Set<String> perms) {
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("Ismeretlen szerep: " + role);
        }
        // Új halmazt hozunk létre (defensive copy), hogy a külső módosítások ne érintsék
        rolePermissions.put(role, new HashSet<>(perms));
    }

    /**
     * SZEREP JOGOSULTSÁGAINAK LEKÉRDEZÉSE
     * 
     * MIÉRT DEFENSIVE COPY?
     * Azért hozunk létre új HashSet-et, hogy ha valaki módosítja a visszaadott halmazt,
     * az NE érintse az eredeti adatokat.
     * 
     * @param role A szerep neve
     * @return A szerephez tartozó jogosultságok (másolat)
     */
    public Set<String> getRolePermissions(String role) {
        return new HashSet<>(rolePermissions.getOrDefault(role, java.util.Collections.emptySet()));
    }

    /**
     * TAG HOZZÁADÁSA A CSOPORTHOZ
     * 
     * HOGYAN MŰKÖDIK?
     * Egyszerűen hozzáadjuk a felhasználó ID-jét és szerepét a memberRoles Map-hez.
     * 
     * PÉLDA:
     * group.addMember(userId, "Résztvevő");
     * // Most már a userId felhasználó "Résztvevő" szerepben van a csoportban
     * 
     * @param userId A felhasználó ID-ja
     * @param role A szerepe (pl. "Résztvevő")
     */
    public void addMember(UUID userId, String role) {
        memberRoles.put(userId, role);
    }

    /**
     * TAG ELTÁVOLÍTÁSA A CSOPORTBÓL
     * 
     * HOGYAN MŰKÖDIK?
     * Eltávolítjuk a felhasználót a memberRoles Map-ből az ID-ja alapján.
     * 
     * @param userId A felhasználó ID-ja
     */
    public void removeMember(UUID userId) {
        memberRoles.remove(userId);
    }

    /**
     * TAG SZEREPÉNEK MÓDOSÍTÁSA
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Ellenőrizzük, hogy a szerep létezik-e
     * 2. Ha igen, módosítjuk a tag szerepét
     * 3. Ha nem, hibát dobunk
     * 
     * PÉLDA:
     * group.setMemberRole(userId, "Adminisztrátor");
     * // A userId felhasználó mostantól Admin lesz
     * 
     * @param userId A felhasználó ID-ja
     * @param role Az új szerep
     * @throws IllegalArgumentException Ha a szerep nem létezik
     */
    public void setMemberRole(UUID userId, String role) {
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("Ismeretlen szerep: " + role);
        }
        memberRoles.put(userId, role);
    }

    /**
     * ADMIN JOGOSULTSÁG ELLENŐRZÉSE
     * 
     * MIÉRT FONTOS?
     * Sok helyen kell ellenőrizni, hogy valaki admin-e (pl. mielőtt engedélyezzük neki,
     * hogy új szerepet hozzon létre vagy tagot távolítson el).
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Lekérdezzük a felhasználó szerepét az ID-ja alapján
     * 2. Ellenőrizzük, hogy ez a szerep "Adminisztrátor"-e
     * 3. Ha igen, true-t adunk vissza, ha nem, false-t
     * 
     * @param userId A felhasználó ID-ja
     * @return true, ha a felhasználó Admin, false ha nem
     */
    public boolean isAdmin(UUID userId) {
        String role = memberRoles.get(userId); // Lekérdezzük a szerepét
        return ROLE_ADMIN.equals(role); // Összehasonlítjuk "Adminisztrátor"-ral
    }

    /**
     * JOGOSULTSÁG ELLENŐRZÉSE
     * 
     * Ez a LEGFONTOSABB METÓDUS a jogosultság rendszerben!
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Lekérdezzük a felhasználó szerepét (pl. "Résztvevő")
     * 2. Ha nincs szerepe (null), nincs jogosultsága -> false
     * 3. Lekérdezzük a szerep jogosultságait (pl. {"GROUP_SEND_MESSAGE"})
     * 4. Ellenőrizzük, hogy:
     *    a) Van-e "ALL" jogosultsága (akkor minden joga van) VAGY
     *    b) Van-e a konkrét kért jogosultsága (pl. "GROUP_SEND_MESSAGE")
     * 
     * PÉLDA 1 - Admin:
     * - userId szerepe: "Adminisztrátor"
     * - "Adminisztrátor" jogai: {"ALL"}
     * - Kérdés: hasPermission(userId, "GROUP_SEND_MESSAGE")
     * - Válasz: true (mert van "ALL" joga)
     * 
     * PÉLDA 2 - Résztvevő:
     * - userId szerepe: "Résztvevő"
     * - "Résztvevő" jogai: {"GROUP_SEND_MESSAGE"}
     * - Kérdés: hasPermission(userId, "GROUP_SEND_MESSAGE")
     * - Válasz: true (mert konkrétan van ilyen joga)
     * - Kérdés: hasPermission(userId, "GROUP_DELETE_MESSAGES")
     * - Válasz: false (nincs ilyen joga)
     * 
     * PÉLDA 3 - Olvasó:
     * - userId szerepe: "Olvasó"
     * - "Olvasó" jogai: {} (üres)
     * - Kérdés: hasPermission(userId, "GROUP_SEND_MESSAGE")
     * - Válasz: false (nincs semmilyen joga)
     * 
     * @param userId A felhasználó ID-ja
     * @param permission A kért jogosultság (pl. "GROUP_SEND_MESSAGE")
     * @return true ha van joga, false ha nincs
     */
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
