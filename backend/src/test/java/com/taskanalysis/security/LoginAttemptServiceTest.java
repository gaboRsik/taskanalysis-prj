package com.taskanalysis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginAttemptService
 */
class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    @DisplayName("Account should not be locked initially")
    void testInitialState() {
        String email = "test@example.com";
        
        assertFalse(service.isLocked(email));
        assertEquals(0, service.getFailedAttempts(email));
        assertEquals(5, service.getRemainingAttempts(email));
    }

    @Test
    @DisplayName("Failed attempts should increment")
    void testFailedAttemptsIncrement() {
        String email = "test@example.com";
        
        service.loginFailed(email);
        assertEquals(1, service.getFailedAttempts(email));
        assertEquals(4, service.getRemainingAttempts(email));
        
        service.loginFailed(email);
        assertEquals(2, service.getFailedAttempts(email));
        assertEquals(3, service.getRemainingAttempts(email));
    }

    @Test
    @DisplayName("Account should lock after 5 failed attempts")
    void testAccountLockAfterMaxAttempts() {
        String email = "test@example.com";
        
        // Try 4 times - should not lock
        for (int i = 0; i < 4; i++) {
            service.loginFailed(email);
            assertFalse(service.isLocked(email), "Should not be locked after " + (i + 1) + " attempts");
        }
        
        // 5th attempt - should lock
        service.loginFailed(email);
        assertTrue(service.isLocked(email), "Should be locked after 5 attempts");
    }

    @Test
    @DisplayName("Successful login should clear failed attempts")
    void testSuccessfulLoginClearsAttempts() {
        String email = "test@example.com";
        
        // Make some failed attempts
        service.loginFailed(email);
        service.loginFailed(email);
        service.loginFailed(email);
        assertEquals(3, service.getFailedAttempts(email));
        
        // Successful login
        service.loginSucceeded(email);
        assertEquals(0, service.getFailedAttempts(email));
        assertEquals(5, service.getRemainingAttempts(email));
        assertFalse(service.isLocked(email));
    }

    @Test
    @DisplayName("Successful login should unlock account")
    void testSuccessfulLoginUnlocksAccount() {
        String email = "test@example.com";
        
        // Lock the account
        for (int i = 0; i < 5; i++) {
            service.loginFailed(email);
        }
        assertTrue(service.isLocked(email));
        
        // Successful login should unlock
        service.loginSucceeded(email);
        assertFalse(service.isLocked(email));
        assertEquals(0, service.getFailedAttempts(email));
    }

    @Test
    @DisplayName("Different emails should have independent counters")
    void testIndependentCounters() {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        
        // User 1 fails 3 times
        for (int i = 0; i < 3; i++) {
            service.loginFailed(email1);
        }
        
        // User 2 fails 1 time
        service.loginFailed(email2);
        
        assertEquals(3, service.getFailedAttempts(email1));
        assertEquals(1, service.getFailedAttempts(email2));
        
        assertEquals(2, service.getRemainingAttempts(email1));
        assertEquals(4, service.getRemainingAttempts(email2));
    }

    @Test
    @DisplayName("Remaining lockout time should be reasonable")
    void testRemainingLockoutTime() {
        String email = "test@example.com";
        
        // Lock the account
        for (int i = 0; i < 5; i++) {
            service.loginFailed(email);
        }
        
        long remainingMinutes = service.getRemainingLockoutMinutes(email);
        
        // Should be approximately 15 minutes (allowing small variance)
        assertTrue(remainingMinutes >= 14 && remainingMinutes <= 15, 
            "Lockout time should be around 15 minutes, got: " + remainingMinutes);
    }

    @Test
    @DisplayName("Remaining attempts calculation is correct")
    void testRemainingAttemptsCalculation() {
        String email = "test@example.com";
        
        assertEquals(5, service.getRemainingAttempts(email));
        
        service.loginFailed(email);
        assertEquals(4, service.getRemainingAttempts(email));
        
        service.loginFailed(email);
        assertEquals(3, service.getRemainingAttempts(email));
        
        service.loginFailed(email);
        assertEquals(2, service.getRemainingAttempts(email));
        
        service.loginFailed(email);
        assertEquals(1, service.getRemainingAttempts(email));
        
        service.loginFailed(email);
        assertEquals(0, service.getRemainingAttempts(email));
    }

    @Test
    @DisplayName("Multiple failed attempts before lockout")
    void testMultipleAttemptsBeforeLockout() {
        String email = "test@example.com";
        
        // Attempt 1
        service.loginFailed(email);
        assertFalse(service.isLocked(email));
        assertEquals(4, service.getRemainingAttempts(email));
        
        // Attempt 2
        service.loginFailed(email);
        assertFalse(service.isLocked(email));
        assertEquals(3, service.getRemainingAttempts(email));
        
        // Attempt 3
        service.loginFailed(email);
        assertFalse(service.isLocked(email));
        assertEquals(2, service.getRemainingAttempts(email));
        
        // Attempt 4
        service.loginFailed(email);
        assertFalse(service.isLocked(email));
        assertEquals(1, service.getRemainingAttempts(email));
        
        // Attempt 5 - should lock
        service.loginFailed(email);
        assertTrue(service.isLocked(email));
        assertEquals(0, service.getRemainingAttempts(email));
    }
}
