package hu.prog3.offlinechatprog3;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.ui.LoginFrame;

import javax.swing.*;

/**
 * Small launcher that starts the Swing application.
 */
public class Main {
    public static void main(String[] args) {
        // Ensure Swing runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            LoginFrame lf = new LoginFrame(controller);
            lf.setVisible(true);
        });
    }
}
