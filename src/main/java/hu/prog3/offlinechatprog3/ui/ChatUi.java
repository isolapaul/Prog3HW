package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.model.Message;

import javax.swing.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * CHAT UI HELPER OSZTÁLY (ChatUi)
 * 
 * Ez egy UTILITY (segéd) osztály, ami STATIKUS METÓDUSOKAT tartalmaz
 * az üzenetek MEGJELENÍTÉSÉHEZ.
 * 
 * MIÉRT VAN RÁ SZÜKSÉG?
 * Több helyen is meg kell jeleníteni üzeneteket:
 * - PrivateChatWindow chat területe
 * - GroupChatWindow chat területe
 * - MainFrame előnézeti területe
 * 
 * Ha mindenhol külön írnánk a renderelő kódot, DUPLIKÁCIÓ lenne.
 * Ez az osztály CENTRALIZÁLJA ezt a logikát → könnyebb karbantartani.
 * 
 * MIÉRT FINAL?
 * Mert NEM LEHET ÖRÖKÖLNI belőle (nem kell, mert csak statikus metódusok vannak).
 * 
 * MIÉRT PRIVATE KONSTRUKTOR?
 * Mert NEM LEHET PÉLDÁNYOSÍTANI (new ChatUi() nem működik).
 * Ez egy TISZTÁN STATIKUS osztály, mint a Math.
 * 
 * HASZNÁLAT:
 * ChatUi.renderMessagesSimple(...)
 * ChatUi.renderMessagesWithTime(...)
 * 
 * Nem kell létrehozni objektumot, csak meghívni a statikus metódusokat.
 */
public final class ChatUi {
    
    /**
     * PRIVATE KONSTRUKTOR - megakadályozza a példányosítást
     * 
     * Ha valaki megpróbálná:
     * ChatUi helper = new ChatUi(); // FORDÍTÁSI HIBA!
     * 
     * Ez jelzi, hogy ez egy UTILITY osztály.
     */
    private ChatUi() {}

    /**
     * EGYSZERŰ ÜZENET MEGJELENÍTÉS (időbélyeg nélkül)
     * 
     * Ez a metódus megjeleníti az üzeneteket EGYSZERŰ FORMÁTUMBAN:
     * név: szöveg
     * 
     * MIKOR HASZNÁLJUK?
     * - MainFrame előnézeti területe (nem kell időpont, csak gyors áttekintés)
     * 
     * PARAMÉTEREK:
     * @param chatArea A JTextArea, amibe írjuk az üzeneteket
     * @param msgs Az üzenetek listája
     * @param usernameResolver Függvény, ami UUID-ból username-et csinál
     * @param prefixOrNull Opcionális prefix minden sor elé (pl. "[Csoport] ")
     * 
     * PÉLDA KIMENET:
     * Anna: Helló!
     * Béla: Szia Anna!
     * Anna: Mi újság?
     * 
     * PÉLDA CSOPORT PREFIXSZEL:
     * [Prog3 csoport] Anna: Valaki tud segíteni?
     * [Prog3 csoport] Béla: Persze, mi a probléma?
     */
    public static void renderMessagesSimple(JTextArea chatArea,
                                            List<Message> msgs,
                                            Function<UUID, String> usernameResolver,
                                            String prefixOrNull) {
        // Chat terület törlése
        chatArea.setText("");
        
        // Prefix beállítása (ha nincs, akkor üres string)
        String prefix = "";
        if (prefixOrNull != null) {
            prefix = prefixOrNull;
        }
        
        // Végigmegyünk az összes üzeneten
        for (Message m : msgs) {
            // Feladó ID-ból név készítése
            UUID senderId = m.getSenderId();
            String who = usernameResolver.apply(senderId);
            
            // Ha nincs név (null), akkor "?" legyen
            if (who == null) {
                who = "?";
            }
            
            // Üzenet hozzáfűzése: "prefix név: szöveg"
            String line = prefix + who + ": " + m.getContent() + "\n";
            chatArea.append(line);
        }
        
        // Görgetés az aljára (hogy az új üzenetek látszódjanak)
        int length = chatArea.getDocument().getLength();
        chatArea.setCaretPosition(length);
    }

    /**
     * RÉSZLETES ÜZENET MEGJELENÍTÉS (időbélyeggel)
     * 
     * Ez a metódus megjeleníti az üzeneteket RÉSZLETES FORMÁTUMBAN:
     * [időpont] név: szöveg
     * 
     * SPECIÁLIS FUNKCIÓK:
     * - Saját üzenetek "Én"-ként jelennek meg
     * - Minden üzenetnél látszik az időpont
     * - Opcionális prefix minden sorhoz
     * 
     * MIKOR HASZNÁLJUK?
     * - PrivateChatWindow (privát chat ablakok)
     * - GroupChatWindow (csoport chat ablakok)
     * 
     * PARAMÉTEREK:
     * @param chatArea A JTextArea, amibe írjuk az üzeneteket
     * @param msgs Az üzenetek listája
     * @param usernameResolver Függvény, ami UUID-ból username-et csinál
     * @param me Az aktuális felhasználó neve (aki bejelentkezett)
     * @param prefixOrNull Opcionális prefix minden sor elé
     * 
     * PÉLDA KIMENET (privát chat):
     * [2024-11-11 15:30] Anna: Szia!
     * [2024-11-11 15:31] Én: Helló Anna!
     * [2024-11-11 15:32] Anna: Hogy vagy?
     * [2024-11-11 15:33] Én: Jól, köszi!
     * 
     * PÉLDA KIMENET (csoport chat prefixszel):
     * [2024-11-11 14:20] [Prog3 csoport] Anna: Segítség kéne!
     * [2024-11-11 14:21] [Prog3 csoport] Én: Miben?
     * [2024-11-11 14:22] [Prog3 csoport] Anna: A házifeladatban
     */
    public static void renderMessagesWithTime(JTextArea chatArea,
                                              List<Message> msgs,
                                              Function<UUID, String> usernameResolver,
                                              String me,
                                              String prefixOrNull) {
        // Chat terület törlése
        chatArea.setText("");
        
        // Dátum formázó: "2024-11-11 15:30" formátum
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        fmt = fmt.withZone(ZoneId.systemDefault());
        
        // Prefix beállítása
        String prefix = "";
        if (prefixOrNull != null) {
            prefix = prefixOrNull;
        }
        
        // Végigmegyünk az összes üzeneten
        for (Message m : msgs) {
            // Feladó ID-ból név készítése
            UUID senderId = m.getSenderId();
            String who = usernameResolver.apply(senderId);
            
            // Ha nincs név, akkor "?" legyen
            if (who == null) {
                who = "?";
            }
            
            // Ha én küldtem, akkor "Én" legyen, különben a feladó neve
            String label;
            if (who.equals(me)) {
                label = "Én";
            } else {
                label = who;
            }
            
            // Időbélyeg formázása
            String time = "";
            if (m.getTimestamp() != null) {
                time = fmt.format(m.getTimestamp());
            }
            
            // Teljes sor összeállítása: "[időpont] prefix név: szöveg"
            String line = "[" + time + "] " + prefix + label + ": " + m.getContent() + "\n";
            chatArea.append(line);
        }
        
        // Görgetés az aljára
        int length = chatArea.getDocument().getLength();
        chatArea.setCaretPosition(length);
    }
}
