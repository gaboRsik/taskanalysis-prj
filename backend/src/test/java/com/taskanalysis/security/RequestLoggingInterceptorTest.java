package com.taskanalysis.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for RequestLoggingInterceptor
 */
class RequestLoggingInterceptorTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Spoofed X-Forwarded-For does not end up in the logged IP")
    void testSpoofedForwardedForIsNotLogged() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tasks");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        request.addHeader("X-Real-IP", "203.0.113.5");

        new RequestLoggingInterceptor().preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals("203.0.113.5", MDC.get("ip"));
    }
}