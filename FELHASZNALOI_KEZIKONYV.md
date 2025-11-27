# Felhasználói kézikönyv - Offline Chat Alkalmazás

## Tartalomjegyzék
1. [Bevezetés](#bevezetés)
2. [Telepítés](#telepítés)
3. [Első indítás](#első-indítás)
4. [Funkciók](#funkciók)
   - [Regisztráció és bejelentkezés](#regisztráció-és-bejelentkezés)
   - [Barátok kezelése](#barátok-kezelése)
   - [Privát üzenetek](#privát-üzenetek)
   - [Csoportok kezelése](#csoportok-kezelése)
5. [Hibaelhárítás](#hibaelhárítás)

---

## Bevezetés

Az **Offline Chat** egy helyi hálózaton vagy egy számítógépen használható üzenetküldő alkalmazás Java Swing felhasználói felülettel. Nem igényel internet kapcsolatot, az összes adat lokálisan tárolódik a `data/offline-chat.dat` fájlban.

### Főbb funkciók:
- ✅ Felhasználói regisztráció és bejelentkezés bcrypt titkosítással
- ✅ Barátok hozzáadása és barátkérelmek kezelése
- ✅ Privát üzenetváltás barátokkal
- ✅ Csoportok létrehozása és kezelése
- ✅ Szerepkör alapú jogosultságkezelés csoportokban
- ✅ Valós idejű frissítés (10 másodpercenként)

---

## Telepítés

### Rendszerkövetelmények
- **Java**: JDK 21 vagy újabb
- **Maven**: 3.6 vagy újabb
- **Operációs rendszer**: Windows, Linux, macOS

### Telepítési lépések

1. **Projekt letöltése**
   ```bash
   git clone <repository-url>
   cd prog3hf-ready
   ```

2. **Függőségek telepítése**
   ```bash
   mvn clean install
   ```

3. **Alkalmazás futtatása**
   ```bash
   mvn exec:java -Dexec.mainClass="hu.prog3.offlinechatprog3.Main"
   ```

   Vagy:
   ```bash
   java -cp target/classes hu.prog3.offlinechatprog3.Main
   ```

---

## Első indítás

Az alkalmazás első indításakor egy bejelentkezési ablak fogad:

![Bejelentkezési képernyő]

### Első felhasználó létrehozása

1. Kattints a **"Regisztráció"** gombra
2. Add meg a felhasználóneved (3-20 karakter)
3. Add meg a jelszavad
4. Kattints az **"OK"** gombra

⚠️ **Figyelem**: A jelszavak bcrypt titkosítással tárolódnak, és **nem helyreállíthatóak**. Ha elfelejted, új felhasználót kell regisztrálnod.

---

## Funkciók

### Regisztráció és bejelentkezés

#### Regisztráció
- **Felhasználónév szabályok**:
  - Minimum 3 karakter
  - Maximum 20 karakter
  - Egyedi a rendszerben
- **Jelszó**: Bcrypt hash-eléssel tárolva

#### Bejelentkezés
1. Add meg a regisztrált felhasználóneved
2. Add meg a jelszavad
3. Kattints a **"Bejelentkezés"** gombra

---

### Barátok kezelése

#### Barát hozzáadása

1. A főablakban kattints a **"Barát hozzáadása"** gombra
2. Válassz egy felhasználót a listából
3. A kiválasztott felhasználó megkapja a barátkérelmet

#### Bejövő barátkérelmek

1. Menü: **Barátok → Bejövő kérelmek**
2. Válassz egy kérelmet a listából
3. Kattints **"Elfogad"** vagy **"Elutasít"** gombra

#### Kimenő barátkérelmek

1. Menü: **Barátok → Kimenő kérelmek**
2. Láthatod, kiknek küldtél barátkérelmet
3. Visszavonhatod a **"Visszavon"** gombbal

#### Barát eltávolítása

1. Válassz ki egy barátot a listából
2. Kattints a **"Barát eltávolítása"** gombra
3. Erősítsd meg a műveletet

---

### Privát üzenetek

#### Privát beszélgetés indítása

1. A **"Barátok"** fülön válassz ki egy barátot
2. Kattints rá duplán **VAGY** nyomd meg az **Enter** billentyűt
3. Megnyílik a privát chat ablak

#### Üzenet küldése

1. Írj be egy üzenetet az alsó szövegmezőbe
2. Kattints a **"Küldés"** gombra **VAGY** nyomd meg az **Enter** billentyűt
3. Az üzenet maximum 1000 karakter lehet

⚠️ **Fontos**: Csak barátokkal lehet privát üzenetet váltani. Ha valaki eltávolítja a baráti kapcsolatot, nem küldhetsz neki több üzenetet.

#### Üzenetek frissítése

- Az üzenetek automatikusan frissülnek 10 másodpercenként
- A legújabb üzenetek lent jelennek meg
- A chat ablak automatikusan görget az utolsó üzenethez

---

### Csoportok kezelése

#### Csoport létrehozása

1. Kattints a **"Csoport létrehozása"** gombra
2. Add meg a csoport nevét (maximum 30 karakter)
3. Automatikusan **Adminisztrátor** szerepet kapsz

#### Csoportkezelő ablak megnyitása

1. Menü: **Csoportok → Csoportok kezelése**
2. Itt láthatod az összes csoportodat

#### Tag hozzáadása csoporthoz

1. Csoportkezelőben válassz ki egy csoportot
2. Kattints **"Tagok listázása"** gombra
3. Kattints **"Tag hozzáadása"** gombra
4. Válassz egy barátot és adj meg szerepet

#### Csoportüzenet küldése

1. A **"Csoportok"** fülön válassz ki egy csoportot
2. Kattints rá duplán **VAGY** nyomd meg az **Enter** billentyűt
3. Csak akkor küldhetsz üzenetet, ha van `GROUP_SEND_MESSAGE` jogosultságod

#### Szerepkörök és jogosultságok

Az alapértelmezett szerepek:

| Szerepkör       | Jogosultságok                                           |
|----------------|---------------------------------------------------------|
| **Adminisztrátor** | Minden jogosultság (ALL)                              |
| **Résztvevő**    | Üzenet küldése (GROUP_SEND_MESSAGE)                   |
| **Olvasó**       | Csak olvasás (nincs aktív jogosultság)                |

**Elérhető jogosultságok**:
- `ALL` - Teljes hozzáférés
- `GROUP_SEND_MESSAGE` - Üzenet küldése
- `GROUP_ADD_MEMBER` - Tag hozzáadása
- `GROUP_REMOVE_MEMBER` - Tag eltávolítása
- `GROUP_DELETE_MESSAGES` - Üzenet törlése
- `GROUP_DELETE_GROUP` - Csoport törlése
- `GROUP_READ` - Olvasás

#### Üzenet törlése csoportból

1. A csoportkezelőben válassz ki egy csoportot
2. Kattints **"Üzenetek listázása"** gombra
3. Válassz ki egy üzenetet
4. Kattints **"Üzenet törlése"** gombra
5. Szükséges jogosultság: `GROUP_DELETE_MESSAGES`

#### Csoport törlése

1. A csoportkezelőben válassz ki egy csoportot
2. Kattints **"Csoport törlése"** gombra
3. Csak adminisztrátorok törölhetnek csoportot
4. A törlés végleges és visszavonhatatlan!

---

## Hibaelhárítás

### "Nincs elérhető felhasználó hozzáadáshoz"

**Ok**: Nincsenek más regisztrált felhasználók, vagy már mindenkinek küldtél barátkérelmet.

**Megoldás**:
- Regisztrálj több felhasználót
- Ellenőrizd a bejövő/kimenő barátkérelmeket

### "Sikertelen barátkérelem küldése"

**Okok**:
1. A felhasználó nem létezik
2. Már barátok vagytok
3. Már küldtél neki barátkérelmet

**Megoldás**:
- Ellenőrizd a barátlistádat
- Ellenőrizd a kimenő kérelmeket

### "Nem küldhetsz üzenetet ennek a felhasználónak"

**Ok**: Már nem vagytok barátok (valaki eltávolította a baráti kapcsolatot).

**Megoldás**:
- Küldj új barátkérelmet
- Várj, amíg elfogadja

### "Nincs jogosultságod üzenet küldésére ebben a csoportban"

**Ok**: A szerepedhez nem tartozik `GROUP_SEND_MESSAGE` jogosultság.

**Megoldás**:
- Kérj meg egy adminisztrátort, hogy adjon jogosultságot
- Az adminisztrátor módosíthatja a szerepedet

### Adatok nem frissülnek

**Megoldás**:
- Az alkalmazás 10 másodpercenként frissít automatikusan
- Bezárhatod és újra megnyithatod az ablakot
- Indítsd újra az alkalmazást

### Jelszó reset

⚠️ **Nincs jelszó-visszaállítás!**

Ha elfelejted a jelszavad:
1. Töröld a `data/offline-chat.dat` fájlt (⚠️ **minden adat elvész!**)
2. Indítsd újra az alkalmazást
3. Regisztrálj újra

### Adatok biztonsági mentése

**Automatikus mentés**: Minden művelet után automatikusan menti a `data/offline-chat.dat` fájlba.

**Manuális biztonsági mentés**:
```bash
cp data/offline-chat.dat data/offline-chat-backup.dat
```

**Visszaállítás**:
```bash
cp data/offline-chat-backup.dat data/offline-chat.dat
```

---

## Technikai részletek

### Adatformátum
- Fájl: `data/offline-chat.dat`
- Formátum: Java Object Serialization
- Titkosítás: bcrypt jelszó hash

### Limitek
- Felhasználónév: 3-20 karakter
- Csoportnév: maximum 30 karakter
- Üzenet: maximum 1000 karakter
- Frissítési gyakoriság: 10 másodperc

### Architektúra
- **Model**: User, Message, Group, Permissions
- **View**: LoginFrame, MainFrame, PrivateChatWindow, GroupChatWindow
- **Controller**: AppController
- **Persistence**: DataStore, FileManager

---

## Kapcsolat és támogatás

**Projekt**: Prog3HF - Offline Chat
**Verzió**: 1.0
**Készítő**: [isolapaul]
**Dátum**: 2024

---

## Licenc

Ezt az alkalmazást oktatási célokra készült. Bármilyen célra szabadon használható és módosítható.
