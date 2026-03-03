# 🚀 Deployment Workflow - Fejlesztésből Production-be
# Lépésről-lépésre útmutató új feature vagy bugfix deployolásához

## 📋 Áttekintés

Ez a guide végigvezet azon, hogyan vigyél fel **új funkciókat vagy javításokat** a lokális fejlesztői környezetből az **AWS production környezetbe** (`tasks.gaborsiknet.hu`).

**Környezetek:**
- 🖥️ **Lokális**: `localhost:4200` (frontend) & `localhost:8080` (backend)
- ☁️ **Production AWS**: `https://tasks.gaborsiknet.hu`

---

## 🔄 TELJES WORKFLOW FOLYAMAT

```
1. Lokális fejlesztés & tesztelés
   ↓
2. Git commit & push (GitHub)
   ↓
3. AWS szerver: git pull
   ↓
4. Docker rebuild (érintett service)
   ↓
5. Containers restart
   ↓
6. Production tesztelés
   ↓
7. ✅ KÉSZ!
```

---

## 🛠️ FÁZIS 1: Lokális Fejlesztés

### 1.1. Frontend módosítások (Angular)

**Példa: UI változtatás, új komponens, bug fix**

```powershell
# Lokális környezet indítása
cd C:\Users\siklo\PROGMASTERS_AI\myproduct\taskanalysis-prj\frontend

# Development server indítása
npm start
# Elérhető: http://localhost:4200
```

**Tesztelés lokálisan:**
- Nyisd meg: `http://localhost:4200`
- Ellenőrizd a változtatásokat
- Tesztelj minden érintett funkciót
- Nézd meg a Console-t (F12) → nincs-e hiba

**Ha minden rendben:**
```powershell
# Build production módban (opcionális teszt)
npm run build

# Ellenőrzés: frontend/dist/frontend/browser/ folder létrejött
```

### 1.2. Backend módosítások (Spring Boot)

**Példa: API endpoint, service logic, entity módosítás**

```powershell
# Backend futtatása (ha még nem megy)
cd C:\Users\siklo\PROGMASTERS_AI\myproduct\taskanalysis-prj\backend

# Maven clean install & run
mvn clean install
mvn spring-boot:run
# Elérhető: http://localhost:8080
```

**Tesztelés lokálisan:**
- API tesztelés: `backend/api-tests.http` fájl használata
- Vagy Postman/Thunder Client
- Ellenőrizd a változásokat
- Nézd a backend console log-ot

**Fontos ellenőrzések:**
- ✅ No compile errors
- ✅ Tests pass: `mvn test`
- ✅ API endpoints válaszolnak
- ✅ Database műveletek működnek

### 1.3. Docker Compose teszt (Opcionális, de ajánlott!)

**Teljes környezet lokális tesztelése Docker-rel:**

```powershell
# Project root
cd C:\Users\siklo\PROGMASTERS_AI\myproduct\taskanalysis-prj

# Docker Compose dev mode
docker-compose up --build

# Ellenőrzés:
# Frontend: http://localhost:4200
# Backend: http://localhost:8080
# MySQL: localhost:3306
```

**Ha minden működik:**
```powershell
# Leállítás
docker-compose down
```

---

## 📝 FÁZIS 2: Git Workflow (GitHub)

### 2.1. Git Status & Changes

```powershell
# Project root
cd C:\Users\siklo\PROGMASTERS_AI\myproduct\taskanalysis-prj

# Ellenőrizd a módosított fájlokat
git status

# Nézd meg a változásokat
git diff
```

### 2.2. Commit & Push

**Frontend változások:**
```powershell
# Add frontend fájlok
git add frontend/src/app/components/...
git add frontend/src/app/services/...

# Vagy minden frontend
git add frontend/

# Commit szabvány
git commit -m "feat: Add new task filtering feature"
# Vagy
git commit -m "fix: Resolve timer reset bug on task update"
# Vagy
git commit -m "refactor: Improve task service performance"
```

**Backend változások:**
```powershell
# Add backend fájlok
git add backend/src/main/java/com/taskanalysis/...

# Vagy minden backend
git add backend/

# Commit
git commit -m "feat: Add task export API endpoint"
# Vagy
git commit -m "fix: Add @Transactional to prevent LazyInitializationException"
```

**Mindkettő együtt:**
```powershell
# Add minden módosítást
git add .

# Commit
git commit -m "feat: Implement task statistics dashboard (frontend + backend)"
```

