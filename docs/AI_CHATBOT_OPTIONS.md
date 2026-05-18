# 🤖 AI Chatbot Integration - Lehetőségek és Költségelemzés

## 📋 Áttekintés

Ez a dokumentum bemutatja az AI chatbot integrációs lehetőségeket a Task Analysis alkalmazásba. A chatbot célja, hogy intelligens teljesítményértékelést adjon a felhasználó task-jairól.

**Chatbot funkciók:**
- 📊 Task teljesítmény elemzése (idő, pontok, hatékonyság)
- 💡 Személyre szabott javaslatok
- 📈 Trendek és minták azonosítása
- ⚡ Natural language interface

**Elérhető adatok a chatbot számára:**
- Részfeladatok (subtasks) időmérései
- Tervezett vs. tényleges pontok
- Teljesítési idő vs. becsült idő
- Kategóriák és task státuszok
- Történeti adatok (előző task-ok)

---

## 🎯 Összehasonlító Táblázat

| Megoldás | Havi Költség (500 user esetén) | Setup Komplexitás | AI Minőség | Adatvédelem | Ajánlás |
|----------|---------|----------|------------|-------------|---------|
| **OpenAI GPT-4o mini** | ~€15-30 | ⭐⭐ Közepes | ⭐⭐⭐⭐⭐ Kiváló | ⚠️ USA szerver | ✅ **TOP választás kezdéshez** |
| **Anthropic Claude Sonnet** | ~€25-50 | ⭐⭐ Közepes | ⭐⭐⭐⭐⭐ Kiváló | ⚠️ USA szerver | ✅ Ajánlott |
| **Google Gemini 1.5 Flash** | ~€5-15 | ⭐⭐ Közepes | ⭐⭐⭐⭐ Nagyon jó | ⚠️ USA/EU szerver | ✅ Költséghatékony |
| **Azure OpenAI** | ~€20-40 | ⭐⭐⭐ Nehéz | ⭐⭐⭐⭐⭐ Kiváló | ✅ EU választható | Vállalati környezet |
| **Groq (LLaMA)** | **€0** (ingyenes tier) | ⭐⭐ Közepes | ⭐⭐⭐⭐ Jó | ⚠️ USA szerver | ✅ **Proof of concept** |
| **OpenRouter Mix** | ~€10-25 | ⭐⭐ Közepes | ⭐⭐⭐⭐ Változó | ⚠️ USA szerver | Rugalmas |
| **Self-hosted Llama** | €50-150 (szerver) | ⭐⭐⭐⭐⭐ Nagyon nehéz | ⭐⭐⭐ Közepes | ✅ Teljes kontroll | Csak nagy skálánál |

---

## 🚀 1. OpenAI GPT-4o mini (AJÁNLOTT KEZDÉSHEZ)

### ✅ Előnyök
- ⚡ **Gyors integráció**: Java SDK elérhető
- 💰 **Ár-érték arány**: GPT-4o mini nagyon költséghatékony
- 🌟 **Kiváló minőség**: Természetes, kontextuális válaszok
- 📚 **Dokumentáció**: Széles körű, sok példa
- 🔧 **Function calling**: Structured output support

### ❌ Hátrányok
- ⚠️ USA szerverek (adatvédelmi kérdések)
- 💳 Fizetős API key szükséges (ingyenes tier korlátozott)
- 🌐 Internet kapcsolat kötelező

### 💰 Költségek (2026-os árak)

**GPT-4o mini (AJÁNLOTT KEZDÉSHEZ):**
- Input: $0.15 / 1M token (~€0.14)
- Output: $0.60 / 1M token (~€0.56)

**Használati példa:**
```
1 teljesítményértékelés = ~1500 input token + ~500 output token
- Input költség: 1500 * €0.14 / 1M = €0.00021
- Output költség: 500 * €0.56 / 1M = €0.00028
- Teljes: €0.00049 (~€0.0005 / értékelés)

Ha egy user naponta 5 task-ot értékeltet:
- Napi: 5 * €0.0005 = €0.0025
- Havi (30 nap): €0.075 / user
- 500 user: €37.5 / hó

Reális használat (átlag 2 értékelés/nap):
- 500 user * 2 * 30 = 30,000 értékelés/hó
- Költség: ~€15-20 / hó
```

