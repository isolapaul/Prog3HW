package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public abstract class BaseChatWindow extends JFrame {
    //controller
    protected final transient AppController controller;
    //saját felhasználó
    protected final String me;
    //chat terület
    protected final JTextArea chatArea = new JTextArea(20, 50);
    //beviteli mező
    protected final JTextField inputField = new JTextField(36);
    //küldés gomb
    protected final JButton sendButton = new JButton("Küldés");
    //élő frissítés timer
    private Timer liveTimer;
    //utolsó üzenetszám
    private int lastCount = -1;
    //konstruktor
    protected BaseChatWindow(AppController controller, String me, String title) {
        //cím beállítása
        super(title);
        //paraméterek
        this.controller = controller;
        this.me = me;
        //UI
        initComponents();
        //eseménykezelők
        bindEvents();
        //timer indítása
        startLive();
        //méretezés
        pack();
        //középre igazítás
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        //nem szerkeszthető a chat terület
        chatArea.setEditable(false);
        //alsó panel
        JPanel bottom = new JPanel();
        bottom.add(inputField);
        bottom.add(sendButton);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        getContentPane().add(bottom, BorderLayout.SOUTH);
        
        applySendPermission();
    }

    //események hozzárendelése
    private void bindEvents() {
        //küldés gomb kattintáskor
        sendButton.addActionListener(e -> sendMessage());
        
        //enter billentyű lenyomásakor a beviteli mezőben
        inputField.addActionListener(e -> sendMessage());
    }

    //üzenet küldése
    private void sendMessage() {
        //szöveg kiolvasása
        String text = inputField.getText().trim();
        //üres szöveg ellenőrzése
        if (text.isEmpty()) return;
        //jogosultság ellenőrzése
        if (!canSendNow()) {
            //hibaüzenet
            JOptionPane.showMessageDialog(
                this,                              //szülő ablak
                UiMessages.NO_PERM_SEND_GROUP,     //üzenet szövege
                UiMessages.WARN_TITLE,             //ablak címe
                JOptionPane.WARNING_MESSAGE        //figyelmeztetés ikon
            );
            return;
        }
        //üzenet küldése
        boolean ok = sendInternal(text);
        //hiba kezelés
        if (!ok) {
            JOptionPane.showMessageDialog(
                this, 
                UiMessages.SEND_FAILED,            
                UiMessages.ERR_TITLE,              
                JOptionPane.ERROR_MESSAGE          
            );
            return;
        }
        //beviteli mező ürítése
        inputField.setText("");
        
        //üzenetek újratöltése
        reloadMessages();
    }

    //üzenetek újratöltése
    protected void reloadMessages() {
        //üzenetek lekérése
        List<Message> msgs = fetchMessages();
        
        //üzenet renderelés
        ChatUi.renderMessagesWithTime(chatArea, msgs, this::resolveUser, me, "");
        
        //üzenetszám frissítése
        lastCount = msgs.size();
        
        //jogosultság frissítése
        applySendPermission();
    }

    //élő frissítés indítása
    private void startLive() {
        
        liveTimer = new Timer(1500, e -> {
            //üzenetek lekérése
            List<Message> msgs = fetchMessages();
            
            //ha megváltozott az üzenetszám, újratöltés
            if (msgs.size() != lastCount) {
                reloadMessages();
            } else {
                //jogosultság ellenőrzés
                applySendPermission();
            }
        });
        
        // ismétlődés beállítása
        liveTimer.setRepeats(true);
        
        //indítás
        liveTimer.start();
        
        //ablak bezárás eseménykezelő
        addWindowListener(new java.awt.event.WindowAdapter() {
            //ablak bezárva
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) { 
                if (liveTimer != null) liveTimer.stop(); 
            }
        });
    }

    //küldési jogosultság alkalmazása
    private void applySendPermission() {
        boolean allowed = canSendNow();
        //küldés gomb aktív/inaktív
        sendButton.setEnabled(allowed);
        //beviteli mező aktív/inaktív 
        inputField.setEnabled(allowed);
    }

    //felhaszálónév ID alapján
    protected String resolveUser(UUID id) {
        //controller függvénye
        return controller.getDataStore().getUsernameById(id);
    }
    //üzenetek lekérése
    protected abstract List<Message> fetchMessages();
    //küldési jogosultság ellenőrzése
    protected abstract boolean canSendNow();
    //üzenet küldése
    protected abstract boolean sendInternal(String text);
    //ezeket a metódusokat a leszármazott osztályok valósítják meg
}
