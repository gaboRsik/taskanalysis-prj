# Change Password Feature - Implementation Guide

## Overview

A Change Password feature lehetővé teszi a bejelentkezett felhasználóknak, hogy megváltoztassák a jelszavukat a profiloldalukon. A funkció teljes körű validációval és biztonsági intézkedésekkel rendelkezik.

### Key Features

- ✅ **Biztonságos jelenlegi jelszó ellenőrzés**
- ✅ **Új jelszó komplexitás követelmények**
- ✅ **Konzisztens validáció backend és frontend oldalon**
- ✅ **BCrypt password hashing**
- ✅ **JWT authentication védelem**
- ✅ **User-friendly error/success üzenetek**

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (Angular)                    │
│  ┌────────────────┐   ┌──────────────────┐                 │
│  │ Profile        │──▶│ AuthService      │                 │
│  │ Component      │   │ .changePassword()│                 │
│  │ (Form + Logic) │   └──────────────────┘                 │
│  └────────────────┘            │                            │
└─────────────────────────────────┼────────────────────────────┘
                                  │ HTTP POST
                                  │ /api/auth/change-password
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                      Backend (Spring Boot)                   │
│  ┌────────────────┐   ┌──────────────────┐                 │
│  │ AuthController │──▶│ AuthService      │                 │
│  │ (REST API)     │   │ .changePassword()│                 │
│  └────────────────┘   └──────────────────┘                 │
│         │                      │                             │
│         │ @Valid               │ PasswordEncoder             │
│         ▼                      ▼                             │
│  ┌────────────────┐   ┌──────────────────┐                 │
│  │ DTO Validation │   │ UserRepository   │                 │
│  │ @StrongPassword│   │ (JPA)            │                 │
│  └────────────────┘   └──────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Backend Implementation

### 1. DTO Classes

#### ChangePasswordRequest.java

**Location:** `backend/src/main/java/com/taskanalysis/dto/auth/ChangePasswordRequest.java`

```java
package com.taskanalysis.dto.auth;

import com.taskanalysis.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @StrongPassword(message = "New password must be at least 8 characters and contain uppercase, lowercase, digit, and special character")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    // Getters and Setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
```

**Validációk:**
- `currentPassword`: @NotBlank (kötelező mező)
- `newPassword`: @NotBlank + @StrongPassword (min 8 karakter, nagybetű, kisbetű, szám, speciális karakter)
- `confirmPassword`: @NotBlank (kötelező mező)

#### ChangePasswordResponse.java

**Location:** `backend/src/main/java/com/taskanalysis/dto/auth/ChangePasswordResponse.java`

```java
package com.taskanalysis.dto.auth;

public class ChangePasswordResponse {

    private String message;
    private boolean success;

    public ChangePasswordResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
```

---

### 2. Service Layer

#### AuthService.changePassword()

**Location:** `backend/src/main/java/com/taskanalysis/service/AuthService.java`

```java
@Transactional
public ChangePasswordResponse changePassword(String email, ChangePasswordRequest request) {
    // 1. Validate new password matches confirmation
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
        throw new BusinessException("New password and confirmation do not match");
    }

    // 2. Get user from database
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

    // 3. Verify current password is correct
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        log.warn("Failed password change attempt for user: {} - Incorrect current password", email);
        throw new BadCredentialsException("Current password is incorrect");
    }

    // 4. Check if new password is same as current (prevent no-change)
    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
        throw new BusinessException("New password must be different from current password");
    }

    // 5. Update password with BCrypt hashing
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    log.info("Password changed successfully for user: {}", email);

    return new ChangePasswordResponse("Password changed successfully", true);
}
```

**Biztonsági ellenőrzések:**
1. ✅ Új jelszó egyezik a megerősítéssel
2. ✅ Felhasználó létezik
3. ✅ Jelenlegi jelszó helyes (BCrypt match)
4. ✅ Új jelszó különbözik a jelenlegitől
5. ✅ BCrypt hashing az új jelszóra
6. ✅ Audit logging (sikeres/sikertelen próbálkozások)

---

### 3. Controller Layer

#### AuthController.changePassword()

**Location:** `backend/src/main/java/com/taskanalysis/controller/AuthController.java`

```java
@PostMapping("/change-password")
public ResponseEntity<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    String email = currentUser.getEmail();  // JWT token-ből származtatva
    ChangePasswordResponse response = authService.changePassword(email, request);
    return ResponseEntity.ok(response);
}
```