**GPT-4o (ha magasabb minőség kell):**
- Input: $2.50 / 1M token
- Output: $10.00 / 1M token
- ~15-20x drágább, de még jobb minőség

### 🔧 Implementáció

**1. Dependency hozzáadása (backend/pom.xml):**
```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
<!-- VAGY hivatalos OpenAI Java SDK (újabb) -->
<dependency>
    <groupId>com.openai</groupId>
    <artifactId>openai-java</artifactId>
    <version>0.3.0</version>
</dependency>
```

**2. Configuration (application.properties):**
```properties
# OpenAI API Configuration
openai.api.key=${OPENAI_API_KEY}
openai.model=gpt-4o-mini
openai.max.tokens=500
openai.temperature=0.7
```

**3. Service példa (OpenAIChatbotService.java):**
```java
@Service
@RequiredArgsConstructor
public class OpenAIChatbotService {

    @Value("${openai.api.key}")
    private String apiKey;

    public String analyzeTaskPerformance(Task task) {
        OpenAiService service = new OpenAiService(apiKey);
        
        String prompt = buildPrompt(task);
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(List.of(
                    new ChatMessage("system", "Te egy task management asszisztens vagy..."),
                    new ChatMessage("user", prompt)
                ))
                .maxTokens(500)
                .temperature(0.7)
                .build();
        
        return service.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
    }
    
    private String buildPrompt(Task task) {
        // Task adatok összegyűjtése és prompt készítése
        return String.format("""
            Értékeld a következő feladat teljesítményét:
            
            Feladat: %s
            Kategória: %s
            Részfeladatok száma: %d
            Tervezett idő: %d perc
            Tényleges idő: %d perc
            Tervezett pontok: %d
            Elért pontok: %d
            
            Add egy rövid, személyes értékelést magyarul!
            """, 
            task.getName(),
            task.getCategory() != null ? task.getCategory().getName() : "Nincs",
            task.getSubtasks().size(),
            task.getPlannedTotalTimeMinutes(),
            task.getTotalActualTimeSeconds() / 60,
            task.getTotalPlannedPoints(),
            task.getTotalActualPoints()
        );
    }
}
```

### 📊 Várható válasz példa:
```
"Szuper munka! 🎉 A feladatot 15%-kal gyorsabban teljesítetted, mint tervezted, 
és pont a célzott pontszámot érted el. Ez azt mutatja, hogy jól becsülted meg a 
részfeladatok nehézségét. A következő alkalommal próbálj még ambiciózusabb pontszámot 
kitűzni, mivel láthatóan van kapacitásod rá! 💪"
```

---

## 🎭 2. Anthropic Claude 3.5 Sonnet/Haiku

### ✅ Előnyök
- 🧠 **Kiváló reasoning**: Komplex elemzésekhez jobb mint GPT
- 📝 **Hosszú context**: 200K token context window
- 🛡️ **Biztonság**: Kevesebb hallucináció, etikusabb
- 🇪🇺 **GDPR-friendly**: Jobb adatvédelmi policy

### ❌ Hátrányok
- 💰 Drágább mint GPT-4o mini
- 📚 Kevesebb community support
- 🔧 Java SDK nem hivatalos (HTTP client szükséges)

### 💰 Költségek

**Claude 3.5 Sonnet:**
- Input: $3.00 / 1M token (~€2.80)
- Output: $15.00 / 1M token (~€14.00)

**Claude 3.5 Haiku (GAZDASÁGOS):**
- Input: $0.25 / 1M token (~€0.23)
- Output: $1.25 / 1M token (~€1.17)

**Havi költség becsülés (Haiku, 500 user):**
```
30,000 értékelés/hó * €0.0008 = ~€24-30 / hó
```

