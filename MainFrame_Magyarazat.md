# MainFrame.java - Részletes Működési Magyarázat

## Áttekintés

A `MainFrame` osztály az alkalmazás fő ablaka, amely a felhasználói felület központi részét képezi. Ez az ablak jelenik meg a sikeres bejelentkezés után, és itt kezelheti a felhasználó a barátait, csoportjait, valamint kommunikálhat velük.

## Főbb Funkciók

### 1. **Dual-View Rendszer (Barátok + Csoportok)**

Az ablak bal oldali paneljén két fül található:
- **Barátok fül**: A felhasználó barátainak listája
- **Csoportok fül**: Azon csoportok listája, amelyeknek a felhasználó tagja

A jobboldali panelen az aktuálisan kiválasztott baráttal vagy csoporttal folytatott beszélgetés jelenik meg.

### 2. **Valós Idejű Szinkronizálás**

Az alkalmazás **1.5 másodpercenként** automatikusan frissít, hogy több felhasználó egyszerre használhassa:

```java
Timer: 1500ms (1.5 másodperc)
├── reloadStore()              // Fájlból újratöltés (más felhasználók változásai)
├── refreshLists()             // Barát és csoport listák frissítése
├── notifyIncomingRequests()   // Új barátkérés értesítés
├── refreshOpenPrivateWindows() // Nyitott chat ablakok frissítése
└── refreshPreviewAndButton()  // Előnézet és gombok állapotának frissítése
```

**Timestamp-alapú újratöltés**: Az `AppController` csak akkor tölti újra az adatokat, ha a fájl újabb, mint az utolsó mentés. Így nem vesznek el a helyi, még nem mentett változások.

### 3. **Barátkérés Kezelés**

A rendszer kétirányú barátkéréseket kezel:

**Bejövő kérések:**
- "Kérések" gomb → lista a beérkező kérésekről
- Elfogadás → barátlista frissül, kérés törlődik
- Elutasítás → kérés törlődik

**Kimenő kérések:**
- "Küldött kérések" gomb → lista az elküldött kérésekről
- Visszavonás lehetséges

**Automatikus értesítés**: Ha új barátkérés érkezik (a lista mérete megnő), a felhasználó popup üzenetet kap.

### 4. **Chat Funkciók**

#### Privát Chat
- **Előnézet mód**: Főablakban, bal listából választva
- **Dedikált ablak**: Dupla kattintással vagy "Chat megnyitása" gombbal
  - `PrivateChatWindow` nyílik meg
  - Nyitott ablakok tárolva: `openChats` Map-ben
  - Automatikus frissülés a timer által

#### Csoport Chat
- **Előnézet mód**: Főablakban, csoportok fülön
- **Dedikált ablak**: Dupla kattintással vagy "Chat megnyitása" gombbal
  - `GroupChatWindow` nyílik meg
  - Jogosultságok ellenőrzése (Olvasó nem küldhet üzenetet)

### 5. **Jogosultság Kezelés**

A "Küldés" gomb állapota dinamikusan változik:
- **Barát chat**: Engedélyezve, ha van kiválasztott barát
- **Csoport chat**: Csak akkor engedélyezve, ha a felhasználónak van `GROUP_SEND_MESSAGE` jogosultsága

```java
updateSendButtonEnabled() {
    if (Barátok fül) {
        sendButton.enabled = van kiválasztott barát
    } else {
        sendButton.enabled = van GROUP_SEND_MESSAGE jog
    }
}
```

### 6. **Menü Rendszer**

**File menü:**
- Mentés: `controller.saveStore()` hívás
- Kilépés: Alkalmazás bezárása

**Eszközök menü:**
- Csoportok: `GroupManager` ablak megnyitása
  - Új csoport létrehozása
  - Tagok kezelése
  - Szerepkörök módosítása
  - Jogosultságok beállítása

## Architekturális Részletek

### Állapot Követés (Change Detection)

Az intelligens frissítés érdekében az osztály több változót követ:

```java
lastIncomingCount       // Barátkérések száma (popup triggereléshez)
lastPreviewFriend       // Utoljára megtekintett barát
lastPreviewCount        // Üzenetek száma az előnézetben
lastPreviewGroupId      // Utoljára megtekintett csoport ID
lastPreviewGroupCount   // Csoport üzenetek száma
```

Csak akkor frissít, ha ténylegesen változás történt → teljesítmény optimalizálás.

### UI Komponens Hierarchia

```
MainFrame (JFrame)
├── MenuBar
│   ├── File menü (Mentés, Kilépés)
│   └── Eszközök menü (Csoportok)
├── SplitPane (bal/jobb osztás)
│   ├── Bal panel
│   │   ├── Címke: "Kapcsolatok / Csoportok"
│   │   ├── Gombok (6 db)
│   │   │   ├── Barát hozzáadása
│   │   │   ├── Barát törlése
│   │   │   ├── Chat megnyitása
│   │   │   ├── Kérések
│   │   │   ├── Küldött kérések
│   │   └── JTabbedPane
│   │       ├── Barátok tab (JList)
│   │       └── Csoportok tab (JList)
│   └── Jobb panel
│       ├── Chat előnézet (JTextArea, csak olvasható)
│       └── Input panel
│           ├── Szövegmező (JTextField)
│           └── Küldés gomb (JButton)
```

### Event Binding Stratégia

Az összes event kezelő külön metódusban van szervezve a tisztaság érdekében:

