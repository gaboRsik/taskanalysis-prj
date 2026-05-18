# 🤖 AI Chatbot Implementációs Útmutató - LangChain4j + Groq

## 📋 Áttekintés

Ez a guide végigvezet egy **AI-powered task performance analyzer chatbot** építésén, amely:
- 💬 Természetes nyelven értékeli a task teljesítményét
- 📊 Elemzi az idő, pontok és hatékonyság adatokat
- 💡 Személyre szabott javaslatokat ad
- 🔄 Később könnyedén skálázható (OpenAI/Claude)

**Technológiai stack:**
- ☕ **LangChain4j** - Java AI framework
- ⚡ **Groq** - INGYENES, gyors LLM API (LLaMA 3.3)
- 🍃 **Spring Boot 3.2** - Backend framework
- 🎨 **Angular 17** - Frontend
- 🔧 **IntelliJ IDEA** - IDE

**Költség pilot fázisban:** €0 / hó (14,400 request/nap ingyenes)

---

## 🎯 Funkcionális Követelmények

### User Story
```
Mint felhasználó,
Szeretnék egy task teljesítményéről AI-alapú értékelést kapni,
Hogy megértsem mennyire voltam hatékony és mit javíthatok.
```

### Példa Interakció

**User**: "Értékeld ezt a task-ot!"

**AI Chatbot**:
```
Remek munka a "Backend API fejlesztés" task-on! 🎉

📊 Teljesítmény összefoglaló:
- ⏱️ Idő: 2 óra 15 perc (tervezett: 3 óra) → 25% gyorsabb! ✅
- 🎯 Pontok: 8/8 → Tökéletes! 💯
- 📈 Hatékonyság: 125% (kiváló)

💡 Javaslatok:
- Látom hogy gyorsan haladtál - következő alkalommal próbálj 
  ambiciózusabb pontszámot kitűzni (pl. 10 pont).
- Az 1. részfeladat 45 percet vett igénybe, míg a többi átlag 20 perc - 
  érdemes ezt a komplexebb részt előre jobban megtervezni.

Így tovább! 💪
```

---

## 📐 Architektúra

```
┌─────────────────┐
│   Angular UI    │ ← User clicks "Értékelés kérése"
└────────┬────────┘
         │ HTTP POST /api/chatbot/analyze/{taskId}
         ↓
┌─────────────────────────────────────────────┐
│        Spring Boot Backend                   │
│  ┌──────────────────────────────────────┐  │
│  │   ChatbotController                   │  │
│  │   - analyzeTask(taskId)               │  │
│  └──────────────┬───────────────────────┘  │
│                 │                            │
│  ┌──────────────↓───────────────────────┐  │
│  │   ChatbotService                      │  │
│  │   - buildPrompt(task)                 │  │
│  │   - callLLM(prompt)                   │  │
│  └──────────────┬───────────────────────┘  │
│                 │                            │
│  ┌──────────────↓───────────────────────┐  │
│  │   LangChain4j ChatLanguageModel      │  │
│  │   - send(prompt)                      │  │
│  └──────────────┬───────────────────────┘  │
└─────────────────┼───────────────────────────┘
                  │
                  ↓ HTTPS API call
         ┌────────────────┐
         │   Groq API     │ (LLaMA 3.3 70B)
         │   (INGYENES)   │
         └────────────────┘
```

---

## 🔧 FÁZIS 1: Backend Setup

### 1.1. Dependencies (backend/pom.xml)

Add hozzá a LangChain4j és Groq dependency-ket:

```xml
<!-- LangChain4j Core -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>0.36.2</version>
</dependency>

<!-- LangChain4j OpenAI (Groq is OpenAI-compatible!) -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.36.2</version>
</dependency>
```

**Miért OpenAI dependency, ha Groq-ot használunk?**
- Groq API **OpenAI-compatible** (ugyanaz az API struktúra)
- LangChain4j OpenAI client működik Groq-kal is (csak base URL kell módosítani)

