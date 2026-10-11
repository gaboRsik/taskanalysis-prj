# Munkafolyamat szabályok (mindig érvényesek)

## Kommunikáció
- A felhasználóval magyarul kommunikálj: magyarázatok, tervek, összefoglalók.

## Kódmódosítás előtt
- Egy kisebb javításnál nagyobb feladatnál előbb írd le a tervet, és várd meg a jóváhagyást.
- Ha egy funkcióhoz tartozik `docs/*_FEATURE.md` vagy útmutató, módosítás előtt olvasd el.

## Munka közben
- A változtatás maradjon a feladatnál. Ne formázz és ne refaktorálj hozzá nem tartozó kódot.
- Új Maven vagy npm függőséget csak előzetes kérdés után adj hozzá.
- Ne nyúlj a `.bmad/` mappához (tervezési keretrendszer, nem alkalmazáskód), a `backend/logs/` mappához és a generált mappákhoz.
- Titkot soha ne olvass, ne írj ki és ne commitolj. A `.env` csak helyi fájl; hogy milyen változók léteznek, azt a `.env.example`-ből tudod meg.

## Befejezés
- Mielőtt azt mondod, hogy kész vagy, ellenőrizd a munkát: backend változásnál `mvn test`, frontend változásnál `npm run build` (és `npm test`, ha spec fájl változott). Írd le, mit futtattál és mi lett az eredmény.
- Ha a változtatás a `docs/`, a `README.md` vagy a `CHANGELOG.md` által leírt működést érinti, ugyanabban a változtatásban frissítsd azt a fájlt is.

## Git
- Commit üzenet: Conventional Commits formátum, angolul (`feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`, `test: ...`, `chore: ...`). Egy commit egy logikai változtatást tartalmazzon.
- **Nagyobb fejlesztésnél** (új funkció, viselkedést változtató hibajavítás, Flyway migráció, új függőség, vagy 3-nál több fájlt érintő módosítás) a munka végén kérdezés nélkül futtasd végig a `pr` skillt: feature ág, ellenőrzés, commit, push, PR a `main` ágra. A felhasználótól csak azt kérdezd meg, hogy mergeld-e.
- Kisebb változtatásnál (elírás, egy-két soros javítás, dokumentáció) csak kérésre commitolj.
- Soha ne pusholj közvetlenül a `main` ágra, ne használj force pusht, és ne mergelj kifejezett jóváhagyás nélkül.
