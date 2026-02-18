# AWS Deployment Útmutató - Task Analysis Project
# Lépésről-lépésre útmutató t3.micro instance-hoz

## 📋 Előkészületek

### Szükséges eszközök:
- ✅ AWS Account (Free Tier eligible)
- ✅ SSH kliens (PowerShell Windows-on)
- ✅ Domain név (opcionális, de ajánlott)

---

## 🚀 FÁZIS 1: AWS EC2 Instance Létrehozása

### 1.1. AWS Console bejelentkezés
1. Lépj be: https://console.aws.amazon.com
2. Válaszd ki a régiót (ajánlott: **eu-central-1** Frankfurt vagy **eu-west-1** Írország)

### 1.2. EC2 Instance indítása

1. **EC2 Dashboard** → "Launch Instance" gomb
2. **Name and tags**:
   - Name: `taskanalysis-production`

3. **Application and OS Images (AMI)**:
   - Válaszd: **Ubuntu Server 22.04 LTS** (Free tier eligible)
   - Architecture: **64-bit (x86)**

4. **Instance type**:
   - Válaszd: **t3.micro** (Free tier: 750 óra/hó az első 12 hónapban)
   - Specs: 2 vCPU, 1 GB RAM
   - ⚠️ Ha később lassú, könnyű upgrade-elni t3.small-ra!

5. **Key pair (login)**:
   - Kattints: "Create new key pair"
   - Name: `taskanalysis-key`
   - Key pair type: RSA
   - Private key format: .pem
   - 💾 **MENTSD EL!** Ez kell az SSH kapcsolathoz!
   - Helyszín: `C:\Users\siklo\.ssh\taskanalysis-key.pem`

6. **Network settings**:
   - Kattints: "Edit"
   - **Firewall (Security Group)**:
     - ✅ Security group name: `taskanalysis-sg`
     - ✅ Description: Task Analysis security group
     - **Inbound rules**:
       ```
       SSH       | TCP | 22   | My IP (a te IP-d)
       HTTP      | TCP | 80   | Anywhere (0.0.0.0/0)
       HTTPS     | TCP | 443  | Anywhere (0.0.0.0/0)
       Custom TCP| TCP | 8080 | My IP (csak teszteléshez)
       ```
   - **Outbound rules**: Leave as default (All traffic)

7. **Configure storage**:
   - Size: **30 GB** (Free tier: 30 GB SSD)
   - Volume type: **gp3** (gyorsabb és olcsóbb mint gp2)
   - Encryption: Default
   - ✅ Delete on termination: Yes

8. **Advanced details** (opcionális, de ajánlott):
   - Scroll down → **User data** (optional):
   ```bash
   #!/bin/bash
   apt-get update
   apt-get upgrade -y
   ```

9. **Review and Launch**:
   - Nézd át a beállításokat
   - Kattints: **Launch Instance** 🚀

### 1.3. Elastic IP allokálása (FONTOS!)

Miért kell? Az instance alapértelmezett IP-címe változik újraindításkor!

1. EC2 Dashboard → **Elastic IPs** (bal menü)
2. **Allocate Elastic IP address**
3. Kattints: **Allocate**
4. Jelöld ki az új Elastic IP-t
5. **Actions** → **Associate Elastic IP address**
6. **Instance**: Válaszd a `taskanalysis-production`-t
7. **Associate** gomb

📝 **Jegyezd fel az Elastic IP címet**: pl. `3.121.XX.XXX`

---

## 🔧 FÁZIS 2: Szerver Beállítása

### 2.1. SSH kapcsolódás

**Windows PowerShell-ben:**

```powershell
# Navigálj a key fájlhoz
cd C:\Users\siklo\.ssh

# Állítsd be a jogosultságokat (ha WSL van):
# wsl chmod 400 taskanalysis-key.pem

# Kapcsolódj SSH-val
ssh -i taskanalysis-key.pem ubuntu@YOUR_ELASTIC_IP
# Példa: ssh -i taskanalysis-key.pem ubuntu@3.121.XX.XXX

# Első kapcsolódásnál: "Are you sure?" → írj be: yes
```