**Megjegyzés**: A `langchain4j-spring-boot-starter` NEM létezik! Csak a core és open-ai modulokat használjuk.

---

### 1.2. Groq API Key beszerzése (INGYENES!)

1. **Regisztráció**: https://console.groq.com/
   - Email vagy Google account
   - Nincs bankkártya kérés! 🎉

2. **API Key generálása**:
   - Console → "API Keys" menü
   - "Create API Key" gomb
   - Másold ki: `gsk_...` (hosszú string)
   - **Mentsd el biztonságosan!**

3. **Rate limits (ingyenes tier)**:
   - 30 requests / minute
   - 14,400 requests / day
   - Több mint elég pilot fázishoz!

---

### 1.3. Configuration (application.properties)

```properties
# AI Chatbot Configuration
chatbot.groq.api.key=${GROQ_API_KEY}
chatbot.groq.model=llama-3.3-70b-versatile
chatbot.max.tokens=500
chatbot.temperature=0.7
```

**IntelliJ Environment Variables**:
1. Run → Edit Configurations
2. Environment variables → szerkesztés
3. Add: `GROQ_API_KEY=gsk_your_actual_key_here`
4. Apply → OK

**AWS Production (.env fájl)**:
```bash
# ~/taskanalysis-prj/.env
GROQ_API_KEY=gsk_your_actual_key_here
```

---

### 1.4. Configuration Class (ChatbotConfig.java)

Hozd létre: `backend/src/main/java/com/taskanalysis/config/ChatbotConfig.java`

```java
package com.taskanalysis.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatbotConfig {

    @Value("${chatbot.groq.api.key}")
    private String groqApiKey;

    @Value("${chatbot.groq.model}")
    private String model;

    @Value("${chatbot.max.tokens}")
    private Integer maxTokens;

    @Value("${chatbot.temperature}")
    private Double temperature;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")  // ← Groq endpoint!
                .apiKey(groqApiKey)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)   // ← Debug logging
                .logResponses(true)
                .build();
    }
}
```

**Fontos részletek:**
- `baseUrl("https://api.groq.com/openai/v1")` - Groq API endpoint
- `OpenAiChatModel` - OpenAI-compatible client
- `logRequests/logResponses(true)` - Hasznos debugoláshoz (látod a prompt-ot és választ)

---

### 1.5. DTO (ChatbotAnalysisRequest.java & Response.java)

**Request DTO** (`backend/src/main/java/com/taskanalysis/dto/ChatbotAnalysisRequest.java`):

```java
package com.taskanalysis.dto;

import lombok.Data;

@Data
public class ChatbotAnalysisRequest {
    // Opcionális: később conversation history-hoz
    private String conversationId;
    private String userMessage; // pl. "Adj több tippet!"
}
```

**Response DTO** (`backend/src/main/java/com/taskanalysis/dto/ChatbotAnalysisResponse.java`):

```java
package com.taskanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAnalysisResponse {
    private String analysis;           // AI generált szöveg
    private LocalDateTime timestamp;   // Mikor generálódott
    private Integer tokensUsed;        // Token használat (költség tracking)
    private String model;              // Melyik model-t használtuk
    
    // Metadata (opcionális, később analytics-hez)
    private Long taskId;
    private String taskName;
}
```

---

### 1.6. Service (ChatbotService.java)

**Hozd létre**: `backend/src/main/java/com/taskanalysis/service/ChatbotService.java`

