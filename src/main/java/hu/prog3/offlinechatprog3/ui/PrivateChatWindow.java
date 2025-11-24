package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import java.util.List;
import java.util.Set;

public class PrivateChatWindow extends BaseChatWindow {
    private final String other;
    public PrivateChatWindow(AppController controller, String me, String other) {
        super(controller, me, "Chat: " + me + " <--> " + other);
    
        this.other = other;
        reloadMessages();
    }

    @Override
    protected List<Message> fetchMessages() {
        return controller.getDataStore().getPrivateMessages(me, other);
    }

    @Override
    protected boolean canSendNow() {
        Set<String> friends = controller.getDataStore().getFriends(me);
        
        return friends != null && friends.contains(other);
    }

    @Override
    protected boolean sendInternal(String text) {
        //privát üzenet küldése a controlleren keresztül
        return controller.sendPrivateMessage(me, other, text);
    }

    public void refreshIfVisible() {
        //ha az ablak nem látható, ne frissítsünk
        if (!isVisible()) return;
        
        //teljes újratöltés
        reloadMessages();
    }
}
