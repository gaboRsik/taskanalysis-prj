# 🎯 Task Status Management System

## 📋 Áttekintés

A Task Status Management rendszer átfogó megoldást nyújt a task-ok és subtask-ok státuszának kezelésére, beleértve:
- ⏱️ Időalapú automatikus befejezést
- 🖱️ Manuális státuszváltást
- ✅ Validációkat és korlátozásokat
- 🔄 Újranyitási lehetőséget adatmegőrzéssel

---

## 🎯 Funkcionális Követelmények

### 1. Timer Indítás Korlátozása
**Követelmény**: Timer csak akkor indítható, ha a task-nak van `planned_total_time_minutes` beállítva.

**Viselkedés**:
- ❌ **Nincs planned time**: Timer start disabled / error message
- ✅ **Van planned time**: Timer start enabled

**Implementáció**:
- **Backend**: `TimerService.startTimer()` - validáció exception dobással
- **Frontend**: `DashboardComponent.startTimer()` - elővalidáció alert-tel

**Error üzenet**: 
```
"Cannot start timer: Task must have planned total time set first"
```

---

### 2. Automatikus Befejezés Időtúllépéskor

**Követelmény**: Ha a tényleges idő eléri/túllépi a tervezett időt, task automatikusan COMPLETED státuszba kerül.

**Működés**:
1. **Scheduled Job**: Percenként ellenőrzi az IN_PROGRESS task-okat
2. **Számítás**: `Σ(subtask.actual_time) >= task.planned_total_time_minutes * 60`
3. **Auto-Complete**:
   - Task → COMPLETED
   - Összes IN_PROGRESS subtask → COMPLETED
   - Futó timerek leállítása
   - Cascade update

**Implementáció**:
- **Backend**: `TaskCompletionScheduler.checkAndCompleteExpiredTasks()` - @Scheduled(fixedRate = 60000)
- **Logika**: `TaskCompletionScheduler.autoCompleteTask(task)`

**Log példa**:
```
INFO: Auto-completing task 36 - planned time expired
INFO: Stopped timer for subtask 157 (auto-completion)
INFO: Task 36 auto-completed successfully
```

---

### 3. Manuális Task Befejezés

**Követelmény**: Felhasználó manuálisan is befejezheti a task-ot a "Complete" gombbal.

**Működés**:
1. **Complete gomb**: Megjelenik IN_PROGRESS task-oknál
2. **Confirm dialog**: "Complete task 'X'? This will stop all running timers..."
3. **Status update**: Task → COMPLETED, IN_PROGRESS subtasks → COMPLETED
4. **Timer stop**: Futó timerek leállítása cascade-del

**Implementáció**:
- **Backend**: 
  - Endpoint: `PATCH /api/tasks/{id}/status`
  - Service: `TaskStatusService.changeTaskStatus()`
  - Logika: `TaskStatusService.completeTaskAndSubtasks()`
- **Frontend**: 
  - Button: `tasks.component.html` - "Complete" gomb
  - Method: `TasksComponent.completeTask(task)`

**UI példa**:
```html
<button 
  *ngIf="canCompleteTask(task)"
  class="btn btn-sm btn-success" 
  (click)="completeTask(task)">
  <i class="bi bi-check-circle-fill"></i> Complete
</button>
```

---

### 4. Status Transition Validációk

**Követelmény**: Bizonyos státuszváltások tiltottak a következetesség érdekében.

#### Tiltott Transition: NOT_STARTED → COMPLETED

**Szabály**: Task nem fejezhető be közvetlenül, ha még nem indult el.

**Implementáció**:
```java
if (currentStatus == Task.TaskStatus.NOT_STARTED && 
    newStatus == Task.TaskStatus.COMPLETED) {
    throw new RuntimeException(
        "Cannot complete task that has not started. Start the task first.");
}
```

**Frontend handling**:
- Complete gomb disabled, ha status !== 'IN_PROGRESS'
- `canCompleteTask(task)` metódus ellenőrzés

#### Engedélyezett Transition: NOT_STARTED → IN_PROGRESS

**Szabály**: Task indítható (első timer start automatikusan váltja).