```java
package com.taskanalysis.service;

import com.taskanalysis.dto.ChatbotAnalysisResponse;
import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.Task;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatLanguageModel chatLanguageModel;
    private final TaskService taskService;

    /**
     * Analyze task performance using AI
     * 
     * @param taskId Task to analyze
     * @param userId User requesting analysis (security)
     * @return AI-generated analysis
     */
    @Transactional(readOnly = true)
    public ChatbotAnalysisResponse analyzeTaskPerformance(Long taskId, Long userId) {
        log.info("Analyzing task performance for taskId={}, userId={}", taskId, userId);
        
        // 1. Fetch task with all relationships loaded
        Task task = taskService.getTaskEntityById(taskId, userId);
        
        // 2. Build prompt with task data
        String prompt = buildAnalysisPrompt(task);
        
        // 3. Call LLM
        String aiResponse = callLLM(prompt);
        
        // 4. Build response
        return ChatbotAnalysisResponse.builder()
                .analysis(aiResponse)
                .timestamp(LocalDateTime.now())
                .model("llama-3.3-70b-versatile")
                .taskId(taskId)
                .taskName(task.getName())
                .build();
    }

    /**
     * Build prompt with task performance data
     */
    private String buildAnalysisPrompt(Task task) {
        // Calculate metrics
        int totalPlannedMinutes = task.getPlannedTotalTimeMinutes() != null 
                ? task.getPlannedTotalTimeMinutes() : 0;
        int totalActualMinutes = task.getTotalActualTimeSeconds() / 60;
        int totalPlannedPoints = task.getTotalPlannedPoints();
        int totalActualPoints = task.getTotalActualPoints();
        
        // Calculate efficiency
        double timeEfficiency = totalPlannedMinutes > 0 
                ? (double) totalPlannedMinutes / totalActualMinutes * 100 
                : 0;
        double pointsEfficiency = totalPlannedPoints > 0 
                ? (double) totalActualPoints / totalPlannedPoints * 100 
                : 0;
        
        // Subtask breakdown
        List<Subtask> subtasks = task.getSubtasks();
        StringBuilder subtaskDetails = new StringBuilder();
        for (Subtask subtask : subtasks) {
            long subtaskMinutes = calculateSubtaskMinutes(subtask);
            subtaskDetails.append(String.format(
                "  - Részfeladat #%d: %d perc, %d/%d pont%n",
                subtask.getSubtaskNumber(),
                subtaskMinutes,
                subtask.getActualPoints() != null ? subtask.getActualPoints() : 0,
                subtask.getPlannedPoints() != null ? subtask.getPlannedPoints() : 0
            ));
        }
        
        // Build prompt
        return String.format("""
            Te egy task management asszisztens vagy, aki segít a felhasználóknak 
            értékelni a teljesítményüket és javaslatokat ad a javításra.
            
            Értékeld a következő feladat teljesítményét MAGYARUL, röviden (max 200 szó):
            
            📋 Feladat adatok:
            - Név: "%s"
            - Kategória: %s
            - Státusz: %s
            - Részfeladatok száma: %d
            
            ⏱️ Idő teljesítmény:
            - Tervezett: %d perc
            - Tényleges: %d perc
            - Hatékonyság: %.1f%%
            
            🎯 Pont teljesítmény:
            - Tervezett: %d pont
            - Elért: %d pont
            - Teljesítés: %.1f%%
            
            📊 Részfeladatok:
            %s
            
            Adj egy személyes, motiváló értékelést ami tartalmazza:
            1. 🎉 Pozitív visszajelzés (mit csinált jól)
            2. 📊 Számszerű összefoglaló (idő, pontok)
            3. 💡 1-2 konkrét javaslat a következő task-ra
            
            Használj emotikonokat és barátságos hangnemet! 😊
            """,
            task.getName(),
            task.getCategory() != null ? task.getCategory().getName() : "Nincs kategória",
            task.getStatus(),
            subtasks.size(),
            totalPlannedMinutes,
            totalActualMinutes,
            timeEfficiency,
            totalPlannedPoints,
            totalActualPoints,
            pointsEfficiency,
            subtaskDetails.toString()
        );
    }

    /**
     * Call LLM with prompt
     */
    private String callLLM(String prompt) {
        try {
            log.debug("Sending prompt to LLM: {}", prompt);
            String response = chatLanguageModel.generate(prompt);
            log.debug("Received response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error calling LLM", e);
            return "Sajnos nem sikerült az értékelést generálni. Próbáld újra később! 😔";
        }
    }

    /**
     * Calculate subtask total time in minutes
     */
    private long calculateSubtaskMinutes(Subtask subtask) {
        return subtask.getTimeEntries().stream()
                .filter(entry -> entry.getDurationSeconds() != null)
                .mapToLong(entry -> entry.getDurationSeconds().longValue())
                .sum() / 60;
    }
}
```

