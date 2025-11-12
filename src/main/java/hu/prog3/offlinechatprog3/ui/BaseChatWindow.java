package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * ALAP CHAT ABLAK OSZTÁLY (BaseChatWindow)
 * 
 * Ez egy ABSTRACT (absztrakt) osztály, ami a KÖZÖS LOGIKÁT tartalmazza
 * a privát és csoport chat ablakok között.
 * 
 * MIÉRT ABSTRACT?
 * Mert vannak olyan részek, amik KÜLÖNBÖZNEK a privát és csoport chatnél:
 * - Honnan töltjük be az üzeneteket? (fetchMessages)
 * - Ki küldhet üzenetet? (canSendNow)
 * - Hogyan küldjünk üzenetet? (sendInternal)
 * 
 * Ezeket a különbségeket az ALCSOSZTÁLYOK implementálják (override).
 * 
 * MI A KÖZÖS MINDKÉT CHAT TÍPUSBAN?
 * - Van egy üzenet megjelenítő terület (chatArea)
 * - Van egy beviteli mező (inputField)
 * - Van egy küldés gomb (sendButton)
 * - Van élő frissítés (live refresh timer)
 * - Van jogosultság ellenőrzés (lehet küldeni vagy sem)
 * 
 * TEMPLATE METHOD PATTERN:
 * Ez egy tervezési minta, ahol a főbb lépések a szülőosztályban vannak,
 * de az egyes lépések megvalósítása az alcsoportokban történik.
 */
public abstract class BaseChatWindow extends JFrame {
    
    // ============================================================
    // PROTECTED MEZŐK - az alcsosztályok is hozzáférnek
    // ============================================================
    
    /**
     * CONTROLLER - kapcsolat az adatokkal
     * protected = az alcsosztályok is használhatják
     * transient = nem mentjük fájlba (bár ezt sosem mentjük)
     */
    protected final transient AppController controller;
    
    /**
     * SAJÁT FELHASZNÁLÓNÉV
     * Ki van bejelentkezve? (hogy tudjuk: "én küldtem" vagy "ő küldte")
     */
    protected final String me;

    /**
     * CHAT TERÜLET - itt jelennek meg az üzenetek
     * JTextArea = többsoros szöveg megjelenítő
     * 20 sor magas, 50 karakter széles
     */
    protected final JTextArea chatArea = new JTextArea(20, 50);
    
    /**
     * BEVITELI MEZŐ - ide írja a felhasználó az új üzenetet
     * 36 karakter széles
     */
    protected final JTextField inputField = new JTextField(36);
    
    /**
     * KÜLDÉS GOMB
     */
    protected final JButton sendButton = new JButton("Küldés");
    
    // ============================================================
    // PRIVATE MEZŐK - csak ez az osztály használja
    // ============================================================
    
    /**
     * ÉLŐ FRISSÍTÉS TIMER
     * Periodikusan ellenőrzi, hogy jött-e új üzenet
     * javax.swing.Timer = időzített esemény (mint egy ébresztő)
     */
    private javax.swing.Timer liveTimer;
    
    /**
     * UTOLSÓ ÜZENETSZÁM
     * Eltároljuk, hogy legutóbb hány üzenet volt.
     * Ha megváltozik, tudjuk, hogy frissíteni kell.
     */
    private int lastCount = -1;

