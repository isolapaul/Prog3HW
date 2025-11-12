package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;

import java.util.List;
import java.util.Set;

/**
 * PRIVÁT CHAT ABLAK (PrivateChatWindow)
 * 
 * Ez az osztály egy KÉT FELHASZNÁLÓ KÖZÖTTI privát beszélgetést kezel.
 * 
 * ÖRÖKLŐDÉS:
 * BaseChatWindow (szülő) → PrivateChatWindow (gyerek)
 * 
 * A szülő osztály (BaseChatWindow) adja az ALAP FUNKCIÓKAT:
 * - Chat terület megjelenítése
 * - Beviteli mező és küldés gomb
 * - Élő frissítés (timer)
 * - Üzenet küldés logika
 * 
 * Ez az osztály IMPLEMENTÁLJA az ABSTRACT METÓDUSOKAT:
 * - fetchMessages() - Honnan kéri le a privát üzeneteket?
 * - canSendNow() - Mikor küldhet üzenetet? (ha barátok)
 * - sendInternal() - Hogyan küldi el az üzenetet?
 * 
 * MIBEN KÜLÖNBÖZIK A CSOPORT CHAT-TŐL?
 * - Privát chat: csak 2 ember, mindig lehet írni ha barátok
 * - Csoport chat: több ember, jogosultságok kellenek az íráshoz
 * 
 * PÉLDA HASZNÁLAT:
 * Béla bejelentkezik, a MainFrame barát listájában rákattint Anna nevére.
 * A MainFrame létrehoz egy PrivateChatWindow-t:
 * 
 * PrivateChatWindow window = new PrivateChatWindow(controller, "Béla", "Anna");
 * window.setVisible(true);
 * 
 * Ekkor megnyílik egy ablak:
 * Cím: "Chat: Béla <--> Anna"
 * Tartalom: korábbi üzenetek + beviteli mező + küldés gomb
 */
public class PrivateChatWindow extends BaseChatWindow {

    /**
     * A MÁSIK FELHASZNÁLÓ NEVE
     * 
     * Privát chat = 2 ember:
     * - me (örökölt a szülőtől) = én
     * - other (ez a mező) = a másik személy
     * 
     * PÉLDA:
     * Ha Béla beszélget Annával:
     * - me = "Béla"
     * - other = "Anna"
     */
    private final String other;

    /**
     * KONSTRUKTOR - privát chat ablak létrehozása
     * 
     * PARAMÉTEREK:
     * @param controller Az alkalmazás controller-e (adatok lekéréshez)
     * @param me Az aktuális felhasználó neve (ki van bejelentkezve)
     * @param other A másik felhasználó neve (kivel beszélgetünk)
     * 
     * MŰKÖDÉS:
     * 1. Szülő osztály konstruktorát meghívja (super)
     * 2. Ablak címe: "Chat: Béla <--> Anna"
     * 3. Másik felhasználó nevének eltárolása
     * 4. Üzenetek betöltése (reloadMessages)
     * 
     * PÉLDA:
     * PrivateChatWindow("controller", "Béla", "Anna")
     * → Ablak címe: "Chat: Béla <--> Anna"
     * → this.me = "Béla" (szülőben)
     * → this.other = "Anna" (ebben az osztályban)
     */
    public PrivateChatWindow(AppController controller, String me, String other) {
        // Szülő konstruktor meghívása (BaseChatWindow)
        // Paraméterek: controller, me, ablak címe
        // FONTOS: A super() NEM hívja meg a reloadMessages()-t!
        super(controller, me, "Chat: " + me + " <--> " + other);
        
        // Másik felhasználó nevének eltárolása
        this.other = other;
        
        // Most már inicializálva van az `other` mező, most betölthetjük az üzeneteket
        reloadMessages();
    }

    /**
     * ÜZENETEK LEKÉRÉSE (ABSTRACT METÓDUS IMPLEMENTÁCIÓ)
     * 
     * Ez a metódus mondja meg a BaseChatWindow-nak, hogy HONNAN kérje le
     * a megjelenítendő üzeneteket.
     * 
     * PRIVÁT CHAT ESETÉN:
     * Csak azok az üzenetek kellenek, ahol:
     * - ÉN küldtem NEKI, VAGY
     * - Ő küldött NEKEM
     * 
     * PÉLDA:
     * Béla beszélget Annával.
     * Az adatbázisban van:
     * 1. Béla → Anna: "Helló!"
     * 2. Anna → Béla: "Szia!"
     * 3. Béla → Csaba: "Hogy vagy?"  (EZ NEM KELL! Más beszélgetés)
     * 4. Anna → Béla: "Mi újság?"
     * 
     * fetchMessages() visszaadja: [1, 2, 4]
     * 
     * @return A privát üzenetek listája időrendi sorrendben
     */
    @Override
    protected List<Message> fetchMessages() {
        // Controller-től kérjük le a privát üzeneteket
        // Paraméterek: én, másik (két fél a beszélgetésben)
        return controller.getPrivateMessages(me, other);
    }

