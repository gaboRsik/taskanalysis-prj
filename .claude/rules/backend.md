---
paths:
  - "backend/**/*"
---

# Backend szabályok (Spring Boot, `com.taskanalysis`)

## Rétegek
- Tartsd meg a `controller` → `service` → `repository` → `entity` rétegzést. A controller vékony marad: azonosítja a felhasználót, meghív egy service metódust, és `ResponseEntity`-t ad vissza.
- A request/response DTO-k a `dto/<feature>/` mappában vannak. Controller soha nem ad vissza entitást.
- A bemenetet a `@RequestBody` melletti `@Valid` és a DTO-n lévő Bean Validation annotációk ellenőrzik. Jelszavakra a `@StrongPassword` vonatkozik.

## Felhasználónak szóló üzenetek nyelve: MAGYAR
- Minden üzenet, ami a felületre eljuthat, magyar: kivételüzenetek (`ResourceNotFoundException`, `BusinessException` stb.), validációs üzenetek (`message = "..."`), e-mailek szövege és az export fejlécei. Példa: `"A felhasználó nem található"` a `"User not found"` helyett.
- A logüzenetek, a kódazonosítók és a kommentek angolul maradnak.
- Meglévő angol üzenetet abban a metódusban fordíts le, amelyet egyébként is módosítasz.

## `/api` context path
- `server.servlet.context-path=/api`. A controllerek az előtag NÉLKÜL mappelnek (pl. `@RequestMapping("/tasks")`).
- A Spring Security matcherek, az MVC interceptor minták és a CORS mappingek szintén a context pathhoz képest illeszkednek, ezért bennük soha ne írj `/api/...`-t. A `SecurityConfig` és a `WebConfig` jelenleg `/api/` mintákat tartalmaz: ezeket feltételezett hibának tekintsd, ne mintának.

## Adatbázis (Flyway)
- A sémát a Flyway kezeli: `src/main/resources/db/migration/V<n>__Leiras.sql`, `ddl-auto=none`.
- Minden entitásváltozáshoz új migráció kell a következő szabad verziószámmal (előbb nézd meg a mappát).
- Lefuttatott migrációt soha ne módosíts és ne nevezz át. Élesben `validate-on-migrate=true` fut, a megváltozott checksum miatt az alkalmazás nem indul el.

## Hitelesítés és tulajdonjog
- Állapotmentes JWT (`JwtTokenProvider`, `JwtAuthenticationFilter`). Az `/auth/**` nyilvános, minden más hitelesítést igényel.
- A principal a felhasználó e-mail címe. A controller a `CurrentUser.getEmail()` + `UserRepository.findByEmail` párossal azonosítja a felhasználót, majd a `userId`-t adja át a service-nek.
- A service minden olvasásnál, módosításnál és törlésnél ellenőrzi a tulajdonjogot (`task.getUser().getId()` összevetése a `userId`-val). A hiányzó ellenőrzés biztonsági hiba.
- Admin végpontokon a `@PreAuthorize("hasRole('ADMIN')")` a preferált megoldás (mint az `AdminController`-ben), nem a kézzel írt `isAdmin()` ellenőrzés.

## Hibakezelés
- Az `exception/` mappa saját kivételeit dobd: `ResourceNotFoundException`, `BusinessException`, `AccessDeniedException`, `AccountLockedException`, `RateLimitExceededException`. A `GlobalExceptionHandler` státuszkódra és `ErrorResponse` JSON-ra képezi le őket.
- Ne írj új `throw new RuntimeException(...)` sort. Ha olyan metódushoz nyúlsz, amely még ilyet dob, cseréld a megfelelő saját kivételre.
- Részletes hibaüzenet csak `app.debug=true` esetén megy ki. Stack trace és belső részlet soha nem kerülhet a válaszba.

## Könnyen elrontható üzleti logika
- A feladat állapotátmeneteit kizárólag a `TaskStatusService` kezeli: NOT_STARTED → COMPLETED tilos, és `plannedTotalTimeMinutes` nélkül a feladat nem indítható. A feladat lezárása a részfeladatokat is lezárja, és leállítja a futó időmérőket. Minden állapotváltás ezen a service-en menjen át.
- A `TaskCompletionScheduler` 60 másodpercenként lefut, és automatikusan lezárja azokat az IN_PROGRESS feladatokat, amelyeknél a `TimeEntry`-k összege (a futó időmérőkkel együtt) meghaladja a tervezett időt.
- A futó időmérő egy `TimeEntry` vége / időtartam nélkül. Ezeket a `TimerService` hozza létre és zárja le.
- Részfeladat-címkék: a GLOBAL címkéket admin kezeli, a PERSONAL címkék egy felhasználóhoz tartoznak (lásd `docs/SUBTASK_TAGS_GUIDE.md`).

## Konfiguráció
- Az `application.properties` a fejlesztői, az `application-prod.properties` az éles konfiguráció, amely mindent környezeti változóból olvas. Új beállításhoz mindkettő kell: fejlesztői alapérték és éles környezeti változó (ezt a `.env.example`-be is vedd fel).
- Titkot, API kulcsot és jelszót soha ne írj be a kódba.

## Tesztek
- A tesztek a `src/test/java` alatt vannak, a fő csomagszerkezetet követve. A meglévő tesztek egyszerű JUnit 5 + Mockito unit tesztek.
- Új service logikához unit teszt kell. Egy osztályt így futtatsz: `mvn test -Dtest=ClassName`. Befejezés előtt futtasd a teljes `mvn test`-et.
