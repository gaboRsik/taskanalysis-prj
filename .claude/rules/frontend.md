---
paths:
  - "frontend/**/*"
---

# Frontend szabályok (Angular 20, `frontend/src/app`)

## Felhasználói felület nyelve: MAGYAR
- Minden, a felhasználó által látható szöveg magyar: feliratok, gombok, címek, placeholderek, tooltipek, validációs és hibaüzenetek, megerősítő párbeszédek, üres állapotok ("Nincs megjeleníthető feladat"), diagramcímkék.
- Tegező vagy magázó forma helyett semleges, rövid megfogalmazást használj ("Mentés", "Kötelező mező", "A jelszavak nem egyeznek").
- Az új szöveg mindig magyar. Ha egy komponens olyan részét módosítod, amelyben angol szöveg van, azt a részt fordítsd le magyarra. A komponens többi angol szövegét csak külön kérésre fordítsd le.
- A backendtől érkező hibaüzenetet (`error.error?.message`) csak akkor jelenítsd meg közvetlenül, ha magyar. Különben adj magyar alapértelmezett üzenetet.
- Kódazonosítók (változók, metódusok, fájlnevek) és kódkommentek angolul maradnak.
- Dátumokat és számokat magyar formátumban jeleníts meg (pl. `2026. 10. 11.`, `1 234,5`).

## Felépítés
- Csak standalone komponensek, NgModule nélkül. Minden komponens saját mappában van a `components/` alatt, `.ts`, `.html` és `.scss` fájlokkal.
- Az útvonalak az `app.routes.ts`-ben vannak. Az oldalakat a funkcionális `authGuard` védi, az admin oldalakat az `AdminGuard` is.
- Minden backend erőforráshoz egy service tartozik a `services/` mappában, a modellek (interface-ek) a `models/` mappában vannak. Komponens nem hívja közvetlenül a `HttpClient`-et.
- Az URL-eket az `environment.apiUrl`-ből építsd fel. Soha ne írd be fixen a `localhost`-ot vagy az éles domaint.
- Az `auth.interceptor.ts` már csatolja a Bearer tokent és kezeli a 401-et (kijelentkeztetés + átirányítás a `/login` oldalra). Máshol ne kezelj tokent.

## Kódstílus
- Kövesd a környező kód mintáját: konstruktoros injektálás, `subscribe({ next, error })`, valamint `isLoading` / `errorMessage` mezők. Signalokra vagy `inject()`-re csak kérésre térj át.
- Mindent típusozz, új kódban kerüld az `any`-t (használd a `models/` típusait).

## UI és témák
- A UI Bootstrap 5-öt és Bootstrap Icons-t használ, a diagramok ngx-charts-szal készülnek. Ne vezess be Angular Materialt vagy más UI könyvtárat.
- A színek a `src/styles.scss` CSS változóiból jönnek (`:root` és `body.dark-theme`). Komponens SCSS-ben soha ne írj fix színt. Minden UI változtatásnak világos és sötét témában is olvashatónak kell maradnia.

## Ellenőrzés
- Változtatás után futtasd az `npm run build` parancsot a `frontend/` mappában, és javítsd ki az általad okozott hibákat és figyelmeztetéseket.
- Ne szerkessz generált mappát: `dist/`, `.angular/`, `node_modules/`.
