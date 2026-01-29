# Architecture Document

**Project:** Task Analysis  
**Date:** 2026-01-29  
**Version:** 1.0  
**Author:** BMad  
**Status:** Living Document (continuously updated)

---

## Document Information

### Purpose

This architecture document defines the technical design, system architecture, database schema, API contracts, and deployment strategy for the Task Analysis application. It serves as the single source of truth for all technical decisions and ensures consistency across development.

### Living Document Policy

This document is a **living document** - it will be updated continuously as architectural decisions evolve during development. All changes will be:
- Documented with version history
- Communicated to the team
- Reflected in implementation

---

## 1. System Overview

### High-Level Architecture

**Architecture Style:** Monolithic Web Application

**Components:**
- **Frontend:** Angular SPA (Single Page Application)
- **Backend:** Spring Boot REST API
- **Database:** MySQL 8.0+
- **Authentication:** JWT token-based (Access + Refresh)
- **Migration:** Flyway (database versioning)

### Deployment Architecture

#### Development Environment

```
┌─────────────────────────────────────┐
│   Developer Workstation             │
│                                     │
│  ┌──────────────┐  ┌─────────────┐ │
│  │   Angular    │  │ Spring Boot │ │
│  │  (ng serve)  │  │  (Maven)    │ │
│  │  Port: 4200  │  │  Port: 8080 │ │
│  └──────────────┘  └─────────────┘ │
│         │                 │         │
│         └────────┬────────┘         │
│                  │                  │
│         ┌────────▼─────────┐        │
│         │  Docker MySQL    │        │
│         │   Port: 3306     │        │
│         └──────────────────┘        │
└─────────────────────────────────────┘
```

**Development Setup:**
- Frontend: `ng serve` (localhost:4200)
- Backend: Spring Boot (localhost:8080)
- Database: Docker MySQL container
- CORS: Enabled (Frontend → Backend cross-origin)

#### Production Environment (AWS)

```
┌─────────────────────────────────────────────┐
│            AWS Cloud                        │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │  Subdomain (taskanalysis.domain.com) │  │
│  │  (HTTPS/SSL - Let's Encrypt)         │  │
│  └────────────┬─────────────────────────┘  │
│               │                             │
│  ┌────────────▼─────────────────────────┐  │
│  │    EC2 Instance / Elastic Beanstalk  │  │
│  │                                      │  │
│  │  ┌──────────────┐  ┌─────────────┐  │  │
│  │  │   Angular    │  │ Spring Boot │  │  │
│  │  │  (Bundled)   │  │  (JAR/WAR)  │  │  │
│  │  │  Static      │  │  Port: 8080 │  │  │
│  │  └──────────────┘  └─────────────┘  │  │
│  └──────────────────────────┬───────────┘  │
│                             │               │
│               ┌─────────────▼────────────┐  │
│               │   AWS RDS (MySQL 8.0)    │  │
│               │   (Managed Database)     │  │
│               └──────────────────────────┘  │
└─────────────────────────────────────────────┘
```

**Production Setup:**
- **Hosting:** AWS EC2 vagy Elastic Beanstalk
- **Database:** AWS RDS MySQL (managed)
- **Domain:** Saját subdomain (pl. taskanalysis.yourdomain.com)
- **SSL:** Let's Encrypt (ingyenes HTTPS)
- **Deployment:** Single JAR/WAR file (Spring Boot + Angular bundled)

---

## 2. Technology Stack

### Backend Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.2+ | Application framework |
| **Language** | Java | 17+ | Programming language |
| **Security** | Spring Security | 6.2+ | Authentication, Authorization |
| **Data Access** | Spring Data JPA | 3.2+ | ORM, Database access |
| **Database** | MySQL | 8.0+ | Relational database |
| **Migration** | Flyway | 10.0+ | Database versioning |
| **Build Tool** | Maven | 3.9+ | Dependency management, build |
| **JWT** | jjwt | 0.12+ | JWT token generation/validation |
| **Export** | Apache POI | 5.2+ | Excel export |
| **Export** | iText / PDFBox | Latest | PDF export |
| **Validation** | Hibernate Validator | 8.0+ | Bean validation |
| **Logging** | Logback | Built-in | Application logging |

