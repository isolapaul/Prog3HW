package hu.prog3.offlinechatprog3.persistence;

import java.io.*;

/**
 * Fájl kezelő osztály az adatok mentéséért és betöltéséért.
 * Java szerializációt használ a DataStore objektum perzisztenciájához.
 * Utility class, csak statikus metódusokat tartalmaz.
 */
public class FileManager {

    private FileManager() { /* utility class */ }

    /** Adatok mentése fájlba szerializációval. */
    public static boolean save(DataStore store, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(store);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Adatok betöltése fájlból deszerializációval. Pattern matching használata. */
    public static DataStore load(File file) {
        if (!file.exists()) return null;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object o = ois.readObject();
            // Pattern matching instanceof (Java 16+)
            if (o instanceof DataStore dataStore) {
                return dataStore;
            }
            return null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