**Validáció**: Planned time kötelező!
```java
if (currentStatus == Task.TaskStatus.NOT_STARTED && 
    newStatus == Task.TaskStatus.IN_PROGRESS &&
    task.getPlannedTotalTimeMinutes() == null) {
    throw new RuntimeException(
        "Cannot start task without planned total time...");
}
```

---

### 5. Időkorlát Validáció

**Követelmény**: `Σ(subtask.totalTimeSeconds) ≤ task.planned_total_time_minutes * 60`

**Működés**:
- **Ellenőrzés**: `TaskStatusService.validateTimeConstraint(task)`
- **Használat**: Jelenleg informational (nem block)
- **Jövő**: Később lehet hard constraint timer stop-nál

**Számítás példa**:
```
Task planned time: 120 minutes (7200 seconds)
Subtask 1: 3600s (60 min)
Subtask 2: 2400s (40 min)
Subtask 3: 1800s (30 min)
Total: 7800s (130 min) → OVER PLANNED! ⚠️
```

---

### 6. AI Chatbot Hozzáférés Korlátozása

**Követelmény**: AI értékelés csak COMPLETED task-okra, time és points adatokkal.

**Validációk**:
1. ✅ `task.status == COMPLETED`
2. ✅ `task.totalActualTimeSeconds > 0`
3. ✅ `task.totalActualPoints > 0`

**Implementáció**:
```java
@Service
public class TaskStatusService {
    public boolean canAnalyzeWithAI(Task task) {
        if (task.getStatus() != Task.TaskStatus.COMPLETED) return false;
        if (task.getTotalActualTimeSeconds() == null || 
            task.getTotalActualTimeSeconds() == 0) return false;
        if (task.getTotalActualPoints() == null || 
            task.getTotalActualPoints() == 0) return false;
        return true;
    }
}
```

**Frontend**:
```html
<button 
  (click)="openChatbotAnalysis(task)"
  [disabled]="task.status !== 'COMPLETED'">
  <i class="bi bi-cpu-fill"></i> AI
</button>
```

**Error message**:
```
"AI analysis requires a COMPLETED task with time and points data. 
Status: IN_PROGRESS, Time: 3600s, Points: 0"
```

---

### 7. Task Újranyitás (Reopen)

**Követelmény**: COMPLETED task újranyitható IN_PROGRESS státuszba, **adatok megőrzésével**.

**Működés**:
1. **Reopen gomb**: Megjelenik COMPLETED task-oknál
2. **Confirm dialog**: "Reopen task 'X'? Time and points data will be preserved."
3. **Status update**: Task → IN_PROGRESS
4. **Adatmegőrzés**: 
   - ✅ `totalActualTimeSeconds` megmarad
   - ✅ `totalActualPoints` megmarad
   - ✅ Subtask status-ok megmaradnak (COMPLETED marad)
   - ✅ TimeEntry rekordok változatlanok

**Implementáció**:
- **Backend**: `TaskStatusService.changeTaskStatus(taskId, userId, IN_PROGRESS)`
- **Frontend**: `TasksComponent.reopenTask(task)`

**UI példa**:
```html
<button 
  *ngIf="canReopenTask(task)"
  class="btn btn-sm btn-warning" 
  (click)="reopenTask(task)">
  <i class="bi bi-arrow-counterclockwise"></i> Reopen
</button>
```

**Use case**: User rájön hogy még van munka, vagy javítani kell valamit.

---

## 🔄 Státusz Flow Diagram

```
┌───────────────┐
│  NOT_STARTED  │ (Kezdeti állapot)
└───────┬───────┘
        │
        │ (Timer start - csak ha planned_time != null)
        ↓
┌───────────────┐
│  IN_PROGRESS  │ (Aktív munka)
└───────┬───────┘
        │
        ├─────────────────────────────────┐
        │                                 │
        │ (Automatic)                     │ (Manual)
        │ Σ(time) >= planned_time         │ "Complete" gomb
        │                                 │
        ↓                                 ↓
┌─────────────────────────────────────────┐
│              COMPLETED                  │
└──────────────┬──────────────────────────┘
               │
               │ (Manual)
               │ "Reopen" gomb
               │ (adatok megmaradnak!)
               ↓
         ┌───────────────┐
         │  IN_PROGRESS  │
         └───────────────┘
```

