package controller;

/**
 * Regisztrációs művelet eredménye.
 */
public enum RegistrationResult {
    /** Sikeres regisztráció */
    SUCCESS,
    /** Felhasználónév túl rövid (minimum 3 karakter) */
    USERNAME_TOO_SHORT,
    /** Felhasználónév túl hosszú (maximum 20 karakter) */
    USERNAME_TOO_LONG,
    /** Felhasználónév már foglalt */
    USERNAME_ALREADY_TAKEN
}
