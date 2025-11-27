package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Permissions;
import hu.prog3.offlinechatprog3.persistence.DataStore;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * Csoportkezelő ablak - csoportok listázása, létrehozása, tagok és szerepkörök kezelése.
 */
public class GroupManager extends JDialog {

    private final transient AppController controller;
    private final String username;

    /**
     * Csoport listában szereplő elem.
     */
    private static class GroupItem {
        final UUID id;
        final String name;
        GroupItem(UUID id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
    private final DefaultListModel<GroupItem> groupsModel = new DefaultListModel<>();
    private final JList<GroupItem> groupsList = new JList<>(groupsModel);
    private static final String SELECT_GROUP_MSG = "Válassz egy csoportot.";

    /**
     * Csoportkezelő ablak konstruktor.
     * @param owner szülő ablak
     * @param controller MVC controller
     * @param username aktuális felhasználó
     */
    public GroupManager(Frame owner, AppController controller, String username) {
        super(owner, "Csoportok", true);
        this.controller = controller;
        this.username = username;
        initComponents();
        loadGroups();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout());
        main.add(new JScrollPane(groupsList), BorderLayout.CENTER);

    JPanel buttons = new JPanel();
    JButton create = new JButton("Új csoport");
    JButton view = new JButton("Tagok");
    JButton openChat = new JButton("Chat megnyitása");
    JButton manageMessages = new JButton("Üzenetek...");
    JButton delete = new JButton("Csoport törlése");
    buttons.add(create);
    buttons.add(view);
    buttons.add(openChat);
    buttons.add(manageMessages);
    buttons.add(delete);
    main.add(buttons, BorderLayout.SOUTH);

        create.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Csoport neve:");
            if (name == null || name.isBlank()) return;
            UUID id = controller.createGroup(name, username);
            if (id == null) JOptionPane.showMessageDialog(this, "Csoport létrehozása sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else loadGroups();
        });

    view.addActionListener(e -> showMembersDialog());
    openChat.addActionListener(e -> openSelectedGroupChat());
    manageMessages.addActionListener(e -> showMessagesDialog());
    delete.addActionListener(e -> deleteSelectedGroup());

        setContentPane(main);
    }

