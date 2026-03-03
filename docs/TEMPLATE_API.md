# Template API Documentation

**Project:** Task Analysis  
**Version:** 1.3.0  
**Date:** 2026-03-04  
**Feature:** Template System with Bulk Task Creation

---

## Overview

A Template System lehetővé teszi újrafelhasználható feladat sablonok létrehozását, amelyek kategóriához kötöttek és előre definiált részfeladatokat tartalmaznak. Ez egységes feladatstruktúrát biztosít a kategóriákon belül, így lehetővé válik a teljesítmény-trend elemzés.

**Főbb jellemzők:**
- ✅ Kategóriához kötött sablonok (kötelező)
- ✅ Újrafelhasználható feladatstruktúra
- ✅ Előre definiált részfeladatok plánolt pontértékekkel
- ✅ Egyedi sablon nevek kategóriánként
- ✅ **Bulk task creation**: Több azonos task létrehozása egy kattintással
- ✅ **Auto-numbering**: Tasks automatically named with #1, #2, #3 pattern

---

## API Endpoints

### Base URL
```
/api/templates
```

**Authentication:** Bearer Token (JWT) szükséges minden endpoint-hoz

---

## 1. Get All Templates

Lekéri az aktuális felhasználó összes sablon definícióját.

### Request

```http
GET /api/templates
Authorization: Bearer <token>
```

### Response

**Success (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Python REST API Endpoint",
    "description": "Create a new REST API endpoint with tests and documentation",
    "categoryId": 5,
    "categoryName": "Python Development",
    "subtaskCount": 4,
    "taskCount": 1,
    "templateSubtasks": [
      {
        "subtaskNumber": 1,
        "plannedPoints": 5
      },
      {
        "subtaskNumber": 2,
        "plannedPoints": 8
      },
      {
        "subtaskNumber": 3,
        "plannedPoints": 3
      },
      {
        "subtaskNumber": 4,
        "plannedPoints": 2
      }
    ],
    "createdAt": "2026-03-03T10:15:30",
    "updatedAt": "2026-03-03T10:15:30"
  },
  {
    "id": 2,
    "name": "Database Migration",
    "description": "Create and test database migration with rollback script",
    "categoryId": 5,
    "categoryName": "Python Development",
    "subtaskCount": 3,
    "taskCount": 5,
    "templateSubtasks": [
      {
        "subtaskNumber": 1,
        "plannedPoints": 5
      },
      {
        "subtaskNumber": 2,
        "plannedPoints": 8
      },
      {
        "subtaskNumber": 3,
        "plannedPoints": 3
      }
    ],
    "createdAt": "2026-03-03T11:20:45",
    "updatedAt": "2026-03-03T11:20:45"
  }
]
```

**Empty Response (200 OK):**
```json
[]
```

### cURL Example

```bash
curl -X GET http://localhost:8080/api/templates \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 2. Get Template by ID

Lekér egy konkrét sablont azonosító alapján.

### Request

```http
GET /api/templates/{id}
Authorization: Bearer <token>
```

**Path Parameters:**
- `id` (Long, required) - A sablon azonosítója

### Response

**Success (200 OK):**
```json
{
  "id": 1,
  "name": "Python REST API Endpoint",
  "description": "Create a new REST API endpoint with tests and documentation",
  "categoryId": 5,
  "categoryName": "Python Development",
  "subtaskCount": 4,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5
    },
    {
      "subtaskNumber": 2,
      "plannedPoints": 8
    },
    {
      "subtaskNumber": 3,
      "plannedPoints": 3
    },
    {
      "subtaskNumber": 4,
      "plannedPoints": 2
    }
  ],
  "createdAt": "2026-03-03T10:15:30",
  "updatedAt": "2026-03-03T10:15:30"
}
```

**Not Found (404):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Template not found with id: 999",
  "path": "/api/templates/999"
}
```

### cURL Example

```bash
curl -X GET http://localhost:8080/api/templates/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 3. Create Template

Új sablon létrehozása. **Kategória kötelező** - minden sablon pontosan egy kategóriához tartozik.

### Request

```http
POST /api/templates
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Python REST API Endpoint",
  "description": "Create a new REST API endpoint with tests and documentation",
  "categoryId": 5,
  "subtaskCount": 4,
  "taskCount": 1,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5
    },
    {
      "subtaskNumber": 2,
      "plannedPoints": 8
    },
    {
      "subtaskNumber": 3,
      "plannedPoints": 3
    },
    {
      "subtaskNumber": 4,
      "plannedPoints": 2
    }
  ]
}
```

