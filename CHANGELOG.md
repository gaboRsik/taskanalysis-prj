# Changelog

All notable changes to the Task Analysis project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-03-01

### Added - Security Hardening Phase 🔒

#### Security Features
- **Rate Limiting** using Bucket4j
  - 100 requests/minute for general API endpoints
  - 5 requests/minute for authentication endpoints
  - IP-based tracking with X-Forwarded-For support
  - Automatic cache cleanup after 1 hour inactivity
  
- **Account Lockout Mechanism**
  - Maximum 5 failed login attempts
  - 15-minute lockout period after max attempts
  - Email-based tracking
  - Automatic unlock after timeout
  - Clear feedback with remaining attempts counter
  
- **Password Policy Enforcement**
  - Custom `@StrongPassword` validation annotation
  - Minimum 8 characters
  - Requires uppercase, lowercase, digit, and special character
  - Applied to user registration
  
- **Global Exception Handler**
  - Standardized JSON error responses
  - Field-level validation errors
  - Secure error messages (no sensitive data leakage)
  - Debug mode for development
  - Proper HTTP status codes
  
- **Structured Logging**
  - JSON-formatted logs using Logstash encoder
  - Request ID tracking (MDC)
  - IP address tracking
  - Separate log files:
    - `taskanalysis.log` - general logs
    - `taskanalysis-error.log` - errors only
    - `security-audit.log` - security events
  - Rolling file policy (10MB/file, 30 days retention)
  - Request/response logging interceptor
  
- **Enhanced Input Validation**
  - Task description max length: 5000 characters
  - Subtask count max: 100 (DOS protection)
  - Category name validation
  - Comprehensive field validation

#### Custom Exceptions
- `RateLimitExceededException` - for rate limit violations
- `AccountLockedException` - for locked accounts
- `ResourceNotFoundException` - for 404 errors
- `AccessDeniedException` - for authorization failures
- `BusinessException` - for business logic violations

#### Testing
- **Unit Tests** (29 tests total)
  - `StrongPasswordValidatorTest` - 11 tests
  - `LoginAttemptServiceTest` - 9 tests
  - `RateLimitServiceTest` - 9 tests
- Test documentation in `backend/src/test/java/README.md`

#### Documentation
- **Security Features Documentation** - `docs/SECURITY_FEATURES.md`
  - Complete API error response formats
  - Testing examples
  - Configuration guide
  - Migration notes
  - Frontend integration examples
- **Testing Guide** - `backend/src/test/java/README.md`
- Updated main README with security overview

#### Dependencies
- `bucket4j-core` 8.1.0 - Rate limiting
- `caffeine` - In-memory cache
- `logstash-logback-encoder` 7.4 - Structured logging

### Changed
- `RegisterRequest` - Updated password validation from `@Size(min=6)` to `@StrongPassword`
- `AuthService` - Integrated LoginAttemptService for lockout tracking
- `WebConfig` - Added RateLimitInterceptor and RequestLoggingInterceptor
- Error responses now follow standardized format across all endpoints

### Security
- Brute force attack protection via rate limiting
- Account lockout after repeated failed logins
- Enhanced password security requirements
- Comprehensive security event logging
- Protected against DOS attacks via input limits

---

## [1.0.0] - 2026-02-18

### Added - Initial Release

#### Backend Features
- Spring Boot 3.2.2 application setup
- MySQL database integration
- Flyway database migrations
- JWT authentication (Access + Refresh tokens)
- User registration and login
- Task management CRUD operations
- Subtask management
- Category management
- Timer functionality (start/stop)
- Time entry tracking
- Excel export functionality (email + download)
- Email service integration

#### Frontend Features
- Angular Standalone Components architecture
- User authentication (login/register)
- Dashboard with analytics
- Task management UI
- Category management
- Timer controls
- Export functionality (adaptive mobile/desktop)

#### Infrastructure
- Docker support (development)
- Docker Compose setup
- Production Docker configuration
- AWS deployment documentation
- Nginx configuration for frontend

#### Documentation
- Product Requirements Document (PRD)
- Architecture Documentation
- Export Feature Documentation
- AWS Deployment Guide
- Testing Guide

---

## [Unreleased]

### Planned Features
- Category Analytics Dashboard
- Task Template System
- AI-Powered Insights (OpenAI integration)
- PDF Export
- Weekly Report Emails
- Performance Optimizations
- Integration Tests
- E2E Tests

---

## Version History Summary

- **v1.1.0** (2026-03-01) - Security Hardening Phase ✅
- **v1.0.0** (2026-02-18) - Initial Release ✅

---

## Breaking Changes

### v1.1.0
- **Password Requirements**: New users must provide passwords with minimum 8 characters including uppercase, lowercase, digit, and special character
- **Error Response Format**: All error responses now follow standardized JSON structure
  ```json
  {
    "timestamp": "...",
    "status": 400,
    "error": "Error Type",
    "message": "User-friendly message",
    "path": "/api/...",
    "validationErrors": { ... }
  }
  ```
- **Rate Limiting**: All API endpoints are now rate-limited (100/min general, 5/min auth)

### Migration Guide v1.0.0 → v1.1.0

**Backend:**
- No database migration needed
- Existing passwords are NOT affected
- New registrations require strong passwords

**Frontend:**
```typescript
// Update error handling to use new format
.catch(error => {
  const errorResponse = error.response.data;
  
  // Handle validation errors
  if (errorResponse.validationErrors) {
    // Display field-level errors
  }
  
  // Handle rate limiting
  if (errorResponse.status === 429) {
    // Show retry message with retryAfter seconds
  }
  
  // General message
  showMessage(errorResponse.message);
});
```

---

**Maintained by:** BMad  
**Project:** Task Analysis  
**Repository:** taskanalysis-prj
