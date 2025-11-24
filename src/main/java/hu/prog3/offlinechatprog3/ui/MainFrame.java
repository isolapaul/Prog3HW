package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.persistence.DataStore;
import hu.prog3.offlinechatprog3.model.Permissions;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.awt.event.*;
import java.util.Comparator;



public class MainFrame extends JFrame {

    //controller és felhasználónév
    private final transient AppController controller;
    private final String username;

    //UI komponensek - barátok listája
    private final DefaultListModel<String> friendsModel = new DefaultListModel<>();
    private final JList<String> friendsList = new JList<>(friendsModel);

    //UI komponensek - csoportok listája
    private final DefaultListModel<GroupItem> groupsModel = new DefaultListModel<>();
    private final JList<GroupItem> groupsList = new JList<>(groupsModel);
    
    //csoport elem (ID + név páros)
    private static class GroupItem {
        final UUID id;
        final String name;
        GroupItem(UUID id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
    
    //fül rendszer
    private final JTabbedPane leftTabs = new JTabbedPane();
    
    //chat előnézet területe
    private final JTextArea chatArea = new JTextArea(20, 40);
    
    //üzenet beviteli mező és küldés gomb
    private final JTextField inputField = new JTextField(30);
    private final JButton sendButton = new JButton("Küldés");
    
    //barát műveleti gombok
    private final JButton addFriendButton = new JButton("Barát hozzáadása");
    private final JButton removeFriendButton = new JButton("Barát törlése");
    private final JButton openChatButton = new JButton("Chat megnyitása");
    
    //barátkérés műveleti gombok
    private final JButton viewRequestsButton = new JButton("Kérések");
    private final JButton outgoingRequestsButton = new JButton("Küldött kérések");
    
    //nyitott privát chat ablakok nyilvántartása
    private final Map<String, PrivateChatWindow> openChats = new HashMap<>();
    
    //barátkérések számának követése
    private int lastIncomingCount = -1;
    
    // előnézet frissítéshez (privát chat)
    private String lastPreviewFriend = null;
    private int lastPreviewCount = -1;
    
    //előnézet frissítéshez (csoport chat)
    private UUID lastPreviewGroupId = null;
    private int lastPreviewGroupCount = -1;

    //főablak inicializálása
    public MainFrame(AppController controller, String username) {
        super("Offline Chat - " + username);
        this.controller = controller;
        this.username = username;
        initMenu();           //menüsáv létrehozása
        initComponents();     //UI komponensek elrendezése
        bindEvents();         //event kezelők hozzárendelése
        refreshFriends();     //barátok listájának betöltése
        startLiveRefresh();   //automatikus frissítés indítása (1.5 mp)
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);  //ablak középre igazítása
    }

    //menüsáv létrehozása
    private void initMenu() {
        JMenuBar mb = new JMenuBar();
        
        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Mentés");
        JMenuItem exit = new JMenuItem("Kilépés");
        file.add(save);
        file.addSeparator();
        file.add(exit);
        mb.add(file);
    
        JMenu tools = new JMenu("Eszközök");
        JMenuItem groups = new JMenuItem("Csoportok");
        tools.add(groups);
        mb.add(tools);
        setJMenuBar(mb);

        //mentés gomb
        save.addActionListener(e -> controller.saveStore());
        
        //kilépés gomb
        exit.addActionListener(e -> System.exit(0));
        
        //csoportok menüpont
        groups.addActionListener(e -> {
            GroupManager gm = new GroupManager(MainFrame.this, controller, username);
            gm.setVisible(true);
        });
    }

    //UI komponensek elrendezése
    private void initComponents() {
        //bal oldali panel
        JPanel left = new JPanel(new BorderLayout());
        JPanel topLeft = new JPanel(new BorderLayout());
        topLeft.add(new JLabel("Kapcsolatok / Csoportok"), BorderLayout.NORTH);
        
        //műveleti gombok elrendezése
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new GridLayout(0,1,4,4));
        leftButtons.add(addFriendButton);
        leftButtons.add(removeFriendButton);
        leftButtons.add(openChatButton);
        leftButtons.add(viewRequestsButton);
        leftButtons.add(outgoingRequestsButton);
        topLeft.add(leftButtons, BorderLayout.SOUTH);
        left.add(topLeft, BorderLayout.NORTH);
        