---

## 🛠️ Technikai Implementáció

### Backend Komponensek

#### 1. TaskStatusRequest DTO
```java
@Data
public class TaskStatusRequest {
    @NotNull(message = "Status is required")
    private Task.TaskStatus status;
}
```

#### 2. TaskStatusService
**Felelősség**: Status business logic, validációk, cascade updates

**Metódusok**:
- `changeTaskStatus(taskId, userId, newStatus)` - Főmetódus status változtatáshoz
- `validateStatusTransition(task, newStatus)` - Transition rule ellenőrzés
- `completeTaskAndSubtasks(task)` - Complete with cascade (stop timers)
- `validateTimeConstraint(task)` - Időkorlát ellenőrzés
- `canAnalyzeWithAI(task)` - AI chatbot előfeltétel ellenőrzés

**Példa használat**:
```java
@Transactional
public Task changeTaskStatus(Long taskId, Long userId, Task.TaskStatus newStatus) {
    Task task = taskRepository.findById(taskId).orElseThrow();
    
    // Security check
    if (!task.getUser().getId().equals(userId)) {
        throw new RuntimeException("Access denied");
    }
    
    // Validate transition
    validateStatusTransition(task, newStatus);
    
    // Change status
    task.setStatus(newStatus);
    
    // Cascade logic
    if (newStatus == Task.TaskStatus.COMPLETED) {
        completeTaskAndSubtasks(task);
    }
    
    return taskRepository.save(task);
}
```

#### 3. TaskCompletionScheduler
**Felelősség**: Időalapú automatikus befejezés

**Konfigurá**: `@Scheduled(fixedRate = 60000)` - 1 percenként

**Logika**:
```java
@Scheduled(fixedRate = 60000)
@Transactional
public void checkAndCompleteExpiredTasks() {
    List<Task> inProgressTasks = taskRepository.findByStatus(Task.TaskStatus.IN_PROGRESS);
    
    for (Task task : inProgressTasks) {
        if (task.getPlannedTotalTimeMinutes() != null && 
            hasExceededPlannedTime(task)) {
            autoCompleteTask(task);
        }
    }
}
```

**Enable scheduling**:
```java
@SpringBootApplication
@EnableScheduling  // ← FONTOS!
public class TaskAnalysisApplication { ... }
```

#### 4. TaskController Endpoint
```java
@PatchMapping("/{id}/status")
public ResponseEntity<TaskResponse> changeTaskStatus(
        @PathVariable Long id,
        @Valid @RequestBody TaskStatusRequest request) {
    Long userId = getCurrentUserId();
    Task updatedTask = taskStatusService.changeTaskStatus(id, userId, request.getStatus());
    TaskResponse response = taskService.getTaskById(userId, id);
    return ResponseEntity.ok(response);
}
```

#### 5. TimerService Validáció
**Új validáció** `startTimer()` metódusban:
```java
@Transactional
public TimerResponse startTimer(Long userId, Long subtaskId) {
    Subtask subtask = subtaskRepository.findById(subtaskId).orElseThrow();
    Task task = subtask.getTask();
    
    // NEW: Validate planned time
    if (task.getPlannedTotalTimeMinutes() == null) {
        throw new RuntimeException(
            "Cannot start timer: Task must have planned total time set first");
    }
    
    // ... rest of timer logic
}
```

#### 6. ChatbotService Validáció
**Új validáció** `analyzeTaskPerformance()` metódusban:
```java
@Transactional(readOnly = true)
public ChatbotAnalysisResponse analyzeTaskPerformance(Long taskId, Long userId) {
    Task task = taskService.getTaskEntityById(taskId, userId);
    
    // NEW: Validate task can be analyzed
    if (!taskStatusService.canAnalyzeWithAI(task)) {
        throw new RuntimeException(
            "AI analysis requires a COMPLETED task with time and points data...");
    }
    
    // ... rest of AI logic
}
```

---

### Frontend Komponensek

