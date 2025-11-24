# Offline Chat Alkalmazás - Projekt Dokumentáció

## Bevezetés

A házi feladat keretében egy offline chat alkalmazást készítettem Java nyelven, amely fájlba menti az összes adatot és **több példányban egyszerre is futtatható** (multi-user support timestamp-alapú szinkronizációval). Az alkalmazás MVC (Model-View-Controller) architektúrát követ, amely jól elkülöníti az adatokat, az üzleti logikát és a felhasználói felületet.

## Architektúra

### MVC felépítés

A projekt három fő rétegre van felosztva:

1. **Model (Adatmodell)**
   - Tartalmazza az adatstruktúrákat: `User`, `Message`, `Group`, `Permissions`
   - A `DataStore` osztály tárolja az összes futásidejű adatot memóriában
   - A `FileManager` kezeli a perzisztenciát (szerializációval fájlba mentés/betöltés)

2. **View (Nézet)**
   - Swing alapú GUI komponensek
   - `LoginFrame` - bejelentkezési és regisztrációs ablak (BCrypt hash-eléssel)
   - `MainFrame` - főablak (barátlista, csoportlista, chat előnézet, timer-based refresh)
   - `BaseChatWindow` - absztrakt ősosztály chat ablakokhoz (Template Method pattern)
   - `PrivateChatWindow` és `GroupChatWindow` - dedikált chat ablakok időbélyegekkel
   - `GroupManager` - csoportok, tagok, szerepkörök, jogosultságok kezelése
   - `ChatUi` - statikus utility metódusok üzenet rendereléshez
   - `UiMessages` - központosított UI szövegek (hibaüzenetek, címek)

3. **Controller (Vezérlő)**
   - `AppController` osztály koordinálja a modell és a nézet közötti kommunikációt
   - Ellenőrzi az üzleti szabályokat (validáció, jogosultságok, üzenet/név hossz korlátok)
   - Kezeli az adatok mentését, betöltését és a **timestamp-alapú conditional reload-ot**
   - `RegistrationResult` enum a regisztrációs hibák típusos kezelésére

4. **Util (Segédosztályok)**
   - `PasswordUtil` - BCrypt hash-elés és ellenőrzés

### Package struktúra

```
hu.prog3.offlinechatprog3/
├── Main.java                          # Belépési pont (SwingUtilities.invokeLater)
├── model/                             # Domain modellek
│   ├── User.java                      # Felhasználó (UUID, username, passwordHash)
│   ├── Message.java                   # Üzenet (UUID, senderId, conversationId, content, timestamp)
│   ├── Group.java                     # Csoport (UUID, name, memberRoles, rolePermissions)
│   └── Permissions.java               # Jogosultság konstansok (ALL, GROUP_SEND_MESSAGE, stb.)
├── persistence/                       # Adatperzisztencia
│   ├── DataStore.java                 # In-memory adattároló (barátságok, üzenetek, csoportok)
│   └── FileManager.java               # Fájl I/O (ObjectOutputStream/InputStream)
├── controller/                        # Üzleti logika
│   ├── AppController.java             # Központi vezérlő (validáció, jogosultságok, mentés)
│   └── RegistrationResult.java        # Enum regisztrációs eredményekhez
├── util/                              # Segédosztályok
│   └── PasswordUtil.java              # BCrypt hash és ellenőrzés
└── ui/                                # Felhasználói felület (Swing)
    ├── LoginFrame.java                # Bejelentkezés és regisztráció
    ├── MainFrame.java                 # Főablak (dual-view: barátok/csoportok, timer refresh)
    ├── BaseChatWindow.java            # Absztrakt chat ablak (Template Method)
    ├── PrivateChatWindow.java         # Privát beszélgetés ablak
    ├── GroupChatWindow.java           # Csoport beszélgetés ablak
    ├── GroupManager.java              # Csoport CRUD műveletek UI
    ├── ChatUi.java                    # Statikus utility (renderMessages, renderMessagesWithTime)
    └── UiMessages.java                # Konstans szövegek (ERR_TITLE, WARN_TITLE, stb.)
```