### 🔧 Implementáció

**REST API hívás (Spring RestTemplate/WebClient):**
```java
@Service
public class ClaudeChatbotService {
    
    @Value("${anthropic.api.key}")
    private String apiKey;
    
    private final WebClient webClient;
    
    public ClaudeChatbotService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }
    
    public String analyzeTaskPerformance(Task task) {
        String prompt = buildPrompt(task);
        
        Map<String, Object> request = Map.of(
            "model", "claude-3-5-haiku-20241022",
            "max_tokens", 500,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );
        
        return webClient.post()
                .uri("/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ((Map) ((List) response.get("content")).get(0)).get("text").toString())
                .block();
    }
}
```

---

## 🔥 3. Google Gemini 1.5 Flash (LEGOLCSÓBB)

### ✅ Előnyök
- 💰 **Nagyon olcsó**: Flash modell költséghatékony
- ⚡ **Gyors**: Alacsony latency
- 🌐 **Ingyenes tier**: Generous free quota
- 🔧 **Java SDK**: Hivatalos Vertex AI SDK

### ❌ Hátrányok
- 🤖 Minőség kissé alacsonyabb mint GPT-4o/Claude
- 📚 Dokumentáció kevésbé részletes
- 🔐 Google Cloud setup bonyolultabb

### 💰 Költségek

**Gemini 1.5 Flash:**
- Input: $0.075 / 1M token (~€0.07)
- Output: $0.30 / 1M token (~€0.28)

**INGYENES TIER:**
- 15 requests / minute
- 1500 requests / day
- Ha kis traffic, lehet INGYENES!

**Havi költség becsülés (500 user):**
```
30,000 értékelés/hó * €0.0002 = ~€6-10 / hó

HA ingyenes tier-ben maradunk (max 1500/nap):
- 45,000 request / hó → 100% INGYENES
```

### 🔧 Implementáció

**Dependency:**
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-vertexai</artifactId>
    <version>1.3.0</version>
</dependency>
```

**Service:**
```java
@Service
public class GeminiChatbotService {
    
    public String analyzeTaskPerformance(Task task) throws IOException {
        try (VertexAI vertexAI = new VertexAI("your-project-id", "us-central1")) {
            
            GenerativeModel model = new GenerativeModel("gemini-1.5-flash", vertexAI);
            
            String prompt = buildPrompt(task);
            
            GenerateContentResponse response = model.generateContent(prompt);
            
            return response.getCandidates(0).getContent().getParts(0).getText();
        }
    }
}
```

---

## ⚡ 4. Groq (LLaMA 3.1, INGYENES!)

### ✅ Előnyök
- 💰 **INGYENES TIER**: 14,400 requests / day (óriási!)
- ⚡⚡⚡ **NAGYON GYORS**: Fastest inference (GroqChip hardware)
- 🎯 **Jó minőség**: LLaMA 3.1 70B model
- 🔧 **OpenAI-compatible API**: Könnyű integráció

### ❌ Hátrányok
- 📉 Rate limiting szigorú (30 req/min ingyenes tier-ben)
- 🌐 USA szerverek
- 💼 Vállalati garancia nincs (startup)

### 💰 Költségek

**INGYENES TIER:**
- 14,400 requests / day
- 30 requests / minute
- Új userek kapnak $25 credit-et

**Fizetős tier (ha kinövöd az ingyenest):**
- Input: $0.05-0.59 / 1M token (modeltől függ)
- Output: $0.79-0.99 / 1M token

**Havi költség:**
```
Ha 500 user és napi 2 értékelés:
- 1,000 request/day (bőven az ingyenes tier-ben)
- Költség: €0 / hó 🎉

Ha kinövöd:
- 30,000 request/hó * €0.0003 = ~€9 / hó
```

### 🔧 Implementáció

**OpenAI-compatible API (ugyanaz a kód, csak más base URL!):**
```java
@Service
public class GroqChatbotService {
    