**Validation Rules:**
- `name`: Kötelező, max 255 karakter
- `description`: Opcionális, max 5000 karakter
- `categoryId`: **Kötelező** (analytics miatt)
- `subtaskCount`: Kötelező, 1-100 között
- `taskCount`: **Kötelező**, 1-100 között (how many tasks to create)
- `templateSubtasks`: Opcionális lista

### Response

**Success (201 Created):**
```json
{
  "id": 1,
  "name": "Python REST API Endpoint",
  "description": "Create a new REST API endpoint with tests and documentation",
  "categoryId": 5,
  "categoryName": "Python Development",
  "subtaskCount": 4,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5
    },
    {
      "subtaskNumber": 2,
      "plannedPoints": 8
    },
    {
      "subtaskNumber": 3,
      "plannedPoints": 3
    },
    {
      "subtaskNumber": 4,
      "plannedPoints": 2
    }
  ],
  "createdAt": "2026-03-03T10:15:30",
  "updatedAt": "2026-03-03T10:15:30"
}
```

**Validation Error (400 Bad Request):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "categoryId",
      "message": "Category is required for analytics"
    },
    {
      "field": "name",
      "message": "Name is required"
    }
  ],
  "path": "/api/templates"
}
```

**Business Logic Error (400 Bad Request):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "A template with name 'Python REST API Endpoint' already exists in this category",
  "path": "/api/templates"
}
```

**Category Not Found (404):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found with id: 999",
  "path": "/api/templates"
}
```

**Category Not Owned (400):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Category does not belong to the current user",
  "path": "/api/templates"
}
```

### cURL Example

```bash
curl -X POST http://localhost:8080/api/templates \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Python REST API Endpoint",
    "description": "Create a new REST API endpoint with tests and documentation",
    "categoryId": 5,
    "subtaskCount": 4,
    "templateSubtasks": [
      {"subtaskNumber": 1, "plannedPoints": 5},
      {"subtaskNumber": 2, "plannedPoints": 8},
      {"subtaskNumber": 3, "plannedPoints": 3},
      {"subtaskNumber": 4, "plannedPoints": 2}
    ]
  }'
```

---

## 4. Update Template

Meglévő sablon frissítése. A kategória módosítható, de a név egyediségét az **új kategórián belül** ellenőrzi.

### Request

```http
PUT /api/templates/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
- `id` (Long, required) - A sablon azonosítója

**Body:**
```json
{
  "name": "Python REST API Endpoint (Updated)",
  "description": "Updated description",
  "categoryId": 5,
  "subtaskCount": 3,
  "taskCount": 2,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5
    },
    {
      "subtaskNumber": 2,
      "plannedPoints": 8
    },
    {
      "subtaskNumber": 3,
      "plannedPoints": 5
    }
  ]
}
```

### Response

**Success (200 OK):**
```json
{
  "id": 1,
  "name": "Python REST API Endpoint (Updated)",
  "description": "Updated description",
  "categoryId": 5,
  "categoryName": "Python Development",
  "subtaskCount": 3,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5
    },
    {
      "subtaskNumber": 2,
      "plannedPoints": 8
    },
    {
      "subtaskNumber": 3,
      "plannedPoints": 5
    }
  ],
  "createdAt": "2026-03-03T10:15:30",
  "updatedAt": "2026-03-03T12:30:45"
}
```

**Not Found (404):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Template not found with id: 999",
  "path": "/api/templates/999"
}
```

**Name Conflict (400):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "A template with name 'Python REST API Endpoint' already exists in this category",
  "path": "/api/templates/1"
}
```

### cURL Example

```bash
curl -X PUT http://localhost:8080/api/templates/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Python REST API Endpoint (Updated)",
    "description": "Updated description",
    "categoryId": 5,
    "subtaskCount": 3,
    "templateSubtasks": [
      {"subtaskNumber": 1, "plannedPoints": 5},
      {"subtaskNumber": 2, "plannedPoints": 8},
      {"subtaskNumber": 3, "plannedPoints": 5}
    ]
  }'