```java
bindEvents() {
    bindFriendListEvents()      // Barát lista kiválasztás, dupla kattintás
    bindGroupListEvents()       // Csoport lista kiválasztás, dupla kattintás
    bindTabChangeEvents()       // Fül váltás → előnézet frissítés
    bindOpenChatButton()        // Chat megnyitása gomb
    bindRequestButtons()        // Kérések gombok
    bindSendButton()            // Küldés gomb
    bindAddRemoveFriendButtons() // Barát hozzáadása/törlése gombok
}
```

### Multi-User Sync Mechanizmus

**Probléma**: Több felhasználó egyidejű használata file-based rendszerben.

**Megoldás**:
1. **Timer-based polling**: 1.5 másodpercenként ellenőrzés
2. **Timestamp tracking**: `AppController.lastLoadedTimestamp`
   - Csak újratöltés, ha `file.lastModified() > lastLoadedTimestamp`
   - Mentés után timestamp frissítés
3. **Swallow exceptions**: Ha polling közben hiba van (pl. file lock), a következő tick újrapróbálja

```java
onTimerTick() {
    try {
        controller.reloadStore();  // Biztonságos, timestamp-alapú
        // ... többi frissítés
    } catch (Exception ignored) {
        // Következő tick újrapróbálja
    }
}
```

## Felhasználói Interakciók Folyamata

### Üzenet Küldése Barátnak

1. Felhasználó begépeli az üzenetet
2. "Küldés" gomb megnyomása vagy Enter
3. `handleSend()` → `sendToFriend(text)`
4. Ellenőrzés: Van-e kiválasztott barát?
5. `controller.sendPrivateMessage(username, friend, text)`
6. Sikeres → input mező törlése, előnézet frissítése
7. Sikertelen → hibaüzenet popup

### Üzenet Küldése Csoportba

1. Felhasználó begépeli az üzenetet
2. "Küldés" gomb megnyomása
3. `handleSend()` → `sendToGroup(text)`
4. Ellenőrzés: Van-e `GROUP_SEND_MESSAGE` jogosultság?
5. Sikeres → input mező törlése, előnézet frissítése
6. Sikertelen → hibaüzenet popup

### Barát Hozzáadása

1. "Barát hozzáadása" gomb
2. `showAddFriendDialog()` betölt minden elérhető felhasználót
3. Szűrés: önmaga és meglévő barátok kizárása
4. Felhasználó választ egy nevet a listából
5. **Barátkérés küldése** (nem azonnal hozzáadás!)
6. `store.sendFriendRequest(username, selected)`
7. Mentés és visszajelzés

### Barátkérés Elfogadása

1. "Kérések" gomb → `showIncomingRequests()`
2. Dialog megjelenítése bejövő kérésekkel
3. Felhasználó kiválaszt egy kérést
4. "Elfogad" gomb → `store.acceptFriendRequest(username, sender)`
5. Mindkét félnél létrejön a barátság
6. Kérés törlődik mindkét oldalról
7. Barátlista frissül

### Privát Chat Ablak Nyitása

1. Dupla kattintás baráton vagy "Chat megnyitása" gomb
2. `openPrivateChat(friend)` ellenőrzi: van-e már nyitott ablak?
   - Igen → előtérbe hozza (`toFront()`)
   - Nem → új `PrivateChatWindow` létrehozása
3. Ablak tárolása: `openChats.put(friend, window)`
4. WindowListener: ablak bezárásakor eltávolítás a Map-ből
5. Timer automatikusan frissíti: `refreshOpenPrivateWindows()`

## Teljesítmény Optimalizálás

### Intelligens Lista Frissítés

```java
refreshFriends() {
    // Csak akkor frissít, ha tényleg változott a lista
    boolean changed = sorted.size() != friendsModel.size();
    if (!changed) {
        // Elem-elem összehasonlítás
        for (i = 0; i < sorted.size(); i++) {
            if (!sorted.get(i).equals(friendsModel.get(i))) {
                changed = true;
                break;
            }
        }
    }
    if (changed) {
        // Kiválasztás megőrzése
        String selected = friendsList.getSelectedValue();
        friendsModel.clear();
        // ... újratöltés
        friendsList.setSelectedValue(selected, true); // Visszaállítás
    }
}
```

### Conditional Preview Refresh

Az előnézet csak akkor frissül, ha:
- Más barát/csoport lett kiválasztva, VAGY
- Az üzenetek száma megváltozott

Ezzel elkerülhető a felesleges UI újrarajzolás.

## Hibakezelés

- **Null ellenőrzések**: Minden kiválasztás előtt ellenőrzés (`requireSelectedFriend()`, `requireSelectedGroup()`)
- **Try-catch a timer tick-ben**: Tranziens hibák nem állítják le az alkalmazást
- **Felhasználóbarát hibaüzenetek**: Minden hiba esetén értelmezett popup

## Összefoglalás

A `MainFrame` egy komplex, de jól strukturált főablak, amely:
- ✅ Multi-user szinkronizációt támogat timestamp-alapú újratöltéssel
- ✅ Valós idejű frissítéseket biztosít timer-alapú polling-gal
- ✅ Barátkérés workflow-t kezel (küldés, elfogadás, elutasítás, visszavonás)
- ✅ Dual-view rendszert használ (barátok + csoportok)
- ✅ Jogosultság-alapú UI vezérlést alkalmaz
- ✅ Teljesítményt optimalizálja intelligens frissítésekkel
- ✅ Dedikált chat ablakokat támogat nyitott ablakok menedzselésével

Ez az osztály az alkalmazás "központi idegrendszere", amely összefogja az összes főbb funkciót és biztosítja a felhasználói élményt.
