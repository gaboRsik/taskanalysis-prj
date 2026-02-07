# Export Debug Guide - 500 Error Troubleshooting

## 500 Error - Mi történt?

A `500` státuszkód azt jelenti, hogy a **backend hibába ütközött** az export során.

---

## 1. Backend Console Ellenőrzése

### Mit Keress az IntelliJ Console-ban?

Görgess le a legutolsó hibákhoz és keresd az **"Export"** vagy **"Email"** szavakat.

### Lehetséges Hibák:

#### A) Email Konfiguráció Hiba

**Hiba üzenet:**
```
org.springframework.mail.MailAuthenticationException: Authentication failed
```
vagy
```
javax.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**Ok:** Hibás Gmail App Password vagy nem állítottad be az environment variables-öket

**Megoldás:**
1. Ellenőrizd az environment variables-öket:
   - IntelliJ: Run → Edit Configurations → Environment Variables
   - Biztos jól bemásoltad a Gmail App Password-öt?
   - Szóközök nélkül? (pl. `abcdefghijklmnop`)

2. Generálj új Gmail App Password-öt:
   - https://myaccount.google.com/security
   - App passwords → Create new
   - Másold be újra (pontosan, szóközök nélkül!)

---

#### B) SMTP Kapcsolódási Hiba

**Hiba üzenet:**
```
org.springframework.mail.MailSendException: Mail server connection failed
```
vagy
```
javax.mail.MessagingException: Could not connect to SMTP host
```

**Ok:** Tűzfal vagy nincs internet kapcsolat

**Megoldás:**
1. Ellenőrizd az internet kapcsolatot
2. Nézd meg, hogy a tűzfal engedi-e a 587-es portot (SMTP)

---

#### C) Task Nem Található

**Hiba üzenet:**
```
RuntimeException: Task not found
```

**Ok:** A task ID 15 nem létezik vagy nem a tiéd

**Megoldás:**
1. Ellenőrizd, hogy van-e task ID 15
2. Próbáld egy másik task ID-val (pl. 1, 2, 3...)

---

#### D) Subtask Nincs Betöltve (Lazy Loading)

**Hiba üzenet:**
```
LazyInitializationException: failed to lazily initialize a collection
```
vagy
```
could not initialize proxy - no Session
```

**Ok:** A Task entity subtask-jait nem töltötte be a Hibernate

**Megoldás:** (Ez már implementálva van, de ha mégis előjön)
- A `TaskService.getTaskEntityById()` metódus lazy loading-ot kéne megoldjon

---

#### E) Excel Generálási Hiba

**Hiba üzenet:**
```
IOException: Error generating Excel file
```
vagy
```
NullPointerException at ExportService.generateExcelExport
```

**Ok:** 
- Nincs subtask a task-hoz
- Nincs time entry a subtask-hoz
- Apache POI dependency hiba

**Megoldás:**
1. Biztos van legalább 1 subtask a task-hoz?
2. Indítottál timer-t a subtask-on?
3. Maven clean & rebuild:
   ```powershell
   cd backend
   mvn clean install
   ```

---

## 2. Hibakeresési Lépések

### Lépés 1: Teljes Hibaüzenet Másolása

IntelliJ Console-ban:
1. Keresd meg a legutolsó Exception stack trace-t
2. Jelöld ki az egészet (Ctrl+C)
3. Másold be ide (chat-be) hogy segíthessek

### Lépés 2: Environment Variables Ellenőrzése

PowerShell-ben:
```powershell
echo $env:MAIL_USERNAME
echo $env:MAIL_PASSWORD
echo $env:MAIL_FROM
```

Ha üresek → IntelliJ Run Configuration-ben állítsd be őket!

### Lépés 3: Application.properties Ellenőrzés

Nyisd meg: `backend/src/main/resources/application.properties`

Ellenőrizd:
```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
app.mail.from=${MAIL_FROM:Task Analysis <noreply@taskanalysis.com>}
```

### Lépés 4: Task Adatok Ellenőrzése

Postman vagy cURL:
```powershell
# Nézd meg a task-ot
curl -X GET http://localhost:8080/api/tasks/15 `
  -H "Authorization: Bearer YOUR_TOKEN"
```

Válasz ellenőrzés:
- Van task ID 15?
- Van benne subtasks tömb?
- Van time entry a subtask-okhoz?

---

## 3. Gyors Megoldás: Próbáld Email Nélkül (Download)

Ha az email nem sikerül, teszteld a **Download** funkciót:

Frontend-en a Desktop nézetben:
1. Kattints a **📥 Download** gombra (nem az Email-re)
2. Ha ez működik → az export logic OK, csak az email a gond
3. Ha ez is hibázik → az export generálás a problém

---

## 4. Backend Újraindítás Email Konfiggal

### IntelliJ-ben:

1. **Stop** (Ctrl+F2)

2. **Edit Configurations:**
   - Run → Edit Configurations
   - TaskAnalysisApplication
   - Environment Variables:
     ```
     MAIL_USERNAME=your-gmail@gmail.com
     MAIL_PASSWORD=abcdefghijklmnop
     MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
     ```
   - Apply → OK

3. **Run** (Shift+F10)

4. **Ellenőrzés console-ban:**
   Lásd-e:
   ```
   Started TaskAnalysisApplication in X.XXX seconds
   ```

---

## 5. Mi A Következő Lépés?

### Ha látod a backend hibát:
📋 **Másold be ide (chat-be) a teljes stack trace-t!**

Példa:
```
java.lang.RuntimeException: Task not found
    at com.taskanalysis.service.TaskService.getTaskEntityById(TaskService.java:98)
    at com.taskanalysis.controller.ExportController.exportTask(ExportController.java:57)
    ...
```

### Ha nem látod a hibát:
1. IntelliJ Console → görgess le a végére
2. Keresd: `ERROR`, `Exception`, `Failed`
3. Másold ki az egészet

### Alternatíva: Tesztelés egyszerűbb móddal

Ha túl bonyolult az email setup, **használd a Download gombot!**
- Desktop nézetben kattints **📥 Download**
- Ez nem igényel email konfigot
- Azonnal letölti a fájlt

---

## 6. Hiányzó Teszt Adatok?

Ha még nincs megfelelő task:

### Gyors Task Létrehozás Timer-rel

```powershell
# 1. Token megszerzése (már be vagy jelentkezve)
$token = "YOUR_JWT_TOKEN"

# 2. Task létrehozása
$task = curl -X POST http://localhost:8080/api/tasks `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer $token" `
  -d '{"name":"Test Export Task","subtaskCount":3,"categoryId":1}' | ConvertFrom-Json

# 3. Timer indítás subtask 1-re
curl -X POST "http://localhost:8080/api/timer/$($task.subtasks[0].id)/start" `
  -H "Authorization: Bearer $token"

Start-Sleep -Seconds 5

# 4. Timer stop
curl -X POST "http://localhost:8080/api/timer/$($task.subtasks[0].id)/stop" `
  -H "Authorization: Bearer $token"

# 5. Most próbáld az exportot!
```

---

## Összefoglalás

**500 Error → Backend hibás → Nézd a backend console-t!**

**Lehetséges okok:**
1. ❌ Hibás/hiányzó Gmail App Password
2. ❌ Environment variables nincsenek beállítva
3. ❌ Task nem létezik / nincs time entry
4. ❌ SMTP kapcsolat hiba

**Megoldás:**
1. Olvasd el a backend console hibát
2. Ellenőrizd az email konfigot
3. Próbáld a Download gombot (email helyett)
4. Másold be ide a stack trace-t!

---

**Várom a backend hibaüzenetet!** 📋