### Frontend Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Angular | 17+ | SPA framework |
| **Language** | TypeScript | 5.0+ | Type-safe JavaScript |
| **Styling** | SCSS | Latest | CSS preprocessor |
| **UI Library** | Angular Material | 17+ | UI components |
| **HTTP** | HttpClient | Built-in | API communication |
| **Forms** | Reactive Forms | Built-in | Form handling |
| **Routing** | Angular Router | Built-in | SPA navigation |
| **State** | RxJS | 7.8+ | Reactive programming |
| **Build** | Angular CLI | 17+ | Build, dev server |

### Development Tools

| Tool | Purpose |
|------|---------|
| **Git** | Version control |
| **GitHub** | Remote repository, CI/CD |
| **Docker** | MySQL containerization (dev) |
| **IntelliJ IDEA** | Backend IDE |
| **VS Code** | Frontend IDE |
| **Postman** | API testing |

---

## 3. Database Architecture

### Schema Overview

**Tables:** 5 core tables
- `users` - User accounts
- `categories` - Task categories
- `tasks` - Main tasks
- `subtasks` - Subtasks (parts of tasks)
- `time_entries` - Time tracking records

### Entity Relationship Diagram

```
┌─────────────┐
│   users     │
│─────────────│
│ id (PK)     │
│ email       │
│ password    │
│ name        │
│ created_at  │
│ updated_at  │
└──────┬──────┘
       │
       │ 1:N
       │
       ├─────────────────────────┐
       │                         │
       ▼                         ▼
┌──────────────┐          ┌─────────────┐
│  categories  │          │   tasks     │
│──────────────│          │─────────────│
│ id (PK)      │          │ id (PK)     │
│ user_id (FK) │◄─────N:1─│ user_id (FK)│
│ name         │          │ category_id │
│ created_at   │          │ name        │
│ updated_at   │          │ description │
└──────────────┘          │ subtask_cnt │
                          │ status      │
                          │ created_at  │
                          │ updated_at  │
                          └──────┬──────┘
                                 │
                                 │ 1:N
                                 │
                                 ▼
                          ┌──────────────┐
                          │  subtasks    │
                          │──────────────│
                          │ id (PK)      │
                          │ task_id (FK) │
                          │ subtask_num  │
                          │ planned_pts  │
                          │ actual_pts   │
                          │ status       │
                          │ created_at   │
                          │ updated_at   │
                          └──────┬───────┘
                                 │
                                 │ 1:N
                                 │
                                 ▼
                          ┌──────────────┐
                          │ time_entries │
                          │──────────────│
                          │ id (PK)      │
                          │ subtask_id   │
                          │ start_time   │
                          │ end_time     │
                          │ duration_sec │
                          │ created_at   │
                          └──────────────┘
```

---

### Detailed Table Schemas

