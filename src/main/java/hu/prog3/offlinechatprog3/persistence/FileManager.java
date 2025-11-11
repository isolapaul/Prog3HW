package hu.prog3.offlinechatprog3.persistence;

import java.io.*;

/**
 * FileManager handles saving and loading the entire DataStore via Java serialization.
 */
public class FileManager {

    private FileManager() { /* utility */ }

    /**
     * Save the given datastore to the given file path using Java built-in serialization.
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
     * Load a DataStore from the given file path. Returns null on failure.
     */
    public static DataStore load(File file) {
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object o = ois.readObject();
            if (o instanceof DataStore) return (DataStore) o;
            return null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
