# 🏷️ Subtask Tags - Dual-Level Tag System

## 📋 Áttekintés

A **Subtask Tags** egy dual-level (kétszintű) tag rendszer, amely lehetővé teszi:
- ✅ **Admin-ok**: Globális tag-eket hozhatnak létre, amiket mindenki láthat és használhat
- ✅ **User-ek**: Saját tag-eket hozhatnak létre, amiket csak ők láthatnak
- ✅ **AI értékelés javítása**: Hasonló témakörű subtask-ok összehasonlíthatósága

---

## 🎯 Funkciók

### **1. Globális Tag-ek (Admin)**
- Mindenki látja és használhatja
- Csak admin hozhat létre, módosíthat, törölhet
- Előre létrehozott tag-ek:
  - Frontend (#3b82f6)
  - Backend (#10b981)
  - Database (#f59e0b)
  - API (#8b5cf6)
  - Testing (#ef4444)
  - Design (#ec4899)
  - DevOps (#6366f1)
  - Deployment (#14b8a6)
  - Documentation (#f97316)
  - Bugfix (#dc2626)
  - Refactoring (#7c3aed)
  - Research (#06b6d4)

### **2. User-specifikus Tag-ek**
- Csak a létrehozó user látja
- Bármilyen témakör hozzáadható
- Csak a tulajdonos módosíthatja/törölheti

### **3. Authorization Szabályok**

| Művelet | Global Tag | User-specific Tag |
|---------|------------|-------------------|
| **Létrehozás** | Csak ADMIN | Bárki |
| **Megtekintés** | Mindenki | Csak tulajdonos |
| **Módosítás** | Csak ADMIN | Csak tulajdonos |
| **Törlés** | Csak ADMIN | Csak tulajdonos |
| **Használat** | Mindenki | Csak tulajdonos |

---

## 🔧 Backend Implementáció

### **1. Database Schema (V7__Subtask_Tags.sql)**

```sql
-- Subtask tags table
CREATE TABLE subtask_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#667eea',
    is_global BOOLEAN DEFAULT FALSE,
    user_id BIGINT,  -- NULL for global tags
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Many-to-many mapping
CREATE TABLE subtask_tag_mapping (
    subtask_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (subtask_id, tag_id)
);
```

### **2. Entity-k**

- `SubtaskTag` - Tag entitás (global + user-specific support)
- `Subtask` - Módosítva, many-to-many kapcsolat tag-ekkel
- `User` - Role mező (USER, ADMIN)

### **3. REST API Endpoints**

| HTTP Method | Endpoint | Leírás | Auth |
|-------------|----------|--------|------|
| `GET` | `/api/subtask-tags` | Get all visible tags | User |
| `GET` | `/api/subtask-tags/global` | Get global tags | Admin |
| `GET` | `/api/subtask-tags/{id}` | Get tag by ID | User |
| `POST` | `/api/subtask-tags` | Create tag | User |
| `PUT` | `/api/subtask-tags/{id}` | Update tag | Owner/Admin |
| `DELETE` | `/api/subtask-tags/{id}` | Delete tag | Owner/Admin |
| `POST` | `/api/subtask-tags/batch` | Get tags by IDs | User |

### **4. API Request Examples**

#### **Create User-Specific Tag**
```http
POST /api/subtask-tags
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "name": "Machine Learning",
  "color": "#ff6b6b",
  "isGlobal": false
}
```

#### **Create Global Tag (Admin only)**
```http
POST /api/subtask-tags
Content-Type: application/json
Authorization: Bearer {{admin_token}}

{
  "name": "Security",
  "color": "#dc2626",
  "isGlobal": true
}
```

#### **Update Tag**
```http
PUT /api/subtask-tags/5
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "name": "Updated Name",
  "color": "#00ff00"
}
```

#### **Delete Tag**
```http
DELETE /api/subtask-tags/5
Authorization: Bearer {{token}}
```

---

## 🎨 Frontend Implementáció (Következő lépés)

### **1. Angular Services**

```typescript
// subtask-tag.service.ts
getAllTags(): Observable<SubtaskTag[]>
getGlobalTags(): Observable<SubtaskTag[]>
createTag(request: CreateTagRequest): Observable<SubtaskTag>
updateTag(id: number, request: CreateTagRequest): Observable<SubtaskTag>
deleteTag(id: number): Observable<void>
```

### **2. Komponensek (létrehozandó)**

- `subtask-tag-selector.component` - Tag választó subtask form-ban
- `admin-tags.component` - Admin tag management UI
- `tag-chip.component` - Tag megjelenítés (színes chip)

### **3. Tag Selector UI Flow**

```
Subtask Form
├─ Selected Tags (színes chipek)
├─ Autocomplete Search Input
│  └─ Filtered Tags (global + user's own)
├─ "+ Create New Tag" gomb
└─ Tag Management (admin: global, user: saját)
```

---

## 🧪 Tesztelés

### **Backend Tesztelés (IntelliJ)**

1. **Indítsd el a backend-et**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Futtasd az API teszteket** (`backend/api-tests.http`):
   - Test #13-20: Subtask Tags API tesztek
   - Ellenőrizd a response-okat

3. **Ellenőrizd a log-okat**:
   ```
   SubtaskTagService: Creating tag: name=Frontend, isGlobal=true, userId=1
   Created GLOBAL tag: id=1, name=Frontend, userId=1
   ```

### **Database Ellenőrzés (MySQL)**

```sql
-- Check global tags
SELECT * FROM subtask_tags WHERE is_global = TRUE;

-- Check user-specific tags
SELECT * FROM subtask_tags WHERE is_global = FALSE;

-- Check tag-subtask mapping
SELECT * FROM subtask_tag_mapping;
```

---

## 💡 Használati Példák

### **Példa 1: Admin Létrehoz Global Tag-et**

```http
POST /api/subtask-tags
Authorization: Bearer {{admin_token}}

{
  "name": "Machine Learning",
  "color": "#ff6b6b",
  "isGlobal": true
}

Response:
{
  "id": 13,
  "name": "Machine Learning",
  "color": "#ff6b6b",
  "isGlobal": true,
  "userId": null,
  "createdById": 1,
  "createdAt": "2026-06-08T10:30:00"
}
```

### **Példa 2: User Létrehoz Saját Tag-et**

```http
POST /api/subtask-tags
Authorization: Bearer {{user_token}}

{
  "name": "Performance Tuning",
  "color": "#9333ea",
  "isGlobal": false
}

Response:
{
  "id": 14,
  "name": "Performance Tuning",
  "color": "#9333ea",
  "isGlobal": false,
  "userId": 2,
  "createdById": 2,
  "createdAt": "2026-06-08T10:35:00"
}
```

### **Példa 3: User Lekéri Látható Tag-eket**

```http
GET /api/subtask-tags
Authorization: Bearer {{user_token}}

Response:
[
  {
    "id": 1,
    "name": "Frontend",
    "color": "#3b82f6",
    "isGlobal": true,
    "userId": null,
    ...
  },
  {
    "id": 2,
    "name": "Backend",
    "color": "#10b981",
    "isGlobal": true,
    "userId": null,
    ...
  },
  {
    "id": 14,
    "name": "Performance Tuning",
    "color": "#9333ea",
    "isGlobal": false,
    "userId": 2,  // ← User's own tag
    ...
  }
]
```

---

## 🚀 Következő Lépések

### **Azonnal (Backend kész)**
- ✅ Database migration
- ✅ Entity-k (SubtaskTag, Subtask)
- ✅ Repository
- ✅ Service (CRUD + authorization)
- ✅ Controller (REST API)
- ✅ DTO-k

### **Következő (Frontend implementáció)**
1. [ ] Angular Service: `SubtaskTagService`
2. [ ] Component: `SubtaskTagSelectorComponent`
3. [ ] Component: `AdminTagsComponent` (admin UI)
4. [ ] Integration: Subtask form-ba tag selector
5. [ ] UI: Tag chipek megjelenítése

### **Később (AI enhancement)**
1. [ ] ChatbotService: Tag context hozzáadása prompt-hoz
2. [ ] RAG: Tag-ek hozzáadása embedding-hez
3. [ ] Analytics: Tag-alapú teljesítmény összehasonlítás

---

## 📊 AI Értékelés Javulása

### **Előtte (tag-ek nélkül)**
```
AI: "Összességében jól haladtál. Az átlagos subtask idő 65 perc volt."
```

### **Utána (tag-ekkel)**
```
AI: "📊 Részletes témakör szerinti értékelés:

🗄️ Database (45 perc):
- Korábbi átlagod: 50 perc → 10% gyorsabb! ✅

⚙️ Backend API (60 perc):
- Korábbi átlagod: 70 perc → 14% gyorsabb! 🚀

🎨 Frontend UI (90 perc):
- Korábbi átlagod: 120 perc → 25% gyorsabb! 🎉"
```

---

## 🆘 Troubleshooting

### **Hiba: "Only admins can create global tags"**
- **Ok**: User próbál global tag-et létrehozni
- **Megoldás**: Set `isGlobal: false` vagy kérj admin jogot

### **Hiba: "You already have a tag with this name"**
- **Ok**: User-specifikus tag neve ütközik
- **Megoldás**: Használj másik nevet

### **Hiba: "Global tag with this name already exists"**
- **Ok**: Admin próbál duplicate global tag-et létrehozni
- **Megoldás**: Használj másik nevet vagy frissítsd a meglévőt

---

## ✅ Összefoglalás

**Mit építettünk?**
- ✅ Dual-level tag rendszer (global + user-specific)
- ✅ Role-based authorization (ADMIN, USER)
- ✅ Full CRUD API (create, read, update, delete)
- ✅ Database schema (tags + mapping táblák)
- ✅ 12 előre létrehozott global tag

**Előnyök:**
- ✅ Kontrollált globális tag készlet (admin moderálja)
- ✅ Rugalmasság (user létrehozhat sajátot)
- ✅ Konzisztencia (globális tag-ek egységesek)
- ✅ Jobb AI context (tag-alapú összehasonlítás)
- ✅ Skálázhatóság (bármilyen témakör)

**Következő lépés:** Frontend implementáció (Angular komponensek)

---

**Hajrá, kezdd el a frontend-et! 🚀**
