package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * Base chat window to remove duplication between private and group chats.
 * Subclasses implement hooks for fetching/sending and permission checks.
 */
public abstract class BaseChatWindow extends JFrame {
    protected final transient AppController controller;
    protected final String me;

    protected final JTextArea chatArea = new JTextArea(20, 50);
    protected final JTextField inputField = new JTextField(36);
    protected final JButton sendButton = new JButton("Küldés");
    private javax.swing.Timer liveTimer;
    private int lastCount = -1;

    protected BaseChatWindow(AppController controller, String me, String title) {
        super(title);
        this.controller = controller;
        this.me = me;
        initComponents();
        bindEvents();
        reloadMessages();
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
        applySendPermission();
    }

    private void bindEvents() {
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (!canSendNow()) {
            JOptionPane.showMessageDialog(this, UiMessages.NO_PERM_SEND_GROUP, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = sendInternal(text);
        if (!ok) {
            JOptionPane.showMessageDialog(this, UiMessages.SEND_FAILED, UiMessages.ERR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputField.setText("");
        reloadMessages();
    }

    protected void reloadMessages() {
        List<Message> msgs = fetchMessages();
        ChatUi.renderMessagesWithTime(chatArea, msgs, this::resolveUser, me, "");
        lastCount = msgs.size();
        applySendPermission();
    }

    private void startLive() {
        liveTimer = new javax.swing.Timer(1500, e -> {
            List<Message> msgs = fetchMessages();
            if (msgs.size() != lastCount) reloadMessages();
            else applySendPermission();
        });
        liveTimer.setRepeats(true);
        liveTimer.start();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { if (liveTimer!=null) liveTimer.stop(); }
            @Override public void windowClosed(java.awt.event.WindowEvent e) { if (liveTimer!=null) liveTimer.stop(); }
        });
    }

    private void applySendPermission() {
        boolean allowed = canSendNow();
        sendButton.setEnabled(allowed);
        inputField.setEnabled(allowed);
    }

    protected String resolveUser(UUID id) {
        return controller.getUsernameForId(id);
    }

    // Hooks for subclasses
    protected abstract List<Message> fetchMessages();
    protected abstract boolean canSendNow();
    protected abstract boolean sendInternal(String text);
}