**Fontos részletek:**
- `@Transactional(readOnly = true)` - Task relationships betöltése
- `buildAnalysisPrompt()` - Strukturált prompt engineering
- Emoji használat - Barátságos, emberi válasz
- Error handling - Ha API fail, user-friendly üzenet

---

### 1.7. Controller (ChatbotController.java)

**Hozd létre**: `backend/src/main/java/com/taskanalysis/controller/ChatbotController.java`

```java
package com.taskanalysis.controller;

import com.taskanalysis.dto.ChatbotAnalysisResponse;
import com.taskanalysis.entity.User;
import com.taskanalysis.repository.UserRepository;
import com.taskanalysis.security.CurrentUser;
import com.taskanalysis.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    /**
     * Analyze task performance
     * 
     * POST /api/chatbot/analyze/{taskId}
     */
    @PostMapping("/analyze/{taskId}")
    public ResponseEntity<ChatbotAnalysisResponse> analyzeTask(@PathVariable Long taskId) {
        log.info("Received chatbot analysis request for taskId={}", taskId);
        
        // Get current user ID
        Long userId = getCurrentUserId();
        
        // Analyze task
        ChatbotAnalysisResponse response = chatbotService.analyzeTaskPerformance(taskId, userId);
        
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        String email = currentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
```

---

### 1.8. API Testing (backend/api-tests.http)

Add hozzá:

```http
### 10. AI Chatbot - Analyze Task Performance
POST http://localhost:8080/api/chatbot/analyze/36
Authorization: Bearer {{auth_token}}
Content-Type: application/json

###
```

---

## 🎨 FÁZIS 2: Frontend Implementation (Angular)

### 2.1. ChatbotService (Angular Service)

**Hozd létre**: `frontend/src/app/services/chatbot.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatbotAnalysisResponse {
  analysis: string;
  timestamp: string;
  tokensUsed?: number;
  model: string;
  taskId: number;
  taskName: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = `${environment.apiUrl}/chatbot`;

  constructor(private http: HttpClient) {}

  /**
   * Request AI analysis for a task
   */
  analyzeTask(taskId: number): Observable<ChatbotAnalysisResponse> {
    const token = localStorage.getItem('access_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<ChatbotAnalysisResponse>(
      `${this.apiUrl}/analyze/${taskId}`,
      {},
      { headers }
    );
  }
}
```

---

### 2.2. Chatbot Modal Component

**Hozd létre**: `frontend/src/app/components/chatbot-modal/`

**TypeScript** (`chatbot-modal.component.ts`):

```typescript
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatbotService, ChatbotAnalysisResponse } from '../../services/chatbot.service';

@Component({
  selector: 'app-chatbot-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chatbot-modal.component.html',
  styleUrls: ['./chatbot-modal.component.scss']
})
export class ChatbotModalComponent {
  @Input() taskId: number | null = null;
  @Input() taskName: string = '';
  @Output() close = new EventEmitter<void>();

  analysis: string | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit() {
    if (this.taskId) {
      this.requestAnalysis();
    }
  }

  requestAnalysis() {
    if (!this.taskId) return;

    this.loading = true;
    this.error = null;

    this.chatbotService.analyzeTask(this.taskId).subscribe({
      next: (response: ChatbotAnalysisResponse) => {
        this.analysis = response.analysis;
        this.loading = false;
      },
      error: (err) => {
        console.error('Chatbot analysis error:', err);
        this.error = 'Nem sikerült az értékelést lekérni. Próbáld újra!';
        this.loading = false;
      }
    });
  }

  onClose() {
    this.close.emit();
  }
}
```

**HTML** (`chatbot-modal.component.html`):