### 2.2. Szerver frissítése

```bash
# Rendszer frissítés
sudo apt update && sudo apt upgrade -y

# Újraindítás (ha kernel update volt)
sudo reboot
# SSH újracsatlakozás 1 perc múlva
```

### 2.3. Docker telepítése

```bash
# Docker hivatalos telepítő script
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Ubuntu user hozzáadása docker csoporthoz
sudo usermod -aG docker ubuntu

# Csoport újratöltése (hogy ne kelljen logout)
newgrp docker

# Ellenőrzés
docker --version
# Kimenet: Docker version 24.x.x

# Docker Compose telepítése
sudo apt install docker-compose -y

# Ellenőrzés
docker-compose --version
# Kimenet: docker-compose version 1.29.x
```

### 2.4. Git telepítése

```bash
sudo apt install git -y
git --version
```

### 2.5. Swap file létrehozása (FONTOS t3.micro-nál!)

```bash
# 2 GB swap file
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Ellenőrzés
free -h
# Swap sorban látnod kell: 2.0Gi

# Állandóvá tétel (újraindítás után is megmarad)
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Swap használat optimalizálása
sudo sysctl vm.swappiness=10
echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
```

### 2.6. Nginx telepítése (reverse proxy)

```bash
sudo apt install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx

# Ellenőrzés
sudo systemctl status nginx
# Active: active (running) - OK!
```

### 2.7. Certbot telepítése (SSL/HTTPS)

```bash
sudo apt install certbot python3-certbot-nginx -y
```

---

## 📦 FÁZIS 3: Alkalmazás Telepítése

### 3.1. GitHub repository klónozása

```bash
cd /home/ubuntu
git clone https://github.com/gaboRsik/taskanalysis-prj.git
cd taskanalysis-prj
```

### 3.2. Environment változók beállítása

```bash
# Másold a példa fájlt .env-be
cp .env.example .env

# Szerkeszd a .env fájlt
nano .env
```

**Töltsd ki a következő értékeket:**

```bash
# Erős jelszavak generálása (példa):
# openssl rand -base64 32

MYSQL_ROOT_PASSWORD=<ERŐS_JELSZÓ_1>
MYSQL_PASSWORD=<ERŐS_JELSZÓ_2>
JWT_SECRET=<openssl rand -base64 64 KIMENET>

# Email beállítások (Gmail példa)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# CORS - cseréld le a domain nevedre!
CORS_ALLOWED_ORIGINS=http://yourdomain.com,https://yourdomain.com
```

**Mentés:**
- `Ctrl + O` (mentés)
- `Enter`
- `Ctrl + X` (kilépés)

**Secure the file:**
```bash
chmod 600 .env
```

### 3.3. Docker image-ek build & indítás

```bash
# Production verzió indítása
docker-compose -f docker-compose.prod.yml up -d --build

# Ez eltarthat 5-10 percig első alkalommal!
# Logs követése:
docker-compose -f docker-compose.prod.yml logs -f

# Várd meg, amíg látod:
# "Started TaskAnalysisApplication"
# Ctrl + C - kilépés a log-ból
```

### 3.4. Ellenőrzés

```bash
# Konténerek státusza
docker ps

# Kimenet (mind RUNNING kell legyen):
# taskanalysis-frontend-prod   Up   0.0.0.0:80->80/tcp
# taskanalysis-backend-prod    Up   0.0.0.0:8080->8080/tcp
# taskanalysis-mysql-prod      Up   0.0.0.0:3306->3306/tcp

# Backend health check
curl http://localhost:8080/api/actuator/health
# Kimenet: {"status":"UP"}

# Frontend check
curl http://localhost
# Kimenet: HTML tartalom

# Memory monitoring
docker stats --no-stream
```

