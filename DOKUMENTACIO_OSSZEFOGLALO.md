# Dokumentáció Összefoglaló

## 📋 Tartalom

Ez a dokumentum összefoglalja az **Offline Chat** alkalmazás teljes dokumentációját.

---

## 1. 📊 Osztálydiagram

**Fájl**: `docs/osztaly-diagram.puml`

### Osztályok áttekintése (19 osztály)

#### Model csomag (4 osztály)
- ✅ **User** - Felhasználó (immutable, bcrypt hash jelszó)
- ✅ **Message** - Üzenet (privát/csoport)
- ✅ **Group** - Csoport (szerepkör-alapú jogosultságkezelés)
- ✅ **Permissions** - Jogosultság konstansok

#### Persistence csomag (2 osztály)
- **DataStore** - In-memory adattárolás
- **FileManager** - Fájl mentés/betöltés (Java Serialization)

#### Controller csomag (2 osztály)
- **AppController** - MVC Controller réteg
- **RegistrationResult** - Enum (regisztráció eredmény)

#### UI csomag (8 osztály)
- **LoginFrame** - Bejelentkezési ablak
- **MainFrame** - Főablak (barátok + csoportok tabokkal)
- **BaseChatWindow** - Abstract chat ablak (Template Method pattern)
- **PrivateChatWindow** - Privát chat ablak
- **GroupChatWindow** - Csoport chat ablak
- **GroupManager** - Csoportkezelő ablak
- **ChatUi** - Chat renderelő utility
- **UiMessages** - UI string konstansok

#### Util csomag (1 osztály)
- **PasswordUtil** - Bcrypt jelszókezelés

#### Main csomag (1 osztály)
- **Main** - Alkalmazás belépési pont

#### Test csomag (1 osztály)
- **ApplicationTest** - Integrációs tesztek

### PlantUML diagram használata

