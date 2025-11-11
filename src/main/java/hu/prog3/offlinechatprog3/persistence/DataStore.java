package hu.prog3.offlinechatprog3.persistence;

import hu.prog3.offlinechatprog3.model.Group;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.User;

import java.io.Serializable;
import java.util.*;

/**
 * KÖZPONTI ADATTÁROLÓ OSZTÁLY (DataStore)
 * 
 * Ez az osztály az EGÉSZ ALKALMAZÁS ADATAIT tárolja egyetlen objektumban!
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * - Minden adat (felhasználók, üzenetek, csoportok) EGY HELYEN van
 * - Az EGÉSZ objektumot tudjuk menteni fájlba (Java szerializáció)
 * - Egyszerű és átlátható adatkezelés
 * 
 * HOGYAN MŰKÖDIK?
 * Képzeld el egy NAGY KONTÉNERNEK, amiben minden adat benne van:
 * - Felhasználók (név és ID szerint is kereshető)
 * - Barátságok (ki kivel barát)
 * - Barátkérések (bejövő és kimenő)
 * - Csoportok (összes csoport ID szerint)
 * - Üzenetek (privát és csoport üzenetek külön)
 * 
 * MIÉRT SERIALIZABLE?
 * Hogy az egész objektumot le tudjuk menteni fájlba, és később
 * vissza tudjuk tölteni PONTOSAN UGYANAZZAL AZ ÁLLAPOTTAL.
 */
public class DataStore implements Serializable {

    // Fájl mentéshez szükséges verzió azonosító
    private static final long serialVersionUID = 1L;

    // ============================================================
    // FELHASZNÁLÓ TÁROLÁS - KÉT FÉLE KERESÉSHEZ
    // ============================================================
    
    /**
     * FELHASZNÁLÓK FELHASZNÁLÓNÉV SZERINT
     * Map típus: Kulcs = username (String), Érték = User objektum
     * 
     * MIÉRT KELL?
     * Amikor bejelentkezünk, felhasználónévvel keresünk.
     * A HashMap gyors keresést biztosít: O(1) idő!
     * 
     * PÉLDA:
     * {
     *   "anna" -> User{id=uuid1, username="anna", password="..."},
     *   "bela" -> User{id=uuid2, username="bela", password="..."}
     * }
     */
    private final Map<String, User> usersByName = new HashMap<>();
    
    /**
     * FELHASZNÁLÓK ID SZERINT
     * Map típus: Kulcs = userId (UUID), Érték = User objektum
     * 
     * MIÉRT KELL?
     * Az üzeneteknél csak az ID-t tároljuk (nem a teljes User objektumot).
     * Így gyorsan megtaláljuk, hogy ki küldte az üzenetet.
     * 
     * PÉLDA:
     * {
     *   uuid1 -> User{id=uuid1, username="anna", ...},
     *   uuid2 -> User{id=uuid2, username="bela", ...}
     * }
     */
    private final Map<UUID, User> usersById = new HashMap<>();
    
    // ============================================================
    // BARÁTSÁG KEZELÉS
    // ============================================================
    
    /**
     * BARÁTSÁGOK TÁROLÁSA
     * Map<String, Set<String>> = Minden felhasználónak van egy halmaza barátokról
     * 
     * MIÉRT SET?
     * - Egy ember csak egyszer lehet barát (nincs duplikáció)
     * - Gyors ellenőrzés: barát-e valaki
     * 
     * PÉLDA:
     * {
     *   "anna" -> {"bela", "cecil"},  // Anna barátai: Béla és Cecil
     *   "bela" -> {"anna", "dora"},   // Béla barátai: Anna és Dóra
     *   "cecil" -> {"anna"}           // Cecil barátja: Anna
     * }
     * 
     * FONTOS! A barátság KÉTIRÁNYÚ:
     * Ha Anna barátja Bélának, akkor Béla is barátja Annának.
     * Mindkét irányban benne kell lennie!
     */
    private final Map<String, Set<String>> friends = new HashMap<>();
    
