package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.Permissions;

import java.util.List;
import java.util.UUID;

/**
 * Csoport chat ablak.
 */
public class GroupChatWindow extends BaseChatWindow {
    private final UUID groupId;

    /**
     * Csoport chat ablak konstruktor.
     * @param controller MVC controller
     * @param groupId csoport UUID
     * @param me aktuális felhasználó
     * @param title csoport neve
     */
    public GroupChatWindow(AppController controller, UUID groupId, String me, String title) {
        super(controller, me, "Csoport: " + title);
        this.groupId = groupId;
        
        reloadMessages();
    }

    @Override
    protected List<Message> fetchMessages() {
        return controller.getDataStore().getGroupMessages(groupId);
    }
    
    @Override
    protected boolean canSendNow() {
        return controller.hasGroupPermission(groupId, me, Permissions.GROUP_SEND_MESSAGE);
    }
    
    @Override
    protected boolean sendInternal(String text) {
        return controller.sendGroupMessage(groupId, me, text);
    }
}