```html
<div class="modal-backdrop" (click)="onClose()">
  <div class="modal-content" (click)="$event.stopPropagation()">
    <div class="modal-header">
      <h2>🤖 AI Teljesítményértékelés</h2>
      <button class="close-btn" (click)="onClose()">✕</button>
    </div>

    <div class="modal-body">
      <h3>{{ taskName }}</h3>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>AI elemzi a teljesítményed...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error && !loading" class="error">
        <p>{{ error }}</p>
        <button (click)="requestAnalysis()">🔄 Újrapróbálás</button>
      </div>

      <!-- Analysis Result -->
      <div *ngIf="analysis && !loading" class="analysis">
        <div class="analysis-text">{{ analysis }}</div>
        <button class="refresh-btn" (click)="requestAnalysis()">
          🔄 Új értékelés kérése
        </button>
      </div>
    </div>
  </div>
</div>
```

**SCSS** (`chatbot-modal.component.scss`):

```scss
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 0;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;

  h2 {
    margin: 0;
    font-size: 1.5rem;
    color: #333;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #666;
    
    &:hover {
      color: #000;
    }
  }
}

.modal-body {
  padding: 20px;

  h3 {
    margin-top: 0;
    color: #555;
    font-size: 1.2rem;
  }
}

.loading {
  text-align: center;
  padding: 40px 20px;

  .spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #3498db;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 0 auto 20px;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  p {
    color: #666;
  }
}

.error {
  text-align: center;
  padding: 20px;
  color: #d32f2f;

  button {
    margin-top: 10px;
    padding: 10px 20px;
    background: #d32f2f;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;

    &:hover {
      background: #b71c1c;
    }
  }
}

.analysis {
  .analysis-text {
    background: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
    line-height: 1.8;
    white-space: pre-wrap;
    color: #333;
    font-size: 1rem;
  }

  .refresh-btn {
    margin-top: 20px;
    padding: 10px 20px;
    background: #4caf50;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    width: 100%;

    &:hover {
      background: #45a049;
    }
  }
}
```

---

### 2.3. Integráció Tasks Component-be

**Módosítsd**: `frontend/src/app/components/tasks/tasks.component.ts`

```typescript
// Imports
import { ChatbotModalComponent } from '../chatbot-modal/chatbot-modal.component';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    // ... existing imports
    ChatbotModalComponent  // ← ADD THIS
  ],
  // ...
})
export class TasksComponent {
  // ... existing properties
  
  // Chatbot modal state
  showChatbotModal = false;
  selectedTaskForAnalysis: Task | null = null;

  // ... existing methods

  /**
   * Open AI chatbot analysis
   */
  openChatbotAnalysis(task: Task) {
    this.selectedTaskForAnalysis = task;
    this.showChatbotModal = true;
  }

  closeChatbotModal() {
    this.showChatbotModal = false;
    this.selectedTaskForAnalysis = null;
  }
}
```

**HTML** (`tasks.component.html`):

Add hozzá minden task card-hoz egy új gombot:

```html
<!-- Existing task card -->
<div class="task-card" *ngFor="let task of tasks">
  <!-- ... existing content ... -->
  
  <!-- Action buttons -->
  <div class="task-actions">
    <!-- ... existing buttons ... -->
    
    <!-- NEW: AI Analysis Button -->
    <button 
      class="btn-ai-analysis" 
      (click)="openChatbotAnalysis(task)"
      [disabled]="task.status !== 'COMPLETED'"
      title="AI Teljesítményértékelés">
      🤖 AI Értékelés
    </button>
  </div>
</div>

<!-- Chatbot Modal -->
<app-chatbot-modal 
  *ngIf="showChatbotModal && selectedTaskForAnalysis"
  [taskId]="selectedTaskForAnalysis.id"
  [taskName]="selectedTaskForAnalysis.name"
  (close)="closeChatbotModal()">
</app-chatbot-modal>
```

**SCSS** (`tasks.component.scss`):

```scss
.btn-ai-analysis {
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }

  &:disabled {
    background: #ccc;
    cursor: not-allowed;
    opacity: 0.6;
  }
}
```

