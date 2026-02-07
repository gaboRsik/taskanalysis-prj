# Export Feature - Desktop Tesztelési Útmutató

## 1. Gmail App Password Beállítása

### Lépések:

1. **Google Account Security** oldal:
   - Nyisd meg: https://myaccount.google.com/security
   - Jelentkezz be a Gmail fiókodba

2. **2-Factor Authentication engedélyezése** (ha még nincs):
   - Keresd meg a "2-Step Verification" opciót
   - Kattints rá és kövesd a lépéseket
   - Választhatsz SMS vagy Authenticator app-ot

3. **App Password generálása**:
   - Menj vissza a Security oldalra
   - Keresd meg az "App passwords" részt
   - Kattints rá (lehet, hogy újra be kell jelentkezned)
   - Device: válassz "Other (Custom name)"
   - Írd be: "Task Analysis"
   - Kattints "Generate"
   - **FONTOS:** Másold ki a 16 karakteres jelszót (szóközök nélkül)
   - Példa: `abcd efgh ijkl mnop` → `abcdefghijklmnop`

---

## 2. Environment Variables Beállítása

### Windows PowerShell-ben:

Nyisd meg a PowerShell-t és futtasd le:

```powershell
# Cseréld ki a saját adataidra!
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="abcdefghijklmnop"
$env:MAIL_FROM="Task Analysis <noreply@taskanalysis.com>"

# Ellenőrzés
echo "MAIL_USERNAME: $env:MAIL_USERNAME"
echo "MAIL_PASSWORD: $env:MAIL_PASSWORD beállítva"
echo "MAIL_FROM: $env:MAIL_FROM"
```

⚠️ **FONTOS:** Ezek a változók csak az aktuális PowerShell sessionben élnek!

---

## 3. IntelliJ IDEA Konfiguráció

### Opció A: Run Configuration (Ajánlott)

1. **IntelliJ-ben nyisd meg a projektet** (backend mappát)

2. **Edit Configurations**:
   - Jobb felső sarokban a Play gomb mellett → "Edit Configurations..."
   - Vagy: Run → Edit Configurations

3. **Environment Variables hozzáadása**:
   - Keresd meg a "TaskAnalysisApplication" konfigurációt
   - Ha nincs, kattints a "+" → Application:
     - Name: `TaskAnalysisApplication`
     - Main class: `com.taskanalysis.TaskAnalysisApplication`
     - Module: `taskanalysis-backend`
   
4. **Environment Variables beállítása**:
   - Kattints az "Environment variables" mező melletti ikonra (vagy "Modify options" → "Environment variables")
   - Add meg (CSERÉLD ki a saját adataidra!):
     ```
     MAIL_USERNAME=your-email@gmail.com
     MAIL_PASSWORD=abcdefghijklmnop
     MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
     ```
   - Vagy egy sorban (szétválasztva `;`-vel Windows-on):
     ```
     MAIL_USERNAME=your@gmail.com;MAIL_PASSWORD=abcd;MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
     ```

5. **Apply → OK**

### Opció B: PowerShell-ből Indítás

Ha a PowerShell-ben már beállítottad az environment variables-öket:

```powershell
cd backend
mvn spring-boot:run
```

---

## 4. MySQL Ellenőrzése

Győződj meg róla, hogy a MySQL Docker container fut:

```powershell
docker ps
```

Ha nem fut, indítsd el:

```powershell
docker-compose up -d
```

---

## 5. Backend Indítása

IntelliJ-ben:
- Kattints a Play gombra (vagy Shift+F10)
- Várd meg, amíg elindul (látod a logban: "Started TaskAnalysisApplication")

Vagy PowerShell-ben (ha ott állítottad be az env var-okat):
```powershell
cd backend
mvn spring-boot:run
```

### Sikeres indítás jelei:
```
Started TaskAnalysisApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

---

## 6. Teszt Adatok Előkészítése

### 6.1 Regisztrálj egy felhasználót

**Postman vagy cURL:**

```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "test@example.com",
    "password": "Test1234",
    "name": "Test User"
  }'
```

**Válasz:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "userId": 1,
  "email": "test@example.com",
  "name": "Test User"
}
```

💡 **Másold ki az `accessToken`-t!** Szükséged lesz rá.

### 6.2 Hozz létre egy kategóriát (opcionális)

```powershell
curl -X POST http://localhost:8080/api/categories `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d '{
    "name": "Tanulás",
    "color": "#3498db"
  }'
```

### 6.3 Hozz létre egy feladatot

```powershell
curl -X POST http://localhost:8080/api/tasks `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d '{
    "name": "Matematika teszt",
    "description": "10 feladatos teszt",
    "subtaskCount": 10,
    "categoryId": 1
  }'
```

**Válasz:**
```json
{
  "id": 1,
  "name": "Matematika teszt",
  "subtasks": [
    { "id": 1, "subtaskNumber": 1, ... },
    ...
  ]
}
```

### 6.4 Indíts timer-t néhány részfeladatra

**Részfeladat 1 start:**
```powershell
curl -X POST http://localhost:8080/api/timer/1/start `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Várj 10-20 másodpercet...

