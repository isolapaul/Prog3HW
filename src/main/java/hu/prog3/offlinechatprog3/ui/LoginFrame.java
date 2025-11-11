package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;

import javax.swing.*;
import java.awt.*;

/**
 * BEJELENTKEZÉSI ABLAK (LoginFrame)
 * 
 * Ez az ELSŐ ABLAK, amit a felhasználó lát az alkalmazás indulásakor.
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * - Felhasználó azonosítás (ki vagy?)
 * - Biztonsági ellenőrzés (jogod van-e belépni?)
 * - Új felhasználók regisztrációja
 * 
 * HOGYAN MŰKÖDIK?
 * 1. Megjelenik egy ablak két mezővel (username, password) és két gombbal
 * 2. Felhasználó választhat:
 *    a) REGISZTRÁCIÓ - új fiókot hoz létre
 *    b) BEJELENTKEZÉS - meglévő fiókkal lép be
 * 3. Ha sikeres a bejelentkezés:
 *    - Megnyílik a MainFrame (főablak)
 *    - Ez a LoginFrame bezáródik
 * 
 * SWING KOMPONENSEK HASZNÁLATA:
 * - JFrame = ablak keret
 * - JTextField = szöveges beviteli mező (username-hez)
 * - JPasswordField = jelszó mező (pont karakterekkel elfedi a begépelt szöveget)
 * - JButton = nyomható gomb
 * - JLabel = statikus szöveg (feliratok)
 * - JPanel = konténer, komponensek csoportosítására
 */
public class LoginFrame extends JFrame {

    /**
     * CONTROLLER - kapcsolat az adatréteggel
     * A transient kulcsszó azt jelenti, hogy ezt a mezőt NEM mentjük fájlba
     * (bár ezt az ablakot sosem mentjük, csak szokás így jelölni)
     */
    private final transient AppController controller;

    // ============================================================
    // UI KOMPONENSEK (Swing widget-ek)
    // ============================================================
    
    /**
     * USERNAME MEZŐ
     * JTextField = sima szöveges beviteli mező
     * A (20) paraméter: 20 karakter széles legyen vizuálisan
     */
    private final JTextField usernameField = new JTextField(20);
    
    /**
     * JELSZÓ MEZŐ
     * JPasswordField = speciális szöveges mező
     * A begépelt karaktereket pont-okkal (•) elfedi a biztonság miatt
     */
    private final JPasswordField passwordField = new JPasswordField(20);
    
    /**
     * BEJELENTKEZÉS GOMB
     * Amikor megnyomják, ellenőrizzük a username/password párost
     */
    private final JButton loginButton = new JButton("Bejelentkezés");
    
    /**
     * REGISZTRÁCIÓ GOMB
     * Amikor megnyomják, új felhasználót hozunk létre
     */
    private final JButton registerButton = new JButton("Regisztráció");

    /**
     * KONSTRUKTOR - az ablak létrehozása
     * 
     * LÉPÉSEK:
     * 1. Beállítjuk az ablak címét (super() hívás)
     * 2. Eltároljuk a controller referenciát
     * 3. Létrehozzuk a komponenseket (initComponents)
     * 4. Hozzákötjük az eseménykezelőket (bindEvents)
     * 5. Beállítjuk az ablak tulajdonságait (méret, pozíció, stb.)
     * 
     * @param controller Az alkalmazás controller objektuma
     */
    public LoginFrame(AppController controller) {
        // super() = szülő osztály (JFrame) konstruktorának hívása
        // Beállítja az ablak fejlécének szövegét
        super("Offline Chat - Bejelentkezés");
        
        // Controller eltárolása későbbi használatra
        this.controller = controller;
        
        // UI komponensek elrendezése
        initComponents();
        
        // Gomb kattintások hozzákötése
        bindEvents();
        
        // EXIT_ON_CLOSE = amikor bezárjuk az ablakot, az egész program leáll
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // pack() = méretezi az ablakot a komponensek mérete alapján
        pack();
        
        // setLocationRelativeTo(null) = középre igazítja az ablakot a képernyőn
        setLocationRelativeTo(null);
    }

