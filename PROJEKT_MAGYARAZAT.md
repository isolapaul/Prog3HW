# OFFLINE CHAT ALKALMAZÁS - RÉSZLETES MAGYARÁZAT

## PROJEKT FELÉPÍTÉSE (ARCHITEKTÚRA)

Ez a projekt egy **MVC (Model-View-Controller)** architektúrát követ:

```
┌─────────────────────────────────────────────────────┐
│                       VIEW                          │
│  (Felhasználói felület - UI package)                │
│  LoginFrame, MainFrame, ChatWindows, GroupManager   │
└────────────────┬────────────────────────────────────┘
                 │
                 │ Felhasználó gombokat nyomogat,
                 │ adatokat kér le és jelenít meg
                 ↓
┌─────────────────────────────────────────────────────┐
│                   CONTROLLER                        │
│         (Üzleti logika - AppController)             │
│  Összeköti a UI-t és az adatréteget                 │
│  Ellenőrzi az üzleti szabályokat                    │
└────────────────┬────────────────────────────────────┘
                 │
                 │ Adatokat ment/tölt,
                 │ műveleteket hajt végre
                 ↓
┌─────────────────────────────────────────────────────┐
│                     MODEL                           │
│  (Adatstruktúrák és perzisztencia)                  │
│  User, Message, Group + DataStore + FileManager     │
└─────────────────────────────────────────────────────┘
```

## PACKAGE STRUKTÚRA

```
hu.prog3.offlinechatprog3/
├── Main.java                    // Belépési pont
├── model/                       // Adatmodellek
│   ├── User.java               // Felhasználó
│   ├── Message.java            // Üzenet
│   ├── Group.java              // Csoport
│   └── Permissions.java        // Jogosultság konstansok
├── persistence/                 // Adattárolás
│   ├── DataStore.java          // Központi adattár
│   └── FileManager.java        // Fájl mentés/betöltés
├── controller/                  // Üzleti logika
│   └── AppController.java      // Fő vezérlő
└── ui/                         // Felhasználói felület
    ├── LoginFrame.java         // Bejelentkezés
    ├── MainFrame.java          // Főablak
    ├── BaseChatWindow.java     // Chat ablak alaposztály
    ├── PrivateChatWindow.java  // Privát chat
    ├── GroupChatWindow.java    // Csoport chat
    ├── GroupManager.java       // Csoport kezelő
    ├── ChatUi.java            // UI segédfüggvények
    └── UiMessages.java        // UI szövegek
```

## HOGYAN INDUL AZ ALKALMAZÁS?

1. **Main.main()** metódus lefut
2. Létrejön egy **AppController** (betölti a data/offline-chat.dat fájlt)
3. Megnyílik a **LoginFrame** (bejelentkezési ablak)
4. Felhasználó:
   - Regisztrál → új User létrehozása → mentés fájlba
   - Bejelentkezik → jelszó ellenőrzés → MainFrame megnyitás
5. **MainFrame** nyílik meg:
   - Bal oldal: Barátok és Csoportok lista
   - Jobb oldal: Előnézet panel (üzenetek)
   - Menü: Fájl mentés, Csoportok kezelése
6. Innen a felhasználó:
   - Barátokat adhat hozzá (Friend request rendszer)
   - Privát chateket nyithat
   - Csoportokat hozhat létre
   - Csoportokban üzenhet (jogosultságok szerint)

## ADATFOLYAM PÉLDA: ÜZENET KÜLDÉS

```
Felhasználó beír egy üzenetet a PrivateChatWindow-ba
        ↓
sendButton megnyomása
        ↓
PrivateChatWindow.sendInternal() meghívódik
        ↓
AppController.sendPrivateMessage(from, to, text) meghívódik
        ↓
AppController ellenőrzi, hogy barátok-e
        ↓
DataStore.sendPrivateMessage() létrehozza a Message objektumot
        ↓
Message hozzáadódik a privateMessages Map-hez
        ↓
AppController.saveStore() fájlba menti az egész DataStore-t
        ↓
A másik PrivateChatWindow liveTimer észreveszi az új üzenetet
        ↓
Újratölti a chatArea-t, megjelenik az új üzenet
```

## ADATMENTÉS MŰKÖDÉSE

### Serialization (Szerializáció)

Az összes adat **egyetlen DataStore objektumban** van:
- Felhasználók (Map-ekben)
- Barátságok (Map-ekben)
- Csoportok (Map-ben)
- Üzenetek (Map-ekben)