**Push to GitHub:**
```powershell
# Push main branch-re
git push origin main

# Ellenőrzés sikeres push után:
# https://github.com/gaboRsik/taskanalysis-prj/commits/main
```

### 2.3. Commit Message Guidelines

**Típusok:**
- `feat:` - Új feature
- `fix:` - Bug fix
- `refactor:` - Kód refaktorálás (funkcionalitás nem változik)
- `style:` - CSS, formatting változások
- `docs:` - Dokumentáció frissítés
- `test:` - Tesztek hozzáadása
- `chore:` - Build, config változások

**Példák:**
```bash
feat: Add category filter to task list
fix: Resolve subtask points calculation error
refactor: Extract timer logic to separate service
style: Update dashboard card layout
docs: Add API endpoint documentation
```

---

## ☁️ FÁZIS 3: AWS Deployment

### 3.1. SSH Kapcsolódás AWS-hez

```powershell
# Új PowerShell ablak
cd C:\Users\siklo\.ssh

# SSH connection
ssh -i taskanalysis-key.pem ubuntu@3.64.207.108
```

**Ellenőrzés sikeres kapcsolat után:**
```bash
# Aktuális hely
pwd
# Kimenet: /home/ubuntu

# Navigálás project-hez
cd taskanalysis-prj

# Git status
git status
# Kimenet: On branch main
```

### 3.2. Git Pull (Friss kód letöltése)

```bash
# Húzd le a friss kódot GitHub-ról
git pull origin main

# Kimenet példa:
# Updating 5285a43..abc1234
# Fast-forward
#  frontend/src/app/components/tasks/tasks.component.ts | 15 +++++++++++++++
#  backend/src/main/java/com/taskanalysis/service/TaskService.java | 8 ++++++--
#  2 files changed, 21 insertions(+), 2 deletions(-)
```

**Ellenőrzés:**
```bash
# Nézd meg a változásokat
git log --oneline -5

# Legutóbbi commit-nak látszania kell a tiédnek
```

### 3.3. Docker Rebuild Döntési Fa

**Melyik service-t kell újraépíteni?**

| Változás típusa | Rebuild szükséges | Parancs |
|----------------|-------------------|---------|
| Csak **Frontend** fájlok (.ts, .html, .scss) | Frontend | `backend NO, frontend YES` |
| Csak **Backend** fájlok (.java) | Backend | `backend YES, frontend NO` |
| Mindkettő | Mindkettő | `backend YES, frontend YES` |
| Csak **config** (.env, docker-compose.yml) | Mindkettő | `backend YES, frontend YES` |
| Csak **dokumentáció** (README.md, docs/) | Egyik sem | `Nincs rebuild` |

---

## 🐳 FÁZIS 4: Docker Rebuild Stratégiák

### Stratégia A: Csak Frontend Rebuild

**Mikor használd:**
- Angular komponens változás (.ts, .html, .scss)
- Új route vagy guard
- Service módosítás (frontend service)
- UI/UX változtatás

```bash
cd ~/taskanalysis-prj

# Stop frontend container
docker-compose -f docker-compose.prod.yml stop frontend

# Remove old frontend image
docker rmi -f taskanalysis-prj_frontend:latest

# Rebuild frontend with --no-cache
docker-compose -f docker-compose.prod.yml build --no-cache frontend

# Start all containers (MySQL és backend már fut, frontend újraindul)
docker-compose -f docker-compose.prod.yml up -d

# Verify
docker ps
```

**Expected output:**
```
CONTAINER ID   IMAGE                         STATUS         PORTS
abc123def456   taskanalysis-prj_frontend     Up 10 seconds  0.0.0.0:80->80/tcp
def456ghi789   taskanalysis-prj_backend      Up 2 hours     0.0.0.0:8080->8080/tcp
ghi789jkl012   mysql:8.0                     Up 2 hours     0.0.0.0:3306->3306/tcp
```

### Stratégia B: Csak Backend Rebuild

**Mikor használd:**
- Java controller, service, repository módosítás
- Entity vagy DTO változás
- Security config módosítás
- Új API endpoint

```bash
cd ~/taskanalysis-prj

# Stop backend container
docker-compose -f docker-compose.prod.yml stop backend

# Remove old backend container and image
docker-compose -f docker-compose.prod.yml rm -f backend
docker rmi -f taskanalysis-prj_backend:latest

# Rebuild backend with --no-cache (FONTOS!)
docker-compose -f docker-compose.prod.yml build --no-cache backend

# Start all containers
docker-compose -f docker-compose.prod.yml up -d

# Check backend logs
docker logs taskanalysis-backend-prod --tail 30
```

