package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Small UI helper to render message lists consistently in text areas.
 * Centralizing this trims duplicate lines across windows and the main frame.
 */
public final class ChatUi {
    private ChatUi() {}

    /**
     * Render messages without timestamps, optional prefix (e.g. group name), and simple "who: text" lines.
     */
    public static void renderMessagesSimple(JTextArea chatArea,
                                            List<Message> msgs,
                                            Function<UUID, String> usernameResolver,
                                            String prefixOrNull) {
        chatArea.setText("");
        final String prefix = prefixOrNull == null ? "" : prefixOrNull;
        for (Message m : msgs) {
            String who = usernameResolver.apply(m.getSenderId());
            if (who == null) who = "?";
            chatArea.append(prefix + who + ": " + m.getContent() + "\n");
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * Render messages with timestamps, mark my own messages as "Én",
     * and optional per-line prefix (e.g. "[Group]").
     */
    public static void renderMessagesWithTime(JTextArea chatArea,
                                              List<Message> msgs,
                                              Function<UUID, String> usernameResolver,
                                              String me,
                                              String prefixOrNull) {
        chatArea.setText("");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        final String prefix = prefixOrNull == null ? "" : prefixOrNull;
        for (Message m : msgs) {
            String who = usernameResolver.apply(m.getSenderId());
            if (who == null) who = "?";
            String label = who.equals(me) ? "Én" : who;
            String time = m.getTimestamp() == null ? "" : fmt.format(m.getTimestamp());
            chatArea.append(String.format("[%s] %s%s: %s%n", time, prefix, label, m.getContent()));
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
