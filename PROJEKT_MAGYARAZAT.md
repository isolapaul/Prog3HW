# Offline Chat Alkalmazás - Projekt Dokumentáció

## Bevezetés

A házi feladat keretében egy offline chat alkalmazást készítettem Java nyelven, amely fájlba menti az összes adatot és több példányban is futtatható. Az alkalmazás MVC (Model-View-Controller) architektúrát követ, amely jól elkülöníti az adatokat, az üzleti logikát és a felhasználói felületet.

## Architektúra

### MVC felépítés

A projekt három fő rétegre van felosztva:

1. **Model (Adatmodell)**
   - Tartalmazza az adatstruktúrákat: `User`, `Message`, `Group`, `Permissions`
   - A `DataStore` osztály tárolja az összes futásidejű adatot
   - A `FileManager` kezeli a perzisztenciát (fájlba mentés/betöltés)

2. **View (Nézet)**
   - Swing alapú GUI komponensek
   - `LoginFrame` - bejelentkezési ablak
   - `MainFrame` - főablak (barátlista, csoportlista)
   - `PrivateChatWindow` és `GroupChatWindow` - chat ablakok
   - `GroupManager` - csoportok kezelése

3. **Controller (Vezérlő)**
   - `AppController` osztály koordinálja a modell és a nézet közötti kommunikációt
   - Ellenőrzi az üzleti szabályokat (pl. jogosultságok)
   - Kezeli az adatok mentését és betöltését

### Package struktúra

```
hu.prog3.offlinechatprog3/
├── Main.java
├── model/
│   ├── User.java
│   ├── Message.java
│   ├── Group.java
│   ├── Permissions.java
│   └── PermissionChecker.java
├── persistence/
│   ├── DataStore.java
│   └── FileManager.java
├── controller/
│   └── AppController.java
└── ui/
    ├── LoginFrame.java
    ├── MainFrame.java
    ├── BaseChatWindow.java
    ├── PrivateChatWindow.java
    ├── GroupChatWindow.java
    ├── GroupManager.java
    ├── ChatUi.java
    └── UiMessages.java
```

## Főbb funkciók

### 1. Felhasználókezelés

Az alkalmazás regisztrációt és bejelentkezést támogat. A felhasználói adatok (felhasználónév, jelszó hash) a `DataStore` osztályban vannak tárolva, és perzisztensen fájlba mentésre kerülnek.

**Fontos osztályok:**
- `User.java` - felhasználó adatai (UUID azonosító, felhasználónév, jelszó)
- `LoginFrame.java` - bejelentkezési és regisztrációs UI

### 2. Barátkérés rendszer

A privát üzenetek küldéséhez a felhasználóknak barátoknak kell lenniük. Ezt egy kérés-elfogadás mechanizmussal oldottam meg:

1. Felhasználó "A" barátkérést küldd "B"-nek
2. "B" elfogadja vagy elutasítja a kérést
3. Elfogadás esetén mindkét irányban létrejön a barátság kapcsolat

**Implementáció:**
- `DataStore` tárolja az `incomingFriendRequests` és `outgoingFriendRequests` map-eket
- `AppController` kezeli a kérések küldését, elfogadását és elutasítását

### 3. Privát üzenetek

Barátok között lehet privát üzeneteket küldeni. Az üzenetek timestamp-el vannak ellátva, és időrendben jelennek meg a chat ablakban.

**Implementáció:**
- `PrivateChatWindow` - chat ablak megjelenítése
- `BaseChatWindow` - közös logika (absztrakt ősosztály)
- Az üzenetek egy `Map<String, List<Message>>` struktúrában tárolódnak, ahol a kulcs a két felhasználó nevének kombinációja (pl. "anna#bela")

### 4. Csoportok és jogosultságok

Csoportokban több felhasználó vehet részt, és különböző szerepköreik lehetnek:

**Alapértelmezett szerepkörök:**
- **Adminisztrátor** - minden jogosultsággal rendelkezik
- **Résztvevő** - üzeneteket küldhet
- **Olvasó** - csak olvashatja az üzeneteket

**Jogosultság típusok** (Permissions osztály):
- `ALL` - minden jogosultság (Admin szerepkör)
- `GROUP_SEND_MESSAGE` - üzenet küldése
- `GROUP_DELETE_MESSAGES` - üzenetek törlése
- `GROUP_ADD_MEMBER` - tagok hozzáadása
- `GROUP_REMOVE_MEMBER` - tagok eltávolítása
- `GROUP_DELETE_GROUP` - csoport törlése

**Implementáció:**
- `Group` osztály tárolja a tagokat és szerepköreiket
- `PermissionChecker` osztály központosítja a jogosultság-ellenőrzéseket
- Az `AppController` metódusai ellenőrzik a jogosultságokat a műveletek végrehajtása előtt