    /**
     * KONSTRUKTOR - az alap chat ablak létrehozása
     * 
     * PROTECTED = csak az alcsosztályok hívhatják meg
     * (nem lehet közvetlenül BaseChatWindow-t készíteni, csak származtatottat)
     * 
     * LÉPÉSEK:
     * 1. Ablak cím beállítása
     * 2. Controller és felhasználónév eltárolása
     * 3. Komponensek létrehozása és elrendezése
     * 4. Eseménykezelők hozzákötése
     * 5. Üzenetek betöltése
     * 6. Élő frissítés indítása
     * 7. Ablak méretezése és pozícionálása
     * 
     * @param controller Az alkalmazás controller-e
     * @param me A bejelentkezett felhasználó neve
     * @param title Az ablak címe (pl. "Chat - Béla")
     */
    protected BaseChatWindow(AppController controller, String me, String title) {
        // Szülő osztály (JFrame) konstruktora - beállítja a címet
        super(title);
        
        // Paraméterek eltárolása
        this.controller = controller;
        this.me = me;
        
        // UI felépítése
        initComponents();
        
        // Gombok és mezők eseménykezelése
        bindEvents();
        
        // FONTOS: A reloadMessages()-t NEM hívjuk meg itt!
        // Az alcsosztályok hívják meg a konstruktoruk végén,
        // miután inicializálták a saját mezőiket (pl. `other` a PrivateChatWindow-ban)
        
        // Timer indítása - 1.5 másodpercenként ellenőrzi az új üzeneteket
        startLive();
        
        // Ablak méretezése a komponensek alapján
        pack();
        
        // Ablak középre igazítása a képernyőn
        setLocationRelativeTo(null);
    }

    /**
     * KOMPONENSEK INICIALIZÁLÁSA
     * 
     * Ez a metódus építi fel az ablak LAYOUT-ját (elrendezését).
     * 
     * ABLAK FELÉPÍTÉSE:
     * ┌─────────────────────────────────────┐
     * │  CHAT TERÜLET (CENTER)              │ <- Üzenetek jelennek meg itt
     * │  - scrollozható                     │    (chatArea + scrollPane)
     * │  - nem szerkeszthető                │
     * ├─────────────────────────────────────┤
     * │  BEVITELI PANEL (SOUTH)             │ <- Alsó panel
     * │  [   Üzenet írása ide...   ] [Küld] │    (inputField + sendButton)
     * └─────────────────────────────────────┘
     * 
     * LÉPÉSEK:
     * 1. ChatArea beállítása (nem szerkeszthető)
     * 2. Alsó panel létrehozása (input + gomb)
     * 3. ScrollPane hozzáadása (görgetősáv)
     * 4. BorderLayout elrendezés beállítása
     * 5. Jogosultság ellenőrzés (lehet-e küldeni?)
     */
    private void initComponents() {
        // ============================================================
        // 1. CHAT TERÜLET BEÁLLÍTÁSA
        // ============================================================
        
        // Nem lehet szerkeszteni az üzeneteket
        // (csak olvasható, mint egy log fájl)
        chatArea.setEditable(false);
        
        // ============================================================
        // 2. ALSÓ PANEL LÉTREHOZÁSA (input + gomb)
        // ============================================================
        
        // JPanel = konténer, ami más komponenseket tartalmaz
        // FlowLayout (alapértelmezett) = egymás mellé rendezi a dolgokat
        JPanel bottom = new JPanel();
        
        // Beviteli mező hozzáadása
        bottom.add(inputField);
        
        // Küldés gomb hozzáadása
        bottom.add(sendButton);
        
        // ============================================================
        // 3. BORDERLAYOUT ELRENDEZÉS
        // ============================================================
        
        // A JFrame tartalma = ContentPane
        // Erre kell rátenni a komponenseket
        
        // BorderLayout = 5 régió (CENTER, NORTH, SOUTH, EAST, WEST)
        getContentPane().setLayout(new BorderLayout());
        
        // Chat terület a KÖZÉPRE (CENTER) - ez tölti ki a legtöbb helyet
        // JScrollPane = görgetősáv automatikusan jelenik meg, ha sok üzenet van
        getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        // Alsó panel az ALJÁRA (SOUTH) - fix magasság
        getContentPane().add(bottom, BorderLayout.SOUTH);
        
        // ============================================================
        // 4. JOGOSULTSÁG ELLENŐRZÉS
        // ============================================================
        
        // Ellenőrzi, hogy a felhasználó küldhet-e üzenetet
        // Ha nincs joga, a küldés gomb és mező letiltva
        applySendPermission();
    }