    /**
     * KOMPONENSEK INICIALIZÁLÁSA ÉS ELRENDEZÉSE
     * 
     * Ez a metódus építi fel a bejelentkezési form-ot.
     * 
     * GRIDBAG LAYOUT MAGYARÁZAT:
     * A GridBagLayout egy rugalmas elrendezési mód, ahol minden komponensnek
     * megadhatjuk a pontos pozícióját (gridx, gridy) és viselkedését.
     * 
     * KOORDINÁTA RENDSZER:
     * gridx = oszlop (0 = bal, 1 = jobb, stb.)
     * gridy = sor (0 = felső, 1 = második, stb.)
     * 
     * VISUAL LAYOUT (amit látunk):
     * ┌─────────────────────────────────┐
     * │  Felhasználónév: [          ]   │  <- 0. sor
     * │  Jelszó:         [          ]   │  <- 1. sor
     * │    [Bejelentkezés] [Regisztráció] │  <- 2. sor
     * └─────────────────────────────────┘
     */
    private void initComponents() {
        // FŐ PANEL LÉTREHOZÁSA GridBagLayout-tal
        JPanel panel = new JPanel(new GridBagLayout());
        
        // GRIDBAGCONSTRAINTS = elrendezési szabályok egy komponenshez
        GridBagConstraints gbc = new GridBagConstraints();
        
        // INSETS = térköz (padding) a komponensek körül
        // new Insets(top, left, bottom, right) - mind 4 pixel
        gbc.insets = new Insets(4, 4, 4, 4);
        
        // ============================================================
        // ELSŐ SOR: "Felhasználónév:" címke és beviteli mező
        // ============================================================
        
        // CÍMKE POZÍCIÓ: 0. oszlop, 0. sor, jobbra igazítva
        gbc.gridx = 0;  // Bal oldali oszlop
        gbc.gridy = 0;  // Első sor
        gbc.anchor = GridBagConstraints.LINE_END;  // Jobbra igazítás (a mező felé)
        panel.add(new JLabel("Felhasználónév:"), gbc);
        
        // MEZŐ POZÍCIÓ: 1. oszlop, 0. sor, balra igazítva
        gbc.gridx = 1;  // Jobb oldali oszlop
        gbc.anchor = GridBagConstraints.LINE_START;  // Balra igazítás
        panel.add(usernameField, gbc);

        // ============================================================
        // MÁSODIK SOR: "Jelszó:" címke és beviteli mező
        // ============================================================
        
        gbc.gridx = 0;  // Bal oldali oszlop
        gbc.gridy = 1;  // Második sor
        gbc.anchor = GridBagConstraints.LINE_END;  // Jobbra igazítás
        panel.add(new JLabel("Jelszó:"), gbc);
        
        gbc.gridx = 1;  // Jobb oldali oszlop
        gbc.anchor = GridBagConstraints.LINE_START;  // Balra igazítás
        panel.add(passwordField, gbc);

        // ============================================================
        // HARMADIK SOR: Gombok (bejelentkezés és regisztráció)
        // ============================================================
        
        // Gombok KÜLÖN panel-be kerülnek egymás mellé
        JPanel buttons = new JPanel();  // Alapértelmezett FlowLayout
        buttons.add(loginButton);
        buttons.add(registerButton);

        // Gomb panel pozíciója: középen, 2. sorban, 2 oszlopot foglal
        gbc.gridx = 0;  // Bal oldaltól indulva
        gbc.gridy = 2;  // Harmadik sor
        gbc.gridwidth = 2;  // 2 oszlopot foglal el (mindkét oszlopon átnyúlik)
        gbc.anchor = GridBagConstraints.CENTER;  // Középre igazítás
        panel.add(buttons, gbc);

        // A panel lesz az ablak tartalma (content pane)
        setContentPane(panel);
    }