#### 1. TaskService - updateTaskStatus()
**Új metódus**:
```typescript
updateTaskStatus(taskId: number, status: string): Observable<Task> {
  return this.http.patch<Task>(`${this.apiUrl}/${taskId}/status`, { status });
}
```

#### 2. TasksComponent - Complete/Reopen
**Metódusok**:
```typescript
completeTask(task: Task): void {
  if (confirm(`Complete task "${task.name}"?...`)) {
    this.taskService.updateTaskStatus(task.id, 'COMPLETED')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedTask) => {
          // Update local list
          const index = this.tasks.findIndex(t => t.id === updatedTask.id);
          if (index !== -1) this.tasks[index] = updatedTask;
          this.filterTasks();
          alert('✅ Task completed successfully!');
        },
        error: (error) => {
          const errorMsg = error.error?.message || 'Failed to complete task';
          alert(`❌ ${errorMsg}`);
        }
      });
  }
}

reopenTask(task: Task): void {
  if (confirm(`Reopen task "${task.name}"?...`)) {
    this.taskService.updateTaskStatus(task.id, 'IN_PROGRESS')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedTask) => {
          const index = this.tasks.findIndex(t => t.id === updatedTask.id);
          if (index !== -1) this.tasks[index] = updatedTask;
          this.filterTasks();
          alert('✅ Task reopened successfully!');
        },
        error: (error) => {
          const errorMsg = error.error?.message || 'Failed to reopen task';
          alert(`❌ ${errorMsg}`);
        }
      });
  }
}

canCompleteTask(task: Task): boolean {
  return task.status === 'IN_PROGRESS';
}

canReopenTask(task: Task): boolean {
  return task.status === 'COMPLETED';
}
```

**UI Gombok**:
```html
<!-- Complete Button -->
<button 
  *ngIf="canCompleteTask(task)"
  class="btn btn-sm btn-success" 
  (click)="completeTask(task)"
  title="Complete Task">
  <i class="bi bi-check-circle-fill"></i> Complete
</button>

<!-- Reopen Button -->
<button 
  *ngIf="canReopenTask(task)"
  class="btn btn-sm btn-warning" 
  (click)="reopenTask(task)"
  title="Reopen Task">
  <i class="bi bi-arrow-counterclockwise"></i> Reopen
</button>
```

#### 3. DashboardComponent - Timer Validáció
**Frontend elővalidáció** timer start előtt:
```typescript
startTimer(subtaskId: number): void {
  // Validate: Task must have planned time
  if (this.selectedTask && !this.selectedTask.plannedTotalTimeMinutes) {
    alert('⚠️ Cannot start timer: Task must have planned total time set first...');
    return;
  }

  this.timerService.startTimer(subtaskId)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (timer) => {
        this.activeTimer = timer;
        this.startTimerDisplay();
        this.loadTasks();
      },
      error: (error) => {
        const errorMsg = error.error?.message || 'Failed to start timer';
        alert(`❌ ${errorMsg}`);
      }
    });
}
```

#### 4. Toast Notifications (Future Enhancement)
**Jelenlegi megoldás**: `alert()` JavaScript dialógok

**Jövőbeli fejlesztés**: Angular Material Snackbar vagy egyéni toast komponens

**Példa használat** (jövő):
```typescript
// Auto-completion notification (WebSocket vagy polling)
this.toastService.success(
  `Task "${task.name}" auto-completed - planned time reached!`,
  { duration: 5000 }
);

// Manual completion
this.toastService.success('Task completed successfully!');

// Reopen
this.toastService.info('Task reopened - continue working!');

// Error
this.toastService.error('Failed to complete task');
```

---

## 🧪 Tesztelési Útmutató

### Backend API Tesztek

**Fájl**: `backend/api-tests.http`

#### Test 1: Task Complete (Manual)
```http
### 11. Change Task Status to COMPLETED
PATCH http://localhost:8080/api/tasks/36/status
Content-Type: application/json
Authorization: Bearer {{auth_token}}

{
  "status": "COMPLETED"
}
```

**Elvárt válasz**: `200 OK`, Task JSON `status: "COMPLETED"`