**Várj, amíg látod:**
```
Started TaskAnalysisApplication in XX.XXX seconds
```

### Stratégia C: Teljes Rebuild (Frontend + Backend)

**Mikor használd:**
- Major feature (frontend + backend együtt módosult)
- Database schema változás (.env MySQL változás)
- Docker config módosítás (docker-compose.prod.yml)
- Biztonsági frissítés

```bash
cd ~/taskanalysis-prj

# Stop all containers
docker-compose -f docker-compose.prod.yml down

# Remove old images (force)
docker rmi -f taskanalysis-prj_frontend:latest
docker rmi -f taskanalysis-prj_backend:latest

# Rebuild ALL with --no-cache
docker-compose -f docker-compose.prod.yml build --no-cache

# Start all containers
docker-compose -f docker-compose.prod.yml up -d

# Monitor startup
docker-compose -f docker-compose.prod.yml logs -f
# Ctrl+C to exit logs
```

**Build időtartam:**
- Frontend: ~2-3 perc
- Backend: ~3-5 perc (Maven dependencies)
- Teljes rebuild: ~5-8 perc

### Stratégia D: Gyors Restart (Konfig változás, nincs kód módosítás)

**Mikor használd:**
- .env fájl módosítás
- Environment variable változás
- Nginx config reload

```bash
cd ~/taskanalysis-prj

# Restart services (build nélkül)
docker-compose -f docker-compose.prod.yml restart

# Vagy csak egy service
docker-compose -f docker-compose.prod.yml restart backend
docker-compose -f docker-compose.prod.yml restart frontend
```

---

## ✅ FÁZIS 5: Verification (Ellenőrzés)

### 5.1. Container Status Check

```bash
# Containers futnak-e?
docker ps

# Expected output (mind Up legyen):
# taskanalysis-frontend-prod   Up X minutes
# taskanalysis-backend-prod    Up X minutes
# taskanalysis-mysql-prod      Up X hours
```

**Ha valamelyik hiányzik vagy Exited:**
```bash
# Logs ellenőrzése
docker logs taskanalysis-backend-prod
docker logs taskanalysis-frontend-prod

# Restart probléma esetén
docker-compose -f docker-compose.prod.yml restart [service-name]
```

### 5.2. Backend Health Check

```bash
# Actuator health endpoint
curl http://localhost:8080/api/actuator/health

# Expected: {"status":"UP"}
```

**Backend logs ellenőrzése:**
```bash
# Utolsó 50 sor
docker logs taskanalysis-backend-prod --tail 50

# Keresés hibákra
docker logs taskanalysis-backend-prod 2>&1 | grep -i error
docker logs taskanalysis-backend-prod 2>&1 | grep -i exception

# Ha nincs output → nincs error ✅
```

### 5.3. Frontend Check

```bash
# Frontend elérhető-e?
curl http://localhost

# Expected: HTML content (lange sorok)
```

### 5.4. Memory & Resources

```bash
# Container resource használat
docker stats --no-stream

# Expected (t3.micro):
# Backend: ~200-400 MB
# Frontend: ~5-10 MB
# MySQL: ~100-200 MB

# Rendszer memória
free -h

# Swap használat (kis mértékű OK)
# Total: 2.0Gi
```

---

## 🧪 FÁZIS 6: Production Teszt (Browser)

### 6.1. Cache Törlés (FONTOS!)

**Miért?** A böngésző cache-eli a régi JavaScript fájlokat!

**Két opció:**

**A) InPrivate/Incognito mód (gyors):**
```
Microsoft Edge: Ctrl + Shift + N
Chrome: Ctrl + Shift + N
Firefox: Ctrl + Shift + P
```

**B) Cache törlés (normál böngésző):**
```
Edge/Chrome: Ctrl + Shift + Delete
→ Jelöld be: "Gyorsítótárazott képek és fájlok"
→ Időtartomány: "Utolsó 1 óra"
→ Törlés most
```

**Ezután:**
```
Ctrl + Shift + R (hard refresh)
Vagy
Ctrl + F5
```

### 6.2. Funkcionális Tesztek

**Navigálj:**
```
https://tasks.gaborsiknet.hu
```

**Tesztelendő (minimum):**
1. **Login/Register** ✅
   - Bejelentkezés működik?
   - JWT token generálódik?

