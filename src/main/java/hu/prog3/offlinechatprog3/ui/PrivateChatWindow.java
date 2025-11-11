package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A simple private chat window between two users.
 * Shows past messages and allows sending new ones.
 */
public class PrivateChatWindow extends JFrame {

    private final transient AppController controller;
    private final String me;
    private final String other;

    private final JTextArea chatArea = new JTextArea(20, 50);
    private final JTextField inputField = new JTextField(36);
    private final JButton sendButton = new JButton("Küldés");
    private javax.swing.Timer liveTimer;
    private int lastCount = -1;

    public PrivateChatWindow(AppController controller, String me, String other) {
        super("Chat: " + me + " <--> " + other);
        this.controller = controller;
        this.me = me;
        this.other = other;
        initComponents();
        bindEvents();
        loadMessages();
        startLive();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        chatArea.setEditable(false);
        JPanel bottom = new JPanel();
        bottom.add(inputField);
        bottom.add(sendButton);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
    }

    private void bindEvents() {
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        boolean ok = controller.sendPrivateMessage(me, other, text);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Nem sikerült üzenetet küldeni.", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputField.setText("");
        loadMessages();
    }

    private void loadMessages() {
        chatArea.setText("");
        List<Message> msgs = controller.getPrivateMessages(me, other);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        for (Message m : msgs) {
            String who = controller.getUsernameForId(m.getSenderId());
            if (who == null) who = "?";
            String label = who.equals(me) ? "Én" : who;
            String time = m.getTimestamp() == null ? "" : fmt.format(m.getTimestamp());
            chatArea.append(String.format("[%s] %s: %s%n", time, label, m.getContent()));
        }
        // Auto-scroll to bottom so newest messages are visible
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        lastCount = msgs.size();
    }

    private void startLive() {
        liveTimer = new javax.swing.Timer(1500, e -> {
            List<Message> msgs = controller.getPrivateMessages(me, other);
            if (msgs.size() != lastCount) {
                loadMessages();
            }
        });
        liveTimer.setRepeats(true);
        liveTimer.start();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { if (liveTimer!=null) liveTimer.stop(); }
            @Override public void windowClosed(java.awt.event.WindowEvent e) { if (liveTimer!=null) liveTimer.stop(); }
        });
    }

    /** Called by MainFrame polling loop to refresh if window visible */
    public void refreshIfVisible() {
        if (!isVisible()) return;
        List<Message> msgs = controller.getPrivateMessages(me, other);
        if (msgs.size() != lastCount) {
            loadMessages();
        }
    }
}
