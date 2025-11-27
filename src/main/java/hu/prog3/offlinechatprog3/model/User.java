package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Felhasználót reprezentáló immutable (megváltozhatatlan) modell osztály.
 * Tartalmazza a felhasználó egyedi azonosítóját, felhasználónevét és a bcrypt hash-elt jelszavát.
 * 
 * @since 1.0
 */
public class User implements Serializable {

    /** Verziószám a szerializációhoz */
    private static final long serialVersionUID = 1L;
    
    /** Egyedi azonosító (UUID) - a felhasználó belső azonosítására szolgál */
    private UUID id;
    
    /** Felhasználónév - egyedi a rendszerben, bejelentkezéshez használatos */
    private String username;
    
    /** Jelszó hash - bcrypt algoritmussal titkosított jelszó */
    private String passwordHash;
    
    /**
     * Létrehoz egy új felhasználót a megadott felhasználónévvel és jelszó hash-sel.
     * Automatikusan generál egy egyedi UUID azonosítót.
     * 
     * @param username a felhasználónév (nem lehet null)
     * @param passwordHash a bcrypt hash-elt jelszó (nem lehet null)
     */
    public User(String username, String passwordHash) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.passwordHash = passwordHash;
    }
    
    /**
     * Visszaadja a felhasználó egyedi azonosítóját.
     * 
     * @return a felhasználó UUID azonosítója
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Visszaadja a felhasználó nevét.
     * 
     * @return a felhasználónév
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Visszaadja a felhasználó jelszavának bcrypt hash-ét.
     * 
     * @return a hash-elt jelszó
     */
    public String getPasswordHash() {
        return passwordHash;
    }
}