    @Value("${groq.api.key}")
    private String apiKey;
    
    public String analyzeTaskPerformance(Task task) {
        // OpenAI SDK-t használunk, csak a base URL más
        OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
        service.setBaseUrl("https://api.groq.com/openai/v1/");
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("llama-3.1-70b-versatile")  // Vagy "mixtral-8x7b-32768"
                .messages(List.of(
                    new ChatMessage("system", "Te egy task management asszisztens vagy..."),
                    new ChatMessage("user", buildPrompt(task))
                ))
                .maxTokens(500)
                .temperature(0.7)
                .build();
        
        return service.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
    }
}
```

**GROQ API KEY beszerzése:**
1. Regisztráció: https://console.groq.com/
2. API Keys menü → Create API Key
3. INGYENES! Nincs bankkártya kérés!

---

## 🎯 5. OpenRouter (Multi-Model Aggregator)

### ✅ Előnyök
- 🔄 **Rugalmasság**: Több model közül választhatsz (GPT, Claude, LLaMA, stb.)
- 💰 **Költséghatékony**: Automatikus cheapest model selection
- 🔧 **Egyszerű**: Egy API key, több model
- 📊 **Dashboard**: Költségkövető

### ❌ Hátrányok
- 🌐 Közvetítő layer (latency +50-100ms)
- 📚 Kevesebb direct control
- 💳 Előre töltött kredit szükséges

### 💰 Költségek

**Model választék (példák):**
- Google Gemini Flash: $0.05 / 1M token
- Meta LLaMA 3.1 8B: $0.06 / 1M token
- OpenAI GPT-4o mini: $0.15 / 1M token
- Anthropic Claude Sonnet: $3.00 / 1M token

**Havi költség (Auto-cheapest, 500 user):**
```
Automatikusan a legolcsóbb model-t választja
→ €8-15 / hó
```

### 🔧 Implementáció

```java
@Service
public class OpenRouterChatbotService {
    
    @Value("${openrouter.api.key}")
    private String apiKey;
    
    private final WebClient webClient;
    
    public OpenRouterChatbotService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
    