    /**
     * ESEMÉNYKEZELŐK BEKÖTÉSE
     * 
     * Ez a metódus összeköti a GOMBOKAT és MEZŐKET az AKCIÓKKAL.
     * 
     * KAPCSOLATOK:
     * - sendButton kattintás → sendMessage()
     * - inputField Enter lenyomás → sendMessage()
     * 
     * LAMBDA KIFEJEZÉS MAGYARÁZAT:
     * e -> sendMessage()
     * 
     * Ez egy RÖVID VÁLTOZAT erre a hosszú kódra:
     * 
     * sendButton.addActionListener(new ActionListener() {
     *     public void actionPerformed(ActionEvent e) {
     *         sendMessage();
     *     }
     * });
     * 
     * A lambda EGYSZERŰBB és RÖVIDEBB!
     * e = ActionEvent objektum (esemény adatai)
     * -> = "akkor csináld ezt"
     * sendMessage() = a meghívandó metódus
     */
    private void bindEvents() {
        // Küldés gomb kattintáskor
        sendButton.addActionListener(e -> sendMessage());
        
        // Enter billentyű lenyomásakor a beviteli mezőben
        // (nem kell külön kattintani a gombra!)
        inputField.addActionListener(e -> sendMessage());
    }

    /**
     * ÜZENET KÜLDÉSE
     * 
     * Ez a metódus fut le, amikor a felhasználó KATTINT a Küldés gombra,
     * vagy megnyomja az ENTER billentyűt a beviteli mezőben.
     * 
     * LÉPÉSEK:
     * 1. Szöveg kiolvasása és trim (whitespace-ek eltávolítása)
     * 2. Üres szöveg ellenőrzés (ha üres, ne küldjön)
     * 3. Jogosultság ellenőrzés (lehet-e küldeni?)
     * 4. Üzenet küldése (sendInternal - alcsosztály implementálja)
     * 5. Ha sikeres: beviteli mező törlése és üzenetek frissítése
     * 6. Ha sikertelen: hibaüzenet megjelenítése
     * 
     * PÉLDA SIKERES KÜLDÉS:
     * Felhasználó beír: "  Helló!  "
     * 1. trim() után: "Helló!"
     * 2. Nem üres ✓
     * 3. Van joga küldeni ✓
     * 4. sendInternal("Helló!") → controller.sendMessage(...) → true
     * 5. Beviteli mező kiürül, chatArea frissül
     * 
     * PÉLDA SIKERTELEN KÜLDÉS:
     * Felhasználó nem tag a csoportban
     * 1-2. Szöveg OK
     * 3. Nincs joga küldeni ✗
     * 4. Popup ablak: "Nincs jogosultságod üzenetet küldeni!"
     */
    private void sendMessage() {
        // ============================================================
        // 1. SZÖVEG KIOLVASÁSA ÉS TISZTÍTÁSA
        // ============================================================
        
        // getText() = beviteli mező tartalma
        // trim() = whitespace-ek (szóköz, tab, újsor) eltávolítása elejéről/végéről
        String text = inputField.getText().trim();
        
        // ============================================================
        // 2. ÜRES SZÖVEG ELLENŐRZÉS
        // ============================================================
        
        // Ha csak whitespace-eket írt be, ne küldjön üres üzenetet
        if (text.isEmpty()) return;
        
        // ============================================================
        // 3. JOGOSULTSÁG ELLENŐRZÉS
        // ============================================================
        
        // canSendNow() = abstract metódus, az alcsosztály implementálja
        // - Privát chat: mindig true (mindig lehet küldeni)
        // - Csoport chat: ellenőrzi a GROUP_SEND_MESSAGE jogosultságot
        if (!canSendNow()) {
            // Hibaüzenet popup ablak
            JOptionPane.showMessageDialog(
                this,                              // Szülő ablak
                UiMessages.NO_PERM_SEND_GROUP,     // Üzenet szöveg
                UiMessages.WARN_TITLE,             // Ablak címe
                JOptionPane.WARNING_MESSAGE        // Figyelmeztetés ikon
            );
            return;
        }
        
        // ============================================================
        // 4. ÜZENET KÜLDÉSE
        // ============================================================
        
        // sendInternal() = abstract metódus, az alcsosztály implementálja
        // - PrivateChatWindow: controller.sendPrivateMessage(...)
        // - GroupChatWindow: controller.sendGroupMessage(...)
        // 
        // Visszatérési érték: boolean (sikeres-e a küldés)
        boolean ok = sendInternal(text);
        
        // ============================================================
        // 5. HIBA KEZELÉSE
        // ============================================================
        
        // Ha valami hiba történt (pl. nincs jogosultság, megszűnt a csoport)
        if (!ok) {
            JOptionPane.showMessageDialog(
                this, 
                UiMessages.SEND_FAILED,            // "Üzenet küldése sikertelen!"
                UiMessages.ERR_TITLE,              // "Hiba"
                JOptionPane.ERROR_MESSAGE          // Piros X ikon
            );
            return;
        }
        
        // ============================================================
        // 6. SIKERES KÜLDÉS UTÁN
        // ============================================================
        
        // Beviteli mező törlése (hogy új üzenetet lehessen írni)
        inputField.setText("");
        
        // Üzenetek újratöltése (hogy az új üzenet megjelenjen)
        reloadMessages();
    }

