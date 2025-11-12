package hu.prog3.offlinechatprog3.ui;

import hu.prog3.offlinechatprog3.controller.AppController;
import hu.prog3.offlinechatprog3.model.Message;
import hu.prog3.offlinechatprog3.model.Permissions;

import java.util.List;
import java.util.UUID;

/**
 * CSOPORT CHAT ABLAK (GroupChatWindow)
 * 
 * Ez az osztály egy CSOPORT BESZÉLGETÉST kezel, ahol TÖBB FELHASZNÁLÓ
 * vehet részt.
 * 
 * ÖRÖKLŐDÉS:
 * BaseChatWindow (szülő) → GroupChatWindow (gyerek)
 * 
 * A szülő osztály (BaseChatWindow) adja az ALAP FUNKCIÓKAT:
 * - Chat terület megjelenítése
 * - Beviteli mező és küldés gomb
 * - Élő frissítés (timer)
 * - Üzenet küldés logika
 * 
 * Ez az osztály IMPLEMENTÁLJA az ABSTRACT METÓDUSOKAT:
 * - fetchMessages() - Honnan kéri le a csoport üzeneteket?
 * - canSendNow() - Mikor küldhet üzenetet? (GROUP_SEND_MESSAGE jogosultság kell)
 * - sendInternal() - Hogyan küldi el az üzenetet?
 * 
 * MIBEN KÜLÖNBÖZIK A PRIVÁT CHAT-TŐL?
 * - Privát chat: csak 2 ember, mindig lehet írni ha barátok
 * - Csoport chat: több ember, JOGOSULTSÁGOK kellenek az íráshoz
 * 
 * PÉLDA HASZNÁLAT:
 * Béla tagja a "Prog3 projekt" csoportnak.
 * A MainFrame csoport listájában rákattint a csoport nevére.
 * A MainFrame létrehoz egy GroupChatWindow-t:
 * 
 * UUID groupId = ...; // a csoport azonosítója
 * GroupChatWindow window = new GroupChatWindow(controller, groupId, "Béla", "Prog3 projekt");
 * window.setVisible(true);
 * 
 * Ekkor megnyílik egy ablak:
 * Cím: "Csoport: Prog3 projekt"
 * Tartalom: csoport üzenetek + beviteli mező (ha van joga írni)
 */
public class GroupChatWindow extends BaseChatWindow {
    
    /**
     * CSOPORT AZONOSÍTÓ (UUID)
     * 
     * Minden csoportnak van egy EGYEDI AZONOSÍTÓJA (UUID).
     * Ez alapján azonosítjuk, hogy MELYIK csoport üzeneteit kérjük le.
     * 
     * MIÉRT UUID ÉS NEM NÉV?
     * Mert a név VÁLTOZHAT, de az UUID ÖRÖKKÉ ugyanaz marad.
     * 
     * PÉLDA:
     * Csoport neve: "Prog3 projekt"
     * Csoport ID: UUID("a1b2c3d4-e5f6-...")
     * 
     * Ha átnevezzük "Java projekt"-re, az ID ugyanaz marad.
     */
    private final UUID groupId;

    /**
     * KONSTRUKTOR - csoport chat ablak létrehozása
     * 
     * PARAMÉTEREK:
     * @param controller Az alkalmazás controller-e (adatok lekéréshez)
     * @param groupId A csoport egyedi azonosítója (UUID)
     * @param me Az aktuális felhasználó neve (ki van bejelentkezve)
     * @param title A csoport neve (ablak címéhez)
     * 
     * MŰKÖDÉS:
     * 1. Szülő osztály konstruktorát meghívja (super)
     * 2. Ablak címe: "Csoport: Prog3 projekt"
     * 3. Csoport ID eltárolása
     * 
     * PÉLDA:
     * UUID id = UUID.fromString("a1b2c3...");
     * GroupChatWindow(controller, id, "Béla", "Prog3 projekt")
     * → Ablak címe: "Csoport: Prog3 projekt"
     * → this.me = "Béla" (szülőben)
     * → this.groupId = UUID("a1b2c3...") (ebben az osztályban)
     */
    public GroupChatWindow(AppController controller, UUID groupId, String me, String title) {
        // Szülő konstruktor meghívása (BaseChatWindow)
        // Paraméterek: controller, me, ablak címe
        // FONTOS: A super() NEM hívja meg a reloadMessages()-t!
        super(controller, me, "Csoport: " + title);
        
        // Csoport azonosító eltárolása
        this.groupId = groupId;
        
        // Most már inicializálva van a `groupId` mező, most betölthetjük az üzeneteket
        reloadMessages();
    }

    /**
     * ÜZENETEK LEKÉRÉSE (ABSTRACT METÓDUS IMPLEMENTÁCIÓ)
     * 
     * Ez a metódus mondja meg a BaseChatWindow-nak, hogy HONNAN kérje le
     * a megjelenítendő üzeneteket.
     * 
     * CSOPORT CHAT ESETÉN:
     * Csak azok az üzenetek kellenek, ahol a MESSAGE conversationId-ja
     * MEGEGYEZIK a csoport ID-jával.
     * 
     * PÉLDA:
     * Van két csoport:
     * - "Prog3 projekt" (ID: a1b2...)
     * - "Java tanulók" (ID: c3d4...)
     * 
     * Az adatbázisban van:
     * 1. Üzenet conversationId = a1b2... → "Prog3 projekt" csoportban
     * 2. Üzenet conversationId = a1b2... → "Prog3 projekt" csoportban
     * 3. Üzenet conversationId = c3d4... → "Java tanulók" csoportban (EZ NEM KELL!)
     * 4. Üzenet conversationId = a1b2... → "Prog3 projekt" csoportban
     * 
     * Ha ez a GroupChatWindow a "Prog3 projekt" csoporté:
     * fetchMessages() visszaadja: [1, 2, 4]
     * 
     * @return A csoport üzenetei időrendi sorrendben
     */
    @Override
    protected List<Message> fetchMessages() {
        // Controller-től kérjük le a csoport üzeneteket
        // Paraméter: a csoport ID-ja
        return controller.getGroupMessages(groupId);
    }