#### `users` Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);
```

**Fields:**
- `id`: Primary key
- `email`: Unique user email (used for login)
- `password`: BCrypt hashed password (cost factor: 12)
- `name`: User's display name
- `created_at`: Account creation timestamp
- `updated_at`: Last update timestamp

**Indexes:**
- `idx_email`: Fast lookup for authentication

---

#### `categories` Table

```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    UNIQUE KEY unique_user_category (user_id, name)
);
```

**Fields:**
- `id`: Primary key
- `user_id`: Owner user (FK)
- `name`: Category name (e.g., "Tanulás", "Projekt")
- `created_at`, `updated_at`: Timestamps

**Constraints:**
- `unique_user_category`: User cannot have duplicate category names
- `ON DELETE CASCADE`: Delete categories when user is deleted

**Indexes:**
- `idx_user_id`: Fast user category lookup

---

#### `tasks` Table

```sql
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    subtask_count INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',  -- NOT_STARTED, IN_PROGRESS, COMPLETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
);
```

**Fields:**
- `id`: Primary key
- `user_id`: Owner user (FK)
- `category_id`: Optional category (FK, nullable)
- `name`: Task name
- `description`: Optional task description
- `subtask_count`: Number of subtasks (1-100)
- `status`: Task status (NOT_STARTED | IN_PROGRESS | COMPLETED)
- `created_at`, `updated_at`: Timestamps

**Constraints:**
- `ON DELETE CASCADE` (user): Delete tasks when user is deleted
- `ON DELETE SET NULL` (category): Keep task when category is deleted

**Indexes:**
- `idx_user_id`: Fast user task lookup
- `idx_category_id`: Fast category filtering
- `idx_status`: Fast status filtering

---

#### `subtasks` Table

```sql
CREATE TABLE subtasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    subtask_number INT NOT NULL,  -- 1, 2, 3, ..., N
    planned_points INT NULL DEFAULT 0,
    actual_points INT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',  -- NOT_STARTED, IN_PROGRESS, COMPLETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY unique_task_subtask (task_id, subtask_number),
    INDEX idx_task_id (task_id),
    INDEX idx_status (status)
);
```

**Fields:**
- `id`: Primary key
- `task_id`: Parent task (FK)
- `subtask_number`: Subtask sequence number (1-based)
- `planned_points`: Expected points (0-1000)
- `actual_points`: Achieved points (0-1000)
- `status`: Subtask status
- `created_at`, `updated_at`: Timestamps

**Constraints:**
- `unique_task_subtask`: Unique subtask number per task
- `ON DELETE CASCADE`: Delete subtasks when task is deleted

**Indexes:**
- `idx_task_id`: Fast task subtask lookup
- `idx_status`: Fast status filtering

---

#### `time_entries` Table

```sql
CREATE TABLE time_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subtask_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NULL,
    duration_seconds INT NULL,  -- Calculated: end_time - start_time
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subtask_id) REFERENCES subtasks(id) ON DELETE CASCADE,
    INDEX idx_subtask_id (subtask_id),
    INDEX idx_start_time (start_time)
);
```

**Fields:**
- `id`: Primary key
- `subtask_id`: Parent subtask (FK)
- `start_time`: Timer start timestamp
- `end_time`: Timer end timestamp (nullable if running)
- `duration_seconds`: Calculated duration (end - start)
- `created_at`: Entry creation timestamp

**Constraints:**
- `ON DELETE CASCADE`: Delete time entries when subtask is deleted

**Indexes:**
- `idx_subtask_id`: Fast subtask time entry lookup
- `idx_start_time`: Fast date range queries

**Duration Calculation:**
- Auto-calculated when `end_time` is set
- Formula: `TIMESTAMPDIFF(SECOND, start_time, end_time)`

---

### Data Integrity Rules

**1. User Cascade:**
- Delete user → cascade delete categories, tasks, subtasks, time_entries

**2. Task Creation:**
- Create task → auto-create N subtasks (subtask_number: 1..N)
- Subtask count cannot be changed after creation (data integrity)

**3. Timer Rules:**
- Only ONE active timer per user (end_time IS NULL)
- New timer start → auto-stop previous timer

**4. Status Management:**
- Task status auto-update based on subtask statuses
- Subtask status auto-update based on time entries

---

## 4. API Architecture

### API Design Principles

- **RESTful API** standard
- **JSON** request/response format
- **JWT Bearer token** authentication
- **HTTP status codes** for response types
- **Consistent error format** across all endpoints

### Base URL

- **Development:** `http://localhost:8080/api`
- **Production:** `https://taskanalysis.yourdomain.com/api`

### Authentication Flow

#### JWT Token Architecture

```
┌─────────────────────────────────────────────┐
│  1. Login                                   │
│  POST /api/auth/login                       │
│  { email, password }                        │
└────────────┬────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────┐
│  2. Response                                │
│  Set-Cookie: access_token (HttpOnly, 15min)│
│  Set-Cookie: refresh_token (HttpOnly, 7d)  │
│  Body: { userId, name, email }             │
└────────────┬────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────┐
│  3. API Requests                            │
│  Authorization: Bearer <access_token>       │
│  (Cookie automatically sent)                │
└────────────┬────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────┐
│  4. Token Refresh (when access expires)     │
│  POST /api/auth/refresh                     │
│  (refresh_token cookie sent automatically)  │
└────────────┬────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────┐
│  5. New Access Token                        │
│  Set-Cookie: access_token (HttpOnly, 15min)│
│  Set-Cookie: refresh_token (HttpOnly, 7d)  │
└─────────────────────────────────────────────┘
```

---

### API Endpoints

#### Authentication Endpoints

**POST /api/auth/register**
- **Purpose:** User registration
- **Request:**
  ```json
  {
    "email": "user@example.com",
    "password": "SecurePass123",
    "name": "John Doe"
  }
  ```