    /**
     * BEJÖVŐ BARÁTKÉRÉSEK
     * Ki kér barátságot TŐLEM?
     * 
     * PÉLDA:
     * {
     *   "bela" -> {"anna", "cecil"},  // Béla két kérést kapott: Annától és Ceciltől
     *   "dora" -> {"bela"}            // Dóra egy kérést kapott: Bélától
     * }
     */
    private final Map<String, Set<String>> incomingFriendRequests = new HashMap<>();
    
    /**
     * KIMENŐ BARÁTKÉRÉSEK
     * KINEK küldtem barátkérést?
     * 
     * PÉLDA:
     * {
     *   "anna" -> {"bela"},    // Anna küldött kérést Bélának
     *   "cecil" -> {"bela"}    // Cecil küldött kérést Bélának
     * }
     * 
     * FONTOS! Ugyanaz a kapcsolat két helyen is szerepel:
     * - incomingFriendRequests["bela"] tartalmazza "anna"-t
     * - outgoingFriendRequests["anna"] tartalmazza "bela"-t
     * Ez azért kell, hogy mindkét irányból gyorsan lehessen keresni!
     */
    private final Map<String, Set<String>> outgoingFriendRequests = new HashMap<>();

    // ============================================================
    // CSOPORT TÁROLÁS
    // ============================================================
    
    /**
     * ÖSSZES CSOPORT
     * Map<UUID, Group> = Csoport ID -> Group objektum
     * 
     * PÉLDA:
     * {
     *   uuid5 -> Group{id=uuid5, name="Prog3 csoport", members=...},
     *   uuid6 -> Group{id=uuid6, name="Barátok", members=...}
     * }
     */
    private final Map<UUID, Group> groups = new HashMap<>();

    // ============================================================
    // ÜZENET TÁROLÁS
    // ============================================================
    
    /**
     * PRIVÁT ÜZENETEK (két felhasználó között)
     * Map<String, List<Message>> = beszélgetés kulcs -> üzenetek listája
     * 
     * MIÉRT ILYEN BONYOLULT A KULCS?
     * Két felhasználó közti beszélgetéshez kell egy egyedi kulcs.
     * Probléma: Anna->Béla és Béla->Anna UGYANAZ a beszélgetés!
     * 
     * MEGOLDÁS: Rendezett kulcs
     * - Anna és Béla közti beszélgetés: "anna#bela"
     * - Béla és Anna közti beszélgetés: szintén "anna#bela" (ABC sorrendben!)
     * Lásd: privateKey() metódus
     * 
     * PÉLDA:
     * {
     *   "anna#bela" -> [
     *     Message{senderId=annaId, content="Szia Béla!"},
     *     Message{senderId=belaId, content="Helló Anna!"}
     *   ],
     *   "bela#cecil" -> [
     *     Message{senderId=belaId, content="Hogy vagy?"}
     *   ]
     * }
     */
    private final Map<String, List<Message>> privateMessages = new HashMap<>();
    
    /**
     * CSOPORT ÜZENETEK
     * Map<UUID, List<Message>> = csoport ID -> üzenetek listája
     * 
     * EGYSZERŰBB mint a privát!
     * Minden csoportnak van egy ID-ja, azon keresztül keresünk.
     * 
     * PÉLDA:
     * {
     *   groupId1 -> [
     *     Message{senderId=annaId, content="Sziasztok!"},
     *     Message{senderId=belaId, content="Helló!"}
     *   ]
     * }
     */
    private final Map<UUID, List<Message>> groupMessages = new HashMap<>();

    /**
     * KONSTRUKTOR
     * Üres konstruktor - a mezők már inicializálva vannak (new HashMap<>())
     * amikor az objektumot létrehozzuk.
     */
    public DataStore() {
        // Az összes Map és Set már létrejött a field inicializálásakor
    }

    // ============================================================
    // FELHASZNÁLÓ MŰVELETEK
    // ============================================================
    