        //fülrendszer
        leftTabs.addTab("Barátok", new JScrollPane(friendsList));
        leftTabs.addTab("Csoportok", new JScrollPane(groupsList));
        left.add(leftTabs, BorderLayout.CENTER);

        //jobb oldali panel
        JPanel right = new JPanel(new BorderLayout());
        chatArea.setEditable(false);  // Csak olvasható
        right.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        //input panel
        JPanel input = new JPanel();
        input.add(inputField);
        input.add(sendButton);
        right.add(input, BorderLayout.SOUTH);

        //split pane összeállítása (bal 200px, jobb rugalmas)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(200);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(split, BorderLayout.CENTER);
    }

    //kezelők hozzárendelése
    private void bindEvents() {
        bindFriendListEvents();         //barát lista kiválasztása
        bindGroupListEvents();          //csoport lista kiválasztása
        bindTabChangeEvents();          //fül váltás esemény
        bindOpenChatButton();           //chat megnyitása
        bindRequestButtons();           //kérések
        bindSendButton();               //küldés gomb
        bindAddRemoveFriendButtons();   //barát hozzáadása/törlése
    }

    //barát lista események
    private void bindFriendListEvents() {
        //chat előnézet betöltése
        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leftTabs.getSelectedIndex() == 0) {
                String friend = friendsList.getSelectedValue();
                if (friend != null) loadFriendConversation(friend);
            }
        });
        //dupla kattintás-chat ablak megnyitása
        friendsList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && leftTabs.getSelectedIndex() == 0) {
                    String friend = friendsList.getSelectedValue();
                    if (friend != null) openPrivateChat(friend);
                }
            }
        });
    }

    //csoport lista események
    private void bindGroupListEvents() {
        //csoport chat előnézet betöltése
        groupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leftTabs.getSelectedIndex() == 1) {
                GroupItem gi = groupsList.getSelectedValue();
                if (gi != null) loadGroupConversation(gi.id, gi.name);
            }
        });
        //dupla kattintás-csoport chat ablak megnyitása
        groupsList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && leftTabs.getSelectedIndex() == 1) {
                    GroupItem gi = groupsList.getSelectedValue();
                    if (gi != null) openGroupChat(gi);
                }
            }
        });
    }

    //fül váltás esemény
    private void bindTabChangeEvents() {
        leftTabs.addChangeListener(e -> {
            //barátok fülre váltás-barát chat betöltése
            if (leftTabs.getSelectedIndex() == 0) {
                String friend = friendsList.getSelectedValue();
                if (friend != null) loadFriendConversation(friend);
            } else {
                //csoportok fülre váltás-csoport chat betöltése
                GroupItem gi = groupsList.getSelectedValue();
                if (gi != null) loadGroupConversation(gi.id, gi.name);
            }
            //küldés gomb állapotának frissítése
            updateSendButtonEnabled();
        });
    }
    //chat megnyitása(gomb)
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
    //kérelmek megtekintése
    private void bindRequestButtons() {
        viewRequestsButton.addActionListener(e -> showIncomingRequests());
        outgoingRequestsButton.addActionListener(e -> showOutgoingRequests());
    }
    //küldés gomb
    private void bindSendButton() {
        sendButton.addActionListener(e -> handleSend());
    }
    //üzenet küldése
    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (leftTabs.getSelectedIndex() == 0) {
            sendToFriend(text);
        } else {
            sendToGroup(text);
        }
    }
    //privát üzenet küldése
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
    //csoport üzenet küldése
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
    //barát hozzáadás/törlés
    private void bindAddRemoveFriendButtons() {
        addFriendButton.addActionListener(e -> showAddFriendDialog());
        removeFriendButton.addActionListener(e -> removeSelectedFriend());
    }

    //automatikus frissítés indítása
    private void startLiveRefresh() {
        final Timer timer = new Timer(1500, e -> onTimerTick());
        timer.setRepeats(true);
        timer.start();
        //timer leállítása az ablak bezárásakor
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { timer.stop(); }
        });
    }

    //timer tick
    private void onTimerTick() {
        controller.reloadStore();              //adatok újratöltése fájlból
        refreshLists();                        //barát és csoport listák frissítése
        notifyIncomingRequestsIfNeeded();      //új barátkérés értesítés
        refreshOpenPrivateWindows();           //nyitott chat ablakok frissítése
        refreshPreviewAndButton();             //előnézet és gombok frissítése
    }

    //listák frissítése
    private void refreshLists() {
        refreshFriends();
        refreshGroups();
    }

    //barátkérés értesítés
    private void notifyIncomingRequestsIfNeeded() {
        Set<String> incoming = controller.getDataStore().getIncomingFriendRequests(username);
        if (incoming != null && incoming.size() != lastIncomingCount) {
            //ha nőtt a kérések száma, értesítés
            if (lastIncomingCount != -1 && incoming.size() > lastIncomingCount) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.this,
                        "Új barátkérés érkezett.", "Értesítés", JOptionPane.INFORMATION_MESSAGE));
            }
            lastIncomingCount = incoming.size();
        }
    }

    //nyitott privát chat ablakok frissítése
    private void refreshOpenPrivateWindows() {
        for (PrivateChatWindow w : openChats.values()) {
            w.refreshIfVisible();
        }
    }

    //chat előnézet frissítése
    private void refreshPreviewAndButton() {
        //privát chat előnézet frissítése
        if (leftTabs.getSelectedIndex() == 0) {
            String sel = friendsList.getSelectedValue();
            if (sel != null) {
                List<Message> msgs = controller.getDataStore().getPrivateMessages(username, sel);
                //csak akkor frissít, ha változott a barát vagy üzenetek száma
                if (!sel.equals(lastPreviewFriend) || msgs.size() != lastPreviewCount) {
                    lastPreviewFriend = sel;
                    lastPreviewCount = msgs.size();
                    loadFriendConversation(sel);
                }
            }
        } else {
            //csoport chat előnézet frissítése
            GroupItem gi = groupsList.getSelectedValue();
            if (gi != null) {
                List<Message> msgs = controller.getDataStore().getGroupMessages(gi.id);
                //csak akkor frissít, ha változott a csoport vagy üzenetek száma
                if (lastPreviewGroupId == null || !gi.id.equals(lastPreviewGroupId) || msgs.size() != lastPreviewGroupCount) {
                    lastPreviewGroupId = gi.id;
                    lastPreviewGroupCount = msgs.size();
                    loadGroupConversation(gi.id, gi.name);
                }
            }
        }
        //küldés gomb állapotának frissítése
        updateSendButtonEnabled();
    }

    private void showAddFriendDialog() {
        DataStore store = controller.getDataStore();
        Set<String> all = store.getAllUsernames();
        Set<String> existing = store.getFriends(username);
        List<String> choices = new ArrayList<>();
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
            boolean ok = controller.getDataStore().sendFriendRequest(username, selected);
            controller.saveStore();
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
            boolean ok = controller.getDataStore().removeFriend(username, sel);
            controller.saveStore();
            if (!ok) JOptionPane.showMessageDialog(MainFrame.this, "Nem sikerült eltávolítani a barátot.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else refreshFriends();
        }
    }

    private void openPrivateChat(String friend) {
        if (friend == null || friend.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nincs kiválasztva barát.", "Hiba", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PrivateChatWindow win = openChats.get(friend);
        if (win != null) {
            win.toFront();
            win.requestFocus();
            return;
        }
        try {
            PrivateChatWindow pcw = new PrivateChatWindow(controller, username, friend);
            pcw.setVisible(true);
            openChats.put(friend, pcw);
            pcw.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    openChats.remove(friend);
                }
                @Override
                public void windowClosing(WindowEvent e) {
                    openChats.remove(friend);
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hiba a chat ablak megnyitásakor: " + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showIncomingRequests() {
        DataStore store = controller.getDataStore();
        Set<String> incoming = store.getIncomingFriendRequests(username);
        if (incoming.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.this, "Nincsenek bejövő kérésed.", "Kérések", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
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
            boolean ok = store.acceptFriendRequest(username, sel);
            controller.saveStore();
            if (!ok) JOptionPane.showMessageDialog(d, "Elfogadás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else {
                model.removeElement(sel);
                refreshFriends();
            }
        });

        reject.addActionListener(ev -> {
            String sel = reqList.getSelectedValue();
            if (sel == null) return;
            boolean ok = store.rejectFriendRequest(username, sel);
            controller.saveStore();
            if (!ok) JOptionPane.showMessageDialog(d, "Elutasítás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else model.removeElement(sel);
        });

        d.setVisible(true);
    }

    private void showOutgoingRequests() {
        DataStore store = controller.getDataStore();
        Set<String> outgoing = store.getOutgoingFriendRequests(username);
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
            boolean ok = store.cancelOutgoingFriendRequest(username, sel);
            controller.saveStore();
            if (!ok) JOptionPane.showMessageDialog(d, "Visszavonás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else model.removeElement(sel);
        });

        d.setVisible(true);
    }

    //barát chat előnézet betöltése és megjelenítése
    private void loadFriendConversation(String friend) {
        DataStore store = controller.getDataStore();
        List<Message> msgs = store.getPrivateMessages(username, friend);
        ChatUi.renderMessages(chatArea, msgs, store::getUsernameById, "");
    }

    //csoport chat előnézet betöltése és megjelenítése
    private void loadGroupConversation(UUID groupId, String groupName) {
        DataStore store = controller.getDataStore();
        List<Message> msgs = store.getGroupMessages(groupId);
        ChatUi.renderMessages(chatArea, msgs, store::getUsernameById, "[" + groupName + "] ");
    }

    //barát lista frissítése
    private void refreshFriends() {
        Set<String> friends = controller.getDataStore().getFriends(username);
        if (friends == null) return;

        //rendezés abc sorrendbe
        List<String> sorted = new ArrayList<>(friends);
        Collections.sort(sorted);
        
        //változás detektálása
        boolean changed = sorted.size() != friendsModel.size();
        if (!changed) {
            for (int i = 0; i < sorted.size(); i++) {
                if (!sorted.get(i).equals(friendsModel.get(i))) { changed = true; break; }
            }
        }
        
        //ha változott, frissítés
        if (changed) {
            String selected = friendsList.getSelectedValue();
            friendsModel.clear();
            for (String f : sorted) friendsModel.addElement(f);
            if (selected != null) friendsList.setSelectedValue(selected, true);
        }
    }

    //csoport lista frissítése
    private void refreshGroups() {
        Map<UUID, String> groups = controller.getDataStore().getAllGroups();
        if (groups == null) return;
        
        //kiválasztás megőrzése
        GroupItem selected = groupsList.getSelectedValue();
        groupsModel.clear();
        
        //szűrés: csak azok a csoportok, ahol tag a felhasználó
        List<GroupItem> items = new ArrayList<>();
        for (Map.Entry<UUID, String> e : groups.entrySet()) {
            Set<String> members = controller.getGroupMembers(e.getKey());
            if (members != null && members.contains(username)) {
                items.add(new GroupItem(e.getKey(), e.getValue()));
            }
        }
        
        //rendezés abc sorrendbee
        items.sort(Comparator.comparing(a -> a.name.toLowerCase()));
        for (GroupItem gi : items) groupsModel.addElement(gi);
        if (selected != null) groupsList.setSelectedValue(selected, true);
    }
    //csoport chat ablak megnyitása
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

    //küldés gomb engedélyezése/tiltása
    private void updateSendButtonEnabled() {
        if (leftTabs.getSelectedIndex() == 0) {
            //barát chat: engedélyezve ha van kiválasztott barát
            sendButton.setEnabled(friendsList.getSelectedValue() != null);
        } else {
            //csoport chat: engedélyezve ha van GROUP_SEND_MESSAGE jogosultság
            GroupItem gi = groupsList.getSelectedValue();
            boolean enabled = gi != null && controller.hasGroupPermission(gi.id, username, Permissions.GROUP_SEND_MESSAGE);
            sendButton.setEnabled(enabled);
        }
    }

    //segédfüggvény-kiválasztott barát ellenőrzése
    private String requireSelectedFriend() {
        String friend = friendsList.getSelectedValue();
        if (friend == null) {
            JOptionPane.showMessageDialog(this, UiMessages.SELECT_FRIEND, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return friend;
    }

    //segédfüggvény-kiválasztott csoport ellenőrzése
    private GroupItem requireSelectedGroup() {
        GroupItem gi = groupsList.getSelectedValue();
        if (gi == null) {
            JOptionPane.showMessageDialog(this, UiMessages.SELECT_GROUP, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return gi;
    }
}
