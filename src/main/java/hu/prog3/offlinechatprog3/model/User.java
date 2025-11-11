package hu.prog3.offlinechatprog3.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple User model.
 * Implements Serializable so it can be stored in the central DataStore via Java serialization.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String username;
    private String passwordHash; // for the assignment we'll keep plain (or simple) hash placeholder

    public User() {
        this.id = UUID.randomUUID();
    }

    public User(String username, String passwordHash) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