2. **Tasks Lista** ✅
   - Megjelennek a task-ok?
   - Console-ban nincs hiba?

3. **Új feature/fix ellenőrzése** ✅
   - Az új funkció működik?
   - A bug fix javította a problémát?

4. **Timer funkció** ✅ (ha érintett)
   - Start/Stop működik?
   - Idő mentése OK?

5. **Kategóriák** ✅ (ha érintett)
   - Létrehozás/szerkesztés működik?

**DevTools Console (F12) ellenőrzés:**
```javascript
// Console-ban NE legyenek:
❌ Error loading tasks: 403 Forbidden
❌ POST http://localhost:8080 (NO localhost!)
❌ LazyInitializationException

// Elvárás:
✅ GET https://tasks.gaborsiknet.hu/api/tasks 200
✅ POST https://tasks.gaborsiknet.hu/api/tasks 201
```

### 6.3. Backend Logs (Production)

**AWS-en (SSH session-ben):**
```bash
# Real-time logs follow
docker logs taskanalysis-backend-prod -f

# Új ablakban a böngészőben végezd a teszteket
# Látnod kell a log-okban:
# GET /api/tasks - 200
# POST /api/tasks - 201
# PUT /api/tasks/123 - 200

# Ctrl+C to stop following
```

**Keresés hibákra:**
```bash
# Utolsó 100 sor, szűrve error-ra
docker logs taskanalysis-backend-prod --tail 100 2>&1 | grep -i "error\|exception"

# Ha üres output → nincs hiba ✅
```

---

## 🚨 FÁZIS 7: Troubleshooting (Ha valami nem működik)

### Probléma 1: Frontend nem tölti be az új kódot

**Tünetek:**
- Régi UI látszik
- Új funkció nem jelenik meg
- Console-ban régi fájlnevek

**Megoldás:**
```bash
# SSH AWS
cd ~/taskanalysis-prj

# Frontend TELJES rebuild
docker-compose -f docker-compose.prod.yml stop frontend
docker rmi -f taskanalysis-prj_frontend:latest
docker-compose -f docker-compose.prod.yml build --no-cache frontend
docker-compose -f docker-compose.prod.yml up -d

# Böngészőben:
# 1. Ctrl + Shift + Delete → Cache törlés
# 2. Ctrl + Shift + R → Hard refresh
# 3. Vagy InPrivate ablak
```

### Probléma 2: Backend 403 Forbidden vagy 500 Error

**Tünetek:**
- API hívások 403/500 választ adnak
- Tasks nem töltődnek be
- Timer nem működik

**Debug:**
```bash
# Backend logs
docker logs taskanalysis-backend-prod --tail 50

# Keresés exception-re
docker logs taskanalysis-backend-prod 2>&1 | grep -i exception | head -20

# Gyakori okok:
# - LazyInitializationException → @Transactional hiányzik
# - NullPointerException → null check hiányzik
# - DataIntegrityViolationException → DB constraint hiba
```

**Megoldás:**
```bash
# Backend rebuild
docker-compose -f docker-compose.prod.yml stop backend
docker-compose -f docker-compose.prod.yml rm -f backend
docker rmi -f taskanalysis-prj_backend:latest
docker-compose -f docker-compose.prod.yml build --no-cache backend
docker-compose -f docker-compose.prod.yml up -d
docker logs taskanalysis-backend-prod --tail 30
```

### Probléma 3: Container nem indul el

**Tünetek:**
```bash
docker ps
# Backend vagy Frontend HIÁNYZIK a listából
```

**Debug:**
```bash
# Container exit code
docker ps -a | grep taskanalysis

# Logs az exited container-ből
docker logs taskanalysis-backend-prod
docker logs taskanalysis-frontend-prod

# Gyakori okok:
# Backend: MySQL connection refused
# Frontend: Nginx config error
```

**Megoldás:**
```bash
# MySQL ellenőrzés
docker logs taskanalysis-mysql-prod --tail 30

# Ha MySQL nem fut:
docker-compose -f docker-compose.prod.yml restart mysql

# Várj 10 másodpercet, majd start backend
docker-compose -f docker-compose.prod.yml start backend
```

### Probléma 4: Out of Memory (t3.micro)

**Tünetek:**
- Container crashed
- `docker stats` mutat 90%+ memory használat
- Lassú működés

