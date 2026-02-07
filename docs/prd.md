# Product Requirements Document (PRD)

**Project:** Task Analysis  
**Date:** 2026-01-28  
**Version:** 1.0  
**Author:** BMad

---

## 1. Vision & Alignment

### Product Vision

A **Task Analysis** alkalmazás lehetővé teszi egyéni felhasználók számára, hogy részfeladat szinten mérjék és elemezzék munkájuk időráfordítását. Az alkalmazás fő értéke a **részletes időrögzítés egyszerűsége**: egyetlen gombnyomással válthatnak részfeladatok között, miközben az app automatikusan rögzíti az időadatokat.

### What Makes It Special

**"Egy gombnyomás = teljes részfeladat-szintű időelemzés"**

A felhasználók:
- **Látják, melyik részfeladat viszi el az időt** (nem csak a teljes feladat időt)
- **Tesztfeladatoknál megértik** a nehézségi szintet és az értéket
- **Összehasonlítják** a tervezett és tényleges teljesítményt (pontértékek)
- **Automatikus váltás** részfeladatok között – nem kell manuálisan stop/start

### Target Audience

- **Egyéni felhasználók**, akik önmaguk produktivitását mérik
- **Tanulók**, akik teszt/tanulási feladatokat elemeznek
- **Önfejlesztők**, akik munkafolyamataikat optimalizálják
- **Személyes használatra** (ingyenes)

---

## 2. Project Classification

### Project Type
**Web Application** (Full-stack SPA)

**Detection Signals:**
- Angular frontend + Spring Boot backend
- User authentication (JWT)
- Database persistence (MySQL)
- Dashboard és analytics UI

### Domain Type
**General / Productivity Tools**

**Complexity Level:** Low-Medium

**Domain Context:**
- Általános produktivitás és időrögzítés
- Nincs speciális domain követelmény (pl. egészségügy, pénzügy)
- Személyes adatok kezelése (GDPR aware)

---

## 3. Product Differentiator

### Unique Value Proposition

**"Részfeladat-alapú időmérés egyetlen gombnyomással"**

**Mi teszi különlegessé?**
1. **Részfeladat szintű granularitás** – nem csak feladat szinten mérsz
2. **Automatikus váltás** – új részfeladat indítása megállítja az előzőt
3. **Pontértékek** – tervezett vs. tényleges (előrejelzés vs. valóság)
4. **Elemzés fókusz** – Dashboard mutatja, melyik részfeladat a leglassabb/legnehezebb

**Példa használati eset:**
- **Teszt feladat:** 10 kérdésből álló teszt
- **Részfeladatok:** Minden kérdés = 1 részfeladat
- **Időmérés:** Látod, melyik kérdés vitt 5 percet, melyik csak 30 másodpercet
- **Pontértékek:** Tervezett: 10 pont/kérdés | Tényleges: 8 pont (nehéz volt)
- **Insight:** "A 3. kérdés vitte a legtöbb időt, mert összetett volt"

---

## 4. Success Criteria

### What Winning Looks Like

**Felhasználói siker:**
- Felhasználók **azonnal megértik**, melyik részfeladatra megy el a legtöbb idő
- **"Aha" élmény:** "Ezt nem gondoltam, hogy ez vesz ennyi időt!"
- Visszatérő használat: **heti 3+ alkalommal** használják feladatok elemzésére

**Technikai siker:**
- **Egy gombnyomás váltás** < 200ms válaszidő
- **Adatvesztés:** 0% (minden timer esemény perzisztált)
- **Intuitív UX:** Új felhasználó < 2 perc alatt megérti a működést

**Érték metrika:**
- Felhasználók **megosztják az insightjaikat** ("Rájöttem, hogy X veszi az időmet")
- **5+ feladatot elemeztek** az első hét során

---

## 5. Scope Definition

### MVP Scope (Must Have)

**Feladat menedzsment:**
- Feladat létrehozása, szerkesztése, törlése
- Feladathoz **részfeladatok számának** megadása (pl. 10 részfeladat)
- Feladatok **kategorizálása** (pl. "Tanulás", "Projekt", "Teszt")
- Kategória CRUD (létrehozás, szerkesztés, törlés)

**Timer funkció:**
- **Részfeladat gomb** minden részfeladathoz (pl. "1", "2", "3"...)
- **Automatikus váltás:** Új részfeladat indítása → előző megáll
- **Visszatérés:** Korábbi részfeladat újraindítása → idő hozzáadódik
- Időadatok mentése adatbázisba (perzisztencia)

