package hu.prog3.offlinechatprog3.persistence;

import java.io.*;

//statikus fájl kezelő osztály
public class FileManager {

    private FileManager() { }

    //adatok mentése
    public static boolean save(DataStore store, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(store);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //adatok betöltése
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
