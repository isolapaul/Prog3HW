# Offline Chat — Projekt dokumentáció

**Tartalom:**
1. Osztálydiagram
2. Felhasználói kézikönyv

---

## 1. Osztálydiagram

![Osztálydiagram](osztalydiagram.png)

---

## 2. Felhasználói kézikönyv

### Bevezetés

Az **Offline Chat** egy számítógépen használható üzenetküldő alkalmazás Java Swing felhasználói felülettel. Nem igényel internet kapcsolatot, az összes adat lokálisan tárolódik a `data/offline-chat.dat` fájlban.

**Főbb funkciók:**
- Felhasználói regisztráció és bejelentkezés bcrypt titkosítással
- Barátok hozzáadása és barátkérelmek kezelése
- Privát üzenetváltás barátokkal
- Csoportok létrehozása és kezelése
- Szerepkör alapú jogosultságkezelés csoportokban
- Valós idejű frissítés (10 másodpercenként)

---

### Első indítás

Az alkalmazás indításakor egy bejelentkezési ablak fogad.

**Első felhasználó létrehozása:**

1. Kattints a **"Regisztráció"** gombra
2. Add meg a felhasználóneved (3-20 karakter)
3. Add meg a jelszavad
4. Kattints az **"OK"** gombra

---

### Funkciók

#### Regisztráció és bejelentkezés

**Regisztráció:**
- **Felhasználónév szabályok**:
  - Minimum 3 karakter
  - Maximum 20 karakter
  - Egyedi a rendszerben
- **Jelszó**: Bcrypt hash-eléssel tárolva

**Bejelentkezés:**
1. Add meg a regisztrált felhasználóneved
2. Add meg a jelszavad
3. Kattints a **"Bejelentkezés"** gombra

---

#### Barátok kezelése

**Barát hozzáadása:**

1. A főablakban kattints a **"Barát hozzáadása"** gombra
2. Válassz egy felhasználót a listából
3. A kiválasztott felhasználó megkapja a barátkérelmet

**Bejövő barátkérelmek:**

1. Menü: **Barátok → Bejövő kérelmek**
2. Válassz egy kérelmet a listából
3. Kattints **"Elfogad"** vagy **"Elutasít"** gombra

**Kimenő barátkérelmek:**

1. Menü: **Barátok → Kimenő kérelmek**
2. Láthatod, kiknek küldtél barátkérelmet
3. Visszavonhatod a **"Visszavon"** gombbal

**Barát eltávolítása:**

1. Válassz ki egy barátot a listából
2. Kattints a **"Barát eltávolítása"** gombra
3. Erősítsd meg a műveletet

---

#### Privát üzenetek

**Privát beszélgetés indítása:**

1. A **"Barátok"** fülön válassz ki egy barátot
2. Kattints rá duplán **VAGY** nyomd meg az **Enter** billentyűt
3. Megnyílik a privát chat ablak

**Üzenet küldése:**

1. Írj be egy üzenetet az alsó szövegmezőbe
2. Kattints a **"Küldés"** gombra **VAGY** nyomd meg az **Enter** billentyűt
3. Az üzenet maximum 1000 karakter lehet


**Üzenetek frissítése:**

- Az üzenetek automatikusan frissülnek 1,5 másodpercenként
- A legújabb üzenetek lent jelennek meg
- A chat ablak automatikusan görget az utolsó üzenethez

---

#### Csoportok kezelése

**Csoport létrehozása:**

1. Kattints a **"Csoport létrehozása"** gombra
2. Add meg a csoport nevét (maximum 30 karakter)
3. Automatikusan **Adminisztrátor** szerepet kapsz

**Csoportkezelő ablak megnyitása:**

1. Menü: **Csoportok → Csoportok kezelése**
2. Itt láthatod az összes csoportodat

**Tag hozzáadása csoporthoz:**

1. Csoportkezelőben válassz ki egy csoportot
2. Kattints **"Tagok listázása"** gombra
3. Kattints **"Tag hozzáadása"** gombra
4. Válassz egy barátot és adj meg szerepet

**Csoportüzenet küldése:**

1. A **"Csoportok"** fülön válassz ki egy csoportot
2. Kattints rá duplán **VAGY** nyomd meg az **Enter** billentyűt
3. Csak akkor küldhetsz üzenetet, ha van hozzá jogosultságod

**Szerepkörök és jogosultságok:**

Az alapértelmezett szerepek:

| Szerepkör       | Jogosultságok                                           |
|----------------|---------------------------------------------------------|
| **Adminisztrátor** | Minden jogosultság                       |
| **Résztvevő**    | Üzenet küldése               |
| **Olvasó**       | Csak olvasás              |

**Elérhető jogosultságok:**
- `ALL` - Teljes hozzáférés
- `GROUP_SEND_MESSAGE` - Üzenet küldése
- `GROUP_ADD_MEMBER` - Tag hozzáadása
- `GROUP_REMOVE_MEMBER` - Tag eltávolítása
- `GROUP_DELETE_MESSAGES` - Üzenet törlése
- `GROUP_DELETE_GROUP` - Csoport törlése
- `GROUP_READ` - Olvasás

**Üzenet törlése csoportból:**

1. A csoportkezelőben válassz ki egy csoportot
2. Kattints **"Üzenetek listázása"** gombra
3. Válassz ki egy üzenetet
4. Kattints **"Üzenet törlése"** gombra
5. Szükséges jogosultság: `GROUP_DELETE_MESSAGES`

**Csoport törlése:**

1. A csoportkezelőben válassz ki egy csoportot
2. Kattints **"Csoport törlése"** gombra
3. Csak adminisztrátorok törölhetnek csoportot
4. A törlés végleges és visszavonhatatlan!

---

### Technikai részletek

**Adatformátum:**
- Fájl: `data/offline-chat.dat`
- Formátum: Java Object Serialization
- Titkosítás: bcrypt jelszó hash

**Limitek:**
- Felhasználónév: 3-20 karakter
- Csoportnév: maximum 30 karakter
- Üzenet: maximum 1000 karakter
- Frissítési gyakoriság: 1,5 másodperc