- **Response (201):**
  ```json
  {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  }
  ```
- **Errors:** 400 (validation), 409 (email exists)

---

**POST /api/auth/login**
- **Purpose:** User login
- **Request:**
  ```json
  {
    "email": "user@example.com",
    "password": "SecurePass123"
  }
  ```
- **Response (200):**
  - Set-Cookie: `access_token` (HttpOnly, 15min)
  - Set-Cookie: `refresh_token` (HttpOnly, 7 days)
  - Body:
    ```json
    {
      "userId": 1,
      "email": "user@example.com",
      "name": "John Doe"
    }
    ```
- **Errors:** 401 (invalid credentials)

---

**POST /api/auth/refresh**
- **Purpose:** Refresh access token
- **Request:** No body (refresh_token cookie)
- **Response (200):**
  - Set-Cookie: new `access_token` (HttpOnly, 15min)
  - Set-Cookie: new `refresh_token` (HttpOnly, 7 days)
  - Body:
    ```json
    {
      "message": "Token refreshed"
    }
    ```
- **Errors:** 401 (invalid/expired refresh token)

---

**POST /api/auth/logout**
- **Purpose:** User logout
- **Request:** No body
- **Response (200):**
  - Clear cookies
  - Body:
    ```json
    {
      "message": "Logged out successfully"
    }
    ```

---

**POST /api/auth/forgot-password**
- **Purpose:** Request password reset
- **Request:**
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Response (200):**
  ```json
  {
    "message": "Password reset email sent"
  }
  ```
- **Errors:** 404 (email not found)

---

**POST /api/auth/reset-password**
- **Purpose:** Reset password with token
- **Request:**
  ```json
  {
    "token": "reset-token-from-email",
    "newPassword": "NewSecurePass123"
  }
  ```
- **Response (200):**
  ```json
  {
    "message": "Password reset successfully"
  }
  ```
- **Errors:** 400 (invalid token), 401 (expired token)

---

#### Category Endpoints

**GET /api/categories**
- **Purpose:** List user's categories
- **Auth:** Required
- **Response (200):**
  ```json
  [
    {
      "id": 1,
      "name": "Tanulás",
      "createdAt": "2026-01-29T10:00:00Z"
    },
    {
      "id": 2,
      "name": "Projektek",
      "createdAt": "2026-01-28T15:30:00Z"
    }
  ]
  ```

---

**POST /api/categories**
- **Purpose:** Create category
- **Auth:** Required
- **Request:**
  ```json
  {
    "name": "Új kategória"
  }
  ```
- **Response (201):**
  ```json
  {
    "id": 3,
    "name": "Új kategória",
    "createdAt": "2026-01-29T12:00:00Z"
  }
  ```
- **Errors:** 400 (validation), 409 (duplicate name)

---

**PUT /api/categories/{id}**
- **Purpose:** Update category
- **Auth:** Required
- **Request:**
  ```json
  {
    "name": "Módosított név"
  }
  ```
- **Response (200):**
  ```json
  {
    "id": 3,
    "name": "Módosított név",
    "updatedAt": "2026-01-29T12:05:00Z"
  }
  ```
- **Errors:** 404 (not found), 403 (not owner)

---

**DELETE /api/categories/{id}**
- **Purpose:** Delete category
- **Auth:** Required
- **Response (204):** No content
- **Errors:** 404 (not found), 403 (not owner)

---

#### Task Endpoints

**GET /api/tasks**
- **Purpose:** List user's tasks
- **Auth:** Required
- **Query params:**
  - `categoryId` (optional): Filter by category
  - `status` (optional): NOT_STARTED | IN_PROGRESS | COMPLETED
  - `search` (optional): Search by name
- **Response (200):**
  ```json
  [
    {
      "id": 1,
      "name": "Matekdolgozat gyakorlás",
      "description": "10 feladatos teszt",
      "categoryId": 1,
      "categoryName": "Tanulás",
      "subtaskCount": 10,
      "status": "IN_PROGRESS",
      "totalTimeSeconds": 1800,
      "completedSubtasks": 5,
      "createdAt": "2026-01-29T10:00:00Z"
    }
  ]
  ```

---