    /**
     * ÚJ FELHASZNÁLÓ REGISZTRÁLÁSA
     * 
     * LÉPÉSEK:
     * 1. Ellenőrzés: van-e username, nem-e üres
     * 2. Ellenőrzés: nem foglalt-e már a username
     * 3. Létrehozunk egy új User objektumot
     * 4. Hozzáadjuk MINDKÉT Map-hez (név szerint ÉS ID szerint)
     * 5. Létrehozunk neki ÜRES halmazokat (barátok, kérések)
     * 6. Visszaadjuk, hogy sikeres volt-e
     * 
     * MIÉRT KELL ÜRES HALMAZOKAT KÉSZÍTENI?
     * Később egyszerűbb, ha már létezik a halmaz, csak hozzáadunk/elveszünk belőle.
     * Nem kell mindig ellenőrizni, hogy létezik-e már.
     * 
     * @param username A felhasználónév (pl. "anna")
     * @param passwordHash A jelszó (egyszerűsített verzió)
     * @return true = sikeres regisztráció, false = hiba (pl. foglalt név)
     */
    public boolean registerUser(String username, String passwordHash) {
        // 1. ELLENŐRZÉS: van-e username, nem-e üres
        if (username == null || username.isBlank()) {
            return false; // Üres username nem megengedett
        }
        
        // 2. ELLENŐRZÉS: nincs-e már ilyen felhasználó
        if (usersByName.containsKey(username)) {
            return false; // Már foglalt a név
        }
        
        // 3. ÚJ USER LÉTREHOZÁSA
        User u = new User(username, passwordHash);
        
        // 4. HOZZÁADÁS MINDKÉT MAP-HEZ
        usersByName.put(username, u);  // Név szerint kereséshez
        usersById.put(u.getId(), u);   // ID szerint kereséshez
        
        // 5. ÜRES HALMAZOK INICIALIZÁLÁSA
        // Még nincs barátja, még nincsenek kérései
        friends.put(username, new HashSet<>());
        incomingFriendRequests.put(username, new HashSet<>());
        outgoingFriendRequests.put(username, new HashSet<>());
        
        return true; // Sikeres regisztráció
    }

    /**
     * BEJELENTKEZÉS ELLENŐRZÉSE (AUTENTIKÁCIÓ)
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Megkeressük a felhasználót név szerint
     * 2. Ha nem létezik, false (rossz username)
     * 3. Összehasonlítjuk a jelszavakat
     * 4. Ha egyeznek, true (sikeres bejelentkezés)
     * 
     * MIÉRT Objects.equals()?
     * Ez biztonságosan összehasonlít két objektumot, még akkor is,
     * ha valamelyik null. A == operátor nem biztonságos String-eknél!
     * 
     * @param username A felhasználónév
     * @param passwordHash A jelszó
     * @return true = helyes jelszó, false = rossz username vagy jelszó
     */
    public boolean authenticateUser(String username, String passwordHash) {
        // 1. Keresés a felhasználók között
        User u = usersByName.get(username);
        
        // 2. Ha nem létezik, nincs mit ellenőrizni
        if (u == null) {
            return false; // Nincs ilyen felhasználó
        }
        
        // 3. Jelszó összehasonlítás
        // Objects.equals() biztonságosan kezeli a null-t is
        return Objects.equals(u.getPasswordHash(), passwordHash);
    }