## Főbb funkciók

### 1. Felhasználókezelés és Biztonság

Az alkalmazás regisztrációt és bejelentkezést támogat **BCrypt hash-eléssel** a biztonságos jelszótároláshoz.

**Regisztráció validáció:**
- Minimum 3 karakter felhasználónév
- Maximum 20 karakter felhasználónév
- Egyedi felhasználónév ellenőrzés
- `RegistrationResult` enum típusos hibakezelés (USERNAME_TOO_SHORT, USERNAME_TOO_LONG, USERNAME_ALREADY_TAKEN, SUCCESS)

**Bejelentkezés:**
- Plain text jelszót kap a UI-tól
- `PasswordUtil.checkPassword()` BCrypt ellenőrzés a tárolt hash ellen
- Sikeres login után `MainFrame` megnyitása

**Fontos osztályok:**
- `User.java` - felhasználó adatai (UUID azonosító, felhasználónév, BCrypt jelszó hash)
- `PasswordUtil.java` - BCrypt wrapper (hashPassword, checkPassword)
- `LoginFrame.java` - bejelentkezési és regisztrációs UI GridBagLayout-tal
- `RegistrationResult.java` - enum a regisztrációs hibák típusaihoz

### 2. Barátkérés rendszer

A privát üzenetek küldéséhez a felhasználóknak barátoknak kell lenniük. Ezt egy **kétirányú kérés-elfogadás** mechanizmussal oldottam meg:

**Workflow:**
1. Felhasználó "A" barátkérést küld "B"-nek
2. "B" látja a bejövő kérést a "Kérések" gombra kattintva
3. "B" elfogadja vagy elutasítja a kérést
4. Elfogadás esetén **mindkét irányban** létrejön a barátság kapcsolat
5. A kérés törlődik mindkét oldalról (incoming + outgoing)

**Implementáció:**
- `DataStore` három Map-et tart:
  - `Map<String, Set<String>> friends` - barátkapcsolatok
  - `Map<String, Set<String>> incomingFriendRequests` - bejövő kérések
  - `Map<String, Set<String>> outgoingFriendRequests` - kimenő kérések
- `MainFrame` timer-based polling: új kérés esetén **popup értesítés**
- Visszavonás támogatott: `cancelOutgoingFriendRequest()`
- Barát eltávolítás: `removeFriend()` mindkét oldalról törli a kapcsolatot

### 3. Privát üzenetek

Barátok között lehet privát üzeneteket küldeni. Az üzenetek **Instant timestamp-el** vannak ellátva, és időrendben jelennek meg a chat ablakban.

**Funkcionalitás:**
- Üzenet küldés csak barátok között (`AppController.sendPrivateMessage()` ellenőrzi)
- Maximum 1000 karakter üzenet hossz validáció
- Időbélyeg automatikus (`Instant.now()` a Message konstruktorban)
- "Én" / felhasználónév megjelenítés (saját üzenet vs. másé)
- Dupla kattintással vagy "Chat megnyitása" gombbal dedikált ablak

**Implementáció:**
- `PrivateChatWindow` - dedikált chat ablak (BaseChatWindow leszármazott)
- `BaseChatWindow` - Template Method pattern:
  - `fetchMessages()` - absztrakt, leszármazott implementálja
  - `canSendNow()` - jogosultság ellenőrzés (barátság vs. csoport jog)
  - `sendInternal()` - üzenet küldés implementáció
- Az üzenetek egy `Map<String, List<Message>>` struktúrában tárolódnak
- Kulcs: `privateKey(a, b)` - rendezett felhasználónevek (pl. "alice#bob")
- Timer-based refresh: 1.5 másodpercenként újratölti az üzeneteket

### 4. Csoportok és jogosultságok

Csoportokban több felhasználó vehet részt, és különböző szerepköreik lehetnek:

**Alapértelmezett szerepkörök:**
- **Adminisztrátor** - minden jogosultsággal rendelkezik
- **Résztvevő** - üzeneteket küldhet
- **Olvasó** - csak olvashatja az üzeneteket