**Ellenőrzés**:
- Task status → COMPLETED
- IN_PROGRESS subtasks → COMPLETED
- Futó timerek leálltak

#### Test 2: Task Reopen
```http
### 12. Reopen Task (Change Status to IN_PROGRESS)
PATCH http://localhost:8080/api/tasks/36/status
Content-Type: application/json
Authorization: Bearer {{auth_token}}

{
  "status": "IN_PROGRESS"
}
```

**Elvárt válasz**: `200 OK`, Task JSON `status: "IN_PROGRESS"`

**Ellenőrzés**:
- Task status → IN_PROGRESS
- `totalActualTimeSeconds` **megmaradt** (nem null!)
- `totalActualPoints` **megmaradt**

#### Test 3: Tiltott Transition (NOT_STARTED → COMPLETED)
```http
### Test: Invalid Transition
PATCH http://localhost:8080/api/tasks/37/status
Content-Type: application/json
Authorization: Bearer {{auth_token}}

{
  "status": "COMPLETED"
}
```

**Elvárt válasz**: `400 Bad Request` vagy `500 Internal Server Error`

**Error message**: `"Cannot complete task that has not started..."`

#### Test 4: Timer Start Without Planned Time
```http
### Test: Start timer for task without planned time
POST http://localhost:8080/api/timer/start/158
Authorization: Bearer {{auth_token}}
```

**Elvárt válasz**: `500 Internal Server Error`

**Error message**: `"Cannot start timer: Task must have planned total time set first"`

#### Test 5: AI Analysis on IN_PROGRESS Task
```http
### Test: AI Analysis (should fail for IN_PROGRESS)
POST http://localhost:8080/api/chatbot/analyze/36
Authorization: Bearer {{auth_token}}
Content-Type: application/json
```

**Elvárt válasz**: `500 Internal Server Error`

**Error message**: `"AI analysis requires a COMPLETED task with time and points data..."`

---

### Frontend Tesztelés

#### Test Scenario 1: Manual Complete
1. **Böngésző**: `http://localhost:4200`
2. **Login** → **Tasks oldal**
3. **Válassz egy IN_PROGRESS task-ot**
4. **Kattints**: "Complete" gomb (zöld, check icon)
5. **Confirm**: Confirmation dialog
6. **Ellenőrzés**:
   - Task card frissült → Status badge "COMPLETED"
   - Complete gomb eltűnt
   - Reopen gomb megjelent (sárga, counterclockwise icon)
   - AI gomb enabled lett

#### Test Scenario 2: Reopen Task
1. **COMPLETED task**: Előző tesztből
2. **Kattints**: "Reopen" gomb
3. **Confirm**: Confirmation dialog
4. **Ellenőrzés**:
   - Task status → IN_PROGRESS
   - Time/Points adatok **megmaradtak** (nem nullázódtak!)
   - Reopen gomb eltűnt
   - Complete gomb megjelent
   - AI gomb disabled lett

#### Test Scenario 3: Timer Start Validation
1. **Új task létrehozása** **PLANNED TIME NÉLKÜL**:
   - Name: "Test Task No Time"
   - Category: Work
   - Subtasks: 2
   - **Planned time**: ÜRESEN HAGYVA!
2. **Dashboard oldal**
3. **Válaszd ki** a task-ot dropdown-ból
4. **Kattints**: Start gomb subtask mellett
5. **Ellenőrzés**:
   - Alert dialog: "⚠️ Cannot start timer: Task must have planned total time set first..."
   - Timer NEM indult el

#### Test Scenario 4: Auto-Completion (Scheduled Job)
**Setup**:
1. **Hozz létre task-ot**: Planned time = 1 perc
2. **Indítsd el**: Timer subtask-ra
3. **Várj**: 61+ másodperc (1 perc + 1 sec safety margin)
4. **Ellenőrzés**:
   - Backend log: "Auto-completing task X - planned time expired"
   - Task status → COMPLETED (automatikusan!)
   - Timer leállt
   - Frontend: F5 után task COMPLETED (vagy WebSocket notification)

**Figyelem**: Scheduled job 1 percenként fut, szóval lehet 1 perc késés!

