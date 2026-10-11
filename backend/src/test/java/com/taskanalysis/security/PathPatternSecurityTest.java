package com.taskanalysis.security;

import com.taskanalysis.controller.AuthController;
import com.taskanalysis.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that security matchers and MVC interceptor patterns work with the
 * "/api" servlet context path (patterns must be relative to the context path).
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RateLimitService.class, PathPatternSecurityTest.HealthStubController.class})
class PathPatternSecurityTest {

    private static final String LOGIN_BODY = "{\"email\":\"user@example.com\",\"password\":\"secret\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CurrentUser currentUser;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Stands in for the actuator health endpoint, which @WebMvcTest does not load.
     */
    @RestController
    static class HealthStubController {
        @GetMapping("/actuator/health")
        Map<String, String> health() {
            return Map.of("status", "UP");
        }
    }

    private MockHttpServletRequestBuilder login() {
        return post("/api/auth/login")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY);
    }

    @Test
    @DisplayName("6th auth request within a minute returns 429")
    void authEndpointIsRateLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(login().header("X-Real-IP", "10.0.0.1"))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(login().header("X-Real-IP", "10.0.0.1"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Health endpoint is reachable without authentication")
    void healthEndpointIsPublic() throws Exception {
        int status = mockMvc.perform(get("/api/actuator/health").contextPath("/api"))
                .andReturn().getResponse().getStatus();

        assertNotEquals(401, status, "Health endpoint must not require authentication");
        assertNotEquals(403, status, "Health endpoint must not be forbidden");
    }

    @Test
    @DisplayName("Spoofed X-Forwarded-For does not bypass the auth rate limit")
    void spoofedForwardedForDoesNotBypassLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(login()
                            .header("X-Real-IP", "10.0.0.2")
                            .header("X-Forwarded-For", "1.2.3." + i))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(login()
                        .header("X-Real-IP", "10.0.0.2")
                        .header("X-Forwarded-For", "1.2.3.99"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Without X-Real-IP the remote address is the rate limit key")
    void remoteAddressIsUsedWithoutRealIpHeader() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(login()
                            .header("X-Forwarded-For", "5.6.7." + i)
                            .with(request -> {
                                request.setRemoteAddr("10.0.0.3");
                                return request;
                            }))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(login()
                        .header("X-Forwarded-For", "5.6.7.99")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.3");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());
    }
}