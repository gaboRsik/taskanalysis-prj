# Security Features Documentation

**Project:** Task Analysis  
**Version:** 1.1.0  
**Date:** 2026-03-01  
**Author:** Security Hardening Phase

---

## Overview

Az alkalmazás biztonsági rétege többszintű védelmet biztosít a felhasználói adatok és rendszer stabilitása érdekében.

---

## 1. Rate Limiting 🚦

### Általános API Rate Limits

**Limit:** 100 request / perc / IP cím

**Érintett endpoint-ok:** Minden `/api/**` útvonal

**Válasz rate limit esetén:**
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60
Content-Type: application/json

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please slow down your requests.",
  "path": "/api/tasks",
  "retryAfter": 60
}
```

### Authentication Rate Limits (Szigorúbb)

**Limit:** 5 request / perc / IP cím

**Érintett endpoint-ok:**
- `POST /api/auth/login`
- `POST /api/auth/register`

**Válasz:**
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many authentication attempts. Please try again later.",
  "path": "/api/auth/login",
  "retryAfter": 60
}
```

### Implementáció:
- **Algorithm:** Token Bucket (Bucket4j)
- **Storage:** In-memory cache (Caffeine)
- **IP Detection:** X-Forwarded-For header támogatás (proxy/load balancer-ekhez)

---

## 2. Password Policy 🔐

### Követelmények

Új felhasználói regisztrációnál az alábbi jelszó követelmények érvényesek:

1. **Minimum hossz:** 8 karakter
2. **Tartalmaznia KELL:**
   - Legalább 1 nagybetűt (A-Z)
   - Legalább 1 kisbetűt (a-z)
   - Legalább 1 számot (0-9)
   - Legalább 1 speciális karaktert (!@#$%^&*()_+-=[]{}|;:,.<>?)

### API Endpoint

**POST /api/auth/register**

**Request Body:**
```json
{
  "name": "Test User",
  "email": "test@example.com",
  "password": "SecurePass123!"
}
```

**Validation Error Response:**
```http
HTTP/1.1 400 Bad Request

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/auth/register",
  "validationErrors": {
    "password": "Password must contain at least one uppercase letter"
  }
}
```

### Egyéb validációs szabályok:
- **Jelszó példák (ELFOGADOTT):**
  - `Password123!`
  - `MyP@ssw0rd`
  - `Secure#123`
  
- **Jelszó példák (ELUTASÍTOTT):**
  - `password` - nincs nagybetű, szám, speciális karakter
  - `PASSWORD123` - nincs kisbetű, speciális karakter
  - `Pass123!` - túl rövid (< 8 karakter)

---

## 3. Account Lockout Mechanism 🔒

### Szabályok

- **Max sikertelen login:** 5 próbálkozás
- **Lockout időtartam:** 15 perc
- **Tracking:** Email cím alapján

### Működés

#### 1-4. sikertelen login:
```http
HTTP/1.1 401 Unauthorized

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid credentials. 3 attempts remaining before account lockout.",
  "path": "/api/auth/login"
}
```

#### 5. sikertelen login (lockout):
```http
HTTP/1.1 403 Forbidden

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 403,
  "error": "Account Locked",
  "message": "Too many failed login attempts. Account is now locked for 15 minutes.",
  "path": "/api/auth/login",
  "retryAfter": 900
}
```

#### Lockout alatt próbálkozás:
```http
HTTP/1.1 403 Forbidden

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 403,
  "error": "Account Locked",
  "message": "Account is temporarily locked due to too many failed login attempts. Please try again in 12 minutes.",
  "path": "/api/auth/login",
  "retryAfter": 720
}
```

### Lockout feloldása:
- **Automatikus:** 15 perc után
- **Sikeres login:** Azonnal törli a failed attempts számlálót

---

## 4. Input Validation Enhancements ✔️

### Task Creation/Update

**Új validációs szabályok:**

```json
{
  "name": "Task name",           // Required, 1-255 karakter
  "description": "Description",  // Optional, MAX 5000 karakter (DOS védelem)
  "categoryId": 1,               // Optional
  "subtaskCount": 10             // Required, MIN: 1, MAX: 100 (DOS védelem)
}
```

### Validation Error Response

```http
HTTP/1.1 400 Bad Request

{
  "timestamp": "2026-03-01T10:15:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/tasks",
  "validationErrors": {
    "subtaskCount": "Subtask count must not exceed 100",
    "description": "Description must not exceed 5000 characters"
  }
}
```

---

## 5. Standardized Error Responses 🛡️

### Error Response Format

Minden hiba válasz az alábbi formátumot követi:

```typescript
interface ErrorResponse {
  timestamp: string;           // ISO 8601 format
  status: number;              // HTTP status code
  error: string;               // Error type
  message: string;             // User-friendly message
  path: string;                // Request path
  validationErrors?: {         // Field-level validation errors
    [field: string]: string;
  };
  retryAfter?: number;         // Seconds (for rate limiting/lockout)
  debugMessage?: string;       // Only in dev mode
}
```

### HTTP Status Codes

| Status | Error Type | Jelentés |
|--------|------------|----------|
| 400 | Bad Request | Validation error vagy hibás adatformátum |
| 401 | Unauthorized | Hiányzó vagy érvénytelen autentikáció |
| 403 | Forbidden | Nincs jogosultság / Account locked |
| 404 | Not Found | Erőforrás nem található |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Szerver oldali hiba |

### Példák kategóriánként

#### Validation Error (400)
```json
{
  "timestamp": "2026-03-01T10:15:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/tasks",
  "validationErrors": {
    "name": "Task name is required"
  }
}
```

#### Authentication Error (401)
```json
{
  "timestamp": "2026-03-01T10:15:30",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid credentials or authentication token",
  "path": "/api/auth/login"
}
```

#### Access Denied (403)
```json
{
  "timestamp": "2026-03-01T10:15:30",
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource",
  "path": "/api/tasks/123"
}
```

#### Not Found (404)
```json
{
  "timestamp": "2026-03-01T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: '123'",
  "path": "/api/tasks/123"
}
```

#### Rate Limit (429)
```json
{
  "timestamp": "2026-03-01T10:15:30",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please slow down your requests.",
  "path": "/api/tasks",
  "retryAfter": 60
}
```

---

## 6. Structured Logging 📊

### Log Formátumok

**Development mode:**
```
2026-03-01 10:15:30.123 [http-nio-8080-exec-1] INFO  c.t.controller.TaskController - Incoming request: GET /api/tasks from 127.0.0.1
```

**Production mode (JSON):**
```json
{
  "@timestamp": "2026-03-01T10:15:30.123Z",
  "level": "INFO",
  "logger": "com.taskanalysis.controller.TaskController",
  "message": "Incoming request: GET /api/tasks from 192.168.1.100",
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "ip": "192.168.1.100",
  "userId": "123"
}
```

### Log Files

| File | Tartalom | Retention |
|------|----------|-----------|
| `taskanalysis.log` | Általános app logok | 30 nap |
| `taskanalysis-error.log` | Csak ERROR szintű logok | 90 nap |
| `security-audit.log` | Security események | 90 nap |

### Security Events Logged

- Failed login attempts
- Successful logins
- Account lockouts
- Rate limit violations
- Access denied attempts
- Authentication errors

---

## 7. Security Headers (Planned)

### Jövőbeli implementáció

```http
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

---

## Testing & Verification

### Rate Limiting Test

```bash
# Test general API rate limit
for i in {1..105}; do 
  curl -X GET http://localhost:8080/api/tasks \
    -H "Authorization: Bearer YOUR_TOKEN"
  echo "Request $i"
done

# Expected: First 100 succeed, then 429 errors
```

### Password Policy Test

```bash
# Weak password (should fail)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "weak"
  }'