---

## 🧪 FÁZIS 3: Tesztelés

### 3.1. Backend Tesztelés (IntelliJ)

1. **Environment variables ellenőrzése**:
   - Run → Edit Configurations
   - Environment variables: `GROQ_API_KEY=gsk_...`

2. **Backend indítása**:
   ```bash
   mvn spring-boot:run
   ```

3. **Console log ellenőrzése**:
   ```
   Started TaskAnalysisApplication in X seconds
   ChatbotConfig: LangChain4j model initialized
   ```

4. **API teszt** (`backend/api-tests.http`):
   - Futtasd a "10. AI Chatbot - Analyze Task" request-et
   - Várható válasz: `200 OK`, JSON response AI szöveggel

5. **Log ellenőrzés**:
   ```
   ChatbotService: Analyzing task performance for taskId=36, userId=1
   Sending prompt to LLM: ...
   Received response: Remek munka! ...
   ```

---

### 3.2. Frontend Tesztelés (Browser)

1. **Frontend indítása**:
   ```bash
   cd frontend
   npm start
   ```

2. **Böngésző**: `http://localhost:4200`

3. **Login** → **Tasks oldal**

4. **Válassz egy COMPLETED task-ot**

5. **Kattints**: "🤖 AI Értékelés" gomb

6. **Modal megnyílik**:
   - Loading spinner (~2-5 másodperc)
   - AI válasz megjelenik

7. **Ellenőrizd**:
   - ✅ Személyre szabott szöveg
   - ✅ Magyar nyelv
   - ✅ Emoji-k használata
   - ✅ Számszerű adatok (idő, pontok)
   - ✅ Javaslatok

8. **Console (F12)**:
   - Nincs error
   - `POST http://localhost:8080/api/chatbot/analyze/36 200 OK`

---

### 3.3. Error Handling Teszt

**Teszt 1: Nincs internet**
- Kapcsold le a Wi-Fi-t
- Kattints "AI Értékelés"
- **Elvárás**: "Nem sikerült az értékelést lekérni" hibaüzenet

**Teszt 2: Rossz API key**
- IntelliJ-ben rossz `GROQ_API_KEY` beállítása
- Restart backend
- Kattints "AI Értékelés"
- **Elvárás**: Error handling, user-friendly üzenet

**Teszt 3: Túl sok request (rate limit)**
- Kattints 35x gyorsan "AI Értékelés" (> 30 req/min)
- **Elvárás**: Groq rate limit error, majd sikeres retry

---

## 🚀 FÁZIS 4: Deployment (AWS Production)

### 4.1. Git Commit & Push

```powershell
# Backend + Frontend változások
git add .
git commit -m "feat: Add AI-powered task performance chatbot (LangChain4j + Groq)"
git push origin main
```

### 4.2. AWS Environment Setup

**SSH AWS-re**:
```bash
ssh -i C:\Users\siklo\.ssh\taskanalysis-key.pem ubuntu@3.64.207.108
cd ~/taskanalysis-prj
```

**Add .env fájlhoz**:
```bash
nano .env
```

Adj hozzá:
```bash
# AI Chatbot
GROQ_API_KEY=gsk_your_actual_key_here
```

**Mentés**: `Ctrl+O` → `Enter` → `Ctrl+X`

### 4.3. Docker Rebuild

```bash
# Git pull
git pull origin main

# Full rebuild (backend + frontend)
docker-compose -f docker-compose.prod.yml down
docker rmi -f taskanalysis-prj_backend:latest
docker rmi -f taskanalysis-prj_frontend:latest
docker-compose -f docker-compose.prod.yml build --no-cache
docker-compose -f docker-compose.prod.yml up -d

# Check logs
docker logs taskanalysis-backend-prod --tail 50
```

**Várj**: `Started TaskAnalysisApplication in XX.XXX seconds`

### 4.4. Production Testing