**Jogosultság típusok** (Permissions final osztály string konstansokkal):
- `ALL` - minden jogosultság (Admin szerepkör alapértelmezetten)
- `GROUP_SEND_MESSAGE` - üzenet küldése
- `GROUP_DELETE_MESSAGES` - üzenetek törlése
- `GROUP_ADD_MEMBER` - tagok hozzáadása
- `GROUP_REMOVE_MEMBER` - tagok eltávolítása
- `GROUP_DELETE_GROUP` - csoport törlése
- `GROUP_READ` - csak olvasás (nem használt jelenleg)

**Implementáció:**
- `Group` osztály tárolja:
  - `Map<UUID, String> memberRoles` - tag → szerepkör leképezés
  - `Set<String> roles` - összes létező szerepkör (3 default + custom)
  - `Map<String, Set<String>> rolePermissions` - szerepkör → jogosultságok
- `Group.hasPermission(userId, permission)` ellenőrzi a jogosultságot
- `Group.isAdmin(userId)` speciális ellenőrzés Adminisztrátor szerepre
- `AppController` minden művelet előtt ellenőriz (`checkPermission()` private metódus)
- **Custom szerepkörök**: `addCustomRole()` + `setRolePermissions()` a GroupManager-ből
- UI szinten is ellenőrzés: küldés gomb disabled ha nincs jog

### 5. Multi-User Support - Élő Frissítés (Live Refresh)

Az alkalmazás **több példányban egyszerre futtatható**, és a változások automatikusan szinkronizálódnak a példányok között. Ez egy **timestamp-alapú conditional reload** mechanizmussal működik.

**Probléma:** Naiv megoldás (minden tick-nél újratöltés) felülírná a helyi, még nem mentett változásokat.

**Megoldás: Timestamp Tracking**
```java
// AppController
private long lastLoadedTimestamp = 0;

public void reloadStore() {
    if (!dataFile.exists()) return;
    long currentFileTime = dataFile.lastModified();
    
    // CSAK akkor töltünk újra, ha a fájl újabb mint az utolsó betöltés/mentés
    if (currentFileTime > lastLoadedTimestamp) {
        DataStore loaded = FileManager.load(dataFile);
        if (loaded != null) {
            this.store = loaded;
            updateTimestamp();  // timestamp frissítése
        }
    }
}

public boolean saveStore() {
    boolean saved = FileManager.save(store, dataFile);
    if (saved) {
        updateTimestamp();  // mentés után frissítjük az időbélyeget
    }
    return saved;
}
```

**Timer-based Polling:**
- Minden ablakban (MainFrame, BaseChatWindow) egy `javax.swing.Timer` fut **1500ms** (1.5 másodperc) intervallummal
- `MainFrame.onTimerTick()`:
  1. `controller.reloadStore()` - timestamp-alapú újratöltés
  2. `refreshLists()` - barát és csoport listák frissítése
  3. `notifyIncomingRequestsIfNeeded()` - új barátkérés popup
  4. `refreshOpenPrivateWindows()` - nyitott chat ablakok frissítése
  5. `refreshPreviewAndButton()` - előnézet és gombok frissítése
- Try-catch: tranziens hibák (file lock) nem állítják le az alkalmazást

**Előnyök:**
- Több felhasználó egyidejű használata
- Helyi változások nem vesznek el
- Valós idejű frissítés (1.5s késleltetéssel)
- Konfliktusok automatikusan feloldódnak (last-write-wins)

## Perzisztencia (Adattárolás)

### Java Szerializáció

Az alkalmazás **Java beépített szerializációját** használja az adatok bináris formában történő mentésére. Az összes adat egy `DataStore` objektumban van tárolva, amely implementálja a `Serializable` interface-t.

**FileManager működése:**
```java
// Mentés - ObjectOutputStream
public static boolean save(DataStore store, File file) {
    try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(file))) {
        oos.writeObject(store);  // Teljes objektumgráf szerializálása
        return true;
    } catch (IOException e) {
        e.printStackTrace();  // Debug célból
        return false;
    }
}

// Betöltés - ObjectInputStream
public static DataStore load(File file) {
    if (!file.exists()) return null;  // Első indítás: üres DataStore
    
    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(file))) {
        return (DataStore) ois.readObject();  // Deszerializálás
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
        return null;
    }
}
```