**GET /api/tasks/{id}**
- **Purpose:** Get task details with subtasks
- **Auth:** Required
- **Response (200):**
  ```json
  {
    "id": 1,
    "name": "Matekdolgozat gyakorlás",
    "description": "10 feladatos teszt",
    "categoryId": 1,
    "categoryName": "Tanulás",
    "subtaskCount": 10,
    "status": "IN_PROGRESS",
    "subtasks": [
      {
        "id": 1,
        "subtaskNumber": 1,
        "plannedPoints": 10,
        "actualPoints": 8,
        "status": "COMPLETED",
        "totalTimeSeconds": 180
      },
      {
        "id": 2,
        "subtaskNumber": 2,
        "plannedPoints": 10,
        "actualPoints": 0,
        "status": "IN_PROGRESS",
        "totalTimeSeconds": 120
      }
    ],
    "createdAt": "2026-01-29T10:00:00Z"
  }
  ```
- **Errors:** 404 (not found), 403 (not owner)

---

**POST /api/tasks**
- **Purpose:** Create task (auto-creates subtasks)
- **Auth:** Required
- **Request:**
  ```json
  {
    "name": "Új feladat",
    "description": "Leírás",
    "categoryId": 1,
    "subtaskCount": 10
  }
  ```
- **Response (201):**
  ```json
  {
    "id": 2,
    "name": "Új feladat",
    "subtaskCount": 10,
    "status": "NOT_STARTED",
    "createdAt": "2026-01-29T12:00:00Z"
  }
  ```
- **Errors:** 400 (validation: subtaskCount 1-100)

---

**PUT /api/tasks/{id}**
- **Purpose:** Update task (name, description, category only)
- **Auth:** Required
- **Request:**
  ```json
  {
    "name": "Módosított név",
    "description": "Új leírás",
    "categoryId": 2
  }
  ```
- **Response (200):** Updated task object
- **Errors:** 404 (not found), 403 (not owner)
- **Note:** `subtaskCount` CANNOT be changed

---

**DELETE /api/tasks/{id}**
- **Purpose:** Delete task (cascades subtasks, time entries)
- **Auth:** Required
- **Response (204):** No content
- **Errors:** 404 (not found), 403 (not owner)

---

#### Subtask Endpoints

**PUT /api/subtasks/{id}/points**
- **Purpose:** Update subtask points
- **Auth:** Required
- **Request:**
  ```json
  {
    "plannedPoints": 10,
    "actualPoints": 8
  }
  ```
- **Response (200):**
  ```json
  {
    "id": 1,
    "subtaskNumber": 1,
    "plannedPoints": 10,
    "actualPoints": 8,
    "updatedAt": "2026-01-29T12:10:00Z"
  }
  ```
- **Errors:** 404 (not found), 403 (not owner)

---

#### Timer Endpoints

**POST /api/timer/start/{subtaskId}**
- **Purpose:** Start timer for subtask
- **Auth:** Required
- **Behavior:**
  - If another timer is active → auto-stop it first
  - Create new time_entry with start_time
- **Response (200):**
  ```json
  {
    "timeEntryId": 123,
    "subtaskId": 5,
    "startTime": "2026-01-29T12:15:00Z",
    "message": "Timer started"
  }
  ```
- **Errors:** 404 (subtask not found), 403 (not owner)

---

**POST /api/timer/stop**
- **Purpose:** Stop currently active timer
- **Auth:** Required
- **Behavior:**
  - Find active time_entry (end_time IS NULL)
  - Set end_time = now
  - Calculate duration_seconds
- **Response (200):**
  ```json
  {
    "timeEntryId": 123,
    "subtaskId": 5,
    "startTime": "2026-01-29T12:15:00Z",
    "endTime": "2026-01-29T12:20:00Z",
    "durationSeconds": 300,
    "message": "Timer stopped"
  }
  ```
- **Errors:** 404 (no active timer)

---

**GET /api/timer/active**
- **Purpose:** Get currently active timer
- **Auth:** Required
- **Response (200):**
  ```json
  {
    "timeEntryId": 123,
    "subtaskId": 5,
    "subtaskNumber": 3,
    "taskId": 1,
    "taskName": "Matekdolgozat",
    "startTime": "2026-01-29T12:15:00Z",
    "elapsedSeconds": 120
  }
  ```
- **Response (204):** No active timer

---

#### Export Endpoints

