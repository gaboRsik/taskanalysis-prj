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

