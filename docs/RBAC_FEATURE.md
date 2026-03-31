# Role-Based Access Control (RBAC) Feature

## Áttekintés

Az alkalmazás most már támogatja a szerepkör alapú hozzáférés-kezelést (RBAC). Két szerepkör érhető el:
- **USER**: Alap felhasználói jogosultság (alapértelmezett)
- **ADMIN**: Adminisztrátori jogosultság a felhasználók kezeléséhez

## Backend Implementáció

### 1. Role Enum

**Fájl:** `backend/src/main/java/com/taskanalysis/entity/Role.java`

```java
public enum Role {
    USER,
    ADMIN
}
```

### 2. User Entity Módosítás

**Fájl:** `backend/src/main/java/com/taskanalysis/entity/User.java`

Hozzáadott mező:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Role role = Role.USER;
```

### 3. Database Migráció

**Fájl:** `backend/src/main/resources/db/migration/V6__Add_User_Roles.sql`

```sql
-- Add role column with default USER
ALTER TABLE users
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Create index for faster queries
CREATE INDEX idx_role ON users(role);

-- Set admin account (customize email)
UPDATE users SET role = 'ADMIN' WHERE email = 'gaborsikdv1@gmail.com';
```

**Megjegyzés:** Az első sorban szereplő email címet az ADMIN jogosultságot kapó felhasználó email címére kell cserélni.

### 4. Admin DTOs

#### UserDTO
**Fájl:** `backend/src/main/java/com/taskanalysis/dto/admin/UserDTO.java`

Admin célú felhasználó adatok átvitele:
- id
- email
- name
- role
- createdAt
- updatedAt

#### UpdateRoleRequest
**Fájl:** `backend/src/main/java/com/taskanalysis/dto/admin/UpdateRoleRequest.java`

Szerepkör módosítási kérés DTO:
- role (Role enum, required)

### 5. AdminService

**Fájl:** `backend/src/main/java/com/taskanalysis/service/AdminService.java`

**Metódusok:**
- `getAllUsers()`: Összes felhasználó lekérdezése
- `getUserById(Long userId)`: Egyedi felhasználó lekérdezése ID alapján
- `updateUserRole(Long userId, UpdateRoleRequest request)`: Felhasználó szerepkörének módosítása

### 6. AdminController

**Fájl:** `backend/src/main/java/com/taskanalysis/controller/AdminController.java`

**Endpoints:**
- `GET /api/admin/users` - Összes felhasználó listázása
- `GET /api/admin/users/{userId}` - Egyedi felhasználó lekérdezése
- `PUT /api/admin/users/{userId}/role` - Felhasználó szerepkörének módosítása

**Védelem:** `@PreAuthorize("hasRole('ADMIN')")` - csak ADMIN role-lal érhető el

### 7. Security Módosítások

#### CustomUserDetailsService
**Fájl:** `backend/src/main/java/com/taskanalysis/security/CustomUserDetailsService.java`

**Változás:** A `loadUserByUsername()` metódus most már helyesen adja vissza a felhasználó authorities-ét:

```java
Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
```

Ez biztosítja, hogy a Spring Security `hasRole('ADMIN')` kifejezés megfelelően működjön.

#### AuthResponse & AuthService
**Fájlok:** 
- `backend/src/main/java/com/taskanalysis/dto/auth/AuthResponse.java`
- `backend/src/main/java/com/taskanalysis/service/AuthService.java`

**Változás:** Az `AuthResponse` most már tartalmazza a `role` mezőt, és az `AuthService` `register()` és `login()` metódusai is visszaadják a felhasználó szerepkörét.

## Frontend Implementáció

### 1. Role Model

**Fájl:** `frontend/src/app/models/auth.model.ts`

```typescript
export enum Role {
  USER = 'USER',
  ADMIN = 'ADMIN'
}
```

`User` és `AuthResponse` interfészek kiegészítve `role?: Role` mezővel.

### 2. Admin Model

**Fájl:** `frontend/src/app/models/admin.model.ts`

```typescript
export interface UserDTO {
  id: number;
  email: string;
  name: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateRoleRequest {
  role: string;
}
```

### 3. AdminService

**Fájl:** `frontend/src/app/services/admin.service.ts`

**Metódusok:**
- `getAllUsers()`: Observable<UserDTO[]>
- `getUserById(userId: number)`: Observable<UserDTO>
- `updateUserRole(userId: number, request: UpdateRoleRequest)`: Observable<UserDTO>

### 4. Admin Component

**Fájlok:**
- `frontend/src/app/components/admin/admin.component.ts`
- `frontend/src/app/components/admin/admin.component.html`
- `frontend/src/app/components/admin/admin.component.scss`

**Funkciók:**
- Felhasználók listázása táblázatos formában
- Role badge megjelenítés (ADMIN: piros, USER: kék)
- Szerepkör módosító gombok (Make Admin / Make User)
- Megerősítő dialógus szerepkör változtatás előtt
- Sikeres/hibás művelet visszajelzés

### 5. AdminGuard

**Fájl:** `frontend/src/app/guards/admin.guard.ts`

Route védelem: csak ADMIN szerepkörű felhasználók férhetnek hozzá az `/admin` útvonalhoz. Nem ADMIN felhasználók átirányítása a `/dashboard`-ra.

### 6. Routes Konfiguráció

**Fájl:** `frontend/src/app/app.routes.ts`

Új route:
```typescript
{ 
  path: 'admin', 
  component: AdminComponent, 
  canActivate: [authGuard, AdminGuard] 
}
```

### 7. Navbar Módosítás

**Fájlok:**
- `frontend/src/app/components/navbar/navbar.component.ts`
- `frontend/src/app/components/navbar/navbar.component.html`

**Változások:**
- Új `isAdmin()` metódus
- "Admin" menüpont hozzáadva, csak ADMIN felhasználóknak látható (`*ngIf="isAdmin()"`)

### 8. AuthService Módosítás

**Fájl:** `frontend/src/app/services/auth.service.ts`

A `handleAuthSuccess()` metódus most már kezeli a `role` mezőt:
```typescript
const user: User = {
  id: response.id,
  email: response.email,
  name: response.name,
  role: response.role
};
```

## Használat

### Admin Szerepkör Beállítása

1. **Első telepítéskor:** A V6 migráció automatikusan beállítja az ADMIN szerepkört a megadott email címhez.

2. **Kézi beállítás:** SQL paranccsal:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
   ```

3. **Admin felületen keresztül:** Egy meglévő ADMIN felhasználó megváltoztathatja más felhasználók szerepkörét.

### Admin Felület Elérése

1. Jelentkezz be ADMIN jogosultságú felhasználóval
2. A navbar-ban megjelenik az "Admin" menüpont
3. Kattints az "Admin" menüpontra
4. Látható lesz a felhasználók listája és elérhető a szerepkör módosítása

### Szerepkör Módosítása

1. Az admin felületen keresd meg a módosítani kívánt felhasználót
2. Kattints a "Make Admin" vagy "Make User" gombra
3. Erősítsd meg a műveletet
4. Sikeres művelet esetén a lista automatikusan frissül

## Biztonsági Megfontolások

1. **Backend védelem:** `@PreAuthorize("hasRole('ADMIN')")` annotáció biztosítja, hogy csak ADMIN felhasználók férjenek hozzá az admin endpoint-okhoz.

2. **Frontend védelem:** `AdminGuard` megakadályozza, hogy nem ADMIN felhasználók elérjék az admin felületet.

3. **JWT tartalom:** A felhasználó szerepköre benne van a Spring Security context-ben, így minden kérés hitelesítve van.

4. **Role prefix:** A Spring Security automatikusan "ROLE_" prefixet vár, amit a `CustomUserDetailsService` biztosít.

## Tesztelés

1. **ADMIN felhasználóként:**
   - Elérhető az Admin menüpont
   - Látható a felhasználók listája
   - Módosítható a felhasználók szerepköre

2. **USER felhasználóként:**
   - Nem látható az Admin menüpont
   - `/admin` URL közvetlen elérése átirányít a dashboard-ra
   - Admin API hívások 403 Forbidden hibát eredményeznek

## Telepítés Során

Amikor új környezetben telepíted az alkalmazást:

1. Állítsd le a backendet
2. Módosítsd a `V6__Add_User_Roles.sql` fájlban az email címet a saját ADMIN felhasználódra
3. Indítsd el a backendet (a Flyway automatikusan futtatja a V6 migrációt)
4. Jelentkezz be az ADMIN email címeddel
5. Most már elérhető az Admin felület

## Hibaelhárítás

### "Access Denied" hiba az admin endpoint-okon

**Ok:** A felhasználó role-ja nincs helyesen beállítva a Spring Security context-ben.

**Megoldás:** Jelentkezz ki és jelentkezz be újra, hogy új JWT token generálódjon.

### Admin menüpont nem látható

**Ok:** A felhasználó role-ja nincs beállítva vagy nem ADMIN.

**Megoldás:** 
1. Ellenőrizd az adatbázisban: `SELECT email, role FROM users;`
2. Állítsd be az ADMIN role-t: `UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';`
3. Jelentkezz ki és be újra

### Flyway checksum mismatch

**Ok:** A V6 migráció már lefutott, de utána módosítottad a fájlt.

**Megoldás:**
```sql
DELETE FROM flyway_schema_history WHERE version = '6';
ALTER TABLE users DROP COLUMN role;
```
Majd indítsd újra a backendet.

## Jövőbeli Fejlesztési Lehetőségek

- További szerepkörök hozzáadása (pl. MODERATOR)
- Részletesebb jogosultságok (permissions)
- Audit log a szerepkör változtatásokhoz
- Bulk szerepkör módosítás
- Felhasználók szűrése szerepkör alapján
- Felhasználók aktív/inaktív státuszának kezelése

## Git Commit

**Commit hash:** `4c40d58`
**Commit üzenet:** "feat: Implement Role-Based Access Control (RBAC) with Admin panel"
**Dátum:** 2026-03-31
**Módosított/új fájlok:** 21
**Változások:** +554 sor kód
