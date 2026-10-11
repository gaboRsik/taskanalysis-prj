---
name: pr
description: Elkészült fejlesztés lezárása Pull Requesttel - feature ág, ellenőrzés, commit, push, PR létrehozása a main ágra, majd rákérdezés a merge-re. Használd, amikor egy nagyobb fejlesztés (új funkció, viselkedést változtató hibajavítás, migráció, több fájlt érintő módosítás) elkészült, vagy amikor a felhasználó PR-t kér.
argument-hint: "[rövid leírás a változásról]"
---

# Pull Request munkafolyamat

A cél: a felhasználónak csak egyetlen kérdésre kelljen válaszolnia, arra, hogy mergeld-e a PR-t a `main` ágra. Minden más lépést magadtól végezz el. Ha valamelyik lépés hibára fut, állj meg, és jelezd a hibát.

A felhasználó által adott leírás (ha van): $ARGUMENTS

## 1. Ág ellenőrzése
- Futtasd a `git status` és a `git branch --show-current` parancsot.
- Ha a `main` ágon vagy, hozz létre új ágat a friss `main`-ből. A név formátuma `<típus>/<rövid-leírás-kötőjelekkel>`, például `feat/task-export-pdf` vagy `fix/health-endpoint-auth`. A típus a Conventional Commits típusa legyen.
- Ha már egy feature ágon vagy, maradj rajta.
- Ha a változások között a feladathoz nem tartozó módosítás is van, kérdezd meg, mi legyen vele. Ne commitold kérdés nélkül.

## 2. Ellenőrzés
- Backend változásnál futtasd a `mvn test` parancsot a `backend/` mappában.
- Frontend változásnál futtasd az `npm run build` parancsot a `frontend/` mappában.
- Ha bármelyik elbukik, ne menj tovább: javítsd ki, vagy jelezd a felhasználónak.

## 3. Commit
- Logikai egységenként commitolj, Conventional Commits formátumban, angolul (`feat: ...`, `fix: ...`).
- Csak a feladathoz tartozó fájlokat add hozzá, név szerint. Ne használd a `git add -A` parancsot.

## 4. Push és PR
- `git push -u origin <ág>`
- Hozd létre a PR-t a `main` ágra: `gh pr create --base main --title "<conventional commit stílusú cím>" --body-file <ideiglenes fájl>`.
- A PR leírása magyar legyen, ezzel a szerkezettel:

```markdown
## Összefoglaló
Mit és miért változtattunk (1-3 mondat).

## Változások
- ...

## Tesztelés
- Lefuttatott parancsok és eredményük (pl. `mvn test`: 12 teszt, mind sikeres)
- Kézi ellenőrzés lépései, ha szükséges

## Kockázatok / figyelendő
- Pl. új Flyway migráció, új környezeti változó, deploy teendő. Ha nincs ilyen: "Nincs."
```

- Ha a repóban van CI (`.github/workflows/`), futtasd a `gh pr checks` parancsot, és add meg az eredményét.

## 5. Az egyetlen kérdés: merge
Röviden foglald össze magyarul a PR linkjét, a változásokat és a tesztek eredményét, majd kérdezd meg:

> **Mergeljem a PR-t a main ágra?**

- Csak kifejezett igen válasz esetén mergelj: `gh pr merge <szám> --squash --delete-branch`, utána `git switch main` és `git pull`.
- Ha a válasz nem vagy később, hagyd nyitva a PR-t, és mondd meg, hogyan lehet később mergelni.

## Tilos
- Közvetlen push a `main` ágra.
- Force push (`--force`, `-f`).
- Merge kifejezett jóváhagyás nélkül.
