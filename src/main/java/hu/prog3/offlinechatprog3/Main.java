package hu.prog3.offlinechatprog3;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.ui.LoginFrame;

import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        //swing alkalmazás indítása
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            LoginFrame loginFrame = new LoginFrame(controller);
            loginFrame.setVisible(true);
        });
    }
}
