package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.Permissions;

import java.util.List;
import java.util.UUID;

public class GroupChatWindow extends BaseChatWindow {
    //csoport azonosító
    private final UUID groupId;

    public GroupChatWindow(AppController controller, UUID groupId, String me, String title) {
        super(controller, me, "Csoport: " + title);
        this.groupId = groupId;
        
        //üzenetek betöltése
        reloadMessages();
    }

    //üzenetek lekérése
    @Override
    protected List<Message> fetchMessages() {
        return controller.getDataStore().getGroupMessages(groupId);
    }
    //küldési jogosultság ellenőrzése
    @Override
    protected boolean canSendNow() {
        //controller ellenőrzi a jogosultságot
        return controller.hasGroupPermission(groupId, me, Permissions.GROUP_SEND_MESSAGE);
    }
    //üzenet küldése
    @Override
    protected boolean sendInternal(String text) {
        return controller.sendGroupMessage(groupId, me, text);
    }
}