### 5. Élő frissítés (Live Refresh)

Az alkalmazás automatikusan frissíti az üzeneteket és a felhasználói felületet, így több példányban is futtatható, és a változások láthatóak lesznek mindegyikben.

**Működés:**
- Minden ablakban (MainFrame, chat ablakok) egy `javax.swing.Timer` fut
- A timer 1,5 másodpercenként újratölti a `data/offline-chat.dat` fájlt
- Ha változás történt, frissíti a UI-t az új adatokkal

## Perzisztencia (Adattárolás)

### Szerializáció

Az alkalmazás Java beépített szerializációját használja az adatok mentésére. Az összes adat egy `DataStore` objektumban van tárolva, amely implementálja a `Serializable` interface-t.

**FileManager működése:**
```java
// Mentés
public static boolean save(DataStore store, File file) {
    try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(file))) {
        oos.writeObject(store);
        return true;
    } catch (IOException e) {
        return false;
    }
}

// Betöltés
public static DataStore load(File file) {
    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(file))) {
        return (DataStore) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
        return null;
    }
}
```

**Mentési hely:** `data/offline-chat.dat` fájl a projekt gyökerében

### Adatstruktúrák

A `DataStore` osztály a következő adatstruktúrákat használja:

- `Map<String, User> usersByName` - felhasználók felhasználónév szerint
- `Map<UUID, User> usersById` - felhasználók UUID szerint
- `Map<String, Set<String>> friends` - barátkapcsolatok
- `Map<String, Set<String>> incomingFriendRequests` - bejövő barátkérések
- `Map<String, Set<String>> outgoingFriendRequests` - kimenő barátkérések
- `Map<String, List<Message>> privateMessages` - privát üzenetek
- `Map<UUID, Group> groups` - csoportok
- `Map<UUID, List<Message>> groupMessages` - csoport üzenetek

**Választott indoklás:**
- HashMap gyors O(1) keresést biztosít
- HashSet automatikusan szűri a duplikációkat
- UUID egyedi azonosítókat biztosít az objektumoknak

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

A chat ablakokhoz a Template Method mintát alkalmaztam. A `BaseChatWindow` absztrakt osztály tartalmazza a közös logikát, míg a specifikus részeket (`PrivateChatWindow`, `GroupChatWindow`) az alosztályok implementálják:

```java
protected abstract List<Message> fetchMessages();
protected abstract boolean canSendNow();
protected abstract boolean sendInternal(String text);
```

## Tesztelés

A projekt JUnit 5 keretrendszert használ a teszteléshez.

**Teszt lefedettség:**
- `AppControllerTest` - controller funkcionalitás tesztelése
- `DataStoreTest` - adattárolási műveletek tesztelése
- `FileManagerTest` - fájlkezelés tesztelése
- `UserTest` - felhasználó osztály tesztelése

**Tesztek futtatása:**
```bash
mvn test
```

**Coverage report:**
```bash
mvn verify
# Eredmény: target/site/jacoco/index.html
```

## Code Quality

A projekt SonarCloud szolgáltatást használ a kód minőség ellenőrzésére.

**Elemzés futtatása:**
```bash
mvn verify sonar:sonar -Dsonar.token=YOUR_TOKEN
```

## Használt technológiák

- **Java 21** - programozási nyelv
- **Maven** - build tool és dependency management
- **Java Swing** - GUI framework
- **JUnit 5** - tesztelési keretrendszer
- **JaCoCo** - code coverage
- **SonarCloud** - statikus kódelemzés

## Futtatás

### IDE-ből
1. Projekt importálása Maven projektként
2. `Main.java` futtatása

### Parancssorból
```bash
# Build
mvn clean package

# Futtatás
java -jar target/offline-chat-prog3-1.0-SNAPSHOT.jar
```

## Továbbfejlesztési lehetőségek

1. **Titkosítás** - jelszavak bcrypt-tel történő hash-elése
2. **Fájl küldés** - támogatás csatolmányok küldésére
3. **Emoji támogatás** - unicode emoji-k megjelenítése
4. **Keresés** - üzenetekben történő keresés funkció
5. **Értesítések** - értesítés új üzenetekről
6. **Profil képek** - felhasználói profilképek támogatása
7. **Téma váltás** - sötét/világos téma

## Összegzés

A projekt során sikeresen implementáltam egy funkciógazdag offline chat alkalmazást, amely demonstrálja a tiszta kód elveket, az MVC architektúrát, és különböző tervezési mintákat (pl. Template Method). Az alkalmazás robusztus, jól tesztelt, és könnyen továbbfejleszthető.
