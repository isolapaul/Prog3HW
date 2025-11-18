package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

//statikus metódusokat tartalmazó osztály
public final class ChatUi {
   
    private ChatUi() {}
    //üzenetek megjelenítése
    public static void renderMessages(JTextArea chatArea,List<Message> msgs,Function<UUID, String> usernameResolver,String prefixOrNull) {
        //chat terület törlése
        chatArea.setText("");
        //prefix beállítása
        final String prefix = prefixOrNull == null ? "" : prefixOrNull;
        for (Message m : msgs) {
            String who = usernameResolver.apply(m.getSenderId()); //függvény, ami UUID-ból nevet csinál
            if (who == null) who = "?";
            chatArea.append(String.format("%s%s: %s%n", prefix, who, m.getContent()));
        }
        
        //görgetés az aljára
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    //üzenetek megjelenítése időbélyeggel
    public static void renderMessagesWithTime(JTextArea chatArea,List<Message> msgs,Function<UUID, String> usernameResolver,String me,String prefixOrNull) {
        chatArea.setText("");
        //dátum formázó létrehozása
        final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        
        //prefix
        final String prefix = prefixOrNull == null ? "" : prefixOrNull;
        
        for (Message m : msgs) {
            String who = usernameResolver.apply(m.getSenderId());
            if (who == null) who = "?";
            
            String label = who.equals(me) ? "Én" : who;
            String time = m.getTimestamp() == null ? "" : format.format(m.getTimestamp());
            
            chatArea.append(String.format("[%s] %s%s: %s%n", time, prefix, label, m.getContent()));
        }
        
        //görgetés az aljára
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