    /**
     * ESEMÉNYKEZELŐK HOZZÁKÖTÉSE
     * 
     * Ez a metódus hozzáköti a gombokhoz azt a kódot, ami lefut, amikor
     * a felhasználó megnyomja őket.
     * 
     * LAMBDA KIFEJEZÉS MAGYARÁZAT:
     * addActionListener(e -> { ... }) azt jelenti:
     * "Amikor megnyomják a gombot (ActionEvent 'e' történik), futtasd le ezt a kódot"
     * 
     * A 'e' paraméter az esemény objektum, de itt nem használjuk.
     */
    private void bindEvents() {
        // ============================================================
        // REGISZTRÁCIÓ GOMB ESEMÉNYKEZELŐJE
        // ============================================================
        
        registerButton.addActionListener(e -> {
            // 1. ADATOK KIOLVASÁSA a mezőkből
            // trim() = levágja a felesleges szóközöket az elejéről/végéről
            String user = usernameField.getText().trim();
            
            // JPasswordField.getPassword() char[] tömböt ad vissza (biztonság miatt)
            // String-gé alakítjuk (egyszerűsített verzió)
            String pw = new String(passwordField.getPassword());
            
            // 2. VALIDÁCIÓ - ellenőrizzük, hogy kitöltötték-e mindkét mezőt
            if (user.isEmpty() || pw.isEmpty()) {
                // JOptionPane = felugró üzenet ablak
                // showMessageDialog = egyszerű információs popup
                JOptionPane.showMessageDialog(
                    LoginFrame.this,  // Melyik ablakhoz tartozik (erre centrálódik)
                    "Add meg a felhasználónevet és jelszót.",  // Üzenet szövege
                    "Hiba",  // Ablak címe
                    JOptionPane.ERROR_MESSAGE  // Hiba ikon (piros X)
                );
                return;  // Kilépünk, nem folytatjuk a regisztrációt
            }
            
            // 3. REGISZTRÁCIÓ VÉGREHAJTÁSA a controller-en keresztül
            boolean ok = controller.registerUser(user, pw);
            
            // 4. EREDMÉNY VISSZAJELZÉSE a felhasználónak
            if (ok) {
                // Sikeres regisztráció - zöld információs üzenet
                JOptionPane.showMessageDialog(
                    LoginFrame.this,
                    "Sikeres regisztráció. Jelentkezz be.",
                    "Siker",
                    JOptionPane.INFORMATION_MESSAGE  // Info ikon (i)
                );
            } else {
                // Sikertelen - a username már létezik
                JOptionPane.showMessageDialog(
                    LoginFrame.this,
                    "A felhasználónév már foglalt.",
                    "Hiba",
                    JOptionPane.ERROR_MESSAGE  // Hiba ikon
                );
            }
        });

        // ============================================================
        // BEJELENTKEZÉS GOMB ESEMÉNYKEZELŐJE
        // ============================================================
        
        loginButton.addActionListener(e -> {
            // 1. ADATOK KIOLVASÁSA (ugyanúgy, mint regisztrációnál)
            String user = usernameField.getText().trim();
            String pw = new String(passwordField.getPassword());
            
            // 2. VALIDÁCIÓ
            if (user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(
                    LoginFrame.this,
                    "Add meg a felhasználónevet és jelszót.",
                    "Hiba",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // 3. BEJELENTKEZÉS ELLENŐRZÉSE
            boolean ok = controller.authenticateUser(user, pw);
            
            // 4. HA SIKERES A BEJELENTKEZÉS
            if (ok) {
                // a) FŐABLAK MEGNYITÁSA
                // Létrehozzuk a MainFrame-et (főablak) a bejelentkezett user-rel
                MainFrame main = new MainFrame(controller, user);
                main.setVisible(true);  // Megjelenítjük
                
                // b) BARÁTKÉRÉS ÉRTESÍTÉS (ha vannak)
                // Lekérdezzük, hogy vannak-e bejövő barátkérések
                java.util.Set<String> incoming = controller.getIncomingFriendRequests(user);
                if (!incoming.isEmpty()) {
                    // SwingUtilities.invokeLater = később futtatja le (a főablak megjelenése után)
                    // Ez azért kell, hogy az értesítés ne az üres ablakra jelenjen meg
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(
                            main,  // A főablakra centrálódik
                            "Vannak bejövő barát kéréseid.",
                            "Értesítés",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    );
                }
                
                // c) BEJELENTKEZÉSI ABLAK BEZÁRÁSA
                // dispose() = memóriából is eltávolítja az ablakot
                dispose();
            } else {
                // SIKERTELEN BEJELENTKEZÉS
                // Rossz username vagy jelszó
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