```

---

## 5. Delete Template

Sablon törlése. A már a sablonból létrehozott feladatokat **nem** érinti.

### Request

```http
DELETE /api/templates/{id}
Authorization: Bearer <token>
```

**Path Parameters:**
- `id` (Long, required) - A sablon azonosítója

### Response

**Success (204 No Content):**
```
(No response body)
```

**Not Found (404):**
```json
{
  "timestamp": "2026-03-03T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Template not found with id: 999",
  "path": "/api/templates/999"
}
```

### cURL Example

```bash
curl -X DELETE http://localhost:8080/api/templates/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 6. Create Tasks from Template (Bulk Creation)

**Több azonos feladat** létrehozása sablonból egy kattintással. Minden task örökli a sablon kategóriáját és részfeladatait (plánolt pontértékekkel). A task-ok automatikusan sorszámozva vannak: `#1`, `#2`, `#3` stb.

### Request

```http
POST /api/templates/{id}/create-tasks
Authorization: Bearer <token>
```

**Path Parameters:**
- `id` (Long, required) - A sablon azonosítója

**Note:** A `taskCount` mező a template-ben határozza meg, hány task jön létre.

### Response

**Success (201 Created):**

Array of created tasks. If template has `taskCount: 3`, returns 3 tasks.

```json
[
  {
    "id": 42,
    "name": "Python REST API Endpoint #1",
    "description": "Create a new REST API endpoint with tests and documentation",
    "status": "OPEN",
    "categoryId": 5,
    "categoryName": "Python Development",
    "subtaskCount": 4,
    "createdFromTemplateId": 1,
    "subtasks": [
      {
        "id": 101,
        "taskId": 42,
        "subtaskNumber": 1,
        "actualPoints": null,
        "plannedPoints": 5,
        "completedAt": null
      },
      {
        "id": 102,
        "taskId": 42,
        "subtaskNumber": 2,
        "actualPoints": null,
        "plannedPoints": 8,
        "completedAt": null
      },
      {
        "id": 103,
        "taskId": 42,
        "subtaskNumber": 3,
        "actualPoints": null,
        "plannedPoints": 3,
        "completedAt": null
      },
      {
        "id": 104,
        "taskId": 42,
        "subtaskNumber": 4,
        "actualPoints": null,
        "plannedPoints": 2,
        "completedAt": null
      }
    ],
    "createdAt": "2026-03-04T00:15:30",
    "updatedAt": "2026-03-04T00:15:30"
  },
  {
    "id": 43,
    "name": "Python REST API Endpoint #2",
    "description": "Create a new REST API endpoint with tests and documentation",
    "status": "OPEN",
    "categoryId": 5,
    "categoryName": "Python Development",
    "subtaskCount": 4,
    "createdFromTemplateId": 1,
    "subtasks": [
      {
        "id": 105,
        "taskId": 43,
        "subtaskNumber": 1,
        "actualPoints": null,
        "plannedPoints": 5,
        "completedAt": null
      },
      {
        "id": 106,
        "taskId": 43,
        "subtaskNumber": 2,
        "actualPoints": null,
        "plannedPoints": 8,
        "completedAt": null
      },
      {
        "id": 107,
        "taskId": 43,
        "subtaskNumber": 3,
        "actualPoints": null,
        "plannedPoints": 3,
        "completedAt": null
      },
      {
        "id": 108,
        "taskId": 43,
        "subtaskNumber": 4,
        "actualPoints": null,
        "plannedPoints": 2,
        "completedAt": null
      }
    ],
    "createdAt": "2026-03-04T00:15:30",
    "updatedAt": "2026-03-04T00:15:30"
  },
  {
    "id": 44,
    "name": "Python REST API Endpoint #3",
    "description": "Create a new REST API endpoint with tests and documentation",
    "status": "OPEN",
    "categoryId": 5,
    "categoryName": "Python Development",
    "subtaskCount": 4,
    "createdFromTemplateId": 1,
    "subtasks": [
      {
        "id": 109,
        "taskId": 44,
        "subtaskNumber": 1,
        "actualPoints": null,
        "plannedPoints": 5,
        "completedAt": null
      },
      {
        "id": 110,
        "taskId": 44,
        "subtaskNumber": 2,
        "actualPoints": null,
        "plannedPoints": 8,
        "completedAt": null
      },
      {
        "id": 111,
        "taskId": 44,
        "subtaskNumber": 3,
        "actualPoints": null,
        "plannedPoints": 3,
        "completedAt": null
      },
      {
        "id": 112,
        "taskId": 44,
        "subtaskNumber": 4,
        "actualPoints": null,
        "plannedPoints": 2,
        "completedAt": null
      }
    ],
    "createdAt": "2026-03-04T00:15:30",
    "updatedAt": "2026-03-04T00:15:30"
  }
]
```

