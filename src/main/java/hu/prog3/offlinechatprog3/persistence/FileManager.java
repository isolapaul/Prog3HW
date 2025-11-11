package hu.prog3.offlinechatprog3.persistence;

import java.io.*;

/**
 * FÁJL KEZELŐ OSZTÁLY
 * 
 * Ez az osztály felelős az adatok fájlba mentéséért és onnan való betöltéséért.
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * Az offline chat azt jelenti, hogy az adatok helyben tárolódnak (nem szerveren).
 * Ha bezárjuk az alkalmazást, az összes adat (felhasználók, üzenetek, csoportok)
 * el kellene vesszen - de megmentjük őket fájlba!
 * 
 * HOGYAN MŰKÖDIK A JAVA SZERIALIZÁCIÓ?
 * A Java be tudja csomagolni egy objektum teljes állapotát byte-okba,
 * majd ezeket a byte-okat fájlba írni. Később vissza tudja olvasni és
 * újra objektummá alakítani.
 * 
 * Képzeld el úgy, mint egy "Save Game" funkciót:
 * - Mentés = az egész játék állapotát lementi
 * - Betöltés = visszaállítja az állapotot pontosan amilyen volt
 * 
 * MIÉRT UTILITY CLASS?
 * Ez egy segédosztály, aminek csak statikus metódusai vannak.
 * Nem kell belőle példányt készíteni (new FileManager()), csak a
 * metódusait hívjuk: FileManager.save(...) és FileManager.load(...)
 */
public class FileManager {

    /**
     * PRIVATE KONSTRUKTOR
     * Megakadályozza, hogy valaki new FileManager()-t csináljon.
     * Ez egy utility class, nem kell példányosítani.
     */
    private FileManager() { /* utility class */ }

    /**
     * ADATOK MENTÉSE FÁJLBA
     * 
     * LÉPÉSEK:
     * 1. Megnyitunk egy fájlt írásra (FileOutputStream)
     * 2. Létrehozunk egy objektum író stream-et (ObjectOutputStream)
     * 3. Beírjuk az egész DataStore objektumot (writeObject)
     * 4. Automatikus bezárás (try-with-resources)
     * 5. Ha sikeres, true-t adunk vissza
     * 6. Ha hiba van, false-t adunk vissza
     * 
     * TRY-WITH-RESOURCES MAGYARÁZAT:
     * A try (...) zárójel között létrehozott objektumok automatikusan
     * lezáródnak a try blokk végén, még akkor is, ha hiba történik.
     * Ez biztonságosabb, mint manuálisan close()-t hívni.
     * 
     * @param store Az összes adatot tartalmazó DataStore objektum
     * @param file A fájl, ahova mentünk (pl. "data/offline-chat.dat")
     * @return true = sikeres mentés, false = hiba történt
     */
    public static boolean save(DataStore store, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Beírjuk az egész store objektumot a fájlba
            // Ez rekurzívan menti az összes benne lévő objektumot is!
            oos.writeObject(store);
            return true; // Sikeres mentés
        } catch (IOException e) {
            // Ha valami hiba történik (nincs jogosultság, tele a lemez, stb.)
            e.printStackTrace(); // Kiírjuk a hibát a konzolra debug-oláshoz
            return false; // Sikertelen mentés
        }
    }

    /**
     * ADATOK BETÖLTÉSE FÁJLBÓL
     * 
     * LÉPÉSEK:
     * 1. Ellenőrizzük, hogy létezik-e a fájl (ha nem, nincs mit betölteni)
     * 2. Megnyitunk egy fájlt olvasásra (FileInputStream)
     * 3. Létrehozunk egy objektum olvasó stream-et (ObjectInputStream)
     * 4. Beolvassuk az objektumot (readObject)
     * 5. Ellenőrizzük, hogy DataStore típusú-e
     * 6. Ha minden rendben, visszaadjuk a DataStore-t
     * 7. Ha hiba van, null-t adunk vissza
     * 
     * INSTANCEOF OPERÁTOR:
     * Az "o instanceof DataStore" azt ellenőrzi, hogy az o objektum
     * DataStore típusú-e. Ez biztonságossá teszi a cast-olást.
     * 
     * @param file A fájl, ahonnan betöltünk (pl. "data/offline-chat.dat")
     * @return A betöltött DataStore objektum, vagy null ha nem sikerült
     */
    public static DataStore load(File file) {
        // Ha nem létezik a fájl, nincs mit betölteni
        if (!file.exists()) return null;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Beolvassuk az objektumot a fájlból
            Object o = ois.readObject();
            
            // Ellenőrizzük, hogy DataStore típusú-e
            if (o instanceof DataStore) {
                return (DataStore) o; // Cast DataStore típusra és visszaadjuk
            }
            return null; // Ha nem DataStore, null-t adunk vissza
        } catch (IOException | ClassNotFoundException e) {
            // IOException = fájl olvasási hiba
            // ClassNotFoundException = nem találja az osztály definícióját
            e.printStackTrace(); // Debug-hoz kiírjuk a hibát
            return null; // Sikertelen betöltés
        }
    }
}
