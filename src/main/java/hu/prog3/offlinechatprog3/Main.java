package hu.prog3.offlinechatprog3;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.ui.LoginFrame;

import javax.swing.*;

/**
 * FŐ BELÉPÉSI PONT (MAIN CLASS)
 * 
 * Ez az osztály indítja el az egész alkalmazást.
 * 
 * HOGYAN MŰKÖDIK AZ ALKALMAZÁS INDÍTÁSA?
 * 1. A Java először a Main.main() metódust hívja meg
 * 2. Mi létrehozunk egy AppController-t (ez kezeli az adatokat)
 * 3. Létrehozunk egy LoginFrame-et (bejelentkezési ablak)
 * 4. Megjelenitjük a LoginFrame-et
 * 5. Innen a felhasználó interakció veszi át az irányítást
 * 
 * MIÉRT SWINGUTILITIES.INVOKELATER?
 * A Swing (Java grafikus keretrendszer) egy speciális szálon (Event Dispatch Thread)
 * fut. Biztonsági okokból minden Swing ablakot ezen a szálon kell létrehozni.
 * 
 * A SwingUtilities.invokeLater(() -> { ... }) azt mondja:
 * "Futtasd ezt a kódot a Swing szálán, amikor rá kerül a sor"
 * 
 * A () -> { ... } egy lambda kifejezés, olyan mint egy névtelen függvény.
 * Régebbi Java-ban így nézett volna ki:
 * SwingUtilities.invokeLater(new Runnable() {
 *     public void run() {
 *         // kód
 *     }
 * });
 */
public class Main {
    /**
     * MAIN METÓDUS - Az alkalmazás kezdőpontja
     * 
     * @param args Parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // Elindítjuk a Swing alkalmazást a megfelelő szálon
        SwingUtilities.invokeLater(() -> {
            // 1. CONTROLLER LÉTREHOZÁSA
            // Ez kezeli az üzleti logikát és az adatmentést
            AppController controller = new AppController();
            
            // 2. BEJELENTKEZÉSI ABLAK LÉTREHOZÁSA
            // Ez az első ablak, amit a felhasználó lát
            LoginFrame lf = new LoginFrame(controller);
            
            // 3. ABLAK MEGJELENÍTÉSE
            // setVisible(true) = mutasd meg az ablakot
            lf.setVisible(true);
            
            // Innentől a felhasználó irányít:
            // - Regisztrál vagy bejelentkezik
            // - Ha sikeres, megnyílik a főablak (MainFrame)
            // - Onnan chateket nyithat, csoportokat kezelhet, stb.
        });
    }
}
