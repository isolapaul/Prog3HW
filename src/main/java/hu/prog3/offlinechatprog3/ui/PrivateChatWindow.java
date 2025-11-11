package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import java.util.List;
import java.util.Set;

/**
 * A simple private chat window between two users.
 * Shows past messages and allows sending new ones.
 */
public class PrivateChatWindow extends BaseChatWindow {

    private final String other;

    public PrivateChatWindow(AppController controller, String me, String other) {
        super(controller, me, "Chat: " + me + " <--> " + other);
        this.other = other;
    }

    @Override
    protected List<Message> fetchMessages() {
        return controller.getPrivateMessages(me, other);
    }

    @Override
    protected boolean canSendNow() {
        Set<String> friends = controller.getFriendsOf(me);
        return friends != null && friends.contains(other);
    }

    @Override
    protected boolean sendInternal(String text) {
        return controller.sendPrivateMessage(me, other, text);
    }

    /** Called by MainFrame polling loop to refresh if window visible */
    public void refreshIfVisible() {
        if (!isVisible()) return;
        reloadMessages();
    }
}
