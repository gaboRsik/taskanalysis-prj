# Export Feature - Email Configuration Guide

## Overview

Az export feature lehetővé teszi a felhasználók számára, hogy a feladat adataikat Excel (később PDF) formátumban exportálják. Az export két módon történhet:

### Adaptive Delivery Modes 🎯

**Desktop (≥ 768px):**
- 📥 **Közvetlen letöltés** - Fájl azonnal letöltődik a böngészőben
- 📧 **Email küldés** - Mellékletként a felhasználó email címére

**Mobil (< 768px):**
- 📧 **Email küldés** (kizárólag) - Mobilbarát megoldás

---

## ✅ Feature Status (2026-05-14)

| Feature | Status | Environment |
|---------|--------|-------------|
| 📥 Download Export (XLSX) | ✅ **Működik** | Lokál + Production |
| 📧 Email Export (XLSX) | ✅ **Működik** | Lokál + Production |
| 📄 PDF Export | ⏳ Tervezett | - |
| 🐛 LazyInitializationException | ✅ **Javítva** | 2026-05-14 |

**Tesztelt környezetek:**
- ✅ **Lokál fejlesztés** (IntelliJ + localhost:4200)
- ✅ **AWS EC2 Production** (https://tasks.gaborsiknet.hu)

---

## Backend Setup

### 1. Dependencies (már hozzáadva)

```xml
<!-- Spring Boot Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Apache POI (Excel) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 2. Email Configuration

Az `application.properties` fájlban az email SMTP beállítások:

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

---

## Email Service Configuration Options

### Option 1: Gmail (Development) ⭐ Recommended

**Előnyök:** Ingyenes, egyszerű, gyors setup

**Lépések:**

1. **Gmail App Password létrehozása:**
   - Google Account → Security → 2-Step Verification (engedélyezd)
   - App Passwords → Generate App Password
   - Másold ki a generált jelszót (16 karakter)

2. **Environment Variables beállítása:**

Windows PowerShell:
```powershell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-16-char-app-password"
$env:MAIL_FROM="Task Analysis <noreply@taskanalysis.com>"
```

Linux/Mac:
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-16-char-app-password"
export MAIL_FROM="Task Analysis <noreply@taskanalysis.com>"
```

3. **IntelliJ IDEA Run Configuration:**
   - Run → Edit Configurations
   - Environment Variables:
     ```
     MAIL_USERNAME=your-email@gmail.com
     MAIL_PASSWORD=your-16-char-app-password
     MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
     ```

---

### Option 2: Mailtrap.io (Testing) 🧪

**Előnyök:** Nem küld valódi emailt, inbox-ban tesztelhető

**Lépések:**

1. Regisztráció: https://mailtrap.io/ (ingyenes)
2. Email Testing → Inboxes → SMTP Settings
3. application.properties:

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=${MAIL_USERNAME:your-mailtrap-username}
spring.mail.password=${MAIL_PASSWORD:your-mailtrap-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

### Option 3: Production SMTP (Production)

**Példák:**
- **SendGrid** - https://sendgrid.com/
- **AWS SES** - https://aws.amazon.com/ses/
- **Mailgun** - https://www.mailgun.com/

---

### Option 4: AWS Production (.env file) ☁️

**Használat:** AWS EC2 instance-on Docker containerben

**Lépések:**

1. **SSH kapcsolat AWS szerverrel:**
   ```bash
   ssh -i ~/.ssh/taskanalysis-key.pem ubuntu@3.64.207.108
   ```

2. **Navigálj a projekthez:**
   ```bash
   cd ~/taskanalysis-prj
   ```

3. **Szerkeszd a .env fájlt:**
   ```bash
   nano .env
   ```

4. **Add hozzá az email konfigurációt:**
   ```bash
   # Email Configuration
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-16-char-app-password
   MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
   ```

5. **Mentés:** `Ctrl+O` → `Enter` → `Ctrl+X`

6. **Backend újraindítása:**
   ```bash
   docker-compose -f docker-compose.prod.yml restart backend
   docker logs taskanalysis-backend-prod --tail 30
   ```

Várj, amíg látod: `Started TaskAnalysisApplication in XX.XXX seconds`

✅ **Production email funkció most már aktív!**

---

## API Endpoints

### Export Task

**POST** `/api/export/task/{taskId}`

**Request Body:**
```json
{
  "format": "XLSX",
  "delivery": "EMAIL" 
}
```

**Options:**
- `format`: `XLSX` | `PDF` (PDF később)
- `delivery`: `DOWNLOAD` | `EMAIL`

**Response (Email):**
```json
{
  "success": true,
  "message": "Export elküldve email-ben! Ellenőrizd: user@example.com",
  "deliveryMethod": "EMAIL",
  "fileName": "taskanalysis_My_Task_20260207_143025.xlsx"
}
```

**Response (Download):**
- File bytes with Content-Disposition header

---

## Email Template

Az elküldött email tartalma:

**Tárgy:** `Task Analysis Export - {Feladat neve}`

**Törzs:**
```
Szia {Felhasználó neve}!

Az exportált feladat adataid csatolva találod.

Feladat: {Feladat neve}
Kategória: {Kategória neve}
Export dátum: 2026-02-07 14:30

Üdv,
Task Analysis
```

**Melléklet:** `taskanalysis_{feladat_neve}_{timestamp}.xlsx`

---

## Excel File Format

**Oszlopok:**
1. Részfeladat # (1, 2, 3...)
2. Idő (HH:MM:SS formátum)
3. Tervezett pont
4. Tényleges pont
5. Feladat
6. Kategória

**Header:** Kék háttér, fehér betű, vastag
**Összesítő sor:** Sárga háttér, vastag betű

---

## Frontend Implementation (példa)

### Service (task.service.ts)

```typescript
exportTask(taskId: number, format: 'XLSX' | 'PDF', delivery: 'DOWNLOAD' | 'EMAIL'): Observable<any> {
  const body = { format, delivery };
  
  if (delivery === 'DOWNLOAD') {
    // File download
    return this.http.post(`${this.apiUrl}/export/task/${taskId}`, body, {
      responseType: 'blob',
      observe: 'response'
    });
  } else {
    // Email delivery
    return this.http.post<ExportResponse>(`${this.apiUrl}/export/task/${taskId}`, body);
  }
}
```

### Component Logic (tasks.component.ts)

```typescript
export class TasksComponent {
  isMobile = window.innerWidth < 768;

  exportTask(taskId: number, delivery: 'DOWNLOAD' | 'EMAIL') {
    this.taskService.exportTask(taskId, 'XLSX', delivery).subscribe({
      next: (response) => {
        if (delivery === 'DOWNLOAD') {
          // Handle file download
          const blob = response.body;
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = this.getFileName(response);
          a.click();
        } else {
          // Email sent
          alert(response.message);
        }
      },
      error: (err) => {
        alert('Export failed: ' + err.error.message);
      }
    });
  }
}
```

### Template (tasks.component.html)

```html
<button (click)="showExportOptions(task.id)">Export</button>

<div *ngIf="showingExport">
  <!-- Desktop: Both options -->
  <button *ngIf="!isMobile" (click)="exportTask(task.id, 'DOWNLOAD')">
    📥 Letöltés most
  </button>
  
  <!-- Always show email option -->
  <button (click)="exportTask(task.id, 'EMAIL')">
    📧 Küldés emailben
  </button>
</div>
```

---

## Testing

### 1. Postman Testing

```http
POST http://localhost:8080/api/export/task/1
Authorization: Bearer {your-jwt-token}
Content-Type: application/json

{
  "format": "XLSX",
  "delivery": "EMAIL"
}
```

### 2. cURL Testing

```bash
curl -X POST http://localhost:8080/api/export/task/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"format":"XLSX","delivery":"EMAIL"}'
```

---

## Troubleshooting

### LazyInitializationException - "could not initialize proxy"

**Hiba példa:**
```
org.hibernate.LazyInitializationException: could not initialize proxy [com.taskanalysis.entity.User#2] - no Session
org.hibernate.LazyInitializationException: could not initialize proxy [com.taskanalysis.entity.Category#5] - no Session
```

**Ok:** A Task entity lazy-loaded kapcsolatai (User, Category, Subtasks, TimeEntries) nincsenek betöltve a Hibernate session lezárása előtt.

**Megoldás:** ✅ **Már javítva! (2026-05-14)**

A `TaskService.getTaskEntityById()` metódus most már kikényszeríti az összes szükséges kapcsolat betöltését:

```java
@Transactional(readOnly = true)
public Task getTaskEntityById(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
    
    if (!task.getUser().getId().equals(userId)) {
        throw new RuntimeException("Access denied");
    }
    
    // Force load all relationships before returning
    task.getSubtasks().size();  // Load subtasks collection
    task.getSubtasks().forEach(subtask -> 
        subtask.getTimeEntries().size()  // Load time entries for each subtask
    );
    
    // Force load category (nullable)
    if (task.getCategory() != null) {
        task.getCategory().getName();
    }
    
    // Force load user
    task.getUser().getName();
    
    return task;
}
```

**Miért működik ez?**
- `@Transactional` annotáció biztosítja, hogy a metódus alatt aktív legyen a Hibernate session
- `.size()`, `.getName()` hívások kikényszerítik a lazy-loaded entitások betöltését
- A Task objektum ezután biztonságosan továbbadható az ExportService-nek

**Ha mégis hibát kapsz:**
1. Frissítsd a kódot: `git pull origin main`
2. Restart backend lokálisan vagy AWS-en

---

### Email nem érkezik meg

1. **Ellenőrizd az environment variables-öket:**
   ```powershell
   echo $env:MAIL_USERNAME
   echo $env:MAIL_PASSWORD
   ```

2. **Gmail App Password:** Biztos 2FA engedélyezve van?

3. **Backend logs:** Nézd meg a console-t:
   ```
   Sending export email to: user@example.com
   Export email sent successfully to: user@example.com
   ```

4. **SMTP hiba:** Ha "535 Authentication failed", akkor rossz a jelszó

### Excel fájl hibás

1. **Nincsenek subtask-ok:** Ellenőrizd, hogy a task-nak vannak-e részfeladatai
2. **Nincsenek time entry-k:** Timer-t indítottál?

---

## Security Notes

⚠️ **Production környezetben:**
- **Ne commitáld** a `.env` fájlt a GitHubra (már `.gitignore`-ban van)
- Használj titkosított environment variables-öket
- Ne írd bele a valódi email credentials-öket a kódba
- Használj dedicated SMTP service-t (SendGrid, AWS SES) személyes Gmail helyett
- Állíts be rate limiting-et az export endpoint-ra

✅ **AWS Production Best Practices:**
1. **.env fájl használata** - Credentials elkülönítve a kódtól
2. **Docker environment variables** - docker-compose.prod.yml átadja őket
3. **Gmail App Password** - Fejlesztéshez megfelelő, production-ben jobb egy dedikált SMTP service
4. **HTTPS** - tasks.gaborsiknet.hu már HTTPS-en fut
5. **JWT Authentication** - Minden export endpoint védett

**Példa .env fájl struktúra AWS-en:**
```bash
# ~/taskanalysis-prj/.env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=16-char-app-password
MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
```

---

## Future Enhancements

- [ ] PDF export
- [ ] Diagramok az export fájlban
- [ ] Bulk export (több task egyszerre)
- [ ] Scheduled exports (napi/heti)
- [ ] Cloud storage opció (Google Drive, OneDrive)