**Debug:**
```bash
# Memory check
free -h
# Swap használat magas? (>1GB)

docker stats --no-stream
# Melyik container használ sok memóriát?
```

**Megoldás rövid távon:**
```bash
# Restart services (memory cleanup)
docker-compose -f docker-compose.prod.yml restart

# Docker cleanup (unused images/containers)
docker system prune -a
# Válasz: y
```

**Megoldás hosszú távon:**
```
# AWS Console:
# Instance type change: t3.micro → t3.small
# Cost: ~€7/hó helyett ~€15/hó
# Memory: 1GB → 2GB
```

### Probléma 5: Git pull conflict

**Tünetek:**
```bash
git pull origin main
# Error: Your local changes would be overwritten by merge
```

**Megoldás:**
```bash
# Lokális változások eldobása (ÓVATOSAN!)
# Ez elveszíti az AWS szerveren tett módosításokat!
git reset --hard origin/main

# Vagy: változások mentése, majd pull
git stash
git pull origin main
git stash pop
```

**Best Practice:** Soha ne módosíts kódot közvetlenül AWS-en! Mindig lokálisan fejlessz!

### Probléma 6: Docker build timeout vagy hiba

**Tünetek:**
```bash
docker-compose build --no-cache backend
# ERROR: failed to solve: timeout
# Vagy: npm install failed
```

**Megoldás:**
```bash
# Network issue → retry
docker-compose -f docker-compose.prod.yml build --no-cache backend

# Ha továbbra is fail:
# 1. Ellenőrizd az AWS instance network connectivity
ping 8.8.8.8

# 2. Docker daemon restart
sudo systemctl restart docker

# 3. Próbáld újra
docker-compose -f docker-compose.prod.yml build --no-cache backend
```

---

## 📊 FÁZIS 8: Post-Deployment Checklist

### ✅ Immediate Verification (5 perc)

- [ ] SSH connection működik
- [ ] `git pull` sikeres volt
- [ ] Docker containers futnak (`docker ps`)
- [ ] Backend logs: "Started TaskAnalysisApplication"
- [ ] Frontend elérhető: `https://tasks.gaborsiknet.hu`
- [ ] InPrivate ablakban login működik
- [ ] Console-ban nincs Error
- [ ] Új feature/fix működik

### ✅ Extended Testing (15 perc)

- [ ] Teljes user flow végigpróbálása
- [ ] Különböző böngészőben teszt (Edge, Chrome)
- [ ] Mobile view (F12 → responsive mode)
- [ ] Backend API manual teszt (Postman/Thunder Client)
- [ ] Performance check (oldal gyorsan tölt?)
- [ ] Memory usage normális (`docker stats`)

### ✅ Monitoring & Cleanup (opcionális)

```bash
# Logs archíválása (ha kell)
docker logs taskanalysis-backend-prod > ~/logs/backend_$(date +%Y%m%d).log

# Old images cleanup (hely felszabadítás)
docker system prune -a

# Git log ellenőrzés
git log --oneline -10
```

---

## 🔁 GYORS REFERENCIA - Parancsok egy helyen

### Lokális Fejlesztés
```powershell
# Frontend
cd frontend
npm start

# Backend
cd backend
mvn spring-boot:run

# Docker compose
docker-compose up --build
```

### Git Workflow
```powershell
git status
git add .
git commit -m "feat: Your feature description"
git push origin main
```

### AWS Deployment - Full Flow
```bash
# 1. SSH
ssh -i C:\Users\siklo\.ssh\taskanalysis-key.pem ubuntu@3.64.207.108

# 2. Navigate
cd ~/taskanalysis-prj

# 3. Pull
git pull origin main

# 4. Backend only
docker-compose -f docker-compose.prod.yml stop backend
docker-compose -f docker-compose.prod.yml rm -f backend
docker rmi -f taskanalysis-prj_backend:latest
docker-compose -f docker-compose.prod.yml build --no-cache backend
docker-compose -f docker-compose.prod.yml up -d
docker logs taskanalysis-backend-prod --tail 30

# 5. Frontend only
docker-compose -f docker-compose.prod.yml stop frontend
docker rmi -f taskanalysis-prj_frontend:latest
docker-compose -f docker-compose.prod.yml build --no-cache frontend
docker-compose -f docker-compose.prod.yml up -d

# 6. Both
docker-compose -f docker-compose.prod.yml down
docker rmi -f taskanalysis-prj_backend:latest taskanalysis-prj_frontend:latest
docker-compose -f docker-compose.prod.yml build --no-cache
docker-compose -f docker-compose.prod.yml up -d

# 7. Verify
docker ps
docker logs taskanalysis-backend-prod --tail 30
curl http://localhost:8080/api/actuator/health
```