    /**
     * Csoport üzeneteinek megjelenítése és törlése.
     */
    private void showMessagesDialog() {
        GroupItem sel = requireSelectedGroup();
        if (sel == null) return;
        if (!controller.hasGroupPermission(sel.id, username, hu.prog3.offlinechatprog3.model.Permissions.GROUP_DELETE_MESSAGES)) {
            JOptionPane.showMessageDialog(this, UiMessages.NO_PERM_DELETE_MSG, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        DataStore store = controller.getDataStore();
        List<hu.prog3.offlinechatprog3.model.Message> msgs = store.getGroupMessages(sel.id);
        DefaultListModel<MessageItem> model = new DefaultListModel<>();
        for (hu.prog3.offlinechatprog3.model.Message m : msgs) {
            model.addElement(new MessageItem(m, store.getUsernameById(m.getSenderId())));
        }
        JList<MessageItem> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(list);
        JPanel btns = new JPanel();
        JButton deleteBtn = new JButton("Kijelölt törlése");
        btns.add(deleteBtn);
        JPanel p = new JPanel(new BorderLayout());
        p.add(scroll, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        JDialog d = new JDialog(this, "Csoport üzenetek", true);
        d.getContentPane().add(p);
        d.setSize(600, 400);
        d.setLocationRelativeTo(this);

        deleteBtn.addActionListener(ev -> {
            MessageItem mi = list.getSelectedValue();
            if (mi == null) return;
            int c = JOptionPane.showConfirmDialog(d, "Biztosan törlöd ezt az üzenetet?", "Megerősítés", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;
            boolean ok = controller.deleteGroupMessage(sel.id, mi.id, username);
            if (!ok) {
                JOptionPane.showMessageDialog(d, "Törlés sikertelen vagy nincs jogosultság.", "Hiba", JOptionPane.ERROR_MESSAGE);
            } else {
                model.removeElement(mi);
            }
        });

        d.setVisible(true);
    }

    /**
     * Üzenet listában szereplő elem.
     */
    private static class MessageItem {
        final UUID id;
        final String display;
        MessageItem(hu.prog3.offlinechatprog3.model.Message m, String sender) {
            this.id = m.getId();
            String who = sender == null ? "?" : sender;
            String ts = m.getTimestamp() == null ? "" : java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault()).format(m.getTimestamp());
            String content = m.getContent() == null ? "" : m.getContent();
            if (content.length() > 60) content = content.substring(0, 60) + "…";
            this.display = String.format("[%s] %s: %s", ts, who, content);
        }
        @Override public String toString() { return display; }
    }

    /**
     * Csoportok betöltése a listába.
     */
    private void loadGroups() {
        groupsModel.clear();
        Map<UUID, String> groups = controller.getDataStore().getAllGroups();
        for (Map.Entry<UUID, String> e : groups.entrySet()) {
            groupsModel.addElement(new GroupItem(e.getKey(), e.getValue()));
        }
    }

    /**
     * Csoport tagok megjelenítése és kezelése.
     */
    private void showMembersDialog() {
        GroupItem sel = requireSelectedGroup();
        if (sel == null) return;
        UUID id = sel.id;

        DefaultListModel<String> model = new DefaultListModel<>();
        refreshMemberList(model, id);  // Szerepkörökkel együtt tölti
        JList<String> list = new JList<>(model);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        JButton add = new JButton("Hozzáad");
        JButton remove = new JButton("Eltávolít");
        JButton addRole = new JButton("Szerep hozzáadása");
        JButton changeRole = new JButton("Szerep módosítása");
        btns.add(changeRole);
        btns.add(add);
        btns.add(remove);
        btns.add(addRole);
        p.add(btns, BorderLayout.SOUTH);

        JDialog d = new JDialog(this, "Csoport tagok", true);
        d.getContentPane().add(p);
        d.pack();
        d.setLocationRelativeTo(this);

        // Extracted simple handlers to reduce method complexity
        add.addActionListener(ev -> handleAddMember(d, model, id));
        remove.addActionListener(ev -> handleRemoveMember(list, model, id));
        addRole.addActionListener(ev -> handleAddRole(d, id, model));
        changeRole.addActionListener(ev -> handleChangeRole(d, list, id));
        d.setVisible(true);
    }

    /**
     * Tag hozzáadása a csoporthoz.
     * @param parent szülő ablak
     * @param model taglista modell
     * @param groupId csoport UUID
     */
    private void handleAddMember(JDialog parent, DefaultListModel<String> model, UUID groupId) {
        if (!controller.hasGroupPermission(groupId, username, Permissions.GROUP_ADD_MEMBER)) {
            JOptionPane.showMessageDialog(parent, UiMessages.NO_PERM_ADD, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        Set<String> all = controller.getDataStore().getAllUsernames();
        List<String> choices = new ArrayList<>(all);
        choices.remove(username);
        String picked = (String) JOptionPane.showInputDialog(parent, "Válassz felhasználót:", "Hozzáad", JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), null);
        if (picked != null) {
            boolean ok = controller.addGroupMember(groupId, picked, "Résztvevő");
            if (!ok) JOptionPane.showMessageDialog(parent, "Hozzáadás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            else model.addElement(picked + " (Résztvevő)");
        }
    }

    /**
     * Tag eltávolítása a csoportból.
     * @param list taglista UI
     * @param model taglista modell
     * @param groupId csoport UUID
     */
    private void handleRemoveMember(JList<String> list, DefaultListModel<String> model, UUID groupId) {
        if (!controller.hasGroupPermission(groupId, username, hu.prog3.offlinechatprog3.model.Permissions.GROUP_REMOVE_MEMBER)) {
            JOptionPane.showMessageDialog(this, UiMessages.NO_PERM_REMOVE, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        String s = list.getSelectedValue();
        if (s == null) return;
        String selectedUser = s.contains(" (") ? s.substring(0, s.indexOf(" (")) : s;
        boolean ok = controller.removeGroupMember(groupId, selectedUser);
        if (!ok) JOptionPane.showMessageDialog(this, "Eltávolítás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
        else model.removeElement(s);
    }

    /**
     * Egyéni szerep hozzáadása a csoporthoz.
     * @param parent szülő ablak
     * @param groupId csoport UUID
     * @param memberModel taglista modell
     */
    private void handleAddRole(JDialog parent, UUID groupId, DefaultListModel<String> memberModel) {
        if (!controller.isGroupAdmin(groupId, username)) {
            JOptionPane.showMessageDialog(parent, "Nincs jogosultságod szerep hozzáadására (Adminisztrátor szükséges).", UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        String role = JOptionPane.showInputDialog(parent, "Szerep neve:");
        if (role == null || role.isBlank()) return;
        boolean ok = controller.addCustomRole(groupId, role);
        if (!ok) {
            JOptionPane.showMessageDialog(parent, "Szerep hozzáadása sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Set<String> perms = promptPermissions(parent);
        if (!perms.isEmpty()) {
            controller.setRolePermissions(groupId, role, perms);
        }
        JOptionPane.showMessageDialog(parent, "Szerep hozzáadva: " + role);
        
        // Frissítjük a taglistát hogy az új szerepek láthatóak legyenek
        refreshMemberList(memberModel, groupId);
    }

    /**
     * Jogosultságok kiválasztása dialógusban.
     * @param parent szülő komponens
     * @return kiválasztott jogosultságok halmaza
     */
    private Set<String> promptPermissions(Component parent) {
        JPanel panel = new JPanel(new GridLayout(0,1));
        JCheckBox addMember = new JCheckBox("Tag hozzáadása", true);
        JCheckBox remMember = new JCheckBox("Tag eltávolítása", false);
        JCheckBox delMsg = new JCheckBox("Üzenetek törlése", false);
        JCheckBox delGroup = new JCheckBox("Csoport törlése", false);
        JCheckBox send = new JCheckBox("Üzenet küldése", true);
        JCheckBox readOnly = new JCheckBox("Csak olvasás", false);
        panel.add(addMember); panel.add(remMember); panel.add(delMsg); panel.add(delGroup); panel.add(send); panel.add(readOnly);
        int res = JOptionPane.showConfirmDialog(parent, panel, "Jogosultságok beállítása", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) {
            return new HashSet<>();
        }
        Set<String> perms = new HashSet<>();
        if (addMember.isSelected()) {
            perms.add(Permissions.GROUP_ADD_MEMBER);
        }
        if (remMember.isSelected()) {
            perms.add(Permissions.GROUP_REMOVE_MEMBER);
        }
        if (delMsg.isSelected()) {
            perms.add(Permissions.GROUP_DELETE_MESSAGES);
        }
        if (delGroup.isSelected()) {
            perms.add(Permissions.GROUP_DELETE_GROUP);
        }
        if (send.isSelected()) {
            perms.add(Permissions.GROUP_SEND_MESSAGE);
        }
        if (!readOnly.isSelected() && !send.isSelected()) {
            perms.add(Permissions.GROUP_READ);
        }
        return perms;
    }

    /**
     * Taglista frissítése szerepkörökkel.
     * @param model taglista modell
     * @param groupId csoport UUID
     */
    private void refreshMemberList(DefaultListModel<String> model, UUID groupId) {
        model.clear();
        hu.prog3.offlinechatprog3.model.Group group = controller.getDataStore().getGroup(groupId);
        if (group == null) return;
        
        for (Map.Entry<UUID, String> entry : group.getMemberRoles().entrySet()) {
            String memberName = controller.getDataStore().getUsernameById(entry.getKey());
            String roleName = entry.getValue();
            if (memberName != null) {
                model.addElement(memberName + " (" + roleName + ")");
            }
        }
    }

    /**
     * Tag szerepkörének módosítása.
     * @param parent szülő ablak
     * @param list taglista UI
     * @param groupId csoport UUID
     */
    private void handleChangeRole(JDialog parent, JList<String> list, UUID groupId) {
        // Jogosultság ellenőrzés - csak Adminisztrátor módosíthat szerepköröket
        if (!controller.isGroupAdmin(groupId, username)) {
            JOptionPane.showMessageDialog(parent, "Nincs jogosultságod szerepkör módosításához (Adminisztrátor szükséges).", UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedEntry = list.getSelectedValue();
        if (selectedEntry == null) return;
        
        // Kivonjuk a felhasználónevet (formátum: "username (szerepkör)")
        String selectedUser = selectedEntry.contains(" (") ? selectedEntry.substring(0, selectedEntry.indexOf(" (")) : selectedEntry;
        
        // Dinamikusan lekérdezzük az elérhető szerepeket a Group-ból
        hu.prog3.offlinechatprog3.model.Group group = controller.getDataStore().getGroup(groupId);
        if (group == null) return;
        Set<String> roles = group.getRoles();
        String[] availableRoles = roles.toArray(new String[0]);
        
        String newRole = (String) JOptionPane.showInputDialog(parent, "Új szerep " + selectedUser + " számára:", "Szerep módosítása", JOptionPane.PLAIN_MESSAGE, null, availableRoles, null);
        if (newRole == null) return;
        boolean ok = controller.setGroupMemberRole(groupId, selectedUser, newRole);
        if (!ok) {
            JOptionPane.showMessageDialog(parent, "Módosítás sikertelen.", "Hiba", JOptionPane.ERROR_MESSAGE);
        } else {
            // Frissítjük a listát hogy lásd az új szerepkört
            refreshMemberList((DefaultListModel<String>) list.getModel(), groupId);
        }
    }

    /**
     * Kiválasztott csoport chat ablakának megnyitása.
     */
    private void openSelectedGroupChat() {
        GroupItem sel = requireSelectedGroup();
        if (sel == null) return;
        UUID id = sel.id;
        GroupChatWindow win = new GroupChatWindow(controller, id, username, sel.name);
        win.setVisible(true);
    }

    /**
     * Kiválasztott csoport törlése.
     */
    private void deleteSelectedGroup() {
        GroupItem sel = requireSelectedGroup();
        if (sel == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Biztosan törlöd a csoportot?", "Megerősítés", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = controller.deleteGroup(sel.id, username);
        if (!ok) {
            JOptionPane.showMessageDialog(this, UiMessages.NO_PERM_DELETE_GROUP, UiMessages.ERR_TITLE, JOptionPane.ERROR_MESSAGE);
        } else {
            loadGroups();
        }
    }

    /**
     * Kiválasztott csoport lekérdezése vagy figyelmeztetés megjelenítése.
     * @return kiválasztott GroupItem vagy null
     */
    private GroupItem requireSelectedGroup() {
        GroupItem sel = groupsList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, SELECT_GROUP_MSG, UiMessages.WARN_TITLE, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return sel;
    }
}