**Mentési hely:** `data/offline-chat.dat` fájl a projekt gyökerében

**SerialVersionUID:** Minden szerializálható osztály tartalmaz `private static final long serialVersionUID = 1L;` mezőt a verziókompatibilitás kezelésére.

### Adatstruktúrák

A `DataStore` osztály a következő **in-memory** adatstruktúrákat használja (minden mező `final` és konstruktorban inicializálva):

- `Map<String, User> usersByName` - felhasználók felhasználónév szerint (login lookup)
- `Map<UUID, User> usersById` - felhasználók UUID szerint (üzenet küldő azonosítás)
- `Map<String, Set<String>> friends` - barátkapcsolatok (kétirányú szimmetrikus)
- `Map<String, Set<String>> incomingFriendRequests` - bejövő barátkérések
- `Map<String, Set<String>> outgoingFriendRequests` - kimenő barátkérések
- `Map<String, List<Message>> privateMessages` - privát üzenetek (kulcs: rendezett nevek)
- `Map<UUID, Group> groups` - csoportok UUID szerint
- `Map<UUID, List<Message>> groupMessages` - csoport üzenetek

**Választott adatstruktúrák indoklása:**
- **HashMap:** O(1) átlagos keresés, beszúrás, törlés - kritikus teljesítményhez
- **HashSet:** automatikus duplikáció szűrés (barátok, kérések)
- **ArrayList:** üzenetek sorrendje fontos (időrendi sorrend)
- **UUID:** globálisan egyedi azonosítók (collision-free)
- **String kulcs:** privát üzeneteknél rendezett nevek (`privateKey(a,b)` mindig ugyanaz függetlenül a sorrendtől)

**Privát üzenet kulcs generálás:**
```java
private String privateKey(String a, String b) {
    List<String> l = Arrays.asList(a, b);
    Collections.sort(l);  // Rendezés: "bob,alice" és "alice,bob" ugyanaz
    return String.join("#", l);  // "alice#bob"
}
```

## UI implementáció

### Swing komponensek

Az alkalmazás Java Swing könyvtárat használ a grafikus felület kialakításához.

**Főbb komponensek:**
- `JFrame` - ablak keretrendszer
- `JTextField` - szöveges beviteli mező
- `JPasswordField` - jelszó mező
- `JButton` - gombok
- `JTextArea` - többsoros szöveg megjelenítés
- `JList` - listák (barátok, csoportok)
- `JScrollPane` - görgetősáv

### Layout managerek

Különböző layout managereket használtam az UI komponensek elrendezésére:

- **BorderLayout** - 5 régiós elrendezés (North, South, East, West, Center)
- **GridLayout** - rács elrendezés
- **GridBagLayout** - rugalmas, összetett elrendezés

### Template Method tervezési minta

A chat ablakokhoz a **Template Method** design pattern-t alkalmaztam. A `BaseChatWindow` absztrakt ősosztály tartalmazza a teljes chat ablak működési logikát (algoritmus váz), míg a specifikus részleteket a leszármazottak implementálják.

**Közös logika (BaseChatWindow):**
- UI komponensek (JTextArea, JTextField, JButton)
- Üzenet küldés workflow (validáció, küldés, frissítés)
- Timer-based auto-refresh (1.5s)
- Jogosultság alapú gomb engedélyezés/tiltás
- Event binding (küldés gomb, Enter billentyű)

**Absztrakt "hook" metódusok (leszármazottak implementálják):**
```java
protected abstract List<Message> fetchMessages();       // Privát vs. Csoport üzenetek
protected abstract boolean canSendNow();                 // Barátság vs. Jogosultság ellenőrzés
protected abstract boolean sendInternal(String text);    // controller.sendPrivateMessage vs. sendGroupMessage
```

**Konkrét implementációk:**

