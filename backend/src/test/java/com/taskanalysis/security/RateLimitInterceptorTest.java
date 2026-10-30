package com.taskanalysis.security;

import com.taskanalysis.exception.RateLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RateLimitInterceptor
 */
@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RateLimitInterceptor interceptor;

    @Test
    @DisplayName("General rate limit message is Hungarian and carries the retry time")
    void testGeneralLimitMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tasks");
        request.setRemoteAddr("10.0.0.1");
        when(rateLimitService.tryConsume("10.0.0.1")).thenReturn(false);
        when(rateLimitService.getSecondsUntilRefill("10.0.0.1")).thenReturn(30L);

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertEquals("Túl sok kérés. Kis idő múlva újra lehet próbálni.", ex.getMessage());
        assertEquals(30L, ex.getRetryAfterSeconds());
    }
}