#### Test Scenario 5: AI Button Enablement
1. **IN_PROGRESS task**: AI gomb disabled (szürke)
2. **Complete task**: AI gomb enabled (lila gradient)
3. **Kattints AI gomb**: Modal megnyílik, AI analysis sikeres
4. **Reopen task**: AI gomb disabled újra

---

## 📊 Database Impact

### Task Tábla
**Módosítások**: NINCS schema change!

**Használt mezők**:
- `status` - ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')
- `planned_total_time_minutes` - INT (nullable)
- `total_actual_time_seconds` - INT (computed field)
- `total_actual_points` - INT (computed field)

### Subtask Tábla
**Módosítások**: NINCS schema change!

**Használt mezők**:
- `status` - ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')

### TimeEntry Tábla
**Módosítások**: NINCS schema change!

**Cascade stop** logika:
```sql
UPDATE time_entry 
SET end_time = NOW(), 
    duration_seconds = TIMESTAMPDIFF(SECOND, start_time, NOW())
WHERE subtask_id IN (SELECT id FROM subtask WHERE task_id = ?)
  AND end_time IS NULL;
```

---

## 🔐 Security Considerations

### Authorization
**Minden endpoint** ellenőrzi: `task.user.id == currentUser.id`

```java
if (!task.getUser().getId().equals(userId)) {
    throw new RuntimeException("Access denied");
}
```

### Rate Limiting (Future)
**Scheduled job**: Nincs rate limit (server-side job)

**API endpoints**: Jelenleg nincs rate limit, de jövőben:
```java
@RateLimiter(name = "taskStatus", fallbackMethod = "rateLimitFallback")
public Task changeTaskStatus(...) { ... }
```

---

## 🚀 Deployment

### Backend
1. **Git commit & push**: Változások feltöltése
2. **SSH AWS-re**: `ssh ubuntu@3.64.207.108`
3. **Pull changes**: `git pull origin main`
4. **Docker rebuild**:
   ```bash
   docker-compose -f docker-compose.prod.yml down
   docker-compose -f docker-compose.prod.yml build --no-cache
   docker-compose -f docker-compose.prod.yml up -d
   ```
5. **Verify logs**: `docker logs taskanalysis-backend-prod --tail 50`
6. **Check scheduling**: Log-ban látható: "Checking for tasks with expired planned time..."

### Frontend
1. **Git commit & push**: UI változások feltöltése
2. **AWS rebuild**: (ugyanaz mint backend)
3. **Browser cache clear**: Ctrl+Shift+R
4. **Test production**: `https://tasks.gaborsiknet.hu`

---

## 📈 Future Enhancements

### 1. WebSocket Notifications
**Probléma**: Jelenleg auto-completion után user nem értesül (csak F5 után látja).

**Megoldás**: WebSocket push notification
```typescript
this.websocket.subscribe('/topic/task-completed', (message) => {
  this.toastService.info(`Task "${message.taskName}" auto-completed!`);
  this.loadTasks();
});
```

### 2. Toast Notification Service
**Probléma**: `alert()` blokkoló és nem szép.

**Megoldás**: Angular Material Snackbar vagy egyéni toast
```typescript
@Injectable()
export class ToastService {
  success(message: string, duration = 3000) { ... }
  error(message: string, duration = 5000) { ... }
  info(message: string, duration = 3000) { ... }
  warning(message: string, duration = 4000) { ... }
}
```

### 3. Undo Functionality
**Feature**: "Undo Complete" gomb 10 másodpercig látható.

**Implementáció**: Temporal status change queue
```typescript
completeTask(task: Task) {
  this.statusHistory.push({ taskId: task.id, oldStatus: task.status });
  // ... complete logic
  this.showUndoToast(10000); // 10 sec undo window
}
```

### 4. Bulk Operations
**Feature**: "Complete All" vagy "Reopen Selected" többszörös kijelölésre.

**UI**: Checkbox task card-okon + bulk action buttons

### 5. Status Change History
**Feature**: Audit log task status változásokról.

