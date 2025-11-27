import controller.AppController;
import ui.LoginFrame;

import javax.swing.*;

/**
 * Alkalmazás belépési pont.
 */
public class Main {
    /**
     * Swing alkalmazás indítása.
     * @param args parancssori argumentumok
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            LoginFrame loginFrame = new LoginFrame(controller);
            loginFrame.setVisible(true);
        });
    }
}
