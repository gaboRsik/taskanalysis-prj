# Task Analysis Project

Task Analysis application - részfeladat szintű időmérés és elemzés.

## Tech Stack

### Backend
- **Spring Boot 3.2.2**
- **Java 17**
- **MySQL 8.0**
- **Spring Security + JWT**
- **Flyway Migration**
- **Security Features:** 🆕
  - Rate Limiting (Bucket4j)
  - Account Lockout
  - Password Policy Enforcement
  - Structured Logging

### Frontend
- **Angular** (Standalone Components)
- **TypeScript**
- **SCSS**

## Fejlesztői környezet beállítása

### 🔧 Előfeltételek

**Fejlesztéshez:**
- Java 17+
- Node.js 18+
- Docker Desktop
- IntelliJ IDEA / VS Code
- Maven

**Production deployment-hez:**
- Docker & Docker Compose
- AWS Account (opcionális)
- Domain név (opcionális)

### 🚀 Development Setup

### 1. MySQL indítása (Docker)

```bash
docker-compose up -d
```

Ellenőrzés:
```bash
docker ps
```

### 2. Backend indítása

IntelliJ IDEA-ban:
1. Nyisd meg a `backend` mappát projektként
2. Maven projekt betöltése
3. Futtasd a `TaskAnalysisApplication` main metódust

Vagy terminálban:
```bash
cd backend
mvn spring-boot:run
```

Backend elérhető: `http://localhost:8080/api`

### 3. Frontend indítása (később)

```bash
cd frontend
npm install
ng serve
```

Frontend elérhető: `http://localhost:4200`

## Adatbázis séma

Az adatbázis automatikusan létrejön a Flyway migration-ökkel az első indításkor.

**Táblák:**
- `users` - Felhasználók
- `categories` - Kategóriák
- `tasks` - Feladatok
- `subtasks` - Részfeladatok
- `time_entries` - Időbejegyzések

## � Security Features 🆕

**Rate Limiting:**
- 100 requests/perc (API)
- 5 requests/perc (Auth endpoints)

**Account Protection:**
- 5 sikertelen login után 15 perc lockout
- Email-based tracking

**Password Policy:**
- Min 8 karakter
- Nagybetű + kisbetű + szám + speciális karakter

**Error Handling:**
- Standardizált JSON error responses
- Structured logging (JSON format)
- Security audit log

Részletes dokumentáció: **[Security Features](docs/SECURITY_FEATURES.md)**

## 🧪 Testing

**Run tests:**
```bash
cd backend
mvn test
```

**Test Coverage:**
- Password validator tests (11 tests)
- Login attempt service tests (9 tests)
- Rate limit service tests (9 tests)

Részletek: [Test README](backend/src/test/java/README.md)

## �🐋 Docker Deployment (Production)

### Quick Start - Production

```bash
# 1. Environment setup
cp .env.example .env
nano .env  # Fill in your production values

# 2. Build and run
docker-compose -f docker-compose.prod.yml up -d --build

# 3. Check status
docker ps
docker-compose -f docker-compose.prod.yml logs -f
```

### 📦 Dockerfile-ok:
- `backend/Dockerfile` - Spring Boot (t3.micro optimalizált, 512MB limit)
- `frontend/Dockerfile` - Angular + Nginx (multi-stage build)
- `docker-compose.prod.yml` - Production setup memory optimalizációval

### ☁️ AWS Deployment:
Teljes lépésről-lépésre útmutató: **[AWS Deployment Guide](docs/AWS_DEPLOYMENT_GUIDE.md)**

- t3.micro instance setup (Free Tier 12 hónapig)
- Docker telepítés és konfiguráció
- Domain beállítás és SSL (Let's Encrypt)
- Memory optimalizáció és monitoring

**Költség:**
- **Első 12 hó**: ~€3/hó (Free Tier + storage)
- **Utána**: ~€7-8/hó (t3.micro) vagy €14-16/hó (t3.small)

---

## Projekt struktúra

```
taskanalysis-prj/
├── backend/
│   ├── Dockerfile                    # Backend container 🆕
│   ├── .dockerignore                 # 🆕
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/taskanalysis/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── security/
│   │   │   │   └── TaskAnalysisApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-prod.properties  # 🆕
│   │   │       └── db/migration/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── Dockerfile                    # Frontend container 🆕
│   ├── nginx.conf                    # Nginx config 🆕
│   ├── .dockerignore                 # 🆕
│   └── src/
├── docs/
│   ├── prd.md
│   ├── architecture.md
│   ├── EXPORT_FEATURE.md
│   └── AWS_DEPLOYMENT_GUIDE.md      # 🆕 Deployment útmutató
├── docker-compose.yml                # Development
├── docker-compose.prod.yml           # 🆕 Production
└── .env.example                      # 🆕 Environment template
```

## Features Status

### ✅ Implementált
- Backend projekt struktúra
- Entity osztályok
- Repository-k
- Flyway migration
- JWT Security konfiguráció
- DTO-k és mapper-ek
- Service réteg
- REST API Controller-ek
- **Security Features** 🆕
  - Rate limiting (Bucket4j)
  - Account lockout mechanism
  - Password policy enforcement
  - Global exception handler
  - Structured logging
  - Unit tests
- **Export funkció (Excel)** 
  - Email küldés mellékletként
  - Közvetlen letöltés
  - Adaptive UX (mobil/desktop)

### 🚧 Fejlesztés alatt
- Frontend Angular komponensek
- Dashboard analytics
- PDF export
- Category Analytics (következő sprint)
- Template System (következő sprint)

## Export Feature 🆕

Az export feature lehetővé teszi a felhasználók számára az adatok Excel formátumban való mentését.

**Adaptive megjelenés:**
- 📱 **Mobilon**: csak email küldés (mobilbarát)
- 💻 **Desktop-on**: letöltés + email küldés

**Setup:**
1. Email konfiguráció environment variables:
   ```bash
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   MAIL_FROM=Task Analysis <noreply@taskanalysis.com>
   ```

2. Részletes dokumentáció: [EXPORT_FEATURE.md](docs/EXPORT_FEATURE.md)

**API Endpoint:**
```
POST /api/export/task/{taskId}
Body: { "format": "XLSX", "delivery": "EMAIL" }
```

## BMAD Workflow

Ez a projekt a BMAD (Business Model Analysis & Design) workflow-t használja a fejlesztési folyamathoz.

## Dokumentáció

- [PRD](docs/prd.md) - Product Requirements Document
- [Architecture](docs/architecture.md) - Technikai architektúra
- [Security Features](docs/SECURITY_FEATURES.md) - Biztonsági funkciók 🆕
- [Export Feature](docs/EXPORT_FEATURE.md) - Export funkció dokumentáció
- [AWS Deployment](docs/AWS_DEPLOYMENT_GUIDE.md) - AWS deployment útmutató
- [Testing Guide](backend/src/test/java/README.md) - Unit teszt dokumentáció 🆕

---

*Utolsó frissítés: 2026-03-01 - Security hardening phase implementálva*
