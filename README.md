# Task Analysis Project

Task Analysis application - részfeladat szintű időmérés és elemzés.

## Tech Stack

### Backend
- **Spring Boot 3.2.2**
- **Java 17**
- **MySQL 8.0**
- **Spring Security + JWT**
- **Flyway Migration**

### Frontend
- **Angular** (Standalone Components)
- **TypeScript**
- **SCSS**

## Fejlesztői környezet beállítása

### Előfeltételek
- Java 17+
- Node.js 18+
- Docker Desktop
- IntelliJ IDEA
- Maven

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

## Projekt struktúra

```
taskanalysis-prj/
├── backend/               # Spring Boot backend
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
│   │   │       └── db/migration/
│   │   └── test/
│   └── pom.xml
├── frontend/              # Angular frontend (később)
├── docs/                  # Dokumentáció
│   ├── prd.md
│   └── architecture.md
└── docker-compose.yml     # MySQL Docker config
```

## Következő lépések

- [x] Backend projekt struktúra
- [x] Entity osztályok
- [x] Repository-k
- [x] Flyway migration
- [ ] JWT Security konfiguráció
- [ ] DTO-k és mapper-ek
- [ ] Service réteg
- [ ] REST API Controller-ek
- [ ] Frontend projekt létrehozása

## BMAD Workflow

Ez a projekt a BMAD (Business Model Analysis & Design) workflow-t használja a fejlesztési folyamathoz.

## Dokumentáció

- [PRD](docs/prd.md) - Product Requirements Document
- [Architecture](docs/architecture.md) - Technikai architektúra

---

*Utolsó frissítés: 2026-01-29*
