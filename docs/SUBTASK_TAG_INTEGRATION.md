# 🏷️ Subtask Tag Integration - Complete Guide

## ✅ What Was Implemented

The **Dual-Level Tag System** is now **fully integrated** into subtasks and templates:

### 1. **Subtask Tag Support**
   - **SubtaskResponse** includes `List<SubtaskTagDTO> tags`
   - **SubtaskRequest** accepts `List<Long> tagIds` for assignment
   - **PUT /subtasks/{id}** endpoint updates subtask tags
   - Tags are validated (user can only assign visible tags: global + their own)

### 2. **Template Subtask Tag Support**
   - **TemplateSubtaskDTO** includes `List<Long> tagIds`
   - **TemplateSubtask entity** has many-to-many relationship with `SubtaskTag`
   - **V8__Template_Subtask_Tags.sql** migration creates `template_subtask_tag_mapping` table
   - **POST/PUT /api/templates** endpoints handle template subtask tags

### 3. **Tag Inheritance from Templates**
   - When creating tasks from templates, **tags are automatically copied** from template subtasks to actual subtasks
   - Only visible tags (global + user-owned) are copied
   - Implemented in `TaskService.createTasksFromTemplate()`

---

## 📊 Database Schema

### New Migration: V8__Template_Subtask_Tags.sql

```sql
-- Junction table for template subtask tags
CREATE TABLE template_subtask_tag_mapping (
    template_subtask_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (template_subtask_id, tag_id),
    FOREIGN KEY (template_subtask_id) REFERENCES template_subtasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES subtask_tags(id) ON DELETE CASCADE
);
```

### Existing: subtask_tag_mapping (V7)

```sql
-- Junction table for subtask tags
CREATE TABLE subtask_tag_mapping (
    subtask_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (subtask_id, tag_id),
    FOREIGN KEY (subtask_id) REFERENCES subtasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES subtask_tags(id) ON DELETE CASCADE
);
```

---

## 🔌 API Usage Examples

### **1. Update Subtask Tags**

```http
PUT /subtasks/123
Authorization: Bearer <token>
Content-Type: application/json

{
  "plannedPoints": 5,
  "actualPoints": 3,
  "tagIds": [1, 2, 5]  // Frontend, Backend, Testing
}
```

**Response:**
```json
{
  "id": 123,
  "taskId": 45,
  "subtaskNumber": 1,
  "plannedPoints": 5,
  "actualPoints": 3,
  "tags": [
    {
      "id": 1,
      "name": "Frontend",
      "color": "#3b82f6",
      "isGlobal": true,
      "userId": null,
      "createdById": 1
    },
    {
      "id": 2,
      "name": "Backend",
      "color": "#10b981",
      "isGlobal": true,
      "userId": null,
      "createdById": 1
    }
  ]
}
```

### **2. Create Template with Tagged Subtasks**

```http
POST /api/templates
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "API Development",
  "description": "Build REST API endpoint",
  "categoryId": 3,
  "subtaskCount": 3,
  "taskCount": 1,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5,
      "tagIds": [1, 2]  // Frontend, Backend
    },
    {
      "subtaskNumber": 2,
      "plannedPoints": 3,
      "tagIds": [3, 5]  // Database, Testing
    }
  ]
}
```

### **3. Create Task from Template (Tags Auto-Copied)**

```http
POST /api/templates/15/tasks
Authorization: Bearer <token>
```

**Result:**
- Task created from template #15
- Subtask #1 automatically gets tags [Frontend, Backend]
- Subtask #2 automatically gets tags [Database, Testing]
- Only visible tags are copied (global + user-owned)

---

## 🔧 Backend Implementation Details

### **Modified Services**

#### **SubtaskService.java**
```java
@Transactional
public SubtaskResponse updateSubtask(Long userId, Long subtaskId, SubtaskRequest request) {
    Subtask subtask = ...;
    
    // Update tags if provided
    if (request.getTagIds() != null) {
        subtask.clearTags();
        
        if (!request.getTagIds().isEmpty()) {
            Set<SubtaskTag> tags = new HashSet<>(
                subtaskTagRepository.findByIdsVisibleToUser(request.getTagIds(), userId)
            );
            
            if (tags.size() != request.getTagIds().size()) {
                throw new RuntimeException("Some tags are not accessible to this user");
            }
            
            tags.forEach(subtask::addTag);
        }
    }
    
    return mapToResponse(subtask);
}
```

#### **TaskService.java - Template Tag Inheritance**
```java
public List<TaskResponse> createTasksFromTemplate(Long userId, Long templateId) {
    // ...
    for (TemplateSubtask templateSubtask : template.getTemplateSubtasks()) {
        Subtask subtask = new Subtask();
        // ... set other fields ...
        
        // Copy tags from template subtask
        if (templateSubtask.getTags() != null && !templateSubtask.getTags().isEmpty()) {
            templateSubtask.getTags().forEach(tag -> {
                // Only copy tags that are visible to the user
                if (tag.isVisibleToUser(userId)) {
                    subtask.addTag(tag);
                }
            });
        }
        
        subtasks.add(subtask);
    }
    // ...
}
```

#### **TemplateService.java**
```java
public TemplateResponse createTemplate(TemplateRequest request, User user) {
    // ...
    for (TemplateSubtaskDTO dto : request.getTemplateSubtasks()) {
        TemplateSubtask templateSubtask = new TemplateSubtask();
        // ... set other fields ...
        
        // Add tags if provided
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<SubtaskTag> tags = subtaskTagRepository.findByIdsVisibleToUser(
                dto.getTagIds(), user.getId()
            );
            if (tags.size() != dto.getTagIds().size()) {
                throw new BusinessException("Some tags are not accessible to this user");
            }
            tags.forEach(templateSubtask::addTag);
        }
        
        templateSubtasks.add(templateSubtask);
    }
    // ...
}
```

