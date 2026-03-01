package com.taskanalysis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitService
 */
class RateLimitServiceTest {

    private RateLimitService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitService();
    }

    @Test
    @DisplayName("First request should be allowed")
    void testFirstRequestAllowed() {
        String key = "192.168.1.100";
        assertTrue(service.tryConsume(key), "First request should be allowed");
    }

    @Test
    @DisplayName("Multiple requests within limit should be allowed")
    void testMultipleRequestsWithinLimit() {
        String key = "192.168.1.100";
        
        // Try 50 requests - all should succeed (limit is 100)
        for (int i = 0; i < 50; i++) {
            assertTrue(service.tryConsume(key), 
                "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Auth endpoint has stricter limits")
    void testAuthEndpointStricterLimit() {
        String key = "192.168.1.100";
        
        // Auth limit is 5 per minute
        // First 5 should succeed
        for (int i = 0; i < 5; i++) {
            assertTrue(service.tryConsumeAuth(key), 
                "Auth request " + (i + 1) + " should be allowed");
        }
        
        // 6th should fail
        assertFalse(service.tryConsumeAuth(key), 
            "Auth request 6 should be blocked");
    }

    @Test
    @DisplayName("Different IPs should have independent rate limits")
    void testIndependentRateLimits() {
        String ip1 = "192.168.1.100";
        String ip2 = "192.168.1.101";
        
        // Exhaust limit for IP1
        for (int i = 0; i < 100; i++) {
            service.tryConsume(ip1);
        }
        
        // IP2 should still be allowed
        assertTrue(service.tryConsume(ip2), 
            "Different IP should have independent limit");
    }

    @Test
    @DisplayName("Rate limit service handles null keys gracefully")
    void testNullKeyHandling() {
        // This test ensures no NPE is thrown
        assertDoesNotThrow(() -> service.tryConsume(null));
    }

    @Test
    @DisplayName("Rate limit service handles empty keys gracefully")
    void testEmptyKeyHandling() {
        assertDoesNotThrow(() -> service.tryConsume(""));
    }

    @Test
    @DisplayName("Regular and auth limits are independent")
    void testRegularAndAuthLimitsIndependent() {
        String key = "192.168.1.100";
        
        // Consume regular limit
        for (int i = 0; i < 50; i++) {
            service.tryConsume(key);
        }
        
        // Auth limit should still be available
        assertTrue(service.tryConsumeAuth(key), 
            "Auth limit should be independent from regular limit");
    }

    @Test
    @DisplayName("Seconds until refill is reasonable")
    void testSecondsUntilRefill() {
        String key = "192.168.1.100";
        
        // Consume all tokens
        for (int i = 0; i < 100; i++) {
            service.tryConsume(key);
        }
        
        // Should fail now
        assertFalse(service.tryConsume(key));
        
        // Get seconds until refill
        long seconds = service.getSecondsUntilRefill(key);
        
        // Should be somewhere between 0 and 60 seconds
        assertTrue(seconds >= 0 && seconds <= 60, 
            "Seconds until refill should be between 0 and 60, got: " + seconds);
    }

    @Test
    @DisplayName("Rate limit allows exactly the configured amount")
    void testExactlyConfiguredAmount() {
        String key = "192.168.1.100";
        
        // Consume exactly 100 requests
        for (int i = 0; i < 100; i++) {
            assertTrue(service.tryConsume(key), 
                "Request " + (i + 1) + " should be allowed");
        }
        
        // 101st should fail
        assertFalse(service.tryConsume(key), 
            "Request 101 should be blocked");
    }

    @Test
    @DisplayName("Auth rate limit allows exactly 5 requests")
    void testAuthExactlyFiveRequests() {
        String key = "192.168.1.100";
        
        // Consume exactly 5 auth requests
        for (int i = 0; i < 5; i++) {
            assertTrue(service.tryConsumeAuth(key), 
                "Auth request " + (i + 1) + " should be allowed");
        }
        
        // 6th should fail
        assertFalse(service.tryConsumeAuth(key), 
            "Auth request 6 should be blocked");
    }
}