    /**
     * FELHASZNÁLÓ KERESÉSE NÉV SZERINT
     * 
     * MIÉRT Optional?
     * Az Optional azt jelenti: "lehet, hogy van eredmény, lehet, hogy nincs".
     * Ez biztonságosabb, mint null-t visszaadni, mert:
     * - Explicit jelzi, hogy lehet null
     * - Lambda-kkal könnyebb dolgozni
     * - Kényszeri a null ellenőrzést
     * 
     * HASZNÁLAT:
     * Optional<User> userOpt = findUserByName("anna");
     * if (userOpt.isPresent()) {
     *     User user = userOpt.get();
     *     // ... dolgozunk a user-rel ...
     * }
     * 
     * VAGY:
     * findUserByName("anna").ifPresent(user -> {
     *     // ... dolgozunk a user-rel ...
     * });
     * 
     * @param username A keresett felhasználónév
     * @return Optional<User> - vagy tartalmaz egy User-t, vagy üres
     */
    public Optional<User> findUserByName(String username) {
        // Optional.ofNullable() azt jelenti:
        // - Ha a get() eredménye nem null, Optional-ba csomagolja
        // - Ha null, üres Optional-t ad vissza (Optional.empty())
        return Optional.ofNullable(usersByName.get(username));
    }

    // Friend management
    public boolean addFriend(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        friends.get(a).add(b);
        friends.get(b).add(a);
        return true;
    }

    /**
     * Send a friend request from 'from' to 'to'. Returns false if users don't exist,
     * if already friends, or if a request already exists.
     */
    public boolean sendFriendRequest(String from, String to) {
        if (!usersByName.containsKey(from) || !usersByName.containsKey(to)) return false;
        if (areFriends(from, to)) return false;
        Set<String> incoming = incomingFriendRequests.computeIfAbsent(to, k -> new HashSet<>());
        Set<String> outgoing = outgoingFriendRequests.computeIfAbsent(from, k -> new HashSet<>());
        // if already requested (either direction) don't add
        if (incoming.contains(from) || outgoing.contains(to)) return false;
        incoming.add(from);
        outgoing.add(to);
        return true;
    }

    /**
     * Get a defensive copy of pending incoming friend requests for a user.
     */
    public Set<String> getIncomingFriendRequests(String username) {
        return new HashSet<>(incomingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    /**
     * Return outgoing friend requests sent by the given user.
     */
    public Set<String> getOutgoingFriendRequests(String username) {
        return new HashSet<>(outgoingFriendRequests.getOrDefault(username, Collections.emptySet()));
    }

    /**
     * Accept a friend request where 'username' accepts a request from 'from'.
     */
    public boolean acceptFriendRequest(String username, String from) {
        if (!usersByName.containsKey(username) || !usersByName.containsKey(from)) return false;
        Set<String> incoming = incomingFriendRequests.get(username);
        if (incoming == null || !incoming.remove(from)) return false;
        // add friendship
        addFriend(username, from);
        // remove outgoing entry for the requester
        Set<String> outgoing = outgoingFriendRequests.get(from);
        if (outgoing != null) outgoing.remove(username);
        return true;
    }

    /**
     * Reject (remove) a friend request without adding friendship.
     */
    public boolean rejectFriendRequest(String username, String from) {
        if (!usersByName.containsKey(username) || !usersByName.containsKey(from)) return false;
        Set<String> incoming = incomingFriendRequests.get(username);
        if (incoming == null) return false;
        boolean removed = incoming.remove(from);
        // also remove outgoing entry
        Set<String> outgoing = outgoingFriendRequests.get(from);
        if (outgoing != null) outgoing.remove(username);
        return removed;
    }

    /**
     * Cancel an outgoing friend request: 'from' cancels a previously sent request to 'to'.
     */
    public boolean cancelOutgoingFriendRequest(String from, String to) {
        if (!usersByName.containsKey(from) || !usersByName.containsKey(to)) return false;
        Set<String> outgoing = outgoingFriendRequests.get(from);
        Set<String> incoming = incomingFriendRequests.get(to);
        boolean removedOut = false;
        boolean removedIn = false;
        if (outgoing != null) removedOut = outgoing.remove(to);
        if (incoming != null) removedIn = incoming.remove(from);
        return removedOut || removedIn;
    }

    public boolean removeFriend(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        boolean ra = friends.get(a).remove(b);
        boolean rb = friends.get(b).remove(a);
        return ra || rb;
    }

    public boolean areFriends(String a, String b) {
        if (!usersByName.containsKey(a) || !usersByName.containsKey(b)) return false;
        return friends.get(a).contains(b);
    }

    // Group management
    public UUID createGroup(String name, String creatorUsername) {
        Group g = new Group(name);
        groups.put(g.getId(), g);
        
        // Létrehozó hozzáadása Adminisztrátor szerepkörrel
        User creator = usersByName.get(creatorUsername);
        if (creator != null) {
            g.addMember(creator.getId(), "Adminisztrátor");
        }
        
        return g.getId();
    }

    public boolean addGroupMember(UUID groupId, String username, String role) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        g.addMember(u.getId(), role);
        return true;
    }

    /** Return mapping of username -> role for a given group. */
    public Map<String, String> getGroupMembersWithRoles(UUID groupId) {
        Group g = groups.get(groupId);
        if (g == null) return java.util.Collections.emptyMap();
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<UUID, String> e : g.getMemberRoles().entrySet()) {
            User u = usersById.get(e.getKey());
            if (u != null) res.put(u.getUsername(), e.getValue());
        }
        return res;
    }