**GET /api/export/task/{taskId}/excel**
- **Purpose:** Export task data to Excel
- **Auth:** Required
- **Response (200):**
  - Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
  - Content-Disposition: `attachment; filename="task_name_2026-01-29.xlsx"`
  - Binary Excel file
- **Errors:** 404 (not found), 403 (not owner)

---

**GET /api/export/task/{taskId}/pdf**
- **Purpose:** Export task data to PDF
- **Auth:** Required
- **Response (200):**
  - Content-Type: `application/pdf`
  - Content-Disposition: `attachment; filename="task_name_2026-01-29.pdf"`
  - Binary PDF file
- **Errors:** 404 (not found), 403 (not owner)

---

### Error Response Format

**Consistent error structure across all endpoints:**

```json
{
  "timestamp": "2026-01-29T12:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "Email is required",
    "Password must be at least 8 characters"
  ],
  "path": "/api/auth/register"
}
```

**HTTP Status Codes:**
- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success, no response body
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Authentication failed
- `403 Forbidden` - Authorization failed (not owner)
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource
- `500 Internal Server Error` - Server error

---

## 5. Security Architecture

### Authentication & Authorization

#### JWT Token Strategy

**Access Token:**
- **Lifetime:** 15 minutes
- **Storage:** HttpOnly Cookie
- **Purpose:** API request authentication
- **Claims:**
  ```json
  {
    "sub": "user@example.com",
    "userId": 1,
    "exp": 1706534400,
    "iat": 1706533500
  }
  ```

**Refresh Token:**
- **Lifetime:** 7 days
- **Storage:** HttpOnly Cookie
- **Purpose:** Generate new access token
- **Claims:**
  ```json
  {
    "sub": "user@example.com",
    "userId": 1,
    "tokenId": "uuid-v4",
    "exp": 1707139200,
    "iat": 1706534400
  }
  ```

**Token Rotation:**
- Every refresh → new refresh token issued
- Old refresh token invalidated
- Prevents token reuse attacks

---

### Spring Security Configuration

**SecurityConfig.java highlights:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

**Key Security Features:**
- ✅ CSRF Protection enabled (CookieCsrfTokenRepository)
- ✅ CORS configured (dev: localhost:4200, prod: domain)
- ✅ Stateless session (JWT-based)
- ✅ JWT filter before authentication filter

---

### Password Security

**BCrypt Configuration:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Cost factor: 12
}
```

**Password Requirements (enforced):**
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit

---

### CORS Configuration

**Development:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);  // Required for cookies
    return source;
}
```

**Production:**
```java
config.setAllowedOrigins(Arrays.asList("https://taskanalysis.yourdomain.com"));
```

---

### HTTPS/SSL (Production)

**Let's Encrypt SSL Certificate:**
- Free SSL certificate
- Auto-renewal with Certbot
- AWS Application Load Balancer handles SSL termination

**Spring Boot Configuration (application-prod.yml):**
```yaml
server:
  ssl:
    enabled: true
  servlet:
    session:
      cookie:
        secure: true       # HTTPS only
        http-only: true
        same-site: strict
```

---

### Rate Limiting

**Spring Boot Rate Limiter (Bucket4j):**
- Max 100 requests / minute / user
- Prevents brute force attacks
- 429 Too Many Requests response

---

### Data Protection

**Sensitive Data Handling:**
- ✅ Passwords: BCrypt hashed (never stored plain)
- ✅ JWT tokens: HttpOnly cookies (not accessible via JS)
- ✅ HTTPS: All traffic encrypted in production
- ✅ SQL Injection: JPA Prepared Statements
- ✅ XSS: Angular built-in sanitization

---

## 6. Component Architecture

### Backend Components

