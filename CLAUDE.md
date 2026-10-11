# CLAUDE.md

Útmutató a Claude Code számára ehhez a repositoryhoz.

## A projekt

Task Analysis: időkövetés és -elemzés részfeladat szinten.
- Backend (`backend/`): Spring Boot 3.2, Java 17, MySQL 8, Flyway, JWT hitelesítés, Maven.
- Frontend (`frontend/`): Angular 20 standalone komponensek, Bootstrap 5, ngx-charts.
- Éles környezet: Docker Compose AWS-en (MySQL + backend + Nginx frontend), domain: `tasks.gaborsiknet.hu`.

A dokumentáció (README, `docs/`) nagyrészt magyar. A felhasználói felület nyelve magyar (részletek: `.claude/rules/frontend.md`).

## Parancsok

```bash
# Helyi MySQL (fejlesztői DB: taskanalysis / taskanalysis_user / taskanalysis_password, port :3306)
docker-compose up -d

# Backend — http://localhost:8080/api
cd backend
mvn spring-boot:run
mvn test                                         # összes teszt
mvn test -Dtest=LoginAttemptServiceTest          # egy tesztosztály
mvn test -Dtest=LoginAttemptServiceTest#methodName

# Frontend — http://localhost:4200
cd frontend
npm install
npm start          # ng serve
npm run build      # éles build -> dist/
npm test           # Karma/Jasmine

# Éles stack, a .env-ből olvas
docker-compose -f docker-compose.prod.yml up -d --build
```

- Lint nincs beállítva. A backend tesztek egyszerű unit tesztek (`backend/src/test/java`), a frontendben csak a generált `app.spec.ts` van.
- Az `api-tests.http` és a `backend/api-tests.http` kézi HTTP kéréseket tartalmaz.
- Az AI chatbothoz `GROQ_API_KEY` kell, az e-mailes funkciókhoz (jelszó-visszaállítás, export e-mailben) `MAIL_USERNAME` / `MAIL_PASSWORD`.

## Architektúra röviden

- Backend csomag: `com.taskanalysis`. Rétegek: `controller` → `service` → `repository` → `entity`. A DTO-k a `dto/<feature>/` alatt vannak, mellettük `security/`, `config/`, `exception/`, `scheduler/`, `validation/`.
- **A servlet context path `/api`**: a controllerekben, a security matcherekben és az interceptor mintákban az `/api` előtag NÉLKÜL kell írni az útvonalakat.
- **A sémát a Flyway kezeli**: minden entitásváltozáshoz új `V<n>__*.sql` migráció kell. Lefuttatott migrációt soha ne módosíts.
- Funkciók:
  - feladatok részfeladatokkal és időmérőkkel;
  - állapotkezelés automatikus lezáró ütemezővel;
  - feladatsablonok;
  - globális és személyes részfeladat-címkék;
  - szerepkörök (USER/ADMIN);
  - XLSX export (letöltés vagy e-mail);
  - Groq alapú chatbot (LangChain4j).
- Frontend: útvonalak az `app.routes.ts`-ben, backend erőforrásonként egy service a `services/` mappában, témázás a `src/styles.scss` CSS változóival.

A területenkénti részletes szabályok a `.claude/rules/` mappában vannak, és automatikusan betöltődnek:
- `workflow.md`: mindig (tervezés, ellenőrzés, git, kommunikáció)
- `backend.md`: amikor a `backend/` alatt dolgozol
- `frontend.md`: amikor a `frontend/` alatt dolgozol
- `deployment.md`: Docker, Nginx, `.env.example`, GitHub konfiguráció

## Dokumentáció

- A `docs/` funkcióleírásai (`*_FEATURE.md`, `*_GUIDE.md`, `TASK_STATUS_MANAGEMENT.md`, `SECURITY_FEATURES.md`) megbízható források az adott funkcióhoz.
- A `docs/architecture.md` és a `docs/prd.md` az eredeti BMAD tervezési dokumentum, és ELAVULT: Angular Materialt, AWS RDS-t, egyetlen JAR-os telepítést és csak 5 táblát említ. Ha ellentmond a kódnak vagy ennek a fájlnak, a kód és ez a fájl az irányadó.
- A `.bmad/` a BMAD tervezési keretrendszer, nem alkalmazáskód.
