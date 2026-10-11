---
paths:
  - "docker-compose*.yml"
  - "**/Dockerfile"
  - "frontend/nginx.conf"
  - ".env.example"
  - ".github/**/*"
---

# Telepítési szabályok

- Az éles környezet a `docker-compose.prod.yml` alapján fut egy kevés memóriájú AWS t3.micro gépen. Minden konténernek van memórialimitje. Limitet emelni vagy új konténert felvenni csak kifejezett jelzéssel szabad.
- Az Nginx (`frontend/nginx.conf`) Let's Encrypt tanúsítvánnyal végzi a TLS-t a `tasks.gaborsiknet.hu` domainen, és az `/api/` kéréseket a `backend:8080`-ra továbbítja. A tanúsítvány útvonalai és az `/api/` proxy maradjon változatlan.
- A backend healthcheck a `http://localhost:8080/api/actuator/health` címet hívja. Ennek hitelesítés nélkül elérhetőnek kell maradnia.
- A konfiguráció a `.env`-ből jön. Új változó a `.env.example`-be kerül helyőrző értékkel, soha nem valódi titokkal.
- A telepítés lépései a `docs/DEPLOYMENT_WORKFLOW.md`-ben és a `docs/AWS_DEPLOYMENT_GUIDE.md`-ben vannak. Ha a folyamat változik, ezeket is frissítsd.