    public String analyzeTaskPerformance(Task task) {
        Map<String, Object> request = Map.of(
            "model", "google/gemini-flash-1.5",  // Vagy "auto" a legolcsóbbért
            "messages", List.of(
                Map.of("role", "user", "content", buildPrompt(task))
            )
        );
        
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> /* parse response */)
                .block();
    }
}
```

---

## 🏢 6. Azure OpenAI (Vállalati Környezet)

### ✅ Előnyök
- 🇪🇺 **EU datacenter választható**: Adatvédelmi compliance
- 🛡️ **Enterprise SLA**: 99.9% uptime garancia
- 🔐 **Microsoft security**: Enterprise-grade
- 🔗 **Azure integráció**: Ha már használod Azure-t

### ❌ Hátrányok
- ⭐⭐⭐ **Bonyolult setup**: Azure subscription, tenant, approval
- 💰 **Drágább**: ~20-30% költség prémium
- ⏳ **Várakozási idő**: Approval kell (1-3 nap)
- 📚 Bonyolultabb SDK

### 💰 Költségek

**GPT-4o mini (Azure):**
- Input: $0.165 / 1M token (~10% drágább)
- Output: $0.66 / 1M token

**Havi költség (500 user):**
```
~€20-35 / hó (10-20% drágább mint direct OpenAI)
```

**Mikor éri meg?**
- 🏢 Ha már van Azure előfizetésed
- 🇪🇺 Ha EU adattárolás kötelező
- 💼 Ha vállalati SLA kell
- 📊 Ha Azure Cost Management-et használsz

---

## 🖥️ 7. Self-Hosted Open-Source Models (Haladó)

### Modellek:
- **Meta LLaMA 3.1 70B** (legjobb minőség)
- **Mixtral 8x7B** (kiváló reasoning)
- **Qwen 2.5 72B** (multilingual, magyar is!)

### ✅ Előnyök
- 🔐 **Teljes adatvédelem**: Adatok NEM hagyják el a szervert
- 💰 **Fix költség**: Csak szerver ár, API call nincs
- 🎛️ **Teljes kontroll**: Model tuning, customization
- ♾️ **Unlimited**: Nincs rate limiting

### ❌ Hátrányok
- ⭐⭐⭐⭐⭐ **NAGYON NEHÉZ**: DevOps tudás kell
- 💰 **Magas fix költség**: Szerver 24/7 fut
- 🔧 **Karbantartás**: Updates, monitoring, scaling
- 📉 **Minőség**: Alacsonyabb mint GPT-4o/Claude

### 💰 Költségek

**GPU szerver opciók:**

1. **AWS EC2 g5.xlarge** (1x NVIDIA A10G, 24GB VRAM)
   - €1.20 / óra = **~€864 / hó** 😱
   - Futtatható: LLaMA 3.1 8B (4-bit quantized)

2. **RunPod / Vast.ai** (bérlés)
   - RTX 4090 (24GB): $0.35-0.50 / óra = **~€250-360 / hó**
   - Futtatható: LLaMA 3.1 70B (4-bit quantized)

3. **Modal.com / Replicate** (serverless GPU)
   - Pay-per-second billing
   - LLaMA 70B: $0.001 / second = ~$3-5 / 1000 inferece
   - Ha 30,000 inference/hó: **~€100-150 / hó**

**Mikor éri meg?**
- Ha **10,000+ user** vagy
- Ha **100,000+ értékelés / hó**
- Ha **kritikus adatvédelem** kell

**Egy kis projektnél: NEM ÉRI MEG! 🚫**

---

## 📊 Összesítő Költségtáblázat (500 aktív user)

| Megoldás | Havi €  | Első hónap setup | Scaling komplexitás | Adatvédelem |
|----------|---------|------------------|---------------------|-------------|
| **Groq (LLaMA)** | **€0-9** | €0 | Egyszerű | ⚠️ USA |
| **Gemini Flash** | **€6-10** | €0 | Egyszerű | ⚠️ USA/EU |
| **OpenAI GPT-4o mini** | **€15-30** | €0 | Egyszerű | ⚠️ USA |
| **OpenRouter** | **€10-25** | €10 (kezdő kredit) | Egyszerű | ⚠️ USA |
| **Claude Haiku** | **€24-35** | €0 | Egyszerű | ⚠️ USA |
| **Azure OpenAI** | **€20-40** | €0-200 (setup) | Közepes | ✅ EU választható |
| **Self-hosted** | **€250-850** | €1000+ (DevOps) | NEHÉZ | ✅ Teljes kontroll |

---

## 🎯 AJÁNLÁS - Döntési Fa

### **PROOF OF CONCEPT / MVP (3-6 hónap):**
→ **Groq (INGYENES)**
- Költség: €0 / hó
- Gyors prototype
- Ha működik, később váltasz

### **PRODUCTION LAUNCH (kis-közepes skála, <1000 user):**
→ **OpenAI GPT-4o mini**
- Költség: €15-30 / hó
- Best ár-érték arány
- Kiváló minőség
- Széles support

**Alternatíva:** **Google Gemini Flash** (ha költségérzékeny vagy)

### **ENTERPRISE / GDPR-kritikus:**
→ **Azure OpenAI**
- EU datacenter
- SLA garancia
- Compliance

### **NAGYON MAGAS VOLUMEN (10,000+ user):**
→ **Self-hosted LLaMA**
- Fix költség
- Scaling
- Teljes kontroll

---

## 🛠️ Implementációs Terv (Javasolt)

### Fázis 1: Prototype (1-2 hét)
1. **Groq API integráció** (ingyenes, gyors)
2. **Backend endpoint**: `POST /api/chatbot/analyze/{taskId}`
3. **Simple prompt engineering**
4. **Frontend: Chat UI** (Angular material dialog)

### Fázis 2: Production (2-3 hét)
1. **OpenAI GPT-4o mini** integrálása
2. **Prompt optimization** (context, examples)
3. **Caching layer** (Redis - ismételt task lekérdezések)
4. **Rate limiting** (user quota: 10 értékelés/nap)
5. **Analytics** (használat tracking)

### Fázis 3: Advanced Features (1-2 hónap)
1. **Multi-turn conversation** (chat history)
2. **Contextual suggestions** (több task összehasonlítás)
3. **Personalization** (user preferenciák)
4. **Email summaries** (heti összefoglaló AI által)

---

## 💡 Gyakorlati Költségcsökkentési Tippek

### 1. **Prompt Optimization**
```java
// ❌ Rossz: hosszú, fölösleges context
String prompt = "Elemezd a következő 500 soros task adatot...";

