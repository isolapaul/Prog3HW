package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * Simple group chat window for a specific group.
 */
public class GroupChatWindow extends JFrame {
    private final transient AppController controller;
    private final UUID groupId;
    private final String me;

    private final JTextArea chatArea = new JTextArea(20, 50);
    private final JTextField inputField = new JTextField(36);
    private final JButton sendButton = new JButton("Küldés");
    private javax.swing.Timer liveTimer;
    private int lastCount = -1;

    public GroupChatWindow(AppController controller, UUID groupId, String me, String title) {
        super("Csoport: " + title);
        this.controller = controller;
        this.groupId = groupId;
        this.me = me;
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

        // Enable/disable sending based on permission
    boolean canSend = controller.hasGroupPermission(groupId, me, hu.prog3.offlinechatprog3.model.Permissions.GROUP_SEND_MESSAGE);
        sendButton.setEnabled(canSend);
        inputField.setEnabled(canSend);
    }

    private void bindEvents() {
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        boolean ok = controller.sendGroupMessage(groupId, me, text);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Nincs jogosultságod írni ebbe a csoportba.", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputField.setText("");
        loadMessages();
    }

    private void loadMessages() {
        List<Message> msgs = controller.getGroupMessages(groupId);
        ChatUi.renderMessagesWithTime(chatArea, msgs, controller::getUsernameForId, me, "");
        lastCount = msgs.size();
    }

    private void startLive() {
        liveTimer = new javax.swing.Timer(1500, e -> {
            List<Message> msgs = controller.getGroupMessages(groupId);
            if (msgs.size() != lastCount) loadMessages();
        });
        liveTimer.setRepeats(true);
        liveTimer.start();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { if (liveTimer!=null) liveTimer.stop(); }
            @Override public void windowClosed(java.awt.event.WindowEvent e) { if (liveTimer!=null) liveTimer.stop(); }
        });
    }
}