Amikor mentünk:
1. **FileManager.save()** meghívódik
2. **ObjectOutputStream** létrehozása
3. **writeObject(dataStore)** - az egész objektum byte-okká alakul
4. Fájlba írás: `data/offline-chat.dat`

Amikor betöltünk:
1. **FileManager.load()** meghívódik
2. **ObjectInputStream** létrehozása
3. **readObject()** - byte-okból visszaalakul objektum
4. Cast DataStore típusra

### Milyen adatok tárolódnak?

```java
DataStore {
    usersByName: {
        "anna" -> User{id=uuid1, username="anna", password="..."},
        "bela" -> User{id=uuid2, username="bela", password="..."}
    },
    friends: {
        "anna" -> ["bela", "cecil"],
        "bela" -> ["anna"]
    },
    privateMessages: {
        "anna#bela" -> [
            Message{id=uuid3, senderId=uuid1, content="Szia!"},
            Message{id=uuid4, senderId=uuid2, content="Helló!"}
        ]
    },
    groups: {
        uuid5 -> Group{id=uuid5, name="Prog3", memberRoles={uuid1->"Admin", uuid2->"Résztvevő"}}
    },
    groupMessages: {
        uuid5 -> [
            Message{id=uuid6, senderId=uuid1, conversationId=uuid5, content="Csoportüzenet"}
        ]
    }
}
```

## JOGOSULTSÁG RENDSZER

### Szerepek a csoportban

Alapértelmezett szerepek minden csoportban:
1. **Adminisztrátor** - minden joga van ("ALL")
2. **Résztvevő** - üzenetet küldhet
3. **Olvasó** - csak olvashat, nem küldhet

### Hogyan működik a jogosultság ellenőrzés?

```java
// 1. Ellenőrizzük, hogy a felhasználó küldhet-e üzenetet:
boolean canSend = controller.hasGroupPermission(groupId, username, Permissions.GROUP_SEND_MESSAGE);

// 2. Ez meghívja:
AppController -> DataStore -> Group.hasPermission(userId, "GROUP_SEND_MESSAGE")

// 3. Group.hasPermission() logika:
//    a) Lekéri a felhasználó szerepét (pl. "Résztvevő")
//    b) Lekéri a szerep jogosultságait (pl. {"GROUP_SEND_MESSAGE"})
//    c) Ellenőrzi:
//       - Van-e "ALL" joga? (Admin esetén igen)
//       - Van-e konkrétan "GROUP_SEND_MESSAGE" joga?
```

### Példa jogosultság ellenőrzésre

```java
// Admin felhasználó - mindenre van joga
Group group = ...;
UUID adminId = ...;
group.addMember(adminId, "Adminisztrátor");

group.hasPermission(adminId, Permissions.GROUP_SEND_MESSAGE); // true
group.hasPermission(adminId, Permissions.GROUP_DELETE_MESSAGES); // true
group.hasPermission(adminId, Permissions.GROUP_DELETE_GROUP); // true
// Miért? Mert "Adminisztrátor" szerepnek van "ALL" jogosultsága

// Résztvevő felhasználó - csak üzenetküldés
UUID userId = ...;
group.addMember(userId, "Résztvevő");

group.hasPermission(userId, Permissions.GROUP_SEND_MESSAGE); // true
group.hasPermission(userId, Permissions.GROUP_DELETE_MESSAGES); // false
group.hasPermission(userId, Permissions.GROUP_DELETE_GROUP); // false
```

## LIVE REFRESH (ÉLŐ FRISSÍTÉS)

### Hogyan frissülnek automatikusan az üzenetek?

Minden chat ablakban (és a MainFrame-ben) van egy **Timer**:

```java
javax.swing.Timer liveTimer = new javax.swing.Timer(1500, e -> {
    // Ez a kód 1500 milliszekundumenként (1.5 másodpercenként) lefut
    
    // 1. Újratöltjük az adatokat a fájlból (ha változott)
    controller.reloadIfChanged();
    
    // 2. Lekérdezzük a friss üzeneteket
    List<Message> msgs = controller.getPrivateMessages(user1, user2);
    
    // 3. Ha több üzenet van, mint legutóbb, újrarajzoljuk a chat területet
    if (msgs.size() != lastCount) {
        reloadMessages();
    }
});
liveTimer.start(); // Timer elindítása
```

### Miért működik több példányban is?