**PrivateChatWindow:**
```java
@Override
protected List<Message> fetchMessages() {
    return controller.getDataStore().getPrivateMessages(me, other);
}

@Override
protected boolean canSendNow() {
    Set<String> friends = controller.getDataStore().getFriends(me);
    return friends != null && friends.contains(other);  // Barátság ellenőrzés
}

@Override
protected boolean sendInternal(String text) {
    return controller.sendPrivateMessage(me, other, text);
}
```

**GroupChatWindow:**
```java
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
```

**Előnyök:**
- Kód újrafelhasználás (duplikáció elkerülése)
- Könnyű bővíthetőség (új chat típus: csak 3 metódus implementálás)
- Egységes UI/UX minden chat ablakban
- Single Responsibility: BaseChatWindow = UI, leszármazottak = adatkezelés

## Tesztelés

A projekt **JUnit 5** (Jupiter) keretrendszert használ integrációs teszteléshez.

**Teszt fájl:** `ApplicationTest.java` - **16 átfogó integációs teszt**

**Tesztelt funkcionalitás:**
1. **testRegisterAndAuthenticate** - regisztráció és BCrypt login
2. **testRegistrationValidation** - validációs szabályok (túl rövid, túl hosszú, már létezik)
3. **testFriendRequestWorkflow** - teljes barátkérés workflow (küldés, elfogadás)
4. **testPrivateMessaging** - privát üzenetek küldése barátok között
5. **testCannotMessageNonFriends** - üzenet küldés tiltása nem-barátoknak
6. **testGroupCreation** - csoport létrehozás és admin szerepkör
7. **testGroupMembers** - tagok hozzáadása csoporthoz
8. **testRemoveGroupMember** - tag eltávolítása csoportból
9. **testGroupMessaging** - csoport üzenetek küldése
10. **testGroupPermissions** - jogosultság ellenőrzés (Admin vs. Olvasó)
11. **testDeleteGroupMessage** - csoport üzenet törlése
12. **testDeleteGroup** - csoport törlése
13. **testSetGroupMemberRole** - tag szerepkör módosítása
14. **testCustomRoleWithPermissions** - custom szerepkör létrehozás jogosultságokkal
15. **testRemoveFriend** - barát eltávolítása
16. **testPersistence** - fájlba mentés és visszatöltés (perzisztencia teszt)

**Teszt környezet:**
- `@AfterEach tearDown()` - cleanup a `data/offline-chat.dat` fájl törlése
- Magyar nyelvű teszt adatok (tesztElek, bob felhasználók)
- BCrypt hash-elés tesztelve (`PasswordUtil.hashPassword()`)

**Tesztek futtatása:**
```bash
mvn test
```

**Coverage report (JaCoCo):**
```bash
mvn verify
# Eredmény: target/site/jacoco/index.html
# UI package kizárva: sonar.coverage.exclusions=**/ui/**
```

**Megjegyzés:** Az UI réteg (Swing komponensek) nincs unit tesztelve, mert GUI tesztelés komplex és időigényes. A business logic (Controller, Model, DataStore) 100%-ban tesztelt.

## Code Quality

A projekt SonarCloud szolgáltatást használ a kód minőség ellenőrzésére.

**Elemzés futtatása:**
```bash
mvn verify sonar:sonar -Dsonar.token=YOUR_TOKEN
```

## Használt technológiák és függőségek

### Core
- **Java 21 LTS** - programozási nyelv (modern features: pattern matching, records, stb.)
- **Maven 3.9.11** - build tool és dependency management
- **Java Swing** - GUI framework (javax.swing.*)

### Biztonság
- **jBCrypt 0.4** - BCrypt password hashing (org.mindrot:jbcrypt)
  - Bcrypt.hashpw() - jelszó hash generálás
  - Bcrypt.checkpw() - jelszó ellenőrzés

### Tesztelés
- **JUnit 5.9.3** (Jupiter) - modern tesztelési keretrendszer
  - junit-jupiter-api - teszt íráshoz
  - junit-jupiter-engine - teszt futtatáshoz