**Pontértékek:**
- **Tervezett pontérték** megadása részfeladatonként (elvárt)
- **Tényleges pontérték** rögzítése részfeladatonként (teljesített)

**Dashboard & Elemzés:**
- Feladat részletes nézet: részfeladatonként idő + pontértékek
- **Összesítések:** Melyik részfeladat vitte a legtöbb időt?
- Idő vs. pontérték vizualizáció (nehézség mutatója)

**Exportálás:**
- Feladat adatok exportálása **Excel** formátumban
- Feladat adatok exportálása **PDF** formátumban
- Részfeladat szintű részletesség az exportban

**Responsive Design:**
- **Mobil optimalizált** UI (telefonon használható)
- **Tablet támogatás** (közepes képernyők)
- **Desktop** UI (teljes funkciókészlet)

**Felhasználó kezelés:**
- Regisztráció (email + jelszó)
- Bejelentkezés (**JWT token-based** authentication)
- Kijelentkezés
- Profil kezelés (név, email módosítás)

---

### Growth Features (Post-MVP)

**Statisztikák & Riportok:**
- Heti/havi összesítések (hány feladat, átlagos idő)
- Trendek (időráfordítás csökken/növekszik?)
- Kategória szintű elemzés (melyik kategóriában töltöm a legtöbb időt?)

**Fejlett dashboard:**
- Interaktív diagramok (oszlop, kör, vonal grafikonok)
- Szűrők (dátum, kategória, pontérték alapján)
- Több feladat összehasonlítása

**Felhasználói élmény:**
- Sötét mód (Dark mode)
- Értesítések (emlékeztető befejezetlen feladatokra)
- Gyorsgombok (billentyűzet shortcuts timer váltáshoz)

---

### Vision Features (Future)

**AI-alapú előrejelzés:**
- "Ez a feladat ~45 percet fog tartani" (korábbi feladatok alapján)
- Nehézségi szint becslés (pontérték alapján)

**Team verzió:**
- Többfelhasználós támogatás
- Csoportos feladatok megosztása
- Közös dashboard

**Integráció:**
- Naptár integráció (Google Calendar, Outlook)
- Projekt menedzsment eszközök (Trello, Jira)

---

## 6. Project-Specific Requirements

### Web Application Architecture

**Frontend:**
- Angular (Standalone Components)
- TypeScript
- SCSS styling
- Responsive layout (Bootstrap vagy Angular Material)

**Backend:**
- Spring Boot 3.x
- Spring Security (JWT authentication)
- JPA/Hibernate
- MySQL database
- Flyway migration

**Infrastructure:**
- RESTful API
- JWT token-based authentication
- CORS support (frontend-backend kommunikáció)

---

### Authentication & Security

**JWT Token-based Authentication:**
- **Access Token:** 15 perc élettartam (rövid, biztonságos)
- **Refresh Token:** 7 nap élettartam (hosszabb, új access token szerzéshez)
- **Tárolás:** HttpOnly Cookie (XSS védelem)
- **CSRF Protection:** Bekapcsolva (Spring Security)
- **SameSite:** Strict cookie attribútum

**Regisztráció:**
- Email cím (egyedi, validáció)
- Jelszó követelmények:
  - Minimum 8 karakter
  - Legalább 1 nagybetű
  - Legalább 1 kisbetű
  - Legalább 1 szám
- BCrypt jelszó titkosítás

**Elfelejtett jelszó funkció (MVP):**
- Email-alapú jelszó visszaállítás
- Egyszer használatos token (1 órás érvényesség)
- Új jelszó beállítása

**Session kezelés:**
- Egy felhasználó = egy aktív session
- Token refresh automatikus (access token lejárta előtt)

---

### Timer Működés

**Alapvető működés:**
- Részfeladat gomb kattintás → Timer indul
- Új részfeladat gomb → Előző megáll, új indul (automatikus váltás)
- Ugyanarra a részfeladatra kattintás → Idő hozzáadódik (累積 időmérés)

**Böngésző bezárás:**
- Timer **megáll** automatikusan
- Utolsó időpont mentése adatbázisba
- Újranyitáskor: Timer állapot = megállítva

**Több eszköz használat:**
- Ha mobil ÉS desktop is be van jelentkezve:
  - **Mobil timer érvényes** (utolsó interakció prioritás)
  - Desktop timer megáll (konfliktus kezelés)
