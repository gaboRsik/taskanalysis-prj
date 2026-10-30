package com.taskanalysis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ClientIpResolver
 */
class ClientIpResolverTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
    }

    @Test
    @DisplayName("X-Real-IP takes precedence over the remote address")
    void testRealIpTakesPrecedence() {
        request.addHeader("X-Real-IP", "203.0.113.5");

        assertEquals("203.0.113.5", ClientIpResolver.resolve(request));
    }

    @Test
    @DisplayName("Missing X-Real-IP falls back to the remote address")
    void testMissingRealIpFallsBack() {
        assertEquals("10.0.0.1", ClientIpResolver.resolve(request));
    }

    @Test
    @DisplayName("Empty X-Real-IP falls back to the remote address")
    void testEmptyRealIpFallsBack() {
        request.addHeader("X-Real-IP", "");

        assertEquals("10.0.0.1", ClientIpResolver.resolve(request));
    }

    @Test
    @DisplayName("X-Forwarded-For is ignored")
    void testForwardedForIsIgnored() {
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertEquals("10.0.0.1", ClientIpResolver.resolve(request));
    }
}