    /** Get the role name of a user in the group, or null if not a member. */
    public String getGroupRole(UUID groupId, String username) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return null;
        return g.getMemberRoles().get(u.getId());
    }

    /** Change a member's role in the group. */
    public boolean setGroupMemberRole(UUID groupId, String username, String role) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        try {
            g.setMemberRole(u.getId(), role);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Available role names in the group. */
    public java.util.Set<String> getAvailableRoles(UUID groupId) {
        Group g = groups.get(groupId);
        if (g == null) return java.util.Collections.emptySet();
        return new java.util.HashSet<>(g.getRoles());
    }

    public boolean setRolePermissions(UUID groupId, String role, java.util.Set<String> perms) {
        Group g = groups.get(groupId);
        if (g == null) return false;
        try {
            g.setRolePermissions(role, perms);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public java.util.Set<String> getRolePermissions(UUID groupId, String role) {
        Group g = groups.get(groupId);
        if (g == null) return java.util.Collections.emptySet();
        return g.getRolePermissions(role);
    }

    /**
     * Return true if the given username is an administrator in the group.
     */
    public boolean isGroupAdmin(UUID groupId, String username) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        return g.isAdmin(u.getId());
    }

    public boolean removeGroupMember(UUID groupId, String username) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        g.removeMember(u.getId());
        return true;
    }

    public boolean addCustomRole(UUID groupId, String role) {
        Group g = groups.get(groupId);
        if (g == null) return false;
        g.addRole(role);
        return true;
    }

    // Messaging (private: key is sorted username pair joined by '#')
    private String privateKey(String a, String b) {
        List<String> l = Arrays.asList(a, b);
        Collections.sort(l);
        return String.join("#", l);
    }

    public void sendPrivateMessage(String fromUsername, String toUsername, String content) {
        // Beszélgetés kulcs létrehozása (pl. "anna#bela")
        String key = privateKey(fromUsername, toUsername);
        
        // Feladó User objektumának lekérése
        User sender = usersByName.get(fromUsername);
        
        // Új üzenet létrehozása (conversationId = null, mert privát chat)
        Message m = new Message(sender.getId(), null, content);
        
        // Megnézzük, van-e már üzenet lista ehhez a beszélgetéshez
        List<Message> messages = privateMessages.get(key);
        
        // Ha nincs, létrehozunk egy üres listát
        if (messages == null) {
            messages = new ArrayList<>();
            privateMessages.put(key, messages);
        }
        
        // Hozzáadjuk az új üzenetet
        messages.add(m);
    }

    public List<Message> getPrivateMessages(String a, String b) {
        // Lekérjük a beszélgetés kulcsát (pl. "anna#bela")
        String key = privateKey(a, b);
        
        // Megnézzük, van-e már üzenet lista ehhez a beszélgetéshez
        List<Message> messages = privateMessages.get(key);
        
        // Ha nincs (null), akkor visszaadunk egy üres listát
        if (messages == null) {
            return Collections.emptyList();
        }
        
        // Ha van, visszaadjuk az üzeneteket
        return messages;
    }

    public void sendGroupMessage(UUID groupId, String fromUsername, String content) {
        // Lekérjük a feladó User objektumát név alapján
        User sender = usersByName.get(fromUsername);
        
        // Létrehozunk egy új Message objektumot
        Message m = new Message(sender.getId(), groupId, content);
        
        // Megnézzük, van-e már üzenet lista ehhez a csoporthoz
        List<Message> messages = groupMessages.get(groupId);
        
        // Ha nincs, létrehozunk egy üres listát
        if (messages == null) {
            messages = new ArrayList<>();
            groupMessages.put(groupId, messages);
        }
        
        // Hozzáadjuk az új üzenetet a listához
        messages.add(m);
    }

    public List<Message> getGroupMessages(UUID groupId) {
        // Lekérjük a csoport üzeneteit
        List<Message> messages = groupMessages.get(groupId);
        
        // Ha nincs (null), üres listát adunk vissza
        if (messages == null) {
            return Collections.emptyList();
        }
        
        // Ha van, visszaadjuk az üzeneteket
        return messages;
    }

    /** Delete a single message from a group's message list by message id. */
    public boolean deleteGroupMessage(UUID groupId, UUID messageId) {
        List<Message> list = groupMessages.get(groupId);
        if (list == null) return false;
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i).getId(), messageId)) { idx = i; break; }
        }
        if (idx >= 0) {
            list.remove(idx);
            return true;
        }
        return false;
    }

    /** Delete a group and its messages. */
    public boolean deleteGroup(UUID groupId) {
        Group g = groups.remove(groupId);
        groupMessages.remove(groupId);
        return g != null;
    }

    /** Permission check wrapper for group actions. */
    public boolean hasGroupPermission(UUID groupId, String username, String permission) {
        Group g = groups.get(groupId);
        User u = usersByName.get(username);
        if (g == null || u == null) return false;
        return g.hasPermission(u.getId(), permission);
    }

    // Some utility/test helpers
    public int userCount() { return usersByName.size(); }

    public int groupCount() { return groups.size(); }

    public void clearAll() {
        usersByName.clear();
        usersById.clear();
        friends.clear();
        groups.clear();
        privateMessages.clear();
        groupMessages.clear();
        incomingFriendRequests.clear();
        outgoingFriendRequests.clear();
    }

    /**
     * Return a map of group id -> group name for all groups. Defensive copy.
     */
    public Map<java.util.UUID, String> getAllGroups() {
        Map<java.util.UUID, String> m = new HashMap<>();
        for (Map.Entry<java.util.UUID, Group> e : groups.entrySet()) {
            m.put(e.getKey(), e.getValue().getName());
        }
        return m;
    }

    /**
     * Return usernames of members for a given group id.
     */
    public Set<String> getGroupMembers(java.util.UUID groupId) {
        Group g = groups.get(groupId);
        if (g == null) return Collections.emptySet();
        Set<String> res = new HashSet<>();
        for (java.util.UUID uid : g.getMemberRoles().keySet()) {
            User u = usersById.get(uid);
            if (u != null) res.add(u.getUsername());
        }
        return res;
    }

    /**
     * Return a defensive copy of the friend usernames for the given user.
     */
    public java.util.Set<String> getFriends(String username) {
        return new java.util.HashSet<>(friends.getOrDefault(username, java.util.Collections.emptySet()));
    }

    /**
     * Resolve username for a given user UUID. Returns null if not found.
     */
    public String getUsernameById(UUID id) {
        User u = usersById.get(id);
        return u == null ? null : u.getUsername();
    }

    /**
     * Return all registered usernames (defensive copy).
     */
    public java.util.Set<String> getAllUsernames() {
        return new java.util.HashSet<>(usersByName.keySet());
    }
}