---

## 🧪 Testing the Integration

### **1. Run Database Migrations**
```bash
# Backend restart will auto-run V7 and V8 migrations
cd backend
mvn spring-boot:run
```

### **2. Test Tag CRUD** (see backend/api-tests.http)
```http
### Get all tags (global + my tags)
GET {{baseUrl}}/api/subtask-tags
Authorization: Bearer {{auth_token}}

### Create global tag (admin only)
POST {{baseUrl}}/api/subtask-tags
Authorization: Bearer {{auth_token}}
Content-Type: application/json

{
  "name": "Security",
  "color": "#ff6b6b",
  "isGlobal": true
}
```

### **3. Test Subtask Tag Assignment**
```http
### Update subtask with tags
PUT {{baseUrl}}/subtasks/1
Authorization: Bearer {{auth_token}}
Content-Type: application/json

{
  "plannedPoints": 5,
  "tagIds": [1, 2, 5]
}
```

### **4. Test Template Tag Inheritance**
```http
### Create template with tagged subtasks
POST {{baseUrl}}/api/templates
Authorization: Bearer {{auth_token}}
Content-Type: application/json

{
  "name": "API Development",
  "categoryId": 1,
  "subtaskCount": 2,
  "templateSubtasks": [
    {
      "subtaskNumber": 1,
      "plannedPoints": 5,
      "tagIds": [1, 2]
    }
  ]
}

### Create task from template (tags auto-copied)
POST {{baseUrl}}/api/templates/1/tasks
Authorization: Bearer {{auth_token}}
```

---

## 📋 Data Flow Summary

```
┌─────────────────┐
│ SubtaskTag      │  Global tags (admin) + User-specific tags
│ (V7 migration)  │
└────────┬────────┘
         │
         ├───────────────────────────────┐
         │                               │
         ▼                               ▼
┌─────────────────┐            ┌─────────────────┐
│ Subtask         │            │ TemplateSubtask │
│ (via mapping)   │            │ (via mapping)   │
└────────┬────────┘            └────────┬────────┘
         │                               │
         │                               │
         │  Tags copied when creating    │
         │  tasks from templates ────────┘
         │
         ▼
  SubtaskResponse
  (API returns tags)
```

---

## 🎯 User Workflow

### **For Regular Users:**

1. **View Available Tags:**
   - GET `/api/subtask-tags` → See global tags + my tags
   
2. **Create Personal Tag:**
   - POST `/api/subtask-tags` with `isGlobal: false`
   
3. **Tag Subtasks:**
   - PUT `/subtasks/{id}` with `tagIds: [1, 2, 5]`
   
4. **Create Template with Tags:**
   - POST `/api/templates` with `templateSubtasks[].tagIds`
   
5. **Use Template:**
   - POST `/api/templates/{id}/tasks` → Tags auto-copied to subtasks

### **For Admins:**

1. **Create Global Tags:**
   - POST `/api/subtask-tags` with `isGlobal: true`
   
2. **Manage Global Tags:**
   - PUT/DELETE `/api/subtask-tags/{id}` (admin only for global tags)

---

## 🚀 Next Steps (Frontend Implementation)

To complete the tag system, implement frontend components:

### **Priority 1: Tag Management UI**
- [ ] Tag list component (admin view)
- [ ] Create/edit tag modal
- [ ] Color picker for tag colors

### **Priority 2: Subtask Tag Selector**
- [ ] Multi-select dropdown for tag assignment
- [ ] Show tag chips on subtask cards
- [ ] Filter subtasks by tag

### **Priority 3: Template Tag Integration**
- [ ] Tag selector in template creation form
- [ ] Display tags in template preview
- [ ] Highlight tag inheritance when creating tasks

### **Priority 4: Analytics & Filtering**
- [ ] Filter tasks/subtasks by tag
- [ ] Tag-based time reports
- [ ] Tag usage statistics

---

## 📚 Related Documentation

- **SUBTASK_TAGS_GUIDE.md** - Tag system architecture and authorization rules
- **TEMPLATE_API.md** - Template API documentation
- **backend/api-tests.http** - API test examples (#13-20 for tags)

---

## ✅ Completion Checklist

- [x] V7__Subtask_Tags.sql migration (tag infrastructure)
- [x] V8__Template_Subtask_Tags.sql migration (template tag support)
- [x] SubtaskTag entity (global + user-specific tags)
- [x] Subtask entity (many-to-many with tags)
- [x] TemplateSubtask entity (many-to-many with tags)
- [x] SubtaskTagRepository (query methods)
- [x] SubtaskTagService (CRUD + authorization)
- [x] SubtaskTagController (REST API)
- [x] SubtaskService (tag update logic)
- [x] TaskService (tag mapping in responses)
- [x] TemplateService (template tag handling)
- [x] TaskService (template tag inheritance)
- [x] DTOs (SubtaskRequest, SubtaskResponse, TemplateSubtaskDTO)
- [x] API tests (backend/api-tests.http)
- [ ] Frontend implementation (Angular components)

---

## 🎉 Summary

**The backend tag integration is COMPLETE!** 

Subtasks can now be tagged, templates can define default tags, and tags are automatically inherited when creating tasks from templates. The dual-level system ensures admins can create global tags visible to everyone, while users can create personal tags for their own workflows.

**Ready for frontend development!** 🚀