// ✅ Jó: csak a lényeg
String prompt = String.format("""
    Task: %s | Idő: %d/%d perc | Pontok: %d/%d
    Adj rövid értékelést!
    """, name, actual, planned, actualPts, plannedPts);
// Token használat: 80% csökkenés!
```

### 2. **Caching**
```java
@Cacheable(value = "task-analysis", key = "#task.id")
public String analyzeTask(Task task) {
    // Ha ugyanaz a task újra elemzésre kerül, 
    // nem hívjuk az API-t, hanem cache-ből adjuk
}
```

### 3. **Batch Processing**
```java
// Ha user egyszerre 5 task-ot kér elemezni,
// küldd 1 API hívásban (multi-task prompt)
// Költség: 50% csökkenés
```

### 4. **Rate Limiting**
```java
// User quota: max 10 értékelés / nap
// Megakadályozza a túlzott használatot
@RateLimiter(name = "chatbot", fallbackMethod = "rateLimitFallback")
public String analyzeTask(Task task) { ... }
```

### 5. **Tier-based Access**
```
Free user: 3 értékelés / nap
Pro user: 20 értékelés / nap
Enterprise: unlimited
```

---

## 📈 Költség Skálázódás

```
10 user:     €0-2 / hó    (Groq ingyenes tier)
100 user:    €3-8 / hó    (Gemini Flash)
500 user:    €15-30 / hó  (OpenAI GPT-4o mini)
1,000 user:  €30-60 / hó  (OpenAI GPT-4o mini)
5,000 user:  €150-300 / hó (OpenAI + caching)
10,000 user: €250-500 / hó (Self-hosted vagy Azure deal)
```

---

## 🚀 Következő Lépések

### 1. Döntés meghozása
- [ ] Költségkeret meghatározása
- [ ] Adatvédelmi követelmények tisztázása
- [ ] Skála becslése (user count, havi értékelések)

### 2. Implementáció (ajánlott: **Groq → OpenAI migráció**)
```bash
# Hetente 1-2 óra munka
Week 1: Groq integráció + basic prompt
Week 2: Frontend chat UI
Week 3: Testing & refinement
Week 4: OpenAI váltás + production launch
```

### 3. Költségkövető
```java
// Log minden API call költségét
log.info("API call cost: ${} - user: {} - task: {}", 
    cost, userId, taskId);
```

---

## 📞 Segítségre van szükséged?

Ha kérdésed van vagy segítségre van szükséged a választásban:
- 📄 [Export Feature dokumentáció](./EXPORT_FEATURE.md)
- 📄 [Deployment workflow](./DEPLOYMENT_WORKFLOW.md)

---

## ✅ ÖSSZEGZÉS - Gyors Választás

**Ha most kezded:** **Groq (ingyenes)** → Prototype kész 1 hét alatt, €0 költség

**Ha komolyan gondolod:** **OpenAI GPT-4o mini** → Best ár-érték, €15-30/hó

**Ha nagyon spórolsz:** **Google Gemini Flash** → €6-10/hó, jó minőség

**Ha GDPR kritikus:** **Azure OpenAI** → EU datacenter, compliance

---

**Következő lépés:** Válassz egy megoldást, és kezdjük el az implementációt! 🚀
