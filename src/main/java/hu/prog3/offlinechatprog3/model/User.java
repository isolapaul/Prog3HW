package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {

    //verziószám a szerializációhoz
    private static final long serialVersionUID = 1L;
    //egyedi azonosító
    private UUID id;
    //felhasználónév
    private String username;
    //jelszó
    private String passwordHash;
    //konstruktor
    public User(String username, String passwordHash) {
        this.id = UUID.randomUUID(); // Egyedi ID generálás
        this.username = username;
        this.passwordHash = passwordHash;
    }
    //id lekérdezése
    public UUID getId() {
        return id;
    }
    //id beállítása
    public void setId(UUID id) {
        this.id = id;
    }
    //felhasználónév lekérdezése
    public String getUsername() {
        return username;
    }
    //felhasználónév beállítása
    public void setUsername(String username) {
        this.username = username;
    }
    //jelszó lekérdezése
    public String getPasswordHash() {
        return passwordHash;
    }
    //jelszó beállítása
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