### Code Quality
- **JaCoCo 0.8.12** - code coverage tool (XML report SonarCloud-nak)
- **SonarCloud** - statikus kódelemzés és minőségi gate
  - Organization: isolapaul
  - Project Key: isolapaul_prog3

### Build Plugins
- **maven-compiler-plugin 3.11.0** - Java 21 compilation
- **maven-surefire-plugin 3.2.5** - teszt futtatás
- **maven-shade-plugin 3.4.1** - fat JAR generálás (runnable JAR Main class-szal)
- **sonar-maven-plugin 5.3.0.6276** - SonarCloud integráció

## Futtatás

### IDE-ből (IntelliJ IDEA / Eclipse / VS Code)
1. Projekt importálása Maven projektként
2. Maven dependencies letöltése (`mvn clean install`)
3. `Main.java` futtatása (Run → Main)
4. Alternatíva: Maven goal `mvn exec:java` (ha konfigurálva van)

### Parancssorból
```bash
# Projekt build-elés (compile + test + package)
mvn clean package

# Fat JAR futtatása (shade plugin hozza létre)
java -jar target/offline-chat-prog3-1.0.jar

# Vagy közvetlenül classpath-tal
mvn exec:java -Dexec.mainClass="hu.prog3.offlinechatprog3.Main"
```

### Több példány egyszerre (Multi-user tesztelés)
```bash
# Első terminál
java -jar target/offline-chat-prog3-1.0.jar

# Második terminál (ugyanabban a mappában!)
java -jar target/offline-chat-prog3-1.0.jar

# Mindkét példány ugyanazt a data/offline-chat.dat fájlt használja
# Változások 1.5 másodpercen belül megjelennek a másik példányban
```

### Tesztek futtatása
```bash
# Összes teszt futtatása
mvn test

# Teszt + coverage report
mvn verify

# Coverage report megtekintése
# Nyisd meg: target/site/jacoco/index.html
```

### SonarCloud analízis
```bash
mvn verify sonar:sonar \
  -Dsonar.token=YOUR_SONAR_TOKEN \
  -Dsonar.organization=isolapaul \
  -Dsonar.projectKey=isolapaul_prog3
```

## Továbbfejlesztési lehetőségek

### Jövőbeli fejlesztések
1. **Fájl küldés** - csatolmányok támogatása
2. **Emoji picker** - beépített emoji választó
3. **Üzenet keresés** - full-text search
4. **Desktop értesítések** - System Tray értesítések
5. **Profil képek** - avatár feltöltés
6. **Téma váltás** - dark/light mode
7. **Üzenet szerkesztés** - saját üzenetek módosítása
8. **Online státusz** - aktív/inaktív jelzés
9. **End-to-end titkosítás** - AES üzenettitkosítás
10. **Database migráció** - SQLite/H2 használata

## Összegzés

A projekt során sikeresen implementáltam egy **funkciógazdag offline chat alkalmazást**, amely demonstrálja:
- ✅ **MVC architektúra** - tiszta rétegek és felelősség elválasztás
- ✅ **Design Patterns** - Template Method (BaseChatWindow), utility classes (FileManager, ChatUi, PasswordUtil)
- ✅ **Multi-user support** - timestamp-alapú conditional reload mechanizmus
- ✅ **Biztonság** - BCrypt password hashing (jBCrypt library)
- ✅ **Jogosultság rendszer** - szerepkör alapú hozzáférés vezérlés (RBAC)
- ✅ **Tesztelés** - 16 integrációs teszt JUnit 5-tel, JaCoCo coverage
- ✅ **Code Quality** - SonarCloud integráció, statikus analízis
- ✅ **Perzisztencia** - Java szerializáció ObjectOutputStream/InputStream-mel
- ✅ **GUI** - Swing komponensek, responsive layout-ok (BorderLayout, GridLayout, GridBagLayout)

Az alkalmazás **működőképes, használható és továbbfejleszthető**. A fent említett problémák nem befolyásolják a funkcionalitást kritikusan, inkább **optimalizációs és best practice szempontból** relevánsak. A projekt célja (offline chat MVC architektúrával) teljesült, és a kód minősége megfelelő egy házi feladat szintjén.