# Strong password (should succeed)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "SecurePass123!"
  }'
```

### Account Lockout Test

```bash
# Try 6 failed logins
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "email": "test@example.com",
      "password": "wrongpassword"
    }'
  echo "Attempt $i"
done

# Expected: First 5 show remaining attempts, 6th locks account
```

---

## Configuration

### Application Properties

```properties
# Debug mode (shows detailed error messages)
app.debug=false

# Log configuration
logging.level.com.taskanalysis=INFO
logging.level.com.taskanalysis.security=DEBUG
logging.file.path=logs
logging.file.name=taskanalysis.log
```

### Environment Variables

```bash
# Production deployment
APP_DEBUG=false
LOG_LEVEL=INFO
```

---

## Migration Notes

### Breaking Changes

1. **Password Requirements:** Existing passwords nem érintettek, csak új regisztrációknál.
2. **Error Response Format:** Új JSON structure - frontend UPDATE SZÜKSÉGES!
3. **Rate Limiting:** Fejlesztői környezetben is aktív.

### Frontend Changes Required

```typescript
// Régi error handling
.catch(error => {
  console.error(error.message);
});

// ÚJ error handling
.catch(error => {
  const errorResponse = error.response.data;
  
  // Field-level validation errors
  if (errorResponse.validationErrors) {
    Object.keys(errorResponse.validationErrors).forEach(field => {
      showFieldError(field, errorResponse.validationErrors[field]);
    });
  }
  
  // Rate limiting
  if (errorResponse.status === 429) {
    showRetryMessage(`Please wait ${errorResponse.retryAfter} seconds`);
  }
  
  // Account lockout
  if (errorResponse.error === "Account Locked") {
    showLockoutMessage(errorResponse.message, errorResponse.retryAfter);
  }
  
  // General message
  showErrorMessage(errorResponse.message);
});
```

---

## Security Best Practices

### Fejlesztőknek

1. **Soha ne logolj jelszavakat vagy tokeneket**
2. **Debug mode CSAK development-ben**
3. **Rate limiting tesztelés local környezetben is**
4. **Validation minden user input-nál**
5. **Generic error messages production-ben** (ne adjon ki internal info-t)

### Production Deployment

1. **HTTPS kötelező**
2. **Environment variables jelszavakhoz**
3. **Structured logging + monitoring**
4. **Regular security updates**
5. **Backup strategy**

---

## Support & Issues

Security kapcsolatos kérdések vagy problémák esetén:
- Check logs: `logs/security-audit.log`
- Debug mode: `app.debug=true` (csak development!)
- Contact: BMad

---

**Last Updated:** 2026-03-01
