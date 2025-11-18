package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * FELHASZNÁLÓ MODELL OSZTÁLY
 * 
 * Ez az osztály egy chat alkalmazás felhasználóját reprezentálja.
 * 
 * MIÉRT VAN RÁJUK SZÜKSÉG?
 * - Minden felhasználót egyedileg azonosítani kell (ID alapján)
 * - Tárolnunk kell a felhasználónevet és jelszót
 * - Az objektumot el kell tudni menteni fájlba (ezért Serializable)
 * 
 * HOGYAN MŰKÖDIK?
 * 1. Amikor létrehozunk egy új felhasználót, automatikusan kap egy egyedi UUID azonosítót
 * 2. A username a felhasználó neve (pl. "János")
 * 3. A passwordHash a jelszó SHA-256 hash-e (nem plain text!)
 * 
 * SERIALIZABLE MAGYARÁZAT:
 * A Serializable interface azt jelenti, hogy ezt az objektumot le lehet menteni fájlba,
 * és később vissza lehet tölteni. Ez olyan, mintha lefotóznánk az objektum állapotát.
 */
public class User implements Serializable {

    // Ez egy speciális szám, ami segít a fájl betöltésben - ne változtasd!
    private static final long serialVersionUID = 1L;

    // MEZŐK (az objektum adatai):
    
    /** Egyedi azonosító - mint egy személyi szám, minden usernek más */
    private UUID id;
    
    /** Felhasználónév - pl. "anna23" */
    private String username;
    
    /** Jelszó (egyszerűsített verzióban sima szöveg, éles rendszerben titkosítva lenne) */
    private String passwordHash;

    /**
     * ÜRES KONSTRUKTOR
     * Amikor új User()-t hozunk létre paraméterek nélkül.
     * Automatikusan generál egy egyedi ID-t.
     */
    public User() {
        this.id = UUID.randomUUID(); // Véletlenszerű egyedi azonosító generálása
    }

    /**
     * TELJES KONSTRUKTOR
     * Amikor új felhasználót hozunk létre névvel és jelszóval.
     * 
     * @param username A felhasználó neve (pl. "peter")
     * @param passwordHash A jelszó SHA-256 hash-e
     */
    public User(String username, String passwordHash) {
        this.id = UUID.randomUUID(); // Egyedi ID generálás
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // GETTER ÉS SETTER METÓDUSOK
    // Ezek segítenek az adatok biztonságos lekérdezésében és beállításában
    
    /**
     * ID LEKÉRDEZÉSE
     * @return A felhasználó egyedi azonosítója
     */
    public UUID getId() {
        return id;
    }

    /**
     * ID BEÁLLÍTÁSA
     * (Ritkán használjuk, mert az ID automatikusan generálódik)
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * FELHASZNÁLÓNÉV LEKÉRDEZÉSE
     * @return A felhasználónév szöveg (pl. "anna23")
     */
    public String getUsername() {
        return username;
    }

    /**
     * FELHASZNÁLÓNÉV BEÁLLÍTÁSA
     * @param username Az új felhasználónév
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * JELSZÓ LEKÉRDEZÉSE
     * @return A jelszó szöveg
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * JELSZÓ BEÁLLÍTÁSA
     * @param passwordHash Az új jelszó SHA-256 hash-e
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * EGYENLŐSÉG ELLENŐRZÉS
     * Két felhasználó akkor egyenlő, ha a felhasználónevük megegyezik.
     * 
     * MIÉRT FONTOS?
     * Java-ban az equals() metódus határozza meg, hogy két objektum "ugyanaz"-e.
     * Ez segít listákban, halmazokban való keresésben.
     * 
     * @param o A másik objektum, amihez hasonlítunk
     * @return true, ha egyenlők, false ha nem
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Ha ugyanaz az objektum memóriában
        if (o == null || getClass() != o.getClass()) return false; // Ha null vagy nem User típusú
        User user = (User) o; // Átkasztoljuk User típusra
        return Objects.equals(username, user.username); // Összehasonlítjuk a neveket
    }

    /**
     * HASH KÓD GENERÁLÁS
     * Ez egy szám, amit a Java használ gyors kereséshez hash-alapú gyűjteményekben (HashMap, HashSet).
     * Két egyenlő objektumnak ugyanaz a hashCode-ja kell legyen.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * SZÖVEGES MEGJELENÍTÉS
     * Amikor kiírjuk a User objektumot (pl. System.out.println(user)),
     * akkor ez a szöveg jelenik meg.
     * 
     * @return Olvasható formátum: "User{id=..., username='...'}"
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