---

## 🌐 FÁZIS 4: Domain Beállítás (opcionális, de ajánlott)

### 4.1. DNS konfiguráció (domain registrár-nál)

Ha van domain neved (pl. `mytaskapp.com`):

1. Lépj be a domain szolgáltatódhoz (GoDaddy, Namecheap, stb.)
2. DNS Management / DNS Settings
3. Add A Records:

```
Type  | Host | Value            | TTL
------|------|------------------|------
A     | @    | YOUR_ELASTIC_IP  | 600
A     | www  | YOUR_ELASTIC_IP  | 600
```

4. Mentés
5. Várj 15-60 percet (DNS propagáció)
6. Ellenőrzés:
   ```bash
   nslookup yourdomain.com
   # Kimenet: YOUR_ELASTIC_IP kell legyen
   ```

### 4.2. Nginx konfiguráció domain-nel

**Létrehozás:**
```bash
sudo nano /etc/nginx/sites-available/taskanalysis
```

**Tartalom (másold be):**
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Frontend (Angular)
    location / {
        proxy_pass http://localhost:80;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
    }
}
```

**⚠️ Cseréld le `yourdomain.com`-ot a saját domain nevedre!**

**Mentés:** `Ctrl+O`, `Enter`, `Ctrl+X`

**Aktiválás:**
```bash
# Symlink létrehozása
sudo ln -s /etc/nginx/sites-available/taskanalysis /etc/nginx/sites-enabled/

# Default site törlése
sudo rm /etc/nginx/sites-enabled/default

# Nginx config teszt
sudo nginx -t
# Kimenet: syntax is ok, test is successful

# Nginx restart
sudo systemctl restart nginx
```

**Teszt:**
```bash
curl http://yourdomain.com
# Működnie kell!
```

### 4.3. SSL/HTTPS beállítása (Let's Encrypt - INGYEN!)

```bash
# Certbot futtatása
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Kérdések:
# Email cím: youremail@gmail.com
# Terms of Service: Y (yes)
# Share email: N (no)

# Certbot automatikusan:
# 1. SSH tanúsítványt generál
# 2. Nginx config frissítése HTTPS-re
# 3. Automatikus renewal beállítása

# Ellenőrzés
sudo certbot certificates

# Tesztelés
curl https://yourdomain.com
# HTTPS működik! 🔒
```

**Auto-renewal teszt:**
```bash
sudo certbot renew --dry-run
# Sikeres: automatikus megújítás működik!
```

---

## ✅ FÁZIS 5: Ellenőrzés és Tesztelés

### 5.1. Teljes teszt

**Nyisd meg böngészőben:**
- `http://yourdomain.com` vagy `http://YOUR_ELASTIC_IP`
- Regisztráció működik? ✅
- Bejelentkezés működik? ✅
- Tasks létrehozás? ✅
- Timer működik? ✅

### 5.2. Monitoring parancsok

```bash
# Konténerek állapota
docker ps

# Memória használat
docker stats

# Backend logs
docker logs taskanalysis-backend-prod -f

# Frontend logs
docker logs taskanalysis-frontend-prod -f

# MySQL logs
docker logs taskanalysis-mysql-prod -f

# Rendszer memória
free -h

# Disk használat
df -h
```

---

## 🔄 FÁZIS 6: Frissítés / Deployment

**Amikor új kódot töltöttél fel GitHub-ra:**

```bash
# SSH-val az AWS szerverre
ssh -i taskanalysis-key.pem ubuntu@YOUR_ELASTIC_IP

cd /home/ubuntu/taskanalysis-prj

# Kód frissítése
git pull origin main

# Újraépítés és indítás
docker-compose -f docker-compose.prod.yml up -d --build

# Logs követése
docker-compose -f docker-compose.prod.yml logs -f

# Kész! 🚀
```

---

## 🛡️ FÁZIS 7: Biztonság és Karbantartás

