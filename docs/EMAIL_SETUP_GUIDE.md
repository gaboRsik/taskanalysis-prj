# 📧 Email Funkció Beállítási Útmutató

## 🎯 Cél
Az email funkció beállítása, hogy a Task Analysis applikáció tudjon Excel fájlokat küldeni email mellékleteként.

---

## 📚 Előfeltételek

✅ **Excel export működik** (DOWNLOAD gomb már működik)  
✅ **Backend és Frontend implementáció kész**  
❌ **Gmail App Password még nincs beállítva** ← ezt fogjuk most megcsinálni

---

## 🔧 1. Gmail App Password Létrehozása

### Mi az App Password?
A Gmail App Password egy **16 karakteres speciális jelszó**, amit külön alkalmazások számára generálsz. **NEM** a sima Gmail jelszavad!

### Lépések:

#### **1.1 Ellenőrizd a 2-lépcsős hitelesítést (2FA)**

1. Menj a Google Account Security oldalra:  
   🔗 **https://myaccount.google.com/security**

2. Keress rá: **"2-Step Verification"** vagy **"2 lépésben történő hitelesítés"**

3. **Ha NINCS bekapcsolva:**
   - Kattints rá → **Turn on** (Bekapcsolás)
   - Kövesd a lépéseket (telefonszám megadása, kód ellenőrzés)
   - ⏱️ Ez ~3-5 percet vesz igénybe

4. **Ha MÁR BE VAN kapcsolva:**
   - ✅ Kész! Folytathatod a következő lépéssel.

---

#### **1.2 Generálj App Password-öt**

1. Maradj a Security oldalon, görgess le:  
   🔗 **https://myaccount.google.com/apppasswords**  
   (vagy keress rá: "App passwords")

2. Válaszd ki:
   - **App:** "Mail" vagy "Other (Custom name)" → írd be: **"Task Analysis"**
   - **Device:** "Windows Computer" vagy "Other"

3. Kattints: **Generate** (Létrehozás)

4. **Fontos! Másolj ki a generált jelszót:**
   ```
   Példa: abcd efgh ijkl mnop
   ```
   ⚠️ **Ezt csak egyszer fogod látni!** Másold ki azonnal egy biztonságos helyre.

5. Távolítsd el a **szóközöket**, így nézzen ki:
   ```
   abcdefghijklmnop
   ```

---

## 🖥️ 2. IntelliJ IDEA Beállítása

Most be kell állítanod az IntelliJ-t, hogy a backend indításakor használja ezeket az adatokat.

### **2.1 Nyisd meg a Run Configuration-t**

1. IntelliJ IDEA-ban menj fel jobbra:  
   **Run → Edit Configurations...**

2. Válaszd ki a Spring Boot futtatási konfigurációdat:  
   ```
   TaskAnalysisApplication (vagy amit használsz)
   ```

---

### **2.2 Állítsd be az Environment Variables-öket**

1. Kattints az **Environment variables** sorban a kis **📝 Edit** ikonra (jobb oldalon)

2. Adj hozzá **3 változót** a következő formátumban:

| Name | Value | Példa |
|------|-------|-------|
| `MAIL_USERNAME` | `a-te-gmail-cimed@gmail.com` | `siklo.gabor@gmail.com` |
| `MAIL_PASSWORD` | `a-16-karakteres-app-password` | `abcdefghijklmnop` |
| `MAIL_FROM` | `Task Analysis <noreply@taskanalysis.com>` | `Task Analysis <noreply@taskanalysis.com>` |

**Fontos:**
- ⚠️ **MAIL_PASSWORD:** Az App Password-öt írd be (amit most generáltál), **NEM** a sima Gmail jelszavadat!
- ⚠️ **MAIL_FROM:** Bármilyen nevet/email címet használhatsz, ez csak a "Feladó" mezőben fog megjelenni

---

### **2.3 Így néz ki helyesen:**

```
MAIL_USERNAME=siklo.gabor@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
```

---

### **2.4 Mentsd el a konfigurációt**

1. Kattints: **OK**
2. Kattints: **Apply** → **OK**

---

## 🚀 3. Backend Újraindítása

Most újra kell indítanod a backendet, hogy érvénybe lépjenek a változások!

### Lépések:

1. **Állítsd le a futó backend-et:**
   - IntelliJ-ben kattints a **Stop** gombra (piros négyzet 🟥)

2. **Indítsd újra:**
   - Jobb klikk a `TaskAnalysisApplication.java` fájlon
   - **Run 'TaskAnalysisApplication'**

3. **Ellenőrizd a console-t:**
   - Keress ilyen sort:
     ```
     o.s.b.a.mail.javamail.MailSenderJndiConfiguration : Mail sender configured successfully
     ```
   - Ha látod → ✅ Az email konfiguráció rendben!

---

## 🧪 4. Email Funkció Tesztelése

Most teszteljük, hogy működik-e!

### **4.1 Frontend tesztelés (böngészőben)**

1. **Nyisd meg az applikációt:**  
   http://localhost:4200

2. **Jelentkezz be** a fiókodba

3. **Menj a Tasks oldalra**

4. **Válassz ki egy task-ot** (amelyiknek vannak részfeladatai és időmérésed rajta)

5. **Kattints a 📧 Email gombra**

6. **Várj ~2-5 másodpercet** (amíg email küldés történik)

7. **Ellenőrizd az üzenetet:**
   - Sikeres: *"Export elküldve email-ben! Ellenőrizd: siklo.gabor@gmail.com"*
   - Hiba esetén: *"Email küldése sikertelen: [hibaüzenet]"*

---