```
┌───────────────────────────────────────────────┐
│            Spring Boot Backend                │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │         Controller Layer                │ │
│  │  AuthController, TaskController, etc.   │ │
│  │  - REST endpoints                       │ │
│  │  - Request/Response DTOs                │ │
│  │  - Validation                           │ │
│  └──────────────┬──────────────────────────┘ │
│                 │                             │
│  ┌──────────────▼──────────────────────────┐ │
│  │         Service Layer                   │ │
│  │  AuthService, TaskService, etc.         │ │
│  │  - Business logic                       │ │
│  │  - Transaction management               │ │
│  │  - Authorization checks                 │ │
│  └──────────────┬──────────────────────────┘ │
│                 │                             │
│  ┌──────────────▼──────────────────────────┐ │
│  │       Repository Layer                  │ │
│  │  UserRepository, TaskRepository, etc.   │ │
│  │  - JPA repositories                     │ │
│  │  - Database queries                     │ │
│  └──────────────┬──────────────────────────┘ │
│                 │                             │
│  ┌──────────────▼──────────────────────────┐ │
│  │         Entity Layer                    │ │
│  │  User, Task, Subtask, TimeEntry         │ │
│  │  - JPA entities                         │ │
│  │  - Relationships                        │ │
│  └──────────────┬──────────────────────────┘ │
└─────────────────┼───────────────────────────┘
                  │
                  ▼
           ┌──────────────┐
           │    MySQL     │
           │   Database   │
           └──────────────┘
```

---

### Frontend Components

```
┌───────────────────────────────────────────────┐
│           Angular Frontend                    │
│                                               │
│  ┌─────────────────────────────────────────┐ │
│  │        Routing Module                   │ │
│  │  app.routes.ts                          │ │
│  │  - Route definitions                    │ │
│  │  - Auth guards                          │ │
│  └──────────────┬──────────────────────────┘ │
│                 │                             │
│  ┌──────────────▼──────────────────────────┐ │
│  │      Component Layer                    │ │
│  │  LoginComponent, TaskListComponent, etc.│ │
│  │  - UI logic                             │ │
│  │  - Template binding                     │ │
│  │  - Event handling                       │ │
│  └──────────────┬──────────────────────────┘ │
│                 │                             │
│  ┌──────────────▼──────────────────────────┐ │
│  │        Service Layer                    │ │
│  │  AuthService, TaskService, etc.         │ │
│  │  - HTTP API calls                       │ │
│  │  - State management                     │ │
│  │  - Business logic                       │ │
│  └──────────────┬──────────────────────────┘ │
│                 │                             │
│  ┌──────────────▼──────────────────────────┐ │
│  │         Model Layer                     │ │
│  │  User, Task, Subtask interfaces         │ │
│  │  - TypeScript interfaces                │ │
│  │  - Type definitions                     │ │
│  └─────────────────────────────────────────┘ │
└───────────────────────────────────────────────┘
```

---

## 7. Database Migration Strategy

### Flyway Migration

**Migration Files Location:**
```
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_indexes.sql
└── V3__future_changes.sql
```

**Naming Convention:**
- `V{version}__{description}.sql`
- Version: Sequential integer (1, 2, 3...)
- Description: Snake_case description

**V1__initial_schema.sql (Initial):**
```sql
-- Create all 5 tables: users, categories, tasks, subtasks, time_entries
-- Add all indexes
-- Add all foreign keys
-- No initial data (empty tables)
```

**Migration Process:**
1. Development: Run Flyway on local Docker MySQL
2. Staging: Run Flyway on staging database
3. Production: Run Flyway on production AWS RDS

**Rollback Strategy:**
- Flyway Community: No automatic rollback
- Manual rollback: Create undo SQL scripts (V2__undo.sql)
- Best practice: Test migrations in staging first

---

## 8. Deployment Architecture

### Development Environment Setup

**Docker MySQL Container:**
```bash
docker run --name taskanalysis-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=taskanalysis_dev \
  -e MYSQL_USER=dev_user \
  -e MYSQL_PASSWORD=devpass \
  -p 3306:3306 \
  -d mysql:8.0
```

**Backend Start:**
```bash
cd taskanalysis-backend
mvn spring-boot:run
```

**Frontend Start:**
```bash
cd taskanalysis-frontend
ng serve
```

**Access:**
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- MySQL: localhost:3306

---

### Production Deployment (AWS)

**Architecture:**
1. **AWS EC2 / Elastic Beanstalk** - Application server
2. **AWS RDS MySQL** - Managed database
3. **Route 53** - DNS management
4. **Application Load Balancer** - HTTPS termination, load balancing
5. **S3** (optional) - Static assets (if separated)

**Deployment Steps:**
1. Build Angular production bundle: `ng build --prod`
2. Copy Angular dist/ to Spring Boot static/ folder
3. Build Spring Boot JAR: `mvn clean package`
4. Upload JAR to AWS Elastic Beanstalk
5. Configure RDS connection (environment variables)
6. Run Flyway migrations
7. Configure domain + SSL certificate