**Key Features:**
- ✅ **Auto-numbering**: Tasks named `Template Name #1`, `#2`, `#3`...
- ✅ **Bulk creation**: Creates `taskCount` tasks in one API call
- ✅ **Identical structure**: All tasks have same subtasks with planned points
- ✅ **Same category**: All created tasks inherit template's category

**Not Found (404):**
```json
{
  "timestamp": "2026-03-04T00:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Template not found with id: 999",
  "path": "/api/templates/999/create-tasks"
}
```

### cURL Example

```bash
curl -X POST http://localhost:8080/api/templates/1/create-tasks \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Business Rules

### 1. Mandatory Category

**Miért kötelező a kategória?**

A sablon **mindig kategóriához kötött**, mert ez teszi lehetővé a strukturált analytics funkciót:

- ✅ Egy kategórián belül minden feladat ugyanazt a szerkezetet követi
- ✅ Az 1. részfeladat mindig ugyanazt jelenti → trend analysis lehetséges
- ✅ Összehasonlítható teljesítmény-adatok (pl. "Python Development" kategória 1. subtaskjának átlagos ideje csökken-e?)

**Példa analytics use case:**
```
Kategória: "Python Development"
Sablon: "REST API Endpoint"

1. részfeladat: Planning & Design (plánolt: 5 pont)
2. részfeladat: Implementation (plánolt: 8 pont)
3. részfeladat: Testing (plánolt: 3 pont)
4. részfeladat: Documentation (plánolt: 2 pont)

→ Trend elemzés: Az idő múlásával a 2. részfeladat (Implementation) 
  átlagos ideje csökken? Javul az accuracy?
```

### 2. Unique Template Name per Category

Egy kategórián belül a sablon név egyedi kell legyen, de **különböző kategóriákban lehet ugyanaz**:

**Megengedett:**
```
✅ Category: "Python Development" → Template: "API Endpoint"
✅ Category: "Java Development"   → Template: "API Endpoint"
```

**Tiltott:**
```
❌ Category: "Python Development" → Template: "API Endpoint"
❌ Category: "Python Development" → Template: "API Endpoint"  (duplicate!)
```

### 3. Cascade Delete

Ha egy kategóriát törölsz, **az összes hozzá tartozó sablon is törlődik**:

```sql
-- V2__Task_Templates.sql
CONSTRAINT fk_template_category 
    FOREIGN KEY (category_id) REFERENCES categories(id) 
    ON DELETE CASCADE
```

**Fontos:** A már létrehozott feladatok **megmaradnak**, csak a sablon törlődik!

### 4. Template Subtasks ≠ Task Subtasks

- **TemplateSubtask**: Sablonban definiált részfeladat (tervezett pontértékkel)
- **Subtask**: Konkrét feladat részfeladata (tervezett + aktuális pontérték, időmérés)

Amikor sablonból feladatot hozol létre, a `TemplateSubtask`-ok átmásolódnak `Subtask`-okká.

### 5. Bulk Task Creation

A `taskCount` mező határozza meg, hogy **hány azonos feladat** jön létre egy template használatakor.

**Példa használat:**
```
Template: "Matematika gyakorlás"
taskCount: 5
subtaskCount: 6
→ Result: 5 task létrejön:
  - "Matematika gyakorlás #1" (6 subtask)
  - "Matematika gyakorlás #2" (6 subtask)
  - "Matematika gyakorlás #3" (6 subtask)
  - "Matematika gyakorlás #4" (6 subtask)
  - "Matematika gyakorlás #5" (6 subtask)
