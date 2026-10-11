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
- Csak kérésre commitolj. A commit üzenet Conventional Commits formátumú és angol nyelvű: `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`, `test: ...`, `chore: ...`.
- Egy commit egy logikai változtatást tartalmazzon.