- Backend figyelmezteti a felhasználót, ha másik eszközön timer fut

**Adatvesztés védelem:**
- Timer események **azonnal** perzisztálnak adatbázisba
- Időbélyegek: Start time + End time (pontosság: másodperc)

---

### Exportálás Specifikáció

**Excel export:**
- Fájlformátum: `.xlsx` (Apache POI használatával)
- Oszlopok:
  1. **Részfeladat #** (Például: 1, 2, 3...)
  2. **Idő** (Formátum: HH:MM:SS vagy "5 perc 32 másodperc")
  3. **Tervezett pont** (Numerikus érték)
  4. **Tényleges pont** (Numerikus érték)
  5. **Feladat** (Feladat neve)
  6. **Kategória** (Kategória neve)
- Első sor: Header (vastag betű)
- Utolsó sor: Összesítés (teljes idő, átlag pontszám)

**PDF export:**
- Fájlformátum: `.pdf` (iText vagy Apache PDFBox használatával)
- Tartalom:
  - **Fejléc:** Feladat neve, Kategória, Exportálás dátuma
  - **Táblázat:** Részfeladat adatok (mint Excel-ben)
  - **Összegzés:** Teljes idő, tervezett/tényleges pontszámok összege
  - **Diagramok (opcionális):** Idő eloszlás részfeladatonként (oszlopdiagram)
- Formázás: Professzionális megjelenés (táblázat border, színezés)

**Delivery módszerek (Adaptive UX):**
- **Desktop-on (≥ 768px):**
  - 📥 **Közvetlen letöltés** - Fájl automatikus letöltése böngészőben
  - 📧 **Email küldés** - Mellékletként a felhasználó email címére
- **Mobilon (< 768px):**
  - 📧 **Email küldés** (kizárólag) - Mellékletként, mobil-barát megoldás

**Email delivery specifikáció:**
- Email cím: Bejelentkezett felhasználó email címe (users.email)
- Tárgy: `Task Analysis Export - {Feladat neve}`
- Törzs: 
  ```
  Szia {Felhasználó neve}!
  
  Az exportált feladat adataid csatolva találod.
  
  Feladat: {Feladat neve}
  Kategória: {Kategória neve}
  Export dátum: {Dátum}
  
  Üdv,
  Task Analysis
  ```
- Melléklet: Generált Excel/PDF fájl
- Technológia: Spring Boot Mail Sender (SMTP)

---

### Responsive Design

**Mobil nézet (< 768px):**
- **Timer gombok:** Nagy méret (min. 60x60px), ujjal könnyen nyomhatók
- **Részfeladat gombok:** Görgetős lista (vertical scroll)
- **Dashboard:** Összecsukható kártyák (collapse)
- **Navigáció:** Hamburger menü
- **Feladat lista:** Lista nézet (nem grid)

**Tablet nézet (768px - 1024px):**
- Timer gombok: Közepes méret (grid layout 3-4 oszlop)
- Dashboard: 2 oszlopos layout
- Navigáció: Teljes menüsor

**Desktop nézet (> 1024px):**
- Timer gombok: Grid layout (5-6 oszlop)
- Dashboard: 3 oszlopos layout vagy sidebar + main
- Navigáció: Teljes menüsor + user profil jobb felső sarokban

**Touch-friendly elemek:**
- Minimális kattintható terület: 44x44px (Apple Human Interface Guidelines)
- Gombok közötti távolság: min. 8px
- Swipe gestures támogatása (opcionális: swipe részfeladatok között)

---

## 7. UX Principles

### Visual Personality

**Minimál/tiszta megjelenés - Produktivitás fókusz**

- Fehér/világos háttér alapértelmezett
- Kevés szín használata (1-2 akcentszín)
- Egyszerű ikonok
- Tiszta tipográfia (sans-serif betűtípus)
- Tágas elrendezés (white space használat)
- Zaj-mentes UI (nincs felesleges dekoráció)

**Cél:** A felhasználó a feladataira koncentráljon, ne az UI-ra.

---

### Timer Gombok Megjelenése

**Számozott + színkódolt gombok:**

**Inaktív állapot:**
- Fehér háttér / világos szürke keret
- Fekete számozás (1, 2, 3...)
- Hover: enyhe árnyék

