Offline Chat - prog3

Ez egy egyszerű, offline (lokális) csevegőalkalmazás Java 11 és Swing alapokon, a programozás3 kurzushoz.
Az alkalmazás állapota Java beépített szerializációval (.dat) kerül mentésre a `data/offline-chat.dat` fájlba.

## Főbb funkciók
- Bejelentkezés és regisztráció (helyi felhasználólista)
- Barátkezelés: barátkérés küldése, bejövő/küldött kérések elfogadása / elutasítása / visszavonása, barát eltávolítása
- Privát üzenetek: külön ablak (dupla katt) + főablak előnézet automatikus frissítéssel
- Csoportok: létrehozás, tagok megtekintése, hozzáadás / eltávolítás jogosultság alapján
- Szerepkörök: alap szerepek (Adminisztrátor, Résztvevő, Olvasó) + egyedi szerepek saját jogosultságokkal
- Jogosultság alapú műveletek: üzenetküldés, tag hozzáadás/eltávolítás, üzenet törlés, csoport törlés
- Csoport üzenetek törlése: erre jogosult szerepek egyenként törölhetik az üzeneteket
- Multi-instance "live" frissítés: több futó példány felismeri a fájl módosulását és automatikusan betölti a változásokat
- Teljes állapot egyetlen szerializált `DataStore` objektumban (`data/offline-chat.dat`), Java szerializációval

### Szerepkör jogosultság kulcsok
Centralizálva a `Permissions` osztályban:
```
ALL
GROUP_SEND_MESSAGE
GROUP_ADD_MEMBER
GROUP_REMOVE_MEMBER
GROUP_DELETE_MESSAGES
GROUP_DELETE_GROUP
GROUP_READ
```
Az `ALL` bármely ellenőrzött jogosultságot igaznak tekint.

### Csoport üzenet törlés
`GroupManager` ablakban az "Üzenetek..." gomb megnyit egy listát, ahol a `GROUP_DELETE_MESSAGES` joggal rendelkező felhasználó kiválaszthatja és törölheti az üzeneteket.

### Előnézet / Live működés
- Főablak bal oldali tabok: Barátok / Csoportok
- Kiválasztott partner vagy csoport üzenetei jobb oldalon előnézetben, 1.5s periódussal frissül
- Dupla katt: saját külön chat ablak (privát vagy csoport)
- Nyitott ablakok szintén automatikusan frissülnek

Futtatás
1) Fordítás és tesztek futtatása:

```bash
mvn test
```

2) Építés (futtatható fat-jar):

```bash
mvn package
```

A kész futtatható jar a `target/` mappába lesz generálva (név: `offline-chat-prog3-1.0-SNAPSHOT-shaded.jar`).
Futtatás:

```bash
java -jar target/offline-chat-prog3-1.0-SNAPSHOT-shaded.jar
```

## Fejlesztés
- Forrás: `src/main/java`, tesztek: `src/test/java`
- Állapot: `DataStore` (Serializable) + `FileManager` mentés / betöltés
- Jogosultságok: `Permissions` konstansok egységesítve (ismétlés csökkentése)

## Megjegyzés
- Jelszavak egyszerű hash / string formában (tanulmányi jelleg, nem produkciós biztonság)
- A UI tudatosan egyszerű ("fapados"), a funkciók demonstrálására optimalizálva
- További ötletek: chat buborékok, több jogosultsági szint, üzenet keresés, export
