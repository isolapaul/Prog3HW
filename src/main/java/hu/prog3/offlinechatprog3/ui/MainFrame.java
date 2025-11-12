package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.awt.*;
// action event imports not needed (using lambdas)
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/**
 * Main application frame shown after successful login.
 *
 * It shows a list of friends (left) and a chat panel (right) for private conversations.
 * This minimal skeleton demonstrates the required Swing widgets (JMenu, JComboBox/JList/JTable
 * could be used — here we use JList for simplicity). The code is heavily commented to help
 * you understand and extend it.
 */
public class MainFrame extends JFrame {

    private final transient AppController controller;
    private final String username;

    // UI components
    private final DefaultListModel<String> friendsModel = new DefaultListModel<>();
    private final JList<String> friendsList = new JList<>(friendsModel);
    private static class GroupItem {
        final UUID id;
        final String name;
        GroupItem(UUID id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
    private final DefaultListModel<GroupItem> groupsModel = new DefaultListModel<>();
    private final JList<GroupItem> groupsList = new JList<>(groupsModel);
    private final JTabbedPane leftTabs = new JTabbedPane();
    private final JTextArea chatArea = new JTextArea(20, 40);
    private final JTextField inputField = new JTextField(30);
    private final JButton sendButton = new JButton("Küldés");
    private final JButton addFriendButton = new JButton("Barát hozzáadása");
    private final JButton removeFriendButton = new JButton("Barát törlése");
    private final JButton openChatButton = new JButton("Chat megnyitása");
    private final JButton viewRequestsButton = new JButton("Kérések");
    private final JButton outgoingRequestsButton = new JButton("Küldött kérések");
    private final java.util.Map<String, PrivateChatWindow> openChats = new java.util.HashMap<>();
    private int lastIncomingCount = -1;
    private String lastPreviewFriend = null;
    private int lastPreviewCount = -1;
    private UUID lastPreviewGroupId = null;
    private int lastPreviewGroupCount = -1;

    public MainFrame(AppController controller, String username) {
        super("Offline Chat - " + username);
        this.controller = controller;
        this.username = username;
        initMenu();
        initComponents();
        bindEvents();
        refreshFriends();
        startLiveRefresh();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void initMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Mentés");
        JMenuItem exit = new JMenuItem("Kilépés");
        file.add(save);
        file.addSeparator();
        file.add(exit);
        mb.add(file);
        // Add a Groups menu entry
        JMenu tools = new JMenu("Eszközök");
        JMenuItem groups = new JMenuItem("Csoportok");
        tools.add(groups);
        mb.add(tools);
        setJMenuBar(mb);

        save.addActionListener(e -> controller.saveStore());
        exit.addActionListener(e -> System.exit(0));
        groups.addActionListener(e -> {
            GroupManager gm = new GroupManager(MainFrame.this, controller, username);
            gm.setVisible(true);
        });
    }

    private void initComponents() {
        JPanel left = new JPanel(new BorderLayout());
        JPanel topLeft = new JPanel(new BorderLayout());
        topLeft.add(new JLabel("Kapcsolatok / Csoportok"), BorderLayout.NORTH);
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new GridLayout(0,1,4,4));
        leftButtons.add(addFriendButton);
        leftButtons.add(removeFriendButton);
        leftButtons.add(openChatButton);
        leftButtons.add(viewRequestsButton);
        leftButtons.add(outgoingRequestsButton);
        topLeft.add(leftButtons, BorderLayout.SOUTH);
        left.add(topLeft, BorderLayout.NORTH);
        // Tabs: Friends and Groups
        leftTabs.addTab("Barátok", new JScrollPane(friendsList));
        leftTabs.addTab("Csoportok", new JScrollPane(groupsList));
        left.add(leftTabs, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        chatArea.setEditable(false);
        right.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel input = new JPanel();
        input.add(inputField);
        input.add(sendButton);
        right.add(input, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(200);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(split, BorderLayout.CENTER);
    }

    private void bindEvents() {
        bindFriendListEvents();
        bindGroupListEvents();
        bindTabChangeEvents();
        bindOpenChatButton();
        bindRequestButtons();
        bindSendButton();
        bindAddRemoveFriendButtons();
    }

    private void bindFriendListEvents() {
        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leftTabs.getSelectedIndex() == 0) {
                String friend = friendsList.getSelectedValue();
                if (friend != null) loadFriendConversation(friend);
            }
        });
        friendsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && leftTabs.getSelectedIndex() == 0) {
                    String friend = friendsList.getSelectedValue();
                    if (friend != null) openPrivateChat(friend);
                }
            }
        });
    }

    private void bindGroupListEvents() {
        groupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leftTabs.getSelectedIndex() == 1) {
                GroupItem gi = groupsList.getSelectedValue();
                if (gi != null) loadGroupConversation(gi.id, gi.name);
            }
        });
        groupsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && leftTabs.getSelectedIndex() == 1) {
                    GroupItem gi = groupsList.getSelectedValue();
                    if (gi != null) openGroupChat(gi);
                }
            }
        });
    }

    private void bindTabChangeEvents() {
        leftTabs.addChangeListener(e -> {
            if (leftTabs.getSelectedIndex() == 0) {
                String friend = friendsList.getSelectedValue();
                if (friend != null) loadFriendConversation(friend);
            } else {
                GroupItem gi = groupsList.getSelectedValue();
                if (gi != null) loadGroupConversation(gi.id, gi.name);
            }
            updateSendButtonEnabled();
        });
    }

    private void bindOpenChatButton() {
        openChatButton.addActionListener(e -> {
            if (leftTabs.getSelectedIndex() == 0) {
                String friend = friendsList.getSelectedValue();
                if (friend == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, UiMessages.SELECT_FRIEND, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
                    return;
                }
                openPrivateChat(friend);
            } else {
                GroupItem gi = groupsList.getSelectedValue();
                if (gi == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Válassz egy csoportot a listából.", "Hiba", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                openGroupChat(gi);
            }
        });
    }

    private void bindRequestButtons() {
        viewRequestsButton.addActionListener(e -> showIncomingRequests());
        outgoingRequestsButton.addActionListener(e -> showOutgoingRequests());
    }

    private void bindSendButton() {
        sendButton.addActionListener(e -> handleSend());
    }

    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (leftTabs.getSelectedIndex() == 0) {
            sendToFriend(text);
        } else {
            sendToGroup(text);
        }
    }

    private void sendToFriend(String text) {
        String friend = requireSelectedFriend();
        if (friend == null) return;
        boolean ok = controller.sendPrivateMessage(username, friend, text);
        if (!ok) {
            JOptionPane.showMessageDialog(MainFrame.this, "Nem sikerült üzenetet küldeni. Ellenőrizd, hogy barátok vagytok.", "Hiba", JOptionPane.ERROR_MESSAGE);
        } else {
            inputField.setText("");
            loadFriendConversation(friend);
        }
    }

    private void sendToGroup(String text) {
        GroupItem gi = requireSelectedGroup();
        if (gi == null) return;
                if (!controller.hasGroupPermission(gi.id, username, hu.prog3.offlinechatprog3.model.Permissions.GROUP_SEND_MESSAGE)) {
                    JOptionPane.showMessageDialog(MainFrame.this, UiMessages.NO_PERM_SEND_GROUP, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = controller.sendGroupMessage(gi.id, username, text);
        if (!ok) {
                    JOptionPane.showMessageDialog(MainFrame.this, UiMessages.SEND_FAILED, UiMessages.ERR_TITLE, JOptionPane.ERROR_MESSAGE);
        } else {
            inputField.setText("");
            loadGroupConversation(gi.id, gi.name);
        }
    }

    private void bindAddRemoveFriendButtons() {
        addFriendButton.addActionListener(e -> showAddFriendDialog());
        removeFriendButton.addActionListener(e -> removeSelectedFriend());
    }

    private void startLiveRefresh() {
        final javax.swing.Timer timer = new javax.swing.Timer(1500, e -> onTimerTick());
        timer.setRepeats(true);
        timer.start();
        // stop when window closes
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { timer.stop(); }
            @Override public void windowClosed(java.awt.event.WindowEvent e) { timer.stop(); }
        });
    }

    private void onTimerTick() {
        try {
            refreshLists();
            notifyIncomingRequestsIfNeeded();
            refreshOpenPrivateWindows();
            refreshPreviewAndButton();
        } catch (Exception ignored) {
            // Swallow transient UI/persistence exceptions during polling; next tick will re-try.
        }
    }

    private void refreshLists() {
        refreshFriends();
        refreshGroups();
    }

    private void notifyIncomingRequestsIfNeeded() {
        java.util.Set<String> incoming = controller.getIncomingFriendRequests(username);
        if (incoming != null && incoming.size() != lastIncomingCount) {
            if (lastIncomingCount != -1 && incoming.size() > lastIncomingCount) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.this,
                        "Új barátkérés érkezett.", "Értesítés", JOptionPane.INFORMATION_MESSAGE));
            }
            lastIncomingCount = incoming.size();
        }
    }

    private void refreshOpenPrivateWindows() {
        for (PrivateChatWindow w : openChats.values()) {
            w.refreshIfVisible();
        }
    }

    private void refreshPreviewAndButton() {
        if (leftTabs.getSelectedIndex() == 0) {
            String sel = friendsList.getSelectedValue();
            if (sel != null) {
                java.util.List<Message> msgs = controller.getPrivateMessages(username, sel);
                if (!sel.equals(lastPreviewFriend) || msgs.size() != lastPreviewCount) {
                    lastPreviewFriend = sel;
                    lastPreviewCount = msgs.size();
                    loadFriendConversation(sel);
                }
            }
        } else {
            GroupItem gi = groupsList.getSelectedValue();
            if (gi != null) {
                java.util.List<Message> msgs = controller.getGroupMessages(gi.id);
                if (lastPreviewGroupId == null || !gi.id.equals(lastPreviewGroupId) || msgs.size() != lastPreviewGroupCount) {
                    lastPreviewGroupId = gi.id;
                    lastPreviewGroupCount = msgs.size();
                    loadGroupConversation(gi.id, gi.name);
                }
            }
        }
        updateSendButtonEnabled();
    }

    private void showAddFriendDialog() {
        java.util.Set<String> all = controller.getAllUsernames();
        java.util.Set<String> existing = controller.getFriendsOf(username);
        java.util.List<String> choices = new java.util.ArrayList<>();
        for (String u : all) {
            if (!u.equals(username) && !existing.contains(u)) {
                choices.add(u);
            }
        }
        if (choices.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "Nincsenek elérhető felhasználók hozzáadáshoz.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String selected = (String) JOptionPane.showInputDialog(MainFrame.this, "Válassz felhasználót:", "Barát hozzáadása", JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), choices.get(0));
        if (selected != null) {
            // send a friend request rather than immediately adding
            boolean ok = controller.sendFriendRequest(username, selected);
            if (!ok) JOptionPane.showMessageDialog(MainFrame.this, "A kérés elküldése sikertelen vagy már létezik.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else JOptionPane.showMessageDialog(MainFrame.this, "Barátkérés elküldve.", "Siker", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void removeSelectedFriend() {
        String sel = friendsList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(MainFrame.this, "Válassz egy törölni kívánt barátot.", "Hiba", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(MainFrame.this, "Tényleg törölni szeretnéd a barátot?", "Megerősítés", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = controller.removeFriend(username, sel);
            if (!ok) JOptionPane.showMessageDialog(MainFrame.this, "Nem sikerült eltávolítani a barátot.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else refreshFriends();
        }
    }

    private void openPrivateChat(String friend) {
        // Null vagy üres string ellenőrzés
        if (friend == null || friend.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nincs kiválasztva barát.", "Hiba", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // If there is an open chat window, bring it to front
        PrivateChatWindow win = openChats.get(friend);
        if (win != null) {
            win.toFront();
            win.requestFocus();
            return;
        }
        // Create and show a new chat window
        try {
            PrivateChatWindow pcw = new PrivateChatWindow(controller, username, friend);
            pcw.setVisible(true);
            openChats.put(friend, pcw);
            // remove from map when closed
            pcw.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    openChats.remove(friend);
                }
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    openChats.remove(friend);
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hiba a chat ablak megnyitásakor: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showIncomingRequests() {
        java.util.Set<String> incoming = controller.getIncomingFriendRequests(username);
        if (incoming.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "Nincsenek bejövő kérésed.", "Kérések", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Build a simple panel listing requests with Accept/Reject buttons
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String r : incoming) model.addElement(r);
        JList<String> reqList = new JList<>(model);
        panel.add(new JScrollPane(reqList), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton accept = new JButton("Elfogad");
        JButton reject = new JButton("Elutasít");
        buttons.add(accept);
        buttons.add(reject);
        panel.add(buttons, BorderLayout.SOUTH);

        JDialog d = new JDialog(this, "Bejövő barát kérések", true);
        d.getContentPane().add(panel);
        d.pack();
        d.setLocationRelativeTo(this);

        accept.addActionListener(ev -> {
            String sel = reqList.getSelectedValue();
            if (sel == null) return;
            boolean ok = controller.acceptFriendRequest(username, sel);
            if (!ok) JOptionPane.showMessageDialog(d, "Elfogadás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else {
                model.removeElement(sel);
                refreshFriends();
            }
        });

        reject.addActionListener(ev -> {
            String sel = reqList.getSelectedValue();
            if (sel == null) return;
            boolean ok = controller.rejectFriendRequest(username, sel);
            if (!ok) JOptionPane.showMessageDialog(d, "Elutasítás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else model.removeElement(sel);
        });

        d.setVisible(true);
    }

    private void showOutgoingRequests() {
        java.util.Set<String> outgoing = controller.getOutgoingFriendRequests(username);
        if (outgoing.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "Nincsenek küldött kéréseid.", "Küldött kérések", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String s : outgoing) model.addElement(s);
        JList<String> list = new JList<>(model);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        JButton cancel = new JButton("Visszavonás");
        btns.add(cancel);
        p.add(btns, BorderLayout.SOUTH);

        JDialog d = new JDialog(this, "Küldött barát kérések", true);
        d.getContentPane().add(p);
        d.pack();
        d.setLocationRelativeTo(this);

        cancel.addActionListener(ev -> {
            String sel = list.getSelectedValue();
            if (sel == null) return;
            boolean ok = controller.cancelOutgoingFriendRequest(username, sel);
            if (!ok) JOptionPane.showMessageDialog(d, "Visszavonás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else model.removeElement(sel);
        });

        d.setVisible(true);
    }

    // Load conversation with a friend and show messages in the chat area
    private void loadFriendConversation(String friend) {
        List<Message> msgs = controller.getPrivateMessages(username, friend);
        ChatUi.renderMessagesSimple(chatArea, msgs, controller::getUsernameForId, "");
    }

    // Load conversation with a group and show messages in the chat area
    private void loadGroupConversation(UUID groupId, String groupName) {
        List<Message> msgs = controller.getGroupMessages(groupId);
        ChatUi.renderMessagesSimple(chatArea, msgs, controller::getUsernameForId, "[" + groupName + "] ");
    }

    // Refresh the friends list from controller
    private void refreshFriends() {
        // Rebuild model if different; avoid flicker when no change
        Set<String> friends = controller.getFriendsOf(username);
        if (friends == null) return;
        java.util.List<String> sorted = new java.util.ArrayList<>(friends);
        java.util.Collections.sort(sorted);
        boolean changed = sorted.size() != friendsModel.size();
        if (!changed) {
            for (int i = 0; i < sorted.size(); i++) {
                if (!sorted.get(i).equals(friendsModel.get(i))) { changed = true; break; }
            }
        }
        if (changed) {
            String selected = friendsList.getSelectedValue();
            friendsModel.clear();
            for (String f : sorted) friendsModel.addElement(f);
            if (selected != null) friendsList.setSelectedValue(selected, true);
        }
    }

    // Refresh the groups list (only those where the user is a member)
    private void refreshGroups() {
        Map<UUID, String> groups = controller.getAllGroups();
        if (groups == null) return;
        GroupItem selected = groupsList.getSelectedValue();
        groupsModel.clear();
        java.util.List<GroupItem> items = new java.util.ArrayList<>();
        for (Map.Entry<UUID, String> e : groups.entrySet()) {
            java.util.Set<String> members = controller.getGroupMembers(e.getKey());
            if (members != null && members.contains(username)) {
                items.add(new GroupItem(e.getKey(), e.getValue()));
            }
        }
        items.sort(java.util.Comparator.comparing(a -> a.name.toLowerCase()));
        for (GroupItem gi : items) groupsModel.addElement(gi);
        if (selected != null) groupsList.setSelectedValue(selected, true);
    }

    private void openGroupChat(GroupItem gi) {
        if (gi == null) {
            JOptionPane.showMessageDialog(this, "Nincs kiválasztva csoport.", "Hiba", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            GroupChatWindow win = new GroupChatWindow(controller, gi.id, username, gi.name);
            win.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hiba a csoport chat ablak megnyitásakor: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateSendButtonEnabled() {
        if (leftTabs.getSelectedIndex() == 0) {
            sendButton.setEnabled(friendsList.getSelectedValue() != null);
        } else {
            GroupItem gi = groupsList.getSelectedValue();
            boolean enabled = gi != null && controller.hasGroupPermission(gi.id, username, hu.prog3.offlinechatprog3.model.Permissions.GROUP_SEND_MESSAGE);
            sendButton.setEnabled(enabled);
        }
    }

    // Small helpers to reduce duplication when requiring a selection
    private String requireSelectedFriend() {
        String friend = friendsList.getSelectedValue();
        if (friend == null) {
            JOptionPane.showMessageDialog(this, UiMessages.SELECT_FRIEND, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return friend;
    }

    private GroupItem requireSelectedGroup() {
        GroupItem gi = groupsList.getSelectedValue();
        if (gi == null) {
            JOptionPane.showMessageDialog(this, UiMessages.SELECT_GROUP, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return gi;
    }
}