**Aktív állapot (fut a timer):**
- **Zöld háttér** (pl. #4CAF50)
- Fehér számozás
- Pulzáló animáció (subtly breathing effect)
- "Futás alatt" indikátor (kis ikon vagy időmutató)

**Befejezett állapot:**
- Szürke háttér (pl. #E0E0E0)
- Szürke számozás
-Checkmark ikon (✓) ha van időadat rögzítve

**Mobil méret:**
- Nagy gombok (min. 60x60px)
- Jól olvasható számozás (18px+ betűméret)

**Desktop méret:**
- Közepes gombok (min. 50x50px)
- Grid layout (5-6 oszlop)

---

### Dashboard Vizualizáció (MVP)

**Táblázatos megjelenés (egyszerű, adatfókusz):**

**Feladat részletes nézet:**
- Táblázat oszlopok:
  1. Részfeladat # (számozás)
  2. Időfelhasználás (HH:MM:SS formátum)
  3. Tervezett pont
  4. Tényleges pont
  5. Státusz (Befejezett/Folyamatban/Nem kezdett)
- Összesítő sor alul: Teljes idő, Átlagos pontszám
- Rendezés támogatása (időre, pontra kattintva)

**Feladat lista nézet:**
- Kártyák / lista elemek
- Feladat neve, kategória, teljes idő, státusz
- Kattintásra: részletes nézet

**Post-MVP:** Diagramok (oszlopdiagram idő eloszláshoz, kördiagram részfeladatok arányához)

---

### Színséma

**Világos mód (alapértelmezett):**
- Háttér: Fehér (#FFFFFF) vagy világos szürke (#F5F5F5)
- Szöveg: Sötét szürke (#212121) vagy fekete
- Akcentszín: Kék (#2196F3) vagy zöld (#4CAF50) - linkek, gombok
- Timer aktív: Zöld (#4CAF50)
- Timer inaktív: Szürke (#E0E0E0)
- Hibák/figyelmeztetések: Piros (#F44336), Sárga (#FFC107)

**Sötét mód:** Post-MVP (opcionális)

---

### Key Interactions

**Timer indítás/váltás:**
1. Felhasználó kattint részfeladat gombra
2. **Azonnali vizuális feedback:** Gomb zöldre vált (<100ms)
3. Timer számláló jelenik meg a gomb mellett vagy felette
4. Előző aktív gomb szürkére vált (ha volt)
5. Toast notification (opcionális): "Részfeladat 3 elindítva"

**Feladat befejezés:**
1. Felhasználó kattint "Befejezés" gombra
2. Modal/Dialog: "Biztosan befejezed? Az aktív timer megáll."
3. Megerősítés után: Dashboard nézetre navigálás
4. Összegzés megjelenítése

**Export funkció:**
1. Felhasználó választ formátumot (Excel / PDF)
2. Loading indikátor
3. Fájl letöltése automatikus (browser download)
4. Success message: "Export kész! Letöltve: feladat_neve.xlsx"

---

## 8. Functional Requirements

### User Management

**FR1:** Felhasználó tud regisztrálni email cím és jelszó megadásával  
**FR2:** Felhasználó tud bejelentkezni email és jelszó használatával  
**FR3:** Felhasználó tud kijelentkezni (JWT token invalidálás)  
**FR4:** Felhasználó tud jelszót visszaállítani email-alapú token linkkel  
**FR5:** Felhasználó tudja módosítani profil adatait (név, email)  
**FR6:** Felhasználó tudja megváltoztatni jelszavát (régi jelszó megerősítés után)

---

### Category Management

**FR7:** Felhasználó tud kategóriát létrehozni (név megadásával)  
**FR8:** Felhasználó tudja kategóriák listáját megtekinteni  
**FR9:** Felhasználó tud kategóriát szerkeszteni (név módosítás)  
**FR10:** Felhasználó tud kategóriát törölni (megerősítés után)  
**FR11:** Kategória törlése nem törli a hozzárendelt feladatokat (feladat kategória = üres)

---

### Task Management

**FR12:** Felhasználó tud feladatot létrehozni:
- Feladat neve (kötelező)
- Kategória kiválasztása (opcionális)
- Részfeladatok száma (kötelező, min. 1, max. 100)
- Leírás (opcionális)

**FR13:** Felhasználó tudja feladatok listáját megtekinteni:
- Összes feladat listája
- Szűrés kategória szerint
- Rendezés (név, létrehozás dátuma, módosítás dátuma)
- Keresés feladat név alapján

**FR14:** Felhasználó tud feladatot szerkeszteni:
- Feladat név módosítása
- Kategória módosítása
- Leírás módosítása
- **Részfeladatok száma NEM módosítható** (adatintegritás védelem)

**FR15:** Felhasználó tud feladatot törölni:
- Megerősítő dialog megjelenik
- Törlés törli az összes részfeladat időadatot és pontértéket
- Visszavonhatatlan művelet

**FR16:** Felhasználó tud feladatot megnyitni részletes nézetben:
- Feladat részletek megjelenítése
- Részfeladatok listája (gombok)
- Timer interfész
- Időadatok és pontértékek megjelenítése

---

### Subtask & Timer Management

**FR17:** Felhasználó látja a részfeladat gombokat (számozott, 1-N ahol N = részfeladatok száma)

**FR18:** Felhasználó tud részfeladat timer-t indítani:
- Kattintás inaktív részfeladat gombra → Timer indul
- Start időbélyeg rögzítése adatbázisba
- Gomb állapot változás: zöld háttér, pulzáló animáció

**FR19:** Felhasználó tud részfeladatra váltani (automatikus stop + start):
- Kattintás új részfeladat gombra
- **Automatikusan:** Előző aktív timer megáll (end időbélyeg rögzítés)
- **Automatikusan:** Új timer indul (start időbélyeg rögzítés)
- Vizuális feedback (<100ms válaszidő)

**FR20:** Felhasználó tud visszatérni korábbi részfeladatra:
- Kattintás már befejezett részfeladat gombra
- Új időintervallum kezdődik (új start időbélyeg)
- Idő hozzáadódik a részfeladat korábbi időadataihoz (kumulatív)

**FR21:** Felhasználó tud timer-t manuálisan megállítani:
- "Stop" gomb kattintás
- End időbélyeg rögzítése
- Aktív részfeladat gomb állapot változás: szürke háttér

**FR22:** Rendszer automatikusan megállítja a timer-t böngésző bezáráskor:
- beforeunload event kezelés
- Utolsó időbélyeg mentése backend-re
- Session lezárás

**FR23:** Rendszer kezeli több eszköz egyidejű használatát:
- Ha mobil és desktop is aktív: mobil timer érvényes
- Desktop timer automatikusan megáll (konfliktus detektálás)
- Felhasználó értesítést kap: "Timer másik eszközön fut"

**FR24:** Felhasználó látja az aktuális részfeladat eltelt idejét real-time:
- Számláló jelenik meg aktív gomb mellett
- Frissítés: másodpercenként
- Formátum: MM:SS vagy HH:MM:SS

---

### Point Values

**FR25:** Felhasználó tud tervezett pontértéket beállítani részfeladatonként:
- Numerikus érték (0-1000)
- Szerkeszthető bármikor (feladat részletes nézetben)

**FR26:** Felhasználó tud tényleges pontértéket rögzíteni részfeladatonként:
- Numerikus érték (0-1000)
- Szerkeszthető bármikor (feladat részletes nézetben)

**FR27:** Rendszer összegzi a pontértékeket feladatonként:
- Tervezett pontok összege
- Tényleges pontok összege
- Különbség kalkulálása (tervezett - tényleges)

---

### Dashboard & Analytics

**FR28:** Felhasználó látja a feladat részletes dashboard-ot:
- Táblázat: Részfeladat #, Idő, Tervezett pont, Tényleges pont, Státusz
- Rendezés: időre, pontra kattintva
- Összesítő sor: Teljes idő, Átlag tervezett pont, Átlag tényleges pont

**FR29:** Felhasználó látja az időeloszlást részfeladatonként:
- Melyik részfeladat vitte a legtöbb időt (TOP 3)
- Melyik részfeladat vitte a legkevesebb időt

**FR30:** Felhasználó látja a nehézségi elemzést:
- Idő vs. pontérték arány (melyik részfeladat nehezebb mint gondoltad?)
- Tervezett vs. tényleges pontok eltérése részfeladatonként

**FR31:** Felhasználó látja a feladat státuszát:
- Hány részfeladat befejezett (van időadat)
- Hány részfeladat folyamatban
- Hány részfeladat nem kezdett
- Progress bar (%-os készültség)

---

### Export Functionality

**FR32:** Felhasználó tud feladat adatokat exportálni Excel formátumban:
- Fájlformátum: .xlsx
- Oszlopok: Részfeladat #, Idő, Tervezett pont, Tényleges pont, Feladat, Kategória
- Összesítő sor: Teljes idő, Átlag pontok
- Fájl név: `{feladat_neve}_{datum}.xlsx`

**FR33:** Felhasználó tud feladat adatokat exportálni PDF formátumban:
- Fájlformátum: .pdf
- Tartalom: Fejléc (feladat név, kategória, dátum), Táblázat, Összegzés
- Formázás: Professzionális megjelenés (táblázat border, színezés)
- Fájl név: `{feladat_neve}_{datum}.pdf`

**FR34:** Exportálás indikátor megjelenik:
- Loading spinner
- "Export készítése..." üzenet
- Success notification export után

---

### Responsive & Cross-Device

**FR35:** Alkalmazás teljesen használható mobil eszközön (< 768px):
- Timer gombok nagy méretűek (min. 60x60px)
- Dashboard görgetős lista nézet
- Érintés-barát interakciók

**FR36:** Alkalmazás teljesen használható tableten (768px - 1024px):
- Timer gombok grid layout (3-4 oszlop)
- Dashboard 2 oszlopos layout

**FR37:** Alkalmazás teljesen használható desktopon (> 1024px):
- Timer gombok grid layout (5-6 oszlop)
- Dashboard 3 oszlopos vagy sidebar + main layout

---

## 9. Non-Functional Requirements

### Performance

**NFR1:** Timer indítás/váltás vizuális feedback < 200ms  
**NFR2:** API válaszidő átlag < 500ms (95%-os percentilis)  
**NFR3:** Dashboard betöltési idő < 2 másodperc (kezdeti betöltés)  
**NFR4:** Feladat lista betöltési idő < 1 másodperc (max. 100 feladat esetén)  
**NFR5:** Export generálás idő < 5 másodperc (max. 100 részfeladat esetén)  
**NFR6:** Real-time timer frissítés: 1 másodperc pontossággal  
**NFR7:** Adatbázis lekérdezések optimalizálva (indexek használata)  
**NFR8:** Frontend asset méret < 2MB (minifikált JS/CSS)

---

### Security

**NFR9:** JWT token-based authentication (Access: 15 perc, Refresh: 7 nap)  
**NFR10:** Jelszavak BCrypt titkosítással tárolva (cost factor: 12)  
**NFR11:** HTTPS kötelező production környezetben  
**NFR12:** CSRF protection bekapcsolva (Spring Security)  
**NFR13:** HttpOnly cookie használata JWT tároláshoz (XSS védelem)  
**NFR14:** SameSite=Strict cookie attribútum  
**NFR15:** SQL injection védelem (Prepared Statements, JPA)  
**NFR16:** XSS védelem (Angular built-in sanitization)  
**NFR17:** Jelszó követelmények:
- Minimum 8 karakter
- Legalább 1 nagybetű, 1 kisbetű, 1 szám

**NFR18:** Refresh token rotation (új refresh token minden használatkor)  
**NFR19:** Rate limiting: Max. 100 kérés / perc / felhasználó (brute force védelem)  
**NFR20:** Session timeout: 30 perc inaktivitás után automatikus kijelentkezés

---

### Reliability & Data Integrity

**NFR21:** Adatvesztés megelőzése: Timer események azonnal perzisztálva  
**NFR22:** Tranzakció kezelés: Timer start/stop műveletek atomi tranzakcióban  
**NFR23:** Adatbázis backup: Napi automatikus backup (production)  
**NFR24:** Böngésző bezárásakor: Aktív timer automatikusan megáll, adatok mentése  
**NFR25:** Hibakezelés: Minden kritikus művelet try-catch blokkban  
**NFR26:** Logging: Minden hibás kérés logolva (backend)  
**NFR27:** Flyway migration használata: Minden schema változás verziókezelve  
**NFR28:** Rollback support: Flyway migration visszavonható (undo scriptek)

---

### Usability

**NFR29:** Új felhasználó < 2 perc alatt megérti a timer működést (intuitív UI)  
**NFR30:** Hibák felhasználóbarát üzenetekkel jelennek meg (nem stacktrace)  
**NFR31:** Form validáció: Azonnali feedback (real-time validáció)  
**NFR32:** Loading indikátorok minden hosszú műveletnél (>500ms)  
**NFR33:** Success/Error notifikációk konzisztens dizájnnal (Toast messages)  
**NFR34:** Accessibility: WCAG 2.1 AA szintű megfelelés (alapvető akadálymentesség)  
**NFR35:** Keyboard navigation támogatás (Tab, Enter, Esc billentyűk)

---

### Maintainability

**NFR36:** Flyway migration fájlok: Minden schema változás külön fájlban  
**NFR37:** Clean Code elvek: Readable, maintainable kód  
**NFR38:** Backend: Service layer elkülönítés (controller → service → repository)  
**NFR39:** Frontend: Component-based architecture (Angular best practices)  
**NFR40:** API dokumentáció: OpenAPI/Swagger (opcionális, post-MVP)  
**NFR41:** Git commit üzenetek: Értelmes, strukturált commit history  
**NFR42:** Code review: Pull request flow használata (GitHub)

---

### Compatibility

**NFR43:** Böngésző támogatás:
- Chrome 100+ ✅
- Firefox 100+ ✅
- Safari 15+ ✅
- Edge 100+ ✅
- Opera 85+ ✅

**NFR44:** Mobil böngésző támogatás:
- Chrome Mobile (Android) ✅
- Safari Mobile (iOS) ✅

**NFR45:** Operációs rendszer támogatás:
- Windows 10+ ✅
- macOS 11+ ✅
- Linux (Ubuntu 20.04+) ✅
- Android 9+ ✅
- iOS 14+ ✅

**NFR46:** Képernyő felbontás támogatás:
- Mobil: 360px - 768px ✅
- Tablet: 768px - 1024px ✅
- Desktop: 1024px - 3840px ✅

---

### Scalability (Post-MVP)

**NFR47:** Adatbázis: Horizontal scaling támogatás (read replicas)  
**NFR48:** Backend: Stateless API (több instance indítható)  
**NFR49:** Max. 1000 felhasználó támogatása (MVP skálázási cél)  
**NFR50:** Max. 10,000 feladat / felhasználó (adatbázis limit)

---

## 10. Technical Stack

### Backend

**Framework & Language:**
- Spring Boot 3.x (Java 17+)
- Spring Web (RESTful API)
- Spring Security (JWT authentication, CSRF protection)
- Spring Data JPA (ORM)

**Database:**
- MySQL 8.0+
- Flyway (Database migration tool)
- Hibernate (JPA implementation)

**Security:**
- JWT (JSON Web Token) - Access + Refresh token
- BCrypt (Password hashing)
- HttpOnly Cookies (Token storage)

**Libraries:**
- Lombok (Boilerplate reduction)
- Apache POI (Excel export)
- iText vagy Apache PDFBox (PDF export)
- Jackson (JSON serialization)

---

### Frontend

**Framework & Language:**
- Angular 17+ (Standalone Components)
- TypeScript 5.0+
- RxJS (Reactive programming)

**UI & Styling:**
- SCSS (CSS preprocessor)
- Angular Material vagy Bootstrap 5 (UI components)
- Responsive layout (Flexbox, CSS Grid)

**Libraries:**
- Angular Forms (Reactive Forms)
- Angular Router (SPA navigation)
- HttpClient (API kommunikáció)
- Chart.js (Post-MVP: diagramok)

---

### Development Tools

**Version Control:**
- Git (Version control system)
- GitHub (Remote repository, CI/CD)

**Build Tools:**
- Maven (Backend build tool)
- Angular CLI (Frontend build tool)

**IDE:**
- IntelliJ IDEA (Backend development)
- VS Code (Frontend development)

**Testing (Post-MVP):**
- JUnit 5 (Backend unit tests)
- Mockito (Mocking framework)
- Jasmine + Karma (Frontend unit tests)

---

### Infrastructure (Production)

**Hosting:**
- Backend: AWS / Heroku / DigitalOcean
- Frontend: Vercel / Netlify / AWS S3 + CloudFront
- Database: AWS RDS / DigitalOcean Managed MySQL

**Domain & SSL:**
- Domain registrar: Namecheap / Porkbun
- SSL Certificate: Let's Encrypt (ingyenes)

**Monitoring (Post-MVP):**
- Application logging: Logback
- Error tracking: Sentry (opcionális)

---

## 11. Development Phases

### Phase 1: MVP (Minimum Viable Product)

**Cél:** Alapvető működő alkalmazás

**Funkciók:**
- User authentication (Regisztráció, Login, Jelszó visszaállítás)
- Kategória kezelés (CRUD)
- Feladat kezelés (CRUD, részfeladatok száma)
- Timer funkció (indítás, váltás, megállítás)
- Pontértékek (tervezett + tényleges)
- Dashboard (táblázatos nézet)
- Exportálás (Excel + PDF)
- Responsive design (mobil, tablet, desktop)

**Tech stack:** Spring Boot, Angular, MySQL, Flyway, JWT

**Várható státusz:** Teljes, használható alkalmazás egyéni felhasználóknak

---

### Phase 2: Growth Features

**Cél:** Felhasználói élmény javítása, bővített elemzés

**Funkciók:**
- Statisztikák (heti/havi összesítések, trendek)
- Fejlett dashboard (interaktív diagramok, szűrők)
- Sötét mód (Dark mode)
- Értesítések (emlékeztetők)
- Gyorsgombok (billentyűzet shortcuts)
- Fejlett exportálás (diagramok PDF-ben)

**Tech stack:** Chart.js, Angular Material Advanced Components

---

### Phase 3: Vision Features

**Cél:** AI-alapú funkciók, többfelhasználós támogatás

**Funkciók:**
- AI-alapú előrejelzés (időbecslés korábbi feladatok alapján)
- Team verzió (többfelhasználós, megosztás)
- Integráció (Google Calendar, Trello, Jira)
- Fejlett analytics (ML-based insights)
- Public profile (megosztható eredmények)

**Tech stack:** Python ML backend (opcionális), WebSocket (real-time)

---

## 12. Open Questions

**Nincs nyitott kérdés.** Minden funkcionális és technikai részlet tisztázva.

---

## 13. Glossary

**Access Token:** Rövid élettartamú JWT token (15 perc), API hívások hitelesítésére használt.

**Refresh Token:** Hosszabb élettartamú JWT token (7 nap), új access token szerzésére használt.

**BCrypt:** Jelszó titkosítási algoritmus (hash + salt).

**CSRF (Cross-Site Request Forgery):** Támadási forma, ahol rosszindulatú oldal kényszeríti a böngészőt kérések küldésére.

**Flyway:** Adatbázis migrációs eszköz, amely verziókezeli a schema változásokat.

**HttpOnly Cookie:** Cookie típus, amely nem érhető el JavaScript-ből (XSS védelem).

**JWT (JSON Web Token):** Token-alapú authentication standard.

**MVP (Minimum Viable Product):** Minimálisan működőképes termék, alapvető funkciókkal.

**NFR (Non-Functional Requirement):** Nem-funkcionális követelmény (teljesítmény, biztonság, stb.).

**FR (Functional Requirement):** Funkcionális követelmény (mit tud az alkalmazás).

**Részfeladat (Subtask):** Egy feladat egy része, amire külön időt mérünk.

**SameSite:** Cookie attribútum, amely korlátozza a cookie küldését cross-site kérésekhez.

**XSS (Cross-Site Scripting):** Támadási forma, ahol rosszindulatú script fut a felhasználó böngészőjében.

---

## 14. Document Version History

| Verzió | Dátum | Szerző | Változások |
|--------|-------|--------|------------|
| 1.0 | 2026-01-28 | BMad | Kezdeti PRD létrehozása |
| 1.1 | 2026-01-29 | BMad | UX Principles, FR, NFR, Tech Stack hozzáadva |

---

## 15. Summary

A **Task Analysis** alkalmazás egy részfeladat-alapú időmérő és elemző eszköz egyéni felhasználók számára. Az egyedi érték az **egy gombnyomással történő részfeladat váltás** és a **részletes időelemzés**, amely lehetővé teszi a felhasználók számára, hogy megértsék, melyik részfeladat veszi el a legtöbb időt.

**Főbb jellemzők:**
- **JWT-alapú biztonságos authentication** (Access + Refresh token)
- **Flyway migration** (adatbázis verziókezelés)
- **Részfeladat szintű időmérés** egyetlen gombnyomással
- **Pontértékek** (tervezett vs. tényleges)
- **Dashboard** (időelemzés, nehézség analízis)
- **Exportálás** (Excel + PDF)
- **Responsive design** (mobil, tablet, desktop)

**Tech Stack:**
- Backend: Spring Boot, MySQL, Flyway
- Frontend: Angular, TypeScript, SCSS
- Security: JWT, BCrypt, HTTPS, CSRF protection

**MVP státusz:** Teljes funkciókészlet meghatározva, kész az Architecture és Epic tervezésre.

---

**🎉 PRD COMPLETE 🎉**

---