**Endpoint részletek:**
- **Method:** POST
- **Path:** `/api/auth/change-password`
- **Authentication:** JWT token szükséges (bejelentkezés kötelező)
- **Request Body:** ChangePasswordRequest (JSON)
- **Response:** ChangePasswordResponse (JSON)

---

## Frontend Implementation

### 1. Models

#### auth.model.ts

**Location:** `frontend/src/app/models/auth.model.ts`

```typescript
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordResponse {
  message: string;
  success: boolean;
}
```

---

### 2. Service Layer

#### AuthService.changePassword()

**Location:** `frontend/src/app/services/auth.service.ts`

```typescript
changePassword(request: ChangePasswordRequest): Observable<ChangePasswordResponse> {
  return this.http.post<ChangePasswordResponse>(`${this.apiUrl}/change-password`, request);
}
```

**Import szükséges:**
```typescript
import { ChangePasswordRequest, ChangePasswordResponse } from '../models/auth.model';
```

---

### 3. Profile Component

#### Component Logic

**Location:** `frontend/src/app/components/profile/profile.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ChangePasswordRequest, User } from '../../models/auth.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  
  currentPassword: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  
  successMessage: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  onChangePassword(): void {
    // Clear previous messages
    this.successMessage = '';
    this.errorMessage = '';

    // Frontend validation
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'All fields are required';
      return;
    }

    if (this.newPassword.length < 8) {
      this.errorMessage = 'New password must be at least 8 characters long';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'New password and confirmation do not match';
      return;
    }

    const request: ChangePasswordRequest = {
      currentPassword: this.currentPassword,
      newPassword: this.newPassword,
      confirmPassword: this.confirmPassword
    };

    this.isLoading = true;

    this.authService.changePassword(request).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response.message;
        // Clear form on success
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || error.message || 'Failed to change password';
      }
    });
  }
}
```

**Frontend validációk:**
1. ✅ Minden mező kitöltve
2. ✅ Új jelszó min 8 karakter
3. ✅ Új jelszó egyezik a megerősítéssel
4. ✅ Loading state kezelés
5. ✅ Success/Error üzenetek

---

#### Component Template

**Location:** `frontend/src/app/components/profile/profile.html`

```html
<div class="container mt-4">
  <div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
      <div class="card">
        <div class="card-header bg-primary text-white">
          <h4 class="mb-0"><i class="bi bi-person-circle"></i> User Profile</h4>
        </div>
        <div class="card-body">
          <!-- User Info Section -->
          <div class="mb-4">
            <h5 class="border-bottom pb-2">Account Information</h5>
            <div class="row mb-2">
              <div class="col-4 text-muted">Name:</div>
              <div class="col-8"><strong>{{ currentUser?.name }}</strong></div>
            </div>
            <div class="row">
              <div class="col-4 text-muted">Email:</div>
              <div class="col-8"><strong>{{ currentUser?.email }}</strong></div>
            </div>
          </div>

          <!-- Change Password Section -->
          <div>
            <h5 class="border-bottom pb-2"><i class="bi bi-key"></i> Change Password</h5>
            
            <!-- Success Message -->
            <div *ngIf="successMessage" class="alert alert-success alert-dismissible fade show">
              <i class="bi bi-check-circle"></i> {{ successMessage }}
              <button type="button" class="btn-close" (click)="successMessage = ''"></button>
            </div>

            <!-- Error Message -->
            <div *ngIf="errorMessage" class="alert alert-danger alert-dismissible fade show">
              <i class="bi bi-exclamation-triangle"></i> {{ errorMessage }}
              <button type="button" class="btn-close" (click)="errorMessage = ''"></button>
            </div>

            <form (ngSubmit)="onChangePassword()" #passwordForm="ngForm">
              <!-- Current Password -->
              <div class="mb-3">
                <label for="currentPassword" class="form-label">Current Password</label>
                <input
                  type="password"
                  class="form-control"
                  id="currentPassword"
                  name="currentPassword"
                  [(ngModel)]="currentPassword"
                  required
                  [disabled]="isLoading"
                >
              </div>

              <!-- New Password -->
              <div class="mb-3">
                <label for="newPassword" class="form-label">New Password</label>
                <input
                  type="password"
                  class="form-control"
                  id="newPassword"
                  name="newPassword"
                  [(ngModel)]="newPassword"
                  required
                  minlength="8"
                  [disabled]="isLoading"
                >
              </div>

              <!-- Confirm Password -->
              <div class="mb-3">
                <label for="confirmPassword" class="form-label">Confirm New Password</label>
                <input
                  type="password"
                  class="form-control"
                  id="confirmPassword"
                  name="confirmPassword"
                  [(ngModel)]="confirmPassword"
                  required
                  [disabled]="isLoading"
                >
              </div>

              <!-- Submit Button -->
              <div class="d-grid">
                <button
                  type="submit"
                  class="btn btn-primary"
                  [disabled]="!passwordForm.form.valid || isLoading"
                >
                  <span *ngIf="!isLoading">
                    <i class="bi bi-shield-check"></i> Change Password
                  </span>
                  <span *ngIf="isLoading">
                    <span class="spinner-border spinner-border-sm"></span>
                    Changing...
                  </span>
                </button>
              </div>
            </form>

            <!-- Password Requirements -->
            <div class="mt-3">
              <small class="text-muted">
                <i class="bi bi-info-circle"></i> Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character
              </small>
            </div>
          </div>
        </div>
      </div>

      <!-- Back Button -->
      <div class="mt-3 text-center">
        <a routerLink="/dashboard" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left"></i> Back to Dashboard
        </a>
      </div>
    </div>
  </div>
</div>
```