### **4.2 Ellenőrizd a Gmail Inbox-ot**

1. **Nyisd meg a Gmail fiókodat:**  
   🔗 https://mail.google.com

2. **Keress rá:**
   ```
   Task Analysis Export
   ```

3. **Nyisd meg az emailt:**
   - **Tárgy:** "Task Analysis Export - [Feladat neve]"
   - **Feladó:** "Task Analysis <noreply@taskanalysis.com>"
   - **Szöveg:**
     ```
     Szia [Neved]!

     Az exportált feladat adataid csatolva találod.

     Feladat: [Feladat neve]
     Kategória: [Kategória neve]
     Export dátum: 2026-03-07 14:30

     Üdv,
     Task Analysis
     ```
   - **Melléklet:** `taskanalysis_Feladat_neve_20260307_143025.xlsx`

4. **Töltsd le az Excel fájlt:**
   - Nyisd meg Excel-ben vagy Google Sheets-ben
   - Ellenőrizd, hogy tartalmazza:
     - ✅ Részfeladatokat
     - ✅ Időket (HH:MM:SS formátum)
     - ✅ Pontokat (tervezett és tényleges)
     - ✅ Összesítő sort (sárga háttér)

---

## ❗ Troubleshooting (Hibaelhárítás)

### 🔴 **Hiba: "535 Authentication failed"**

**Ok:** Rossz jelszó vagy App Password nincs beállítva

**Megoldás:**
1. Ellenőrizd, hogy **2FA be van-e kapcsolva** a Gmail fiókodban
2. Generálj **új App Password-öt**
3. Másold ki **szóközök nélkül**: `abcdefghijklmnop`
4. Állítsd be újra az **MAIL_PASSWORD** environment változót
5. Indítsd újra a backend-et

---

### 🔴 **Hiba: "Mail server connection failed"**

**Ok:** Internet kapcsolat vagy tűzfal blokkolja

**Megoldás:**
1. Ellenőrizd az **internet kapcsolatot**
2. Próbáld ki böngészőben: https://mail.google.com (be tudsz-e jelentkezni?)
3. Ha céges hálózaton vagy → kérdezd meg az IT-t, hogy **Gmail SMTP (port 587)** nyitva van-e

---

### 🔴 **Hiba: "Email küldése sikertelen: null"**

**Ok:** Environment változók nem töltődtek be

**Megoldás:**
1. IntelliJ-ben menj: **Run → Edit Configurations**
2. Ellenőrizd, hogy a **3 változó helyesen van beállítva**:
   ```
   MAIL_USERNAME=...
   MAIL_PASSWORD=...
   MAIL_FROM=...
   ```
3. Kattints **Apply → OK**
4. **Állítsd le** és **indítsd újra** a backend-et (fontos!)

---

### 🔴 **Hiba: Email nem érkezik meg**

**Megoldás 1 - Ellenőrizd a Spam mappát:**
- Gmail → Spam (Levélszemét) mappa
- Ha ott van → Jobb klikk → "Not spam" (Nem levélszemét)

**Megoldás 2 - Ellenőrizd a backend console-t:**
```
Sending export email to: siklo.gabor@gmail.com
Export email sent successfully to: siklo.gabor@gmail.com
```
- Ha látod → Email elment, valószínűleg késik vagy Spam-ben van

**Megoldás 3 - Várj 1-2 percet:**
- Néha a Gmail késlelteti az alkalmazásokból küldött emaileket

---

## 📊 Backend Console Log (mit fogsz látni)

Ha minden rendben van, a backend console-ban ezt látod:

```
2026-03-07 14:30:15.123  INFO --- [nio-8080-exec-3] c.t.controller.ExportController : Export request for task 1 with format XLSX and delivery EMAIL
2026-03-07 14:30:15.234  INFO --- [nio-8080-exec-3] c.t.service.ExportService        : Excel export generated successfully for task: Feladatom
2026-03-07 14:30:15.345  INFO --- [nio-8080-exec-3] c.t.service.EmailService         : Sending export email to: siklo.gabor@gmail.com
2026-03-07 14:30:17.456  INFO --- [nio-8080-exec-3] c.t.service.EmailService         : Export email sent successfully to: siklo.gabor@gmail.com
```

✅ **"Export email sent successfully"** → Email sikeresen elküldve!

---

## 🎉 Siker! Készen vagy!

Most már tudod használni az **email export funkciót**! 🚀

### Következő lépések:
- ✅ Teszteld **különböző task-okkal**
- ✅ Próbáld ki **mobilon is** (ott csak az Email gomb látszik)
- ✅ Később implementálhatod a **PDF export-ot** is

---

## 🔒 Biztonsági Megjegyzések

⚠️ **Ne commitáld a GitHubra:**
- A **MAIL_PASSWORD** (App Password) értékét **SOHA** ne írd bele a kódba
- Mindig **environment változókban** tárold
- IntelliJ Run Configuration **nem** kerül fel a GitHubra (biztonságos)

✅ **Production környezetben:**
- Használj dedicated email service-t (SendGrid, AWS SES)
- Ne használj személyes Gmail fiókot
- Állíts be rate limiting-et

---

## 📞 Segítségre van szükséged?

Ha elakadtál, nézd meg ezeket a fájlokat:
- 📄 [EXPORT_FEATURE.md](./EXPORT_FEATURE.md) - Teljes export dokumentáció
- 📄 [EXPORT_DEBUG.md](./EXPORT_DEBUG.md) - Hibakeresési útmutató
- 📄 [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Tesztelési lépések

---

✍️ **Készítve:** 2026-03-07  
📝 **Utolsó frissítés:** 2026-03-07