1. **Példány A** üzenetet küld
   - Meghívja `controller.sendPrivateMessage()`
   - AppController elmenti a fájlba
   - Fájl módosítási ideje megváltozik

2. **Példány B** timer-e lefut
   - Meghívja `controller.reloadIfChanged()`
   - AppController észreveszi, hogy a fájl időbélyege változott
   - Újratölti a DataStore-t a fájlból
   - Lekérdezi az új üzeneteket
   - Frissíti a UI-t

## BARÁTKÉRÉS RENDSZER

### Barátkérés életciklusa

```
1. Anna küld barát kérést Bélának
   ↓
   controller.sendFriendRequest("anna", "bela")
   ↓
   DataStore:
   - incomingFriendRequests["bela"].add("anna")
   - outgoingFriendRequests["anna"].add("bela")

2. Béla elfogadja a kérést
   ↓
   controller.acceptFriendRequest("bela", "anna")
   ↓
   DataStore:
   - Töröljük a kérést mindkét listából
   - friends["anna"].add("bela")
   - friends["bela"].add("anna")
   - Most már barátok!

3. Alternatíva: Béla elutasítja
   ↓
   controller.rejectFriendRequest("bela", "anna")
   ↓
   DataStore:
   - Töröljük a kérést mindkét listából
   - NEM lesznek barátok
```

### Miért nem lehet direkt barátot hozzáadni?

A barát kérés rendszer azért jó, mert:
- **Kétirányú megerősítés**: mindkét félnek bele kell egyeznie
- **Spam védelem**: nem tudnak random emberek hozzáadni
- **Visszavonás**: el lehet utasítani vagy visszavonni a kérést

## HASHMAP ÉS HASHSET MAGYARÁZAT

### Mi az a HashMap?

HashMap = kulcs-érték párok tárolása, gyors keresés

```java
Map<String, User> usersByName = new HashMap<>();

// Hozzáadás:
usersByName.put("anna", annaUser); // Kulcs: "anna", Érték: annaUser objektum

// Lekérdezés:
User u = usersByName.get("anna"); // O(1) időben megtalálja!

// Ellenőrzés:
boolean exists = usersByName.containsKey("anna"); // true vagy false
```

**Miért gyors?**
A HashMap hash függvényt használ. Nem keres végig az összes elemen,
hanem matematikailag kiszámolja, hogy hol van az adat.

### Mi az a HashSet?

HashSet = halmaz, minden elem csak egyszer szerepelhet

```java
Set<String> friends = new HashSet<>();

// Hozzáadás:
friends.add("bela");
friends.add("cecil");
friends.add("bela"); // Ez nem ad hozzá semmit, már benne van!

// Ellenőrzés:
boolean isFriend = friends.contains("bela"); // true

// Méret:
int count = friends.size(); // 2 (csak bela és cecil van benne)
```

**Miért jó?**
- Automatikusan szűri a duplikációkat
- Gyors keresés (mint HashMap)
- Nem számít a sorrend

## UI KOMPONENSEK

### Swing alapok

Swing = Java beépített grafikus könyvtár

```java
// Ablak (Frame):
JFrame frame = new JFrame("Cím");

// Szöveges mező (TextField):
JTextField textField = new JTextField(20); // 20 karakter széles

// Jelszó mező:
JPasswordField passwordField = new JPasswordField(20);

// Gomb (Button):
JButton button = new JButton("Kattints ide");

// Gomb eseménykezelő:
button.addActionListener(e -> {
    // Ez fut le, amikor megnyomják a gombot
    String text = textField.getText();
    System.out.println("Írtál: " + text);
});

// Szöveg terület (TextArea):
JTextArea textArea = new JTextArea(20, 50); // 20 sor, 50 oszlop
textArea.setEditable(false); // Csak olvasható

// Lista (List):
DefaultListModel<String> model = new DefaultListModel<>();
model.addElement("Első elem");
model.addElement("Második elem");
JList<String> list = new JList<>(model);
```

### Layout Manager-ek

Layout = hogyan rendezzük el a komponenseket az ablakban

```java
// BorderLayout - 5 régió: North, South, East, West, Center
panel.setLayout(new BorderLayout());
panel.add(topPanel, BorderLayout.NORTH);
panel.add(bottomPanel, BorderLayout.SOUTH);
panel.add(centerPanel, BorderLayout.CENTER);

// GridLayout - rács elrendezés
panel.setLayout(new GridLayout(3, 2)); // 3 sor, 2 oszlop
panel.add(button1); // Balra felülre
panel.add(button2); // Jobbra felülre
// stb.

// GridBagLayout - rugalmas, de bonyolult
panel.setLayout(new GridBagLayout());
GridBagConstraints gbc = new GridBagConstraints();
gbc.gridx = 0; gbc.gridy = 0; // pozíció
panel.add(label, gbc);
```