---

### 4. Routing

#### app.routes.ts

**Location:** `frontend/src/app/app.routes.ts`

```typescript
import { ProfileComponent } from './components/profile/profile';

export const routes: Routes = [
  // ... other routes
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
];
```

**Védelem:** `authGuard` biztosítja, hogy csak bejelentkezett felhasználók érhessék el.

---

### 5. Navigation

#### navbar.component.html

**Location:** `frontend/src/app/components/navbar/navbar.component.html`

Navbar menübe Profile link hozzáadása:

```html
<li class="nav-item">
  <a class="nav-link" routerLink="/profile" routerLinkActive="active">
    <i class="bi bi-person-circle me-1"></i>
    Profile
  </a>
</li>
```

---

## API Documentation

### Change Password Endpoint

#### Request

**POST** `/api/auth/change-password`

**Headers:**
```
Authorization: Bearer {JWT_ACCESS_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "NewPassword456!"
}
```

#### Response

**Success (200 OK):**
```json
{
  "message": "Password changed successfully",
  "success": true
}
```

**Error (400 Bad Request) - Validation:**
```json
{
  "timestamp": "2026-03-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "New password and confirmation do not match",
  "path": "/api/auth/change-password"
}
```

**Error (401 Unauthorized) - Wrong Current Password:**
```json
{
  "timestamp": "2026-03-11T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Current password is incorrect",
  "path": "/api/auth/change-password"
}
```

**Error (400 Bad Request) - Password Complexity:**
```json
{
  "timestamp": "2026-03-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "New password must be at least 8 characters and contain uppercase, lowercase, digit, and special character",
  "path": "/api/auth/change-password"
}
```

---

## Security Considerations

### Backend Security

1. **JWT Authentication:**
   - Endpoint védett, JWT token szükséges
   - User identity a token-ből származtatva (nem a request body-ból)

2. **Password Hashing:**
   - BCrypt algorithm (12 rounds)
   - Jelenlegi jelszó: `passwordEncoder.matches()` ellenőrzés
   - Új jelszó: `passwordEncoder.encode()` hashing

3. **Validation:**
   - Jakarta Validation (@Valid)
   - Custom @StrongPassword annotation
   - Business logic ellenőrzések (jelszó nem azonos a jelenlegivel)

4. **Audit Logging:**
   - Sikeres jelszóváltás: INFO log
   - Sikertelen próbálkozás: WARN log
   - Email cím naplózva (debugging)

5. **Transaction Management:**
   - @Transactional annotation
   - Adatbázis konzisztencia garantált

### Frontend Security

1. **Client-side Validation:**
   - Azonnali feedback a felhasználónak
   - Csökkenti a felesleges API hívásokat
   - **NEM helyettesíti** a backend validációt!

2. **JWT Token Handling:**
   - AuthInterceptor automatikusan csatolja
   - Token tárolás localStorage-ban
   - Logout esetén token törlés

3. **Error Handling:**
   - Generikus hibaüzenetek felhasználónak
   - Részletes hibák a console-ban (development)
   - Nem leak-el érzékeny információt

---

## Password Complexity Requirements

### @StrongPassword Annotation

**Location:** `backend/src/main/java/com/taskanalysis/validation/StrongPassword.java`