**Online renderelés**: [PlantUML Server](https://www.plantuml.com/plantuml/uml/)

**VS Code extension**: PlantUML extension telepítése

**Parancssorból**:
```bash
java -jar plantuml.jar docs/osztaly-diagram.puml
```

---

## 2. 📝 Javadoc Dokumentáció

### Dokumentált osztályok (Jelenleg 4/19)

#### ✅ Model csomag
| Osztály | Státusz | Metódusok |
|---------|---------|-----------|
| User | ✅ Kész | constructor, getId(), getUsername(), getPasswordHash() |
| Message | ✅ Kész | constructor, getId(), getSenderId(), getConversationId(), getContent(), getTimestamp(), equals(), hashCode() |
| Group | ✅ Kész | constructor, getId(), getName(), setName(), getMemberRoles(), getRoles(), addRole(), setRolePermissions(), getRolePermissions(), addMember(), removeMember(), setMemberRole(), isAdmin(), hasPermission() |
| Permissions | ✅ Kész | ALL, GROUP_SEND_MESSAGE, GROUP_ADD_MEMBER, GROUP_REMOVE_MEMBER, GROUP_DELETE_MESSAGES, GROUP_DELETE_GROUP, GROUP_READ |

#### ⏳ Persistence csomag (Részben kész)
| Osztály | Státusz | Metódusok |
|---------|---------|-----------|
| DataStore | ⏳ TODO | 30+ metódus dokumentálása szükséges |
| FileManager | ⏳ TODO | save(), load() |

#### ⏳ Controller csomag (Részben kész)
| Osztály | Státusz | Metódusok |
|---------|---------|-----------|
| AppController | ⏳ TODO | 25+ metódus dokumentálása szükséges |
| RegistrationResult | ⏳ TODO | Enum értékek |

#### ⏳ UI csomag (TODO)
| Osztály | Státusz | Metódusok |
|---------|---------|-----------|
| LoginFrame | ❌ TODO | Összes metódus |
| MainFrame | ❌ TODO | Összes metódus |
| BaseChatWindow | ❌ TODO | Template method + abstract metódusok |
| PrivateChatWindow | ❌ TODO | Összes metódus |
| GroupChatWindow | ❌ TODO | Összes metódus |
| GroupManager | ❌ TODO | Összes metódus |
| ChatUi | ❌ TODO | renderMessages(), renderMessagesWithTime() |
| UiMessages | ❌ TODO | String konstansok |

#### ⏳ Util csomag (TODO)
| Osztály | Státusz | Metódusok |
|---------|---------|-----------|
| PasswordUtil | ❌ TODO | hashPassword(), checkPassword() |

### Javadoc generálása

```bash
mvn javadoc:javadoc
```

A generált dokumentáció: `target/site/apidocs/index.html`

---

## 3. 📖 Felhasználói kézikönyv

**Fájl**: `FELHASZNALOI_KEZIKONYV.md`

### Tartalomjegyzék
1. ✅ Bevezetés
2. ✅ Telepítés (JDK 21, Maven)
3. ✅ Első indítás (regisztráció, bejelentkezés)
4. ✅ Funkciók
   - Regisztráció és bejelentkezés
   - Barátok kezelése (hozzáadás, barátkérelmek, eltávolítás)
   - Privát üzenetek (beszélgetés indítása, üzenet küldése)
   - Csoportok kezelése (létrehozás, tagok hozzáadása, szerepkörök, törlés)
5. ✅ Hibaelhárítás (gyakori problémák és megoldások)

### Kiemelések
- 📝 Részletes lépésről-lépésre útmutatók
- ⚠️ Figyelmeztetések (pl. jelszó nem visszaállítható)
- 📊 Táblázatok (szerepkörök és jogosultságok)
- 🔧 Technikai részletek (adatformátum, limitek, architektúra)

---

## 4. 🏗️ Architektúra

### MVC (Model-View-Controller) minta

```
┌─────────────────────────────────────────────┐
│               VIEW (UI)                     │
│  LoginFrame, MainFrame, ChatWindows, ...   │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│           CONTROLLER                        │
│           AppController                     │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│             MODEL                           │
│    User, Message, Group, Permissions        │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│          PERSISTENCE                        │
│      DataStore, FileManager                 │
└─────────────────────────────────────────────┘
```

### Design Pattern-ek

1. **MVC (Model-View-Controller)**
   - Szeparálja az üzleti logikát a UI-tól

2. **Template Method** (BaseChatWindow)
   - Abstract osztály definiálja a chat ablak szkeletét
   - PrivateChatWindow és GroupChatWindow implementálják a specifikus részeket

3. **Singleton-like** (AppController)
   - Egy controller instance az egész alkalmazásban

4. **DAO (Data Access Object)** (DataStore)
   - Absztrahálja az adatelérést

5. **Utility Class** (Permissions, UiMessages, ChatUi, PasswordUtil)
   - Statikus helper metódusok

---

## 5. 🧪 Tesztelés

### Teszt lefedettség

**Fájl**: `src/test/java/hu/prog3/offlinechatprog3/ApplicationTest.java`

| Teszteset | Státusz | Leírás |
|-----------|---------|--------|
| testUserRegistration | ✅ | Felhasználó regisztráció validálása |
| testFriendRequestWorkflow | ✅ | Barátkérelem teljes folyamata |
| testPrivateMessaging | ✅ | Privát üzenetküldés |
| testGroupCreation | ✅ | Csoport létrehozása |
| testGroupPermissions | ✅ | Szerepkör-alapú jogosultságok |
| testGroupMessaging | ✅ | Csoportos üzenetküldés |
| ... | ✅ | További 11 teszteset |

**Összes teszt**: 17 ✅ (100% sikeres)

```bash
mvn test
```

---

## 6. 📦 Függőségek

### pom.xml

```xml
<dependencies>
    <!-- BCrypt jelszó titkosítás -->
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>

    <!-- JUnit 5 tesztelés -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.9.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 7. ⚙️ Konfiguráció

### Alkalmazás limitek

| Paraméter | Érték | Helye |
|-----------|-------|-------|
| MIN_USERNAME_LENGTH | 3 | AppController |
| MAX_USERNAME_LENGTH | 20 | AppController |
| MAX_MESSAGE_LENGTH | 1000 | AppController |
| MAX_GROUP_NAME_LENGTH | 30 | AppController |
| UI_REFRESH_INTERVAL | 10 sec | MainFrame |

### Adatfájl

- **Útvonal**: `data/offline-chat.dat`
- **Formátum**: Java Object Serialization
- **Automatikus mentés**: Minden műveletnél

---

## 8. 🔒 Biztonság

### Jelszókezelés
- **Algoritmus**: bcrypt (jBCrypt 0.4)
- **Hash tárolás**: User.passwordHash mező
- **Validáció**: PasswordUtil.checkPassword()

### Adatvédelem
- Nincs hálózati kommunikáció (offline)
- Adatok lokálisan tárolódnak
- Nincs plain text jelszó tárolás

---

## 9. 🐛 Ismert problémák és korlátozások

### Ismert bugok

✅ **Nincsenek aktív bugok** - Az összes ismert probléma javítva lett.

### Funkcionális korlátozások

1. **Nincs jelszó-visszaállítás** - Ha elfelejted, új felhasználót kell regisztrálnod
2. **Egy számítógép** - Nem támogatja a hálózati kommunikációt
3. **Nincs üzenet visszahívás** - Küldött üzeneteket nem lehet törölni (privát chatben)
4. **Nincs fájlküldés** - Csak szöveges üzenetek

### Technikai korlátozások

1. **Szerializáció** - Osztályváltoztatás inkompatibilis lehet a régi adatfájllal
2. **Nincs adatbázis** - Nagyobb adatmennyiségnél lassulhat
3. **Nincs indexelés** - Keresés O(n) komplexitású

---

## 10. 🚀 Jövőbeli fejlesztések

### Tervezett funkciók

- [ ] Jelszó-visszaállítás biztonsági kérdésekkel
- [ ] Üzenetek keresése
- [ ] Profilkép támogatás
- [ ] Fájlküldés
- [ ] Csoportos videóhívás
- [ ] Témák/színsémák

### Technikai fejlesztések

- [ ] Database migráció (SQLite/H2)
- [ ] REST API
- [ ] WebSocket valós idejű frissítés
- [ ] OAuth2 bejelentkezés
- [ ] End-to-end titkosítás

---

## 11. 📚 Hivatkozások

### Forráskód
- **Repository**: Prog3HW (GitHub)
- **Branch**: main
- **Commit**: (legutolsó)

### Külső dokumentációk
- [Java 21 API](https://docs.oracle.com/en/java/javase/21/docs/api/)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [jBCrypt](https://www.mindrot.org/projects/jBCrypt/)
- [Maven](https://maven.apache.org/guides/)

### Oktatási anyagok
- Programozás 3 kurzus
- Budapest Műszaki és Gazdaságtudományi Egyetem

---

## 📝 Verziókezelés

| Verzió | Dátum | Változások |
|--------|-------|-----------|
| 1.0 | 2024 | Kezdeti kiadás |

---

**Készítette**: isolapaul  
**Utolsó frissítés**: 2024  
**Státusz**: ✅ Aktív fejlesztés