### Browser Testing
```
1. Ctrl + Shift + N (InPrivate)
2. https://tasks.gaborsiknet.hu
3. F12 (DevTools Console)
4. Tesztelj!
```

---

## 💡 Best Practices

### ✅ DO (Ajánlott)

1. **Lokálisan tesztelj mindent** mielőtt push-olsz
2. **Commit message-ek legyenek beszédesek** (feat:, fix:, refactor:)
3. **Kis, gyakori commit-ok** (ne egy hatalmas változtatás)
4. **`--no-cache` flag használata** rebuild-nél (biztosítja a friss kódot)
5. **InPrivate/Incognito mód** teszteléshez (cache issues elkerülése)
6. **Backend logs monitoring** deploy után (hibák észrevétele)
7. **Git pull előtt check status** (nincs-e lokális módosítás AWS-en)
8. **Backup készítése** major change előtt (MySQL dump)

### ❌ DON'T (Kerülendő)

1. **Ne módosíts kódot közvetlenül AWS-en** (mindig lokálisan fejlessz)
2. **Ne felejtsd el a `--no-cache` flaget** (régi cached layer használata)
3. **Ne skip-eld a production tesztet** (ne tételezd fel, hogy működik)
4. **Ne push-olj törött kódot** (lokális tesztelés először!)
5. **Ne felejtsd el a cache törlést** böngészőben (friss kód látható legyen)
6. **Ne használd a `docker-compose up -d --build`-et** hashazon re-használja a régi layereket
7. **Ne ignore-old a Docker logs-ot** (értékes debug info)

---

## 📅 Deployment Schedule Javaslat

### Entwicklung Phase (Development)
- **Frequency**: Amikor kész egy feature/fix
- **Time**: Bármikor (de inkább napközben, hogy tesztelni tudj)
- **Rollback plan**: Git revert + rebuild

### Production Phase (Live users)
- **Frequency**: Hetente 1-2x (összegyűjtött változások)
- **Time**: **Kedd vagy Csütörtök délután** (kerüld a Hétfőt és Pénteket!)
- **Best time**: 14:00-16:00 (amikor kevés user van, de te elérhető vagy)
- **Rollback plan**: Előző working commit + gyors rebuild

### Emergency Hotfix
- **Time**: Azonnal (critical bug)
- **Process**: Gyors fix → teszt → deploy
- **Notification**: Értesítsd a usereket (ha vannak)

---

## 🎯 Összefoglalás

**Teljes deployment 4 lépésben:**

```
1. LOKÁL → npm start / mvn spring-boot:run → TESZT
2. GIT   → git add . → commit → push origin main
3. AWS   → ssh → git pull → docker build --no-cache → up -d
4. TESZT → InPrivate mode → https://tasks.gaborsiknet.hu → TESZTELÉS
```

**Időigény:**
- Lokális fejlesztés: változó
- Git workflow: 1-2 perc
- AWS deployment: 5-10 perc (rebuild idő)
- Production teszt: 5-10 perc
- **Teljes deployment: ~15-20 perc**

---

## 🆘 Ha elakadtál

1. **Ellenőrizd a Docker logs-ot** (container name + `--tail 50`)
2. **Nézd meg a git status-t** (AWS-en és lokálisan)
3. **Próbálj full rebuild-et** (down + rmi + build --no-cache + up)
4. **Cache törlés** böngészőben (Ctrl+Shift+Delete)
5. **Restart Docker daemon** (`sudo systemctl restart docker`) (last resort)

**Ha továbbra is probléma van, dokumentáld:**
- Mi a hiba? (screenshot, console log)
- Melyik lépésnél történt?
- Mi volt az utolsó sikeres állapot?

---

## 📚 További Dokumentációk

- [AWS_DEPLOYMENT_GUIDE.md](AWS_DEPLOYMENT_GUIDE.md) - Teljes AWS setup
- [TESTING_GUIDE.md](../TESTING_GUIDE.md) - Tesztelési stratégiák
- [README.md](../README.md) - Project overview

---

## ✅ KÉSZ!

Most már tudsz **magabiztosan deployolni** lokális fejlesztésből production-be! 

**Sok sikert a fejlesztéshez!** 🚀💪