1. **Browser**: `https://tasks.gaborsiknet.hu`
2. **InPrivate mode** (cache elkerülés)
3. **Login** → **Tasks** → **AI Értékelés**
4. **Ellenőrzés**: Működik production-ben is!

---

## 📊 FÁZIS 5: Monitoring & Analytics

### 5.1. Költség Tracking

**Backend log** (`ChatbotService.java`):

```java
@Service
public class ChatbotService {
    
    private long totalRequestsCount = 0;
    
    public ChatbotAnalysisResponse analyzeTaskPerformance(...) {
        totalRequestsCount++;
        log.info("Total chatbot requests: {}", totalRequestsCount);
        
        // ... existing code
    }
}
```

**Groq Dashboard**:
- https://console.groq.com/usage
- Látható: Daily requests, tokens used
- **FREE TIER**: 14,400 req/day (követheted)

### 5.2. User Feedback

**Future enhancement**: Rating system

```typescript
// ChatbotModalComponent
rating: number | null = null;

rateAnalysis(stars: number) {
  this.rating = stars;
  // POST /api/chatbot/feedback
}
```

---

## 🔄 FÁZIS 6: Scaling Path (Ha kinövöd Groq-ot)

### OpenAI-ra váltás (1 sor módosítás!)

**ChatbotConfig.java**:

```java
@Bean
public ChatLanguageModel chatLanguageModel() {
    return OpenAiChatModel.builder()
            .baseUrl("https://api.openai.com/v1")  // ← Csak ez változik!
            .apiKey(openaiApiKey)                   // ← OPENAI_API_KEY env var
            .modelName("gpt-4o-mini")              // ← OpenAI model
            .temperature(temperature)
            .maxTokens(maxTokens)
            .timeout(Duration.ofSeconds(30))
            .build();
}
```

**application.properties**:
```properties
chatbot.openai.api.key=${OPENAI_API_KEY}
chatbot.openai.model=gpt-4o-mini
```

**Costs**: ~€15-30/hó (500 user)

**Migráció idő**: ~10 perc (config change + redeploy)

---

## 💡 Best Practices & Tips

### ✅ Prompt Engineering

**Jó prompt szerkezet:**
```
1. Role definition: "Te egy task management asszisztens vagy..."
2. Task: "Értékeld a következő feladat teljesítményét..."
3. Context: Task adatok strukturáltan
4. Output format: "Adj egy személyes értékelést ami tartalmazza..."
5. Style: "Használj emotikonokat és barátságos hangnemet!"
```

### ✅ Security

**Rate Limiting** (jövőbeli fejlesztés):

```java
@RateLimiter(name = "chatbot", fallbackMethod = "rateLimitFallback")
public ChatbotAnalysisResponse analyzeTaskPerformance(...) {
    // ...
}

public ChatbotAnalysisResponse rateLimitFallback(...) {
    return ChatbotAnalysisResponse.builder()
            .analysis("Túl sok kérést küldtél! Várj egy kicsit. 😊")
            .build();
}
```

### ✅ Caching (költségcsökkentés)

**Redis cache** (future):

```java
@Cacheable(value = "task-analysis", key = "#taskId")
public ChatbotAnalysisResponse analyzeTaskPerformance(...) {
    // Ha ugyanazt a task-ot újra elemzik,
    // nem hívjuk az API-t, cache-ből adjuk
}
```

### ✅ Error Handling

**Graceful degradation**:
- Ha API fail → user-friendly üzenet
- Retry mechanizmus
- Timeout kezelés (30s)

---

## 🎯 Összefoglalás

### Mit építettünk?

✅ **Backend**:
- LangChain4j integráció
- Groq API connection (ingyenes)
- ChatbotService (prompt engineering)
- REST API endpoint

✅ **Frontend**:
- Angular modal component
- ChatbotService
- UI integration (AI gomb task card-okon)

✅ **Features**:
- Task teljesítmény elemzés
- Natural language feedback
- Személyre szabott javaslatok
- Emoji-gazdag, barátságos UI