**Schema**:
```sql
CREATE TABLE task_status_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  old_status VARCHAR(20),
  new_status VARCHAR(20) NOT NULL,
  changed_by_user_id BIGINT NOT NULL,
  change_type ENUM('MANUAL', 'AUTOMATIC'),
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (task_id) REFERENCES task(id)
);
```

### 6. Configurable Auto-Completion
**Feature**: User beállíthatja hogy automatikus vagy manuális completion.

**Settings UI**:
```
[ ] Enable automatic task completion when planned time expires
[✓] Notify me 5 minutes before planned time expires
```

---

## 🆘 Troubleshooting

### Backend nem indul el

**Hiba**: `ClassNotFoundException: TaskStatusService`
**Megoldás**: 
```bash
mvn clean compile
# IntelliJ: Maven → Reimport
```

### Scheduled job nem fut

**Hiba**: Task-ok nem complete-olódnak automatikusan
**Ellenőrzés**:
1. `@EnableScheduling` annotáció megvan? (`TaskAnalysisApplication.java`)
2. Backend log: "Checking for tasks with expired planned time..." látható?
3. Task-nak van `planned_total_time_minutes` értéke?
4. Task status valóban `IN_PROGRESS`?

**Debug log**:
```java
@Scheduled(fixedRate = 60000)
public void checkAndCompleteExpiredTasks() {
    log.info("Scheduler running - checking tasks...");
    // ...
}
```

### Complete gomb nem jelenik meg

**Hiba**: IN_PROGRESS task-nál sem látszik Complete gomb
**Ellenőrzés**:
1. Frontend `canCompleteTask(task)` metódus jó?
2. `*ngIf="canCompleteTask(task)"` conditional rendering OK?
3. Task status string: `"IN_PROGRESS"` (nem enum!)
4. Browser console error?

### AI gomb enabled marad IN_PROGRESS-nél

**Hiba**: AI gomb nem disabled IN_PROGRESS task-nál
**Ellenőrzés**:
```html
[disabled]="task.status !== 'COMPLETED'"
```
**Figyelem**: String comparison, NEM enum!

---

## ✅ Checklist - Tesztelés előtt

### Backend
- [ ] `@EnableScheduling` annotáció hozzáadva
- [ ] `TaskStatusService` létezik és bean
- [ ] `TaskCompletionScheduler` létezik és `@Component`
- [ ] `TaskRepository.findByStatus()` metódus létezik
- [ ] `PATCH /tasks/{id}/status` endpoint működik
- [ ] `TimerService.startTimer()` validál planned time-ot
- [ ] `ChatbotService.analyzeTaskPerformance()` validál COMPLETED status-t
- [ ] Backend log-ok látszanak (scheduled job running)

### Frontend
- [ ] `TaskService.updateTaskStatus()` metódus létezik
- [ ] `TasksComponent` Complete/Reopen metódusok implementálva
- [ ] UI gombok kondicionálisan renderelődnek
- [ ] Timer start validáció frontend-en is megvan
- [ ] Bootstrap Icons betöltve (check-circle-fill, arrow-counterclockwise)
- [ ] Confirm dialógok működnek
- [ ] Error handling alert-tel vagy toast-tal

### Testing
- [ ] API teszt: Manual complete
- [ ] API teszt: Reopen task
- [ ] API teszt: Tiltott transition
- [ ] UI teszt: Complete gomb kattintás
- [ ] UI teszt: Reopen gomb kattintás
- [ ] UI teszt: Timer start validation
- [ ] End-to-end: Auto-completion (1 perc várakozás)
- [ ] AI gomb enable/disable logic

---

## 📚 Kapcsolódó Dokumentumok

- **API Endpoints**: [`TESTING_GUIDE.md`](./TESTING_GUIDE.md)
- **AI Chatbot**: [`AI_CHATBOT_IMPLEMENTATION_GUIDE.md`](./AI_CHATBOT_IMPLEMENTATION_GUIDE.md)
- **Architecture**: [`architecture.md`](./architecture.md)
- **Backend API Tests**: [`backend/api-tests.http`](../backend/api-tests.http)

---

**Készítette**: Copilot AI  
**Dátum**: 2026. május 18.  
**Verzió**: 1.0.0  
**Status**: ✅ Implementálva és Tesztelve