    /**
     * KÜLDÉS JOGOSULTSÁG ELLENŐRZÉS (ABSTRACT METÓDUS IMPLEMENTÁCIÓ)
     * 
     * Ez a metódus mondja meg a BaseChatWindow-nak, hogy MOST küldhet-e üzenetet.
     * 
     * CSOPORT CHAT SZABÁLY:
     * Csak akkor lehet üzenetet küldeni, ha a felhasználónak van
     * GROUP_SEND_MESSAGE JOGOSULTSÁGA ebben a csoportban.
     * 
     * SZEREPKÖRÖK ÉS JOGOSULTSÁGOK:
     * - Adminisztrátor: VAN GROUP_SEND_MESSAGE joga → LEHET küldeni
     * - Résztvevő: VAN GROUP_SEND_MESSAGE joga → LEHET küldeni
     * - Olvasó: NINCS GROUP_SEND_MESSAGE joga → NEM LEHET küldeni
     * 
     * ELLENŐRZÉS FOLYAMATA:
     * 1. Controller lekéri a csoportot
     * 2. Megkeresi a felhasználó szerepkörét
     * 3. Ellenőrzi, hogy a szerepkör rendelkezik-e GROUP_SEND_MESSAGE jogosultsággal
     * 4. Visszaadja: true (van joga) vagy false (nincs joga)
     * 
     * PÉLDA:
     * Béla tagja a "Prog3 projekt" csoportnak "Résztvevő" szerepkörrel.
     * 
     * Béla megnyitja a csoport chat ablakot:
     * - canSendNow() ellenőrzi: "Résztvevő" szerepkörnek van GROUP_SEND_MESSAGE joga? → IGEN
     * - Küldés gomb AKTÍV
     * 
     * Admin megváltoztatja Béla szerepkörét "Olvasó"-ra:
     * - Timer újraellenőrzi: canSendNow()
     * - "Olvasó" szerepkörnek van GROUP_SEND_MESSAGE joga? → NEM
     * - Küldés gomb INAKTÍV (szürke, nem lehet kattintani)
     * - Béla továbbra is LÁTJA a régi üzeneteket, de újat NEM tud küldeni
     * 
     * @return true = lehet küldeni (van joga), false = nem lehet (nincs joga)
     */
    @Override
    protected boolean canSendNow() {
        // Controller ellenőrzi a jogosultságot
        // Paraméterek: csoport ID, felhasználó név, jogosultság típus
        return controller.hasGroupPermission(groupId, me, Permissions.GROUP_SEND_MESSAGE);
    }

    /**
     * ÜZENET KÜLDÉSE (ABSTRACT METÓDUS IMPLEMENTÁCIÓ)
     * 
     * Ez a metódus mondja meg a BaseChatWindow-nak, hogy HOGYAN küldje el
     * az üzenetet.
     * 
     * CSOPORT CHAT ESETÉN:
     * A controller sendGroupMessage metódusát hívja meg.
     * 
     * PARAMÉTEREK:
     * - groupId: melyik csoportba küldi (csoport azonosító)
     * - me: ki küldi (feladó neve)
     * - text: az üzenet szövege
     * 
     * MI TÖRTÉNIK A HÁTTÉRBEN?
     * 1. Controller létrehoz egy Message objektumot:
     *    - senderId = Béla UUID-ja
     *    - conversationId = csoport UUID-ja
     *    - content = üzenet szövege
     *    - timestamp = aktuális idő
     * 2. Üzenet hozzáadása a DataStore-hoz
     * 3. Fájlba mentés (persistence)
     * 4. Visszatérési érték: true (sikeres)
     * 
     * PÉLDA:
     * Béla beír egy üzenetet: "Sziasztok! Ki segít a házifeladatban?"
     * 1. BaseChatWindow sendMessage() metódusa meghívja sendInternal("Sziasztok! Ki segít a házifeladatban?")
     * 2. sendInternal() meghívja controller.sendGroupMessage(groupId, "Béla", "Sziasztok! Ki segít a házifeladatban?")
     * 3. Controller létrehozza az üzenetet és elmenti
     * 4. MINDEN CSOPORT TAG látja ezt az üzenetet (ha megnyitják a chat ablakot)
     * 5. Visszatérési érték: true (sikeres)
     * 6. BaseChatWindow törli a beviteli mezőt és frissíti a chat területet
     * 
     * @param text Az üzenet szövege
     * @return true = sikeres küldés, false = hiba történt
     */
    @Override
    protected boolean sendInternal(String text) {
        // Controller csoport üzenet küldése
        return controller.sendGroupMessage(groupId, me, text);
    }
}