    /**
     * ÜZENETEK ÚJRATÖLTÉSE
     * 
     * Ez a metódus frissíti a chat területet az AKTUÁLIS üzenetekkel.
     * 
     * MIKOR HÍVÓDIK MEG?
     * - Az ablak megnyitásakor (első betöltés)
     * - Üzenet küldése után (hogy lásd az új üzenetedet)
     * - Élő frissítéskor (ha új üzenet érkezett)
     * - Jogosultság változáskor (ha valaki kikerült/bekerült a csoportba)
     * 
     * LÉPÉSEK:
     * 1. Üzenetek lekérése (fetchMessages - alcsosztály implementálja)
     * 2. Üzenetek renderelése ChatUi segítségével
     * 3. Üzenetszám eltárolása (élő frissítéshez)
     * 4. Jogosultság ellenőrzés (küldés gomb aktív/inaktív)
     * 
     * PÉLDA KIMENET (chatArea-ban):
     * 15:30 - Béla: Helló!
     * 15:31 - Anna: Szia! Hogy vagy?
     * 15:32 - Te: Jól, köszi!
     */
    protected void reloadMessages() {
        // 1. Üzenetek lekérése (abstract metódus, alcsosztály implementálja)
        List<Message> msgs = fetchMessages();
        
        // 2. Üzenetek renderelése - ID → név konverzióval
        // Lambda: minden üzenet feladó ID-jából megcsináljuk a nevet
        ChatUi.renderMessagesWithTime(chatArea, msgs, id -> resolveUser(id), me, "");
        
        // 3. Üzenetszám eltárolása (élő frissítéshez)
        lastCount = msgs.size();
        
        // 4. Jogosultság frissítése (küldés gomb aktív/inaktív)
        applySendPermission();
    }

