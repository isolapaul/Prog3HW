package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;

import javax.swing.*;
import java.awt.*;
// Using lambdas; explicit ActionEvent/ActionListener imports not needed

/**
 * A simple login / registration Swing frame.
 *
 * Per the PDF spec: when the app starts, the user is greeted with a login/registration screen.
 * This frame implements both actions and hands control to the main UI on success.
 */
public class LoginFrame extends JFrame {

    private final transient AppController controller;

    // UI controls
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Bejelentkezés");
    private final JButton registerButton = new JButton("Regisztráció");

    public LoginFrame(AppController controller) {
        super("Offline Chat - Bejelentkezés");
        this.controller = controller;
        initComponents();
        bindEvents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Layout: simple GridBag for label+field rows and buttons
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Felhasználónév:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Jelszó:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(passwordField, gbc);

        JPanel buttons = new JPanel();
        buttons.add(loginButton);
        buttons.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttons, gbc);

        setContentPane(panel);
    }

    private void bindEvents() {
        // Register action
        registerButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pw = new String(passwordField.getPassword());
            if (user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Add meg a felhasználónevet és jelszót.", "Hiba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = controller.registerUser(user, pw);
            if (ok) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Sikeres regisztráció. Jelentkezz be.");
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "A felhasználónév már foglalt.", "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Login action
        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pw = new String(passwordField.getPassword());
            if (user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Add meg a felhasználónevet és jelszót.", "Hiba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = controller.authenticateUser(user, pw);
            if (ok) {
                // Open main UI and close login
                MainFrame main = new MainFrame(controller, user);
                main.setVisible(true);
                // show notification about incoming friend requests if any
                java.util.Set<String> incoming = controller.getIncomingFriendRequests(user);
                if (!incoming.isEmpty()) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(main, "Vannak bejövő barát kéréseid.", "Értesítés", JOptionPane.INFORMATION_MESSAGE));
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "Hibás felhasználónév vagy jelszó.", "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
