package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Jelszókezelő utility osztály bcrypt titkosítással.
 */
public final class PasswordUtil {

    private PasswordUtil() {}
    
    /**
     * Bcrypt hash generálása a jelszóból.
     * @param plainPassword egyszerű szöveges jelszó
     * @return bcrypt hash
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    /**
     * Jelszó ellenőrzése a tárolt hash-sel.
     * @param plainPassword egyszerű szöveges jelszó
     * @param hashedPassword tárolt bcrypt hash
     * @return true ha egyezik
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