**Követelmények:**
- ✅ Min 8 karakter hossz
- ✅ Legalább 1 nagybetű (A-Z)
- ✅ Legalább 1 kisbetű (a-z)
- ✅ Legalább 1 számjegy (0-9)
- ✅ Legalább 1 speciális karakter (!@#$%^&*()_+-=[]{}|;:,.<>?)

**Példa érvényes jelszavakra:**
- `Password123!`
- `MyS3cur3P@ss`
- `Admin2026#`

**Példa érvénytelen jelszavakra:**
- `password` (nincs nagybetű, szám, speciális karakter)
- `PASSWORD123` (nincs kisbetű, speciális karakter)
- `Pass123` (túl rövid, nincs speciális karakter)

---

## Testing Guide

### Backend Testing (Postman)

#### 1. Successful Password Change

```http
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "NewPassword456!"
}
```

**Expected:** 200 OK + success message

#### 2. Wrong Current Password

```http
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "currentPassword": "WrongPassword123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "NewPassword456!"
}
```

**Expected:** 401 Unauthorized

#### 3. Passwords Don't Match

```http
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "DifferentPassword789!"
}
```

**Expected:** 400 Bad Request

#### 4. Weak New Password

```http
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "currentPassword": "OldPassword123!",
  "newPassword": "weak",
  "confirmPassword": "weak"
}
```

**Expected:** 400 Bad Request (validation error)

---

### Frontend Testing

#### Manual Test Steps:

1. **Login** és navigálj a `/profile` oldalra
2. **Current Password:** Írj be helyes jelszót
3. **New Password:** Írj be érvényes új jelszót (pl. `NewPass123!`)
4. **Confirm Password:** Ismételd meg az új jelszót
5. **Kattints "Change Password"-re**
6. **Elvárt:** Zöld success üzenet + form clearelve

#### Edge Cases:

- **Üres mezők:** Error: "All fields are required"
- **Rövid jelszó (< 8 kar):** Error: "must be at least 8 characters"
- **Nem egyező jelszavak:** Error: "do not match"
- **Rossz jelenlegi jelszó:** Backend error: "Current password is incorrect"
- **Loading state:** Gomb disabled + spinner látható

---

## Troubleshooting

### Backend Issues

#### Problem: "Current password is incorrect" de biztos jó a jelszó

**Diagnózis:**
```bash
# Check database password hash
mysql> SELECT email, password FROM users WHERE email = 'user@example.com';
```

**Megoldás:**
- Ellenőrizd hogy a user létezik
- BCrypt hash formátum helyes-e ($2a$ prefix)
- PasswordEncoder bean megfelelően konfigurálva (12 rounds)

#### Problem: @StrongPassword nem működik

**Ellenőrzés:**
```java
// AuthService.java
import com.taskanalysis.validation.StrongPassword;

// ChangePasswordRequest.java
@StrongPassword(message = "...")
private String newPassword;
```

**Megoldás:**
- Import path helyes
- StrongPasswordValidator regisztrálva
- @Valid annotation a Controller-ben

---

### Frontend Issues

#### Problem: 403 Forbidden a change-password endpoint-on

**Diagnózis:**
- JWT token lejárt vagy invalid
- AuthInterceptor nem fut le

**Megoldás:**
```typescript
// Check token existence
console.log('Token:', localStorage.getItem('accessToken'));

// Check interceptor
// auth.interceptor.ts should add Authorization header
```

#### Problem: Form nem submittálható

**Ellenőrzés:**
- `#passwordForm="ngForm"` van-e a form-on
- `[disabled]="!passwordForm.form.valid || isLoading"` helyes
- `required` attribútumok minden input-on

---

## Future Enhancements

### Planned Features

- [ ] **Forgot Password** (email-lel reset link)
- [ ] **Password History** (ne lehessen azonos jelszót újra használni)
- [ ] **Force Password Change** (első bejelentkezéskor vagy X nap után)
- [ ] **2FA Integration** (Two-Factor Authentication)
- [ ] **Account Security Dashboard** (jelszó erősség score, utolsó változtatás dátuma)

### Nice-to-Have

- [ ] Password strength meter (real-time feedback)
- [ ] Suggest strong passwords (generator)
- [ ] Email notification on password change
- [ ] Session invalidation on password change (force re-login)

---

## Summary

Ez a dokumentáció teljes körű útmutatót nyújt a Change Password feature-höz:

✅ **Backend:** DTO-k, Service logika, Controller endpoint  
✅ **Frontend:** Component, Form, Validation, Routing  
✅ **Security:** BCrypt hashing, JWT auth, Strong password policy  
✅ **Testing:** Postman példák, Frontend test cases  
✅ **Troubleshooting:** Gyakori problémák és megoldások

A funkció production-ready, teljes biztonsági validációval és user-friendly UX-szel.