    /**
     * ÉLŐ FRISSÍTÉS INDÍTÁSA
     * 
     * Ez a metódus elindít egy TIMER-t (időzítőt), ami PERIODIKUSAN
     * ellenőrzi, hogy érkezett-e új üzenet.
     * 
     * HOGYAN MŰKÖDIK?
     * 1. Létrehoz egy javax.swing.Timer objektumot
     * 2. Beállítja, hogy 1500 milliszekundum = 1.5 másodperc DELAY
     * 3. Minden 1.5 másodpercben lekéri az üzenetek számát
     * 4. Ha MEGVÁLTOZOTT a szám (új üzenet érkezett), FRISSÍT
     * 5. Ha NEM változott, csak a jogosultságot ellenőrzi
     * 6. Timer indítása
     * 7. WindowListener hozzáadása (ablak bezárásakor timer leállítása)
     * 
     * MIÉRT NEM MINDEN FRISSÍTÉSKOR TÖLTI ÚJRA?
     * Optimalizálás! Ha nincs új üzenet, felesleges újrarenderelni.
     * Csak akkor frissít, ha TÉNYLEG változott valami.
     * 
     * PÉLDA:
     * 0.0s - Ablak megnyílik, 5 üzenet van
     * 1.5s - Timer ellenőriz: még mindig 5 üzenet → NEM frissít, csak jogosultság
     * 3.0s - Timer ellenőriz: még mindig 5 üzenet → NEM frissít, csak jogosultság
     * 4.5s - Timer ellenőriz: MOST 6 üzenet van! → FRISSÍT (reloadMessages())
     * 6.0s - Timer ellenőriz: még mindig 6 üzenet → NEM frissít, csak jogosultság
     * ...
     * 
     * MIÉRT KELL EZ?
     * Mert ez egy OFFLINE chat alkalmazás, ami FÁJLBAN tárolja az adatokat.
     * Más felhasználók is módosíthatják a fájlt, ezért folyamatosan
     * ellenőrizni kell, hogy változott-e valami.
     * 
     * TIMER LEÁLLÍTÁS:
     * WindowListener figyeli, hogy bezárták-e az ablakot.
     * Ha igen, leállítja a timer-t (hogy ne fusson a háttérben feleslegesen).
     */
    private void startLive() {
        // ============================================================
        // 1. TIMER LÉTREHOZÁSA ÉS KONFIGURÁLÁSA
        // ============================================================
        
        // javax.swing.Timer paraméterek:
        // - 1500 = delay milliszekundumban (1.5 másodperc)
        // - e -> { ... } = lambda kifejezés, ez fut le minden 1.5 mp-ben
        liveTimer = new javax.swing.Timer(1500, e -> {
            // Üzenetek lekérése
            List<Message> msgs = fetchMessages();
            
            // Ha a méret megváltozott → teljes újratöltés
            if (msgs.size() != lastCount) {
                reloadMessages();
            } else {
                // Ha nem változott → csak jogosultság ellenőrzés
                // (pl. csoport jogosultságok változhatnak)
                applySendPermission();
            }
        });
        
        // Ismétlődő timer beállítása (minden 1.5 mp-ben újra lefut)
        liveTimer.setRepeats(true);
        
        // Timer indítása
        liveTimer.start();
        
        // ============================================================
        // 2. ABLAK BEZÁRÁS FIGYELÉSE (TIMER LEÁLLÍTÁS)
        // ============================================================
        
        // WindowAdapter = WindowListener egyszerűsített változata
        // Csak azokat a metódusokat kell override-olni, amik kellenek
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            // windowClosing = az ablak BEZÁRÁS ALATT van (X gomb megnyomva)
            @Override 
            public void windowClosing(java.awt.event.WindowEvent e) { 
                if (liveTimer != null) liveTimer.stop(); 
            }
            
            // windowClosed = az ablak MÁR BEZÁRULT (teljesen eltűnt)
            @Override 
            public void windowClosed(java.awt.event.WindowEvent e) { 
                if (liveTimer != null) liveTimer.stop(); 
            }
        });
    }

    /**
     * KÜLDÉS JOGOSULTSÁG ALKALMAZÁSA
     * 
     * Ez a metódus ellenőrzi, hogy a felhasználó KÜLDHET-E üzenetet,
     * és ennek megfelelően AKTIVÁLJA vagy DEAKTIVÁLJA a küldés gombot és mezőt.
     * 
     * LOGIKA:
     * - canSendNow() → true: Gomb és mező AKTÍV (lehet küldeni)
     * - canSendNow() → false: Gomb és mező INAKTÍV (NEM lehet küldeni)
     * 
     * MIKOR LEHET KÜLDENI?
     * - Privát chat: MINDIG (canSendNow() = true)
     * - Csoport chat: csak ha van GROUP_SEND_MESSAGE jogosultság
     * 
     * PÉLDA:
     * Felhasználó bekerül egy csoportba "Olvasó" szerepkörrel.
     * Az "Olvasó" szerepkör NEM rendelkezik GROUP_SEND_MESSAGE jogosultsággal.
     * Ekkor:
     * - canSendNow() → false
     * - sendButton.setEnabled(false) → gomb SZÜRKE, nem kattintható
     * - inputField.setEnabled(false) → mező SZÜRKE, nem lehet beleírni
     * 
     * Ha az admin megváltoztatja a szerepkört "Résztvevő"-re:
     * - applySendPermission() újra lefut (élő frissítéskor)
     * - canSendNow() → true
     * - sendButton.setEnabled(true) → gomb AKTÍV
     * - inputField.setEnabled(true) → mező AKTÍV
     */
    private void applySendPermission() {
        // canSendNow() = abstract metódus, alcsosztály implementálja
        boolean allowed = canSendNow();
        
        // Küldés gomb aktív/inaktív beállítása
        sendButton.setEnabled(allowed);
        
        // Beviteli mező aktív/inaktív beállítása
        inputField.setEnabled(allowed);
    }

    /**
     * FELHASZNÁLÓ NÉV FELOLDÁSA
     * 
     * Ez a helper metódus átalakítja a felhasználó UUID-ját (azonosítóját)
     * a FELHASZNÁLÓNÉVVÉ.
     * 
     * MIÉRT KELL EZ?
     * Az üzenetek csak a FELADÓ ID-JÁT tárolják (UUID),
     * de a UI-ban a NEVET akarjuk megjeleníteni.
     * 
     * PÉLDA:
     * Message objektum:
     * - senderId = UUID("a1b2c3...")
     * 
     * resolveUser(UUID("a1b2c3...")) → "Béla"
     * 
     * Chat területen megjelenik: "15:30 - Béla: Helló!"
     * 
     * METHOD REFERENCE:
     * A ChatUi.renderMessagesWithTime() paraméterként kapja ezt a metódust:
     * this::resolveUser
     * 
     * Ez egyenértékű ezzel a lambdával:
     * (UUID id) -> resolveUser(id)
     * 
     * @param id A felhasználó UUID azonosítója
     * @return A felhasználó neve (String)
     */
    protected String resolveUser(UUID id) {
        // Controller-ből kéri le a nevet az ID alapján
        return controller.getUsernameForId(id);
    }

    // ============================================================
    // ABSTRACT METÓDUSOK - ALCSOSZTÁLYOK IMPLEMENTÁLJÁK
    // ============================================================
    
    /**
     * ÜZENETEK LEKÉRÉSE
     * 
     * Ez az abstract metódus felelős az üzenetek listájának lekéréséért.
     * 
     * IMPLEMENTÁCIÓK:
     * - PrivateChatWindow: controller.getPrivateMessages(meId, friendId)
     * - GroupChatWindow: controller.getGroupMessages(groupId)
     * 
     * @return Az üzenetek listája időrendi sorrendben
     */
    protected abstract List<Message> fetchMessages();
    
    /**
     * KÜLDÉS JOGOSULTSÁG ELLENŐRZÉS
     * 
     * Ez az abstract metódus ellenőrzi, hogy MOST küldhet-e a felhasználó üzenetet.
     * 
     * IMPLEMENTÁCIÓK:
     * - PrivateChatWindow: return true; (mindig lehet küldeni)
     * - GroupChatWindow: return group.hasPermission(meId, GROUP_SEND_MESSAGE);
     * 
     * @return true = küldhet, false = nem küldhet
     */
    protected abstract boolean canSendNow();
    
    /**
     * ÜZENET KÜLDÉSE (INTERNAL)
     * 
     * Ez az abstract metódus felelős az üzenet tényleges elküldéséért.
     * 
     * IMPLEMENTÁCIÓK:
     * - PrivateChatWindow: controller.sendPrivateMessage(meId, friendId, text)
     * - GroupChatWindow: controller.sendGroupMessage(groupId, meId, text)
     * 
     * @param text Az üzenet szövege
     * @return true = sikeres küldés, false = hiba történt
     */
    protected abstract boolean sendInternal(String text);
}
