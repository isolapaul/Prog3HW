package ui;

import controller.AppController;
import util.PasswordUtil;
import controller.RegistrationResult;

import javax.swing.*;
import java.awt.*;

/**
 * Bejelentkezési ablak - regisztráció és bejelentkezés.
 */
public class LoginFrame extends JFrame {

    private final transient AppController controller;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Bejelentkezés");
    private final JButton registerButton = new JButton("Regisztráció");
    
    /**
     * Bejelentkezési ablak konstruktor.
     * @param controller MVC controller
     */
    public LoginFrame(AppController controller) {
        //fejléc beállítása
        super("Offline Chat - Bejelentkezés");
        this.controller = controller;
        
        // Ui elrendezése
        initComponents();
        
        //Gomb kattintások
        bindEvents();
        
        //ablak bezárásánál leáll a program
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        //ablak méretezése a komponensekhez
        pack();
        
        //középre igazítás a képernyőn
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        //fő panel
        JPanel panel = new JPanel(new GridBagLayout());
        
        //elrendezési szabályok hozzárendeléséhez
        GridBagConstraints gbc = new GridBagConstraints();
        
        
        //térköz mindenhonnan 4 pixel
        gbc.insets = new Insets(4, 4, 4, 4);
        
        //felhasználónév címke
        gbc.gridx = 0;  //bal oldali oszlop
        gbc.gridy = 0;  //1. sor
        gbc.anchor = GridBagConstraints.LINE_END;  //jobbra igazítás
        panel.add(new JLabel("Felhasználónév:"), gbc);
        
        //felhasználónév mező
        gbc.gridx = 1;  //jobb oldali oszlop
        gbc.anchor = GridBagConstraints.LINE_START;  //balra igazítás
        panel.add(usernameField, gbc);

        //jelszó címke
        gbc.gridx = 0;  //bal oldali oszlop
        gbc.gridy = 1;  //2. sor
        gbc.anchor = GridBagConstraints.LINE_END;  //jobbra igazítás
        panel.add(new JLabel("Jelszó:"), gbc);
        //jelszó mező
        gbc.gridx = 1;  //jobb oldali oszlop
        gbc.anchor = GridBagConstraints.LINE_START;  //balra igazítás
        panel.add(passwordField, gbc);

        //gombok
        JPanel buttons = new JPanel();  
        buttons.add(loginButton);
        buttons.add(registerButton);

        //gomb panel pozíciója: középen, 2. sorban, 2 oszlopot foglal
        gbc.gridx = 0;  //bal oldali oszlop
        gbc.gridy = 2;  //3. sor
        gbc.gridwidth = 2;  //2 oszlopot foglal el
        gbc.anchor = GridBagConstraints.CENTER;  //középre igazítás
        panel.add(buttons, gbc);

        // panel a tartalom
        setContentPane(panel);
    }

    //eseménykezelők
    private void bindEvents() {
        //regisztrációs gomb
        registerButton.addActionListener(e -> {
            String user = usernameField.getText().trim(); //adatok kiolvasása
            String pw = new String(passwordField.getPassword());
            
            //validáció
            if (user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(
                    LoginFrame.this, 
                    "Add meg a felhasználónevet és jelszót.",
                    "Hiba",  
                    JOptionPane.ERROR_MESSAGE  //hiba ikon
                );
                return; 
            }
            
            //regisztráció végrehajtása a controller segítségével
            String hashedPw = PasswordUtil.hashPassword(pw);
            RegistrationResult result = controller.registerUser(user, hashedPw);
            
            switch (result) {
                case SUCCESS:
                    JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "Sikeres regisztráció. Jelentkezz be.",
                        "Siker",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    break;
                case USERNAME_TOO_SHORT:
                    JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "A felhasználónév túl rövid (legalább 3 karakter).",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE
                    );
                    break;
                case USERNAME_TOO_LONG:
                    JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "A felhasználónév túl hosszú (maximum 20 karakter).",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE
                    );
                    break;
                case USERNAME_ALREADY_TAKEN:
                    JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "A felhasználónév már foglalt.",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE
                    );
                    break;
            }
        });
        //bejelentkezés gomb
        loginButton.addActionListener(e -> {
            //adatok kiolvasása
            String user = usernameField.getText().trim();
            String pw = new String(passwordField.getPassword());
            
            //validáció
            if (user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(
                    LoginFrame.this,
                    "Add meg a felhasználónevet és jelszót.",
                    "Hiba",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            //hitelesítés a controller segítségével
            boolean ok = controller.authenticateUser(user, pw);
            //ha sikeres a bejelentkezés
            if (ok) {
                //mainframe létrehozása a felhasználóval
                MainFrame main = new MainFrame(controller, user);
                main.setVisible(true); 
                
                dispose(); //ablak bezárása
            } else {
                //sikertelen bejelentkezés
                JOptionPane.showMessageDialog(
                    LoginFrame.this,
                    "Hibás felhasználónév vagy jelszó.",
                    "Hiba",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