```

**Auto-numbering pattern:**
- Single task: `taskCount: 1` → `"Template Name #1"`
- Multiple tasks: `taskCount: N` → `"Template Name #1"`, `"#2"`, ..., `"#N"`

**Use cases:**
- ✅ Gyakorló feladatok sorozata (pl. 10 matekpélda ugyanazzal a struktúrával)
- ✅ Heti ismétlődő taskok (pl. 5 napi rutinfeladat)
- ✅ Sprint taskok (pl. 8 user story ugyanolyan részfeladatokkal)

---

## Database Schema

### task_templates table

```sql
CREATE TABLE task_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,  -- REQUIRED for analytics
    name VARCHAR(255) NOT NULL,
    description TEXT,
    subtask_count INT NOT NULL,
    task_count INT NOT NULL DEFAULT 1,  -- How many tasks to create (bulk creation)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_template_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_template_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_category_template 
        UNIQUE (user_id, category_id, name)
);
```

### template_subtasks table

```sql
CREATE TABLE template_subtasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    subtask_number INT NOT NULL,
    planned_points INT DEFAULT 0,
    
    CONSTRAINT fk_template_subtask_template FOREIGN KEY (template_id) 
        REFERENCES task_templates(id) ON DELETE CASCADE
);
```

---

## Testing

### Unit Tests

**File:** `backend/src/test/java/com/taskanalysis/service/TemplateServiceTest.java`

**Test Coverage:** 16 unit tests

```
✅ getAllTemplates (2 tests)
   - Should return all templates for user
   - Should return empty list when user has no templates

✅ getTemplateById (2 tests)
   - Should return template by id
   - Should throw ResourceNotFoundException when template not found

✅ createTemplate (5 tests)
   - Should create template successfully
   - Should throw ResourceNotFoundException when category not found
   - Should throw BusinessException when category does not belong to user
   - Should throw BusinessException when template name already exists in category
   - Should allow same template name in different category

✅ updateTemplate (4 tests)
   - Should update template successfully
   - Should throw ResourceNotFoundException when updating non-existing template
   - Should throw BusinessException when updating template with name conflict in category
   - Should throw BusinessException when updating with category not belonging to user

✅ deleteTemplate (2 tests)
   - Should delete template successfully
   - Should throw ResourceNotFoundException when deleting non-existing template
```

**Run tests:**
```bash
mvn test -Dtest=TemplateServiceTest
```

---

## Integration Example

### Frontend Workflow (Angular)

```typescript
// 1. Load user's templates
templateService.getAllTemplates().subscribe(templates => {
  this.templates = templates;
});

// 2. Create new template
const newTemplate: TemplateRequest = {
  name: 'Python REST API Endpoint',
  description: 'Create REST API with tests',
  categoryId: 5, // Python Development
  subtaskCount: 4,
  taskCount: 3, // Creates 3 tasks at once
  templateSubtasks: [
    { subtaskNumber: 1, plannedPoints: 5 },
    { subtaskNumber: 2, plannedPoints: 8 },
    { subtaskNumber: 3, plannedPoints: 3 },
    { subtaskNumber: 4, plannedPoints: 2 }
  ]
};

templateService.createTemplate(newTemplate).subscribe(
  response => console.log('Template created:', response),
  error => console.error('Error:', error)
);

// 3. Create tasks from template (bulk creation)
templateService.createTasksFromTemplate(templateId).subscribe(
  tasks => {
    console.log(`${tasks.length} tasks created from template`);
    // tasks = [{name: 'Template #1', ...}, {name: 'Template #2', ...}, ...]
    // All tasks have same structure with auto-numbered names
  }
);
```

---

## Next Steps (Phase 2)

### Category Analytics Dashboard 📊

- Aggregált statisztikák kategóriánként
- Részfeladat szintű teljesítmény trendek
- Átlagos időmérés részfeladatonként
- Plánozott vs. aktuális pontérték összehasonlítás
- Időbeli fejlődés vizualizáció

**Expected endpoints:**
```
GET /api/categories/{id}/analytics
GET /api/categories/{id}/subtask-trends
```

---

## Changelog

### Version 1.3.0 (2026-03-04)
- ✅ **Bulk Task Creation**: Create multiple tasks from one template
- ✅ **taskCount field**: Define how many tasks to create (1-100)
- ✅ **Auto-numbering**: Tasks automatically named `#1`, `#2`, `#3`
- ✅ **Endpoint change**: `/create-task` → `/create-tasks` (returns array)
- ✅ **V3 Migration**: Added `task_count` column to `task_templates` table
- ✅ Frontend support: Task count input field and bulk creation handling

### Version 1.2.0 (2026-03-03)
- ✅ Template System Phase 1 teljes implementáció
- ✅ Kategória kötelezővé tétel analytics miatt
- ✅ 16 unit teszt (mind sikeres)
- ✅ REST API dokumentáció

---

## Support

**Issues:** https://github.com/gaboRsik/taskanalysis-prj/issues  
**Documentation:** `/docs/TEMPLATE_API.md`