**Részfeladat 1 stop:**
```powershell
curl -X POST http://localhost:8080/api/timer/1/stop `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Részfeladat 2 start & stop** (hasonlóan):
```powershell
curl -X POST http://localhost:8080/api/timer/2/start `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
  
# Várj egy kicsit...

curl -X POST http://localhost:8080/api/timer/2/stop `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 7. Export Tesztelés 🎯

### Teszt 1: Email Delivery

```powershell
curl -X POST http://localhost:8080/api/export/task/1 `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d '{
    "format": "XLSX",
    "delivery": "EMAIL"
  }'
```

**Sikeres válasz:**
```json
{
  "success": true,
  "message": "Export elküldve email-ben! Ellenőrizd: test@example.com",
  "deliveryMethod": "EMAIL",
  "fileName": "taskanalysis_Matematika_teszt_20260207_143025.xlsx"
}
```

✅ **Ellenőrizd az emailedet!** (A Gmail címedre kell érkezzen a melléklettel)

---

### Teszt 2: Direct Download

```powershell
curl -X POST http://localhost:8080/api/export/task/1 `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" `
  -d '{
    "format": "XLSX",
    "delivery": "DOWNLOAD"
  }' `
  --output task_export.xlsx
```

✅ **Ellenőrizd:** A `task_export.xlsx` fájl létrejött a mappában!

Nyisd meg Excel-ben vagy LibreOffice Calc-ban.

---

## 8. Postman Használata (Vizuális Alternatíva)

### 8.1 Postman Telepítése
- Töltsd le: https://www.postman.com/downloads/

### 8.2 Collection Létrehozása

1. **New Collection:** "Task Analysis API"

2. **Add Request:** "Register"
   - Method: POST
   - URL: `http://localhost:8080/api/auth/register`
   - Body (JSON):
     ```json
     {
       "email": "test@example.com",
       "password": "Test1234",
       "name": "Test User"
     }
     ```
   - Send → Másold ki az `accessToken`-t

3. **Add Request:** "Create Task"
   - Method: POST
   - URL: `http://localhost:8080/api/tasks`
   - Headers: `Authorization: Bearer YOUR_ACCESS_TOKEN`
   - Body (JSON):
     ```json
     {
       "name": "Matematika teszt",
       "description": "10 feladatos teszt",
       "subtaskCount": 10
     }
     ```

4. **Add Request:** "Export Task (Email)"
   - Method: POST
   - URL: `http://localhost:8080/api/export/task/1`
   - Headers: `Authorization: Bearer YOUR_ACCESS_TOKEN`
   - Body (JSON):
     ```json
     {
       "format": "XLSX",
       "delivery": "EMAIL"
     }
     ```
   - Send → Ellenőrizd az emailedet!

---

## 9. Troubleshooting

### Problem: Email nem érkezik meg

**1. Ellenőrizd az environment variables-öket:**

IntelliJ-ben:
- Run → Edit Configurations → Environment Variables
- Nézd meg, hogy helyesek-e

PowerShell-ben:
```powershell
echo $env:MAIL_USERNAME
echo $env:MAIL_PASSWORD
```

**2. Backend logok ellenőrzése:**

Keresd a console outputban:
```
Sending export email to: test@example.com
Export email sent successfully to: test@example.com
```

Ha látod ezt:
```
Failed to send export email
javax.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```
→ Rossz a Gmail App Password!

**3. Gmail spam folder:**
Nézd meg a spam mappát!

**4. Gmail App Password újragenerálása:**
Ha nem működik, generálj egy új app password-öt.

---

### Problem: "Task not found" vagy "Access denied"

- Biztos jó a task ID? (Az első task ID általában 1)
- Biztos jó JWT tokennel próbálod? (Nem lejárt?)
- Biztos a saját felhasználód által létrehozott task-ot exportálod?

---

### Problem: Backend nem indul el

**MySQL nincs elindítva:**
```powershell
docker-compose up -d
docker ps  # Ellenőrzés
```

**Port foglalt:**
Ha a 8080-as port foglalt, módosítsd az `application.properties`-ben:
```properties
server.port=8081
```

---

## 10. Mit Teszteljünk?

✅ **Email delivery:**
- Email megérkezik?
- Melléklet megvan?
- Excel fájl helyes formátumú?
- Adatok helyesek? (részfeladatok, idők, pontok)

✅ **Direct download:**
- Fájl letöltődik?
- Helyes fájlnév?
- Excel megnyitható?

✅ **Authorization:**
- Más felhasználó task-ját nem lehet exportálni?

✅ **Error handling:**
- Nem létező task ID → 404 vagy error message?
- Rossz format/delivery érték → validációs hiba?

---

## 11. Következő Lépések

Ha minden működik:
- ✅ Export feature készen áll!
- 🎯 Frontend implementáció (Angular komponens export gombbal)
- 📊 PDF export hozzáadása később
- 🎨 UI/UX finomítások

---

Sikeres tesztelést! 🚀

Ha bármilyen problémád van, nézd meg a **Troubleshooting** részt vagy a backend console logokat!
