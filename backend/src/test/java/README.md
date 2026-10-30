# Security Features Tests

## Overview

Ez a package tartalmazza a biztonsági funkciók unit tesztjeit.

## Test Coverage

### 1. StrongPasswordValidatorTest
**Tesztelt osztály:** `StrongPasswordValidator`

**Test Cases:**
- ✅ Valid passwords (különböző formátumok)
- ✅ Null/empty jelszavak
- ✅ Túl rövid jelszavak (< 8 karakter)
- ✅ Hiányzó nagybetű
- ✅ Hiányzó kisbetű
- ✅ Hiányzó szám
- ✅ Hiányzó speciális karakter
- ✅ Minimum hossz validáció
- ✅ Összes speciális karakter elfogadása
- ✅ Real-world jelszó példák

**Test Count:** 11 test methods

### 2. LoginAttemptServiceTest
**Tesztelt osztály:** `LoginAttemptService`

**Test Cases:**
- ✅ Kezdeti állapot (nincs lockout)
- ✅ Failed attempts számláló növekedés
- ✅ Account lockout 5 sikertelen login után
- ✅ Sikeres login törli a failed attempts-et
- ✅ Sikeres login feloldja a lockout-ot
- ✅ Különböző email-ek független számlálói
- ✅ Lockout idő ellenőrzés (15 perc)
- ✅ Remaining attempts számítás
- ✅ Lockout folyamat teljes tesztelése

**Test Count:** 9 test methods

### 3. RateLimitServiceTest
**Tesztelt osztály:** `RateLimitService`

**Test Cases:**
- ✅ Első request engedélyezve
- ✅ Több request limit alatt
- ✅ Auth endpoint szigorúbb limitje (5/perc)
- ✅ Különböző IP-k független limitjei
- ✅ Null/empty key kezelés
- ✅ Regular és auth limitek függetlensége
- ✅ Seconds until refill számítás
- ✅ Pontosan 100 request limit
- ✅ Pontosan 5 auth request limit
- ✅ Auth kulcs refill ideje az auth bucketből
- ✅ Jelszó-visszaállítási limit (5/15 perc)
- ✅ Jelszó-visszaállítási és auth limitek függetlensége
- ✅ Jelszó-visszaállítási limit null/empty key kezelése

**Test Count:** 14 test methods

### 4. ClientIpResolverTest
**Tesztelt osztály:** `ClientIpResolver`

**Test Cases:**
- ✅ `X-Real-IP` elsőbbsége
- ✅ Hiányzó vagy üres `X-Real-IP` esetén a kapcsolat címe
- ✅ `X-Forwarded-For` figyelmen kívül hagyása

**Test Count:** 4 test methods

### 5. RateLimitInterceptorTest
**Tesztelt osztály:** `RateLimitInterceptor`

**Test Cases:**
- ✅ Az általános limit magyar üzenete és a várakozási idő

**Test Count:** 1 test method

### 6. RequestLoggingInterceptorTest
**Tesztelt osztály:** `RequestLoggingInterceptor`

**Test Cases:**
- ✅ Hamis `X-Forwarded-For` nem kerül a naplózott IP-be

**Test Count:** 1 test method

### 7. PathPatternSecurityTest
**Tesztelt osztályok:** `SecurityConfig`, `RateLimitInterceptor`, `AuthController` (`@WebMvcTest`)

**Test Cases:**
- ✅ Auth 429 pozitív `Retry-After` értékkel és magyar üzenettel
- ✅ A 6. forgot-password kérés 429, a reset-password közös kerettel
- ✅ A jelszó-visszaállítási keret kimerülése nem blokkolja a bejelentkezést

**Test Count:** 8 test methods

## Running Tests

### IntelliJ IDEA
1. Right-click a test class-ra
2. "Run 'TestClassName'"

### Maven Command Line
```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=StrongPasswordValidatorTest

# Run all security tests
mvn test -Dtest=com.taskanalysis.security.*Test

# Skip tests during build
mvn clean install -DskipTests
```

### PowerShell (Windows)
```powershell
cd backend
mvn test
```

## Test Results Example

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.taskanalysis.validation.StrongPasswordValidatorTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.taskanalysis.security.LoginAttemptServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.taskanalysis.security.RateLimitServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
```

## Test Dependencies

```xml
<!-- Already included in pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Includes:
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Spring Test

## Writing New Tests

### Template
```java
package com.taskanalysis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyServiceTest {

    private MyService service;

    @BeforeEach
    void setUp() {
        service = new MyService();
    }

    @Test
    @DisplayName("Description of what this test does")
    void testSomething() {
        // Arrange
        String input = "test";
        
        // Act
        boolean result = service.doSomething(input);
        
        // Assert
        assertTrue(result);
    }
}
```

## Best Practices

1. **Use @DisplayName** - Magyarázza mi a test célja
2. **Arrange-Act-Assert pattern** - Tiszta test struktúra
3. **One assertion per concept** - Ne kevert tesztek
4. **Meaningful test names** - testMethodName() helyett testWhatItDoes()
5. **Test edge cases** - null, empty, boundary values

## Coverage Goals

- **Unit Tests:** 80%+ code coverage
- **Critical Security Features:** 100% coverage
- **Business Logic:** 90%+ coverage

## CI/CD Integration

Tests futnak automatikusan:
- Local build során (`mvn clean install`)
- Pull request során (GitHub Actions)
- Production deployment előtt

## Troubleshooting

### Test fails: "Bucket cannot be resolved"
```bash
# Maven dependency letöltése
cd backend
mvn clean install
```

### Test fails: Timeout
```bash
# Növeld a timeout-ot
@Test(timeout = 5000) // 5 seconds
```

### Mock issues
```java
// Mockito setup
@BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
}
```

## Future Tests

Planned test additions:
- Integration tests (TestContainers + MySQL)
- Controller tests (MockMvc)
- Security integration tests
- Performance tests (JMeter/Gatling)

---

**Last Updated:** 2026-03-01