    /**
     * KÜLDÉS JOGOSULTSÁG ELLENŐRZÉS (ABSTRACT METÓDUS IMPLEMENTÁCIÓ)
     * 
     * Ez a metódus mondja meg a BaseChatWindow-nak, hogy MOST küldhet-e üzenetet.
     * 
     * PRIVÁT CHAT SZABÁLY:
     * Csak akkor lehet üzenetet küldeni, ha a két felhasználó BARÁTOK.
     * 
     * MIÉRT?
     * Ha valaki TÖRLI a barátságot, akkor NEM küldhet több üzenetet.
     * (A régi üzenetek még látszódnak, de újat már nem lehet írni.)
     * 
     * ELLENŐRZÉS LÉPÉSEI:
     * 1. Lekéri a saját barátok listáját
     * 2. Ellenőrzi, hogy a másik személy BENNE VAN-E a listában
     * 3. Ha igen → true (lehet küldeni)
     * 4. Ha nem → false (nem lehet küldeni, gomb és mező letiltva)
     * 
     * PÉLDA:
     * Béla barátai: ["Anna", "Csaba"]
     * 
     * Béla megnyitja Anna chat-jét:
     * - canSendNow() ellenőrzi: "Anna" benne van a barátok listájában? → IGEN
     * - Küldés gomb AKTÍV
     * 
     * Béla törli Anna barátságát:
     * - canSendNow() ellenőrzi: "Anna" benne van a barátok listájában? → NEM
     * - Küldés gomb INAKTÍV (szürke, nem lehet kattintani)
     * 
     * @return true = lehet küldeni (barátok), false = nem lehet (nem barátok)
     */
    @Override
    protected boolean canSendNow() {
        // Saját barátok listájának lekérése
        Set<String> friends = controller.getFriendsOf(me);
        
        // Ellenőrzés:
        // 1. friends != null → van barát lista (nem üres)
        // 2. friends.contains(other) → a másik személy benne van-e?
        return friends != null && friends.contains(other);
    }

    /**
     * ÜZENET KÜLDÉSE (ABSTRACT METÓDUS IMPLEMENTÁCIÓ)
     * 
     * Ez a metódus mondja meg a BaseChatWindow-nak, hogy HOGYAN küldje el
     * az üzenetet.
     * 
     * PRIVÁT CHAT ESETÉN:
     * A controller sendPrivateMessage metódusát hívja meg.
     * 
     * PARAMÉTEREK:
     * - me: ki küldi (feladó)
     * - other: kinek küldi (címzett)
     * - text: az üzenet szövege
     * 
     * PÉLDA:
     * Béla beír egy üzenetet: "Helló Anna!"
     * 1. BaseChatWindow sendMessage() metódusa meghívja sendInternal("Helló Anna!")
     * 2. sendInternal() meghívja controller.sendPrivateMessage("Béla", "Anna", "Helló Anna!")
     * 3. Controller létrehoz egy Message objektumot és elmenti
     * 4. Visszatérési érték: true (sikeres)
     * 5. BaseChatWindow törli a beviteli mezőt és frissíti a chat területet
     * 
     * @param text Az üzenet szövege
     * @return true = sikeres küldés, false = hiba történt
     */
    @Override
    protected boolean sendInternal(String text) {
        // Controller privát üzenet küldése
        return controller.sendPrivateMessage(me, other, text);
    }

    /**
     * FRISSÍTÉS HA LÁTHATÓ
     * 
     * Ez a metódus KÜLSŐ HÍVÁS céljára készült (MainFrame-től).
     * 
     * MIÉRT KELL EZ?
     * A MainFrame-nek van egy ÉLŐ FRISSÍTŐ timer-je, ami MINDEN NYITOTT
     * privát chat ablakot frissít periodikusan.
     * 
     * Ez DUPLA VÉDELEM:
     * 1. A BaseChatWindow saját timer-je is frissít (1.5 mp)
     * 2. A MainFrame is frissíti őket (1.5 mp)
     * 
     * OPTIMALIZÁLÁS:
     * Csak akkor frissít, ha az ablak LÁTHATÓ (isVisible()).
     * Ha az ablak minimalizálva van vagy háttérben, felesleges frissíteni.
     * 
     * MŰKÖDÉS:
     * 1. Ellenőrzi: látható-e az ablak?
     * 2. Ha nem látható → return (kilép, nem csinál semmit)
     * 3. Ha látható → reloadMessages() (teljes újratöltés)
     * 
     * PÉLDA:
     * Béla megnyitja 3 privát chat ablakot:
     * - Anna chat: LÁTHATÓ (előtérben)
     * - Csaba chat: MINIMALIZÁLT
     * - Dóra chat: HÁTTÉRBEN (más ablak takarja)
     * 
     * MainFrame timer meghívja mindhárom ablak refreshIfVisible() metódusát:
     * - Anna chat: isVisible() = true → FRISSÍT
     * - Csaba chat: isVisible() = false → NEM frissít
     * - Dóra chat: isVisible() = false → NEM frissít
     */
    public void refreshIfVisible() {
        // Ha az ablak nem látható, ne frissítsünk (optimalizálás)
        if (!isVisible()) return;
        
        // Teljes újratöltés (örökölt metódus a BaseChatWindow-ból)
        reloadMessages();
    }
}
