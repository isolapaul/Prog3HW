package persistence;

import java.io.*;

/**
 * Fájlkezelő osztály DataStore szerializációhoz.
 */
public class FileManager {

    private FileManager() { }

    /**
     * DataStore mentése fájlba.
     * @param store mentendő adattár
     * @param file célfájl
     * @return true ha sikeres
     */
    public static boolean save(DataStore store, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(store);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * DataStore betöltése fájlból.
     * @param file forrásfájl
     * @return betöltött DataStore vagy null
     */
    public static DataStore load(File file) {
        if (!file.exists()) return null;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (DataStore) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