---

## 9. Performance Optimization

### Backend Optimizations

**1. Database Indexes:**
- All foreign keys indexed
- Email (unique) indexed
- Status fields indexed

**2. Query Optimization:**
- JPA fetch strategies (LAZY loading)
- Pagination for large result sets
- Native queries for complex analytics

**3. Caching (Post-MVP):**
- Spring Cache abstraction
- Redis for session storage
- Category/User data caching

---

### Frontend Optimizations

**1. Lazy Loading:**
- Route-based lazy loading (Angular modules)
- Defer non-critical components

**2. Change Detection:**
- OnPush strategy for performance
- Avoid unnecessary re-renders

**3. Build Optimization:**
- Production build minification
- Tree-shaking unused code
- AOT compilation

---

## 10. Monitoring & Logging

### Backend Logging

**Logback Configuration:**
- INFO level for general logs
- ERROR level for exceptions
- Separate log files: application.log, error.log

**What to Log:**
- Authentication events (login, logout, failed attempts)
- API errors (500, 400, 401)
- Timer events (start, stop)
- Database errors

---

### Frontend Error Tracking

**Angular Error Handler:**
- Global error interceptor
- Log errors to backend API (optional)
- User-friendly error messages

---

## 11. Testing Strategy

### Backend Testing (Post-MVP)

**Unit Tests:**
- Service layer (JUnit 5, Mockito)
- Controller layer (MockMvc)
- Repository layer (Spring Data Test)

**Integration Tests:**
- API endpoint tests
- Database integration tests
- JWT authentication flow tests

---

### Frontend Testing (Post-MVP)

**Unit Tests:**
- Component tests (Jasmine, Karma)
- Service tests (HttpClientTestingModule)

**E2E Tests:**
- Critical user flows (login, timer, export)
- Playwright or Cypress

---

## 12. Technical Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Architecture** | Monolithic | Simpler deployment, MVP-friendly |
| **Backend Framework** | Spring Boot | Mature, enterprise-ready, JWT support |
| **Frontend Framework** | Angular | TypeScript, component-based, scalable |
| **Database** | MySQL | Relational data, ACID compliance |
| **Migration Tool** | Flyway | Version control for database schema |
| **Authentication** | JWT (Access + Refresh) | Stateless, scalable, secure |
| **API Style** | RESTful | Standard, well-understood |
| **Timer API** | Simple (auto-stop) | Easier implementation, better UX |
| **Subtask Storage** | Separate table | Data integrity, easier querying |
| **Deployment** | AWS (EC2/RDS) | Scalable, production-ready |
| **SSL** | Let's Encrypt | Free, auto-renewal |

---

## 13. Open Questions & Future Considerations

### Resolved Questions

✅ Timer API design (auto-stop chosen)  
✅ Subtask storage (separate table chosen)  
✅ Deployment strategy (AWS monolith)

### Future Considerations (Post-MVP)

**1. Scalability:**
- Horizontal scaling (multiple backend instances)
- Database read replicas
- Redis caching layer

**2. Features:**
- WebSocket for real-time timer sync
- Mobile app (React Native)
- Offline mode (PWA, service workers)

**3. Analytics:**
- Advanced dashboard visualizations (Chart.js)
- AI-based time predictions
- Export customization options

---

## 14. Document Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-29 | BMad | Initial architecture document |

---

## 15. Summary

This architecture document defines the complete technical design for the **Task Analysis** application. Key highlights:

- **Monolithic architecture** (Spring Boot + Angular bundled)
- **JWT-based authentication** (Access 15min + Refresh 7day, HttpOnly cookies)
- **RESTful API** with 25+ endpoints
- **5-table database schema** (users, categories, tasks, subtasks, time_entries)
- **Flyway migration** for database versioning
- **Simple timer API** (auto-stop on switch)
- **AWS deployment** (EC2/Elastic Beanstalk + RDS MySQL)
- **Security-first** (HTTPS, CSRF, BCrypt, rate limiting)

**Status:** Ready for implementation. All technical decisions documented.

---

**🏗️ ARCHITECTURE DOCUMENT COMPLETE 🏗️**

---