### Időigény:
- Backend setup: 2-3 óra
- Frontend implementation: 2-3 óra
- Testing & refinement: 1-2 óra
- **Total: ~1 munkanap** 🚀

### Költség (pilot fázis):
- **€0 / hó** (Groq ingyenes tier)
- Scaling later: €15-30/hó (OpenAI GPT-4o mini)

### Tanulási érték:
- ⭐⭐⭐⭐⭐ LangChain4j framework
- ⭐⭐⭐⭐⭐ Prompt engineering
- ⭐⭐⭐⭐ AI API integráció
- ⭐⭐⭐⭐ Spring Boot + Angular full-stack

---

## 🆘 Troubleshooting

### Backend nem indul

**Hiba**: `Could not autowire ChatLanguageModel`
**Megoldás**: 
1. Ellenőrizd `ChatbotConfig.java` létezik
2. `@Configuration` annotáció megvan
3. Maven dependencies refresh: IntelliJ → Maven → Reload

### Groq API 401 Unauthorized

**Hiba**: `401 Unauthorized`
**Megoldás**:
1. Ellenőrizd `GROQ_API_KEY` env var
2. API key helyes? (gsk_...)
3. IntelliJ restart

### Frontend modal nem jelenik meg

**Hiba**: Modal gomb nem működik
**Megoldás**:
1. Console (F12) → Errors?
2. `ChatbotModalComponent` import megvan?
3. `tasks.component.ts` methods léteznek?

### AI válasz üres vagy rossz

**Hiba**: "Sajnos nem sikerült..."
**Megoldás**:
1. Backend logs: `docker logs taskanalysis-backend-prod`
2. Groq Dashboard: Rate limit?
3. Prompt túl hosszú? (max ~8000 token)

### Groq API Access Denied (VPN/Antivirus)

**Hiba**: `OpenAiHttpException: Access denied. Please check your network settings.`
**Ok**: Antivirus VPN vagy Web Shield blokkolja az API hívást

**Megoldás**:
1. **Kapcsold ki az antivirus VPN/Web Shield-et**:
   - Avast/AVG: Settings → Core Shields → VPN Shield → OFF
   - Norton: Settings → Firewall → Smart Firewall → OFF (ideiglenesen)
   - Kaspersky: Settings → Network → Disable VPN

2. **Whitelist hozzáadása** (jobb megoldás):
   - Antivirus → Exceptions → Add: `api.groq.com`

3. **Windows Firewall ellenőrzés** (ha továbbra sem megy):
   ```powershell
   # Teszt: Groq API elérhetőség
   Invoke-WebRequest -Uri https://api.groq.com/openai/v1/models -Headers @{"Authorization"="Bearer $env:GROQ_API_KEY"}
   ```

**Fontos**: Ez a probléma gyakran előfordul fejlesztés közben. Production környezetben (AWS) nincs VPN, ezért ott nem jelentkezik!

---

## 📚 További Tanulási Források

**LangChain4j**:
- Dokumentáció: https://docs.langchain4j.dev/
- GitHub: https://github.com/langchain4j/langchain4j
- Examples: https://github.com/langchain4j/langchain4j-examples

**Groq**:
- Console: https://console.groq.com/
- Docs: https://console.groq.com/docs
- Models: LLaMA 3.3, Mixtral, Gemma

**Prompt Engineering**:
- OpenAI Guide: https://platform.openai.com/docs/guides/prompt-engineering
- Best practices: https://www.promptingguide.ai/

---

## ✅ Következő Lépések

**Azonnal:**
1. [ ] Groq API key beszerzése (5 perc)
2. [ ] Backend dependencies hozzáadása
3. [ ] ChatbotConfig létrehozása
4. [ ] Első API teszt

**Később (ha beválik):**
1. [ ] Conversation history (multi-turn chat)
2. [ ] RAG (Retrieval Augmented Generation) - előző task-ok adatai
3. [ ] Személyre szabás (user preferenciák)
4. [ ] Email summary (heti AI riport)

---

**Hajrá, kezdd el! 🚀 Ha elakadsz, írj!**