## GYAKORI JAVA KONCEPCIÓK

### Lambda kifejezések

Régi stílus:
```java
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Kattintás");
    }
});
```

Új stílus (lambda):
```java
button.addActionListener(e -> System.out.println("Kattintás"));

// Több soros lambda:
button.addActionListener(e -> {
    String text = textField.getText();
    System.out.println("Írtál: " + text);
});
```

### Try-with-resources

Régi stílus:
```java
FileOutputStream fos = null;
try {
    fos = new FileOutputStream(file);
    // ... használat ...
} finally {
    if (fos != null) fos.close(); // Manuális bezárás
}
```

Új stílus:
```java
try (FileOutputStream fos = new FileOutputStream(file)) {
    // ... használat ...
} // Automatikusan bezáródik!
```

### Optional

Optional = lehet, hogy van érték, lehet, hogy nincs (null helyett)

```java
Optional<User> userOpt = findUserByName("anna");

// Régi stílus (null ellenőrzés):
User u = findUserByName("anna");
if (u != null) {
    System.out.println(u.getUsername());
}

// Új stílus (Optional):
userOpt.ifPresent(user -> System.out.println(user.getUsername()));

// Vagy:
User u = userOpt.orElse(new User()); // Ha nincs, készít egy újat
```

## TESZTELÉS

A projektben JUnit 5 tesztek vannak:

```java
@Test
void testRegisterUser() {
    AppController ctrl = new AppController();
    boolean ok = ctrl.registerUser("test", "password");
    assertTrue(ok); // Ellenőrizzük, hogy sikeres volt
}
```

### Tesztek futtatása

```bash
# Összes teszt futtatása:
mvn test

# Tesztek + coverage:
mvn verify

# Coverage eredmény:
# target/site/jacoco/index.html
```

## SONARQUBE ELEMZÉS

SonarCloud = kód minőség ellenőrző szolgáltatás

```bash
# Teljes elemzés (tesztek + coverage + Sonar):
mvn verify sonar:sonar -Dsonar.token=YOUR_TOKEN

# Csak Sonar (gyorsabb, de nincs coverage):
mvn sonar:sonar -Dsonar.token=YOUR_TOKEN
```

Dashboard: https://sonarcloud.io/dashboard?id=isolapaul_prog3

## FUTTATÁS

### IDE-ből (pl. IntelliJ IDEA, Eclipse)

1. Nyisd meg a projektet
2. Futtasd a `Main.java` fájlt (jobb klikk → Run)

### Parancssorból

```bash
# 1. Build (fordítás + teszt + jar készítés):
mvn clean package

# 2. Futtatás:
java -jar target/offline-chat-prog3-1.0-SNAPSHOT.jar
```

## GYAKORI PROBLÉMÁK ÉS MEGOLDÁSOK

### "Nem találja a fájlt"

Probléma: Az alkalmazás nem találja a `data/offline-chat.dat` fájlt.
Megoldás: Az első indulásnál automatikusan létrehozza a `data/` mappát.

### "Több példány fut, de nem frissül"

Probléma: Két ablak fut, de nem látják egymás üzeneteit.
Megoldás: Ellenőrizd, hogy ugyanazt a fájlt használják (`data/offline-chat.dat`).

### "Java verzió hiba"

Probléma: `Unsupported class file major version`
Megoldás: Java 11 vagy újabb kell. Ellenőrizd: `java -version`

## ÖSSZEFOGLALÁS

Ez az alkalmazás egy **offline chat**, ami:
- **Helyben tárolja** az adatokat (data/offline-chat.dat fájlban)
- **Java Swing** GUI-t használ
- **Szerepkör alapú jogosultságokat** implementál (Admin, Résztvevő, Olvasó)
- **Live refresh**-el frissül (1.5 másodpercenként ellenőrzi a változásokat)
- **MVC architektúrát** követ
- **Java szerializációval** menti az adatokat

Minden komponens jól el van választva:
- **Model**: User, Message, Group (adatstruktúrák)
- **View**: LoginFrame, MainFrame, ChatWindows (UI)
- **Controller**: AppController (üzleti logika)
- **Persistence**: DataStore, FileManager (adattárolás)