### 7.1. Firewall beállítás (UFW)

```bash
# UFW engedélyezése
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# Státusz
sudo ufw status
```

### 7.2. Automatikus biztonsági frissítések

```bash
sudo apt install unattended-upgrades -y
sudo dpkg-reconfigure --priority=low unattended-upgrades
# Válaszd: Yes
```

### 7.3. Backup script (opcionális)

```bash
# MySQL backup
docker exec taskanalysis-mysql-prod mysqldump -u root -pYOUR_ROOT_PASSWORD taskanalysis > backup_$(date +%Y%m%d).sql

# Backup feltöltése S3-ba vagy máshova
```

---

## 💰 Költség Monitoring

**AWS Console:**
- Billing Dashboard: https://console.aws.amazon.com/billing/
- Set up Budget Alert (ajánlott): €10/hó limit

**t3.micro Free Tier:**
- ✅ 750 óra/hó = 24/7 futás ingyen 12 hónapig!
- ✅ 30 GB storage ingyen
- ⚠️ 13. hónaptól: ~€7-8/hó

---

## 🆘 Troubleshooting

### Probléma: Out of Memory

```bash
# Swap használat ellenőrzése
free -h

# Ha nincs swap:
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Konténerek újraindítása
docker-compose -f docker-compose.prod.yml restart
```

### Probléma: Lassú művelet

```bash
# Upgrade t3.small-ra (AWS Console):
# 1. Stop instance
# 2. Actions → Instance Settings → Change Instance Type
# 3. Válaszd: t3.small
# 4. Start instance

# Elastic IP automatikusan megmarad!
```

### Probléma: Backend nem indul

```bash
# Logs
docker logs taskanalysis-backend-prod

# Gyakori ok: MySQL nem elérhető
docker logs taskanalysis-mysql-prod

# Újraindítás
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📚 Hasznos parancsok

```bash
# Összes konténer leállítása
docker-compose -f docker-compose.prod.yml down

# Indítás
docker-compose -f docker-compose.prod.yml up -d

# Rebuild
docker-compose -f docker-compose.prod.yml up -d --build

# Logs
docker-compose -f docker-compose.prod.yml logs -f [service-name]

# Shell a konténerben
docker exec -it taskanalysis-backend-prod /bin/sh

# Cleanup (régi image-ek törlése)
docker system prune -a

# Disk space
df -h
du -sh /var/lib/docker
```

---

## ✅ Checklist

Pre-deployment:
- [ ] AWS account létrehozva
- [ ] Domain név megvásárolva (opcionális)
- [ ] GitHub repo up-to-date

EC2 Setup:
- [ ] t3.micro instance elindítva
- [ ] Security group beállítva (22, 80, 443)
- [ ] Elastic IP hozzárendelve
- [ ] SSH kulcs letöltve

Server Configuration:
- [ ] Docker telepítve
- [ ] Docker Compose telepítve
- [ ] Swap file létrehozva (2GB)
- [ ] Nginx telepítve
- [ ] Git telepítve

Application:
- [ ] Repo klónozva
- [ ] .env file kitöltve
- [ ] docker-compose.prod.yml futtatva
- [ ] Konténerek running

Domain & SSL:
- [ ] DNS A record beállítva
- [ ] Nginx site config létrehozva
- [ ] SSL tanúsítvány (Certbot)
- [ ] HTTPS működik

Testing:
- [ ] Frontend elérhető
- [ ] Backend API működik
- [ ] Regisztráció/Login működik
- [ ] Timer funkciók működnek

---

## 🎉 GRATULÁLOK!

Az alkalmazásod most él az AWS-en! 🚀

**Következő lépések:**
- Monitorozd a költségeket (AWS Billing)
- Állíts be alerteket
- Rendszeres backup
- Ha lassú → upgrade t3.small-ra

**Bármi kérdés? Írj nyugodtan!** 💪
