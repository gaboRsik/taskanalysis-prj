package com.taskanalysis.security;

import com.taskanalysis.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to enforce rate limiting on API requests
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        String clientIp = getClientIP(request);
        
        // Apply stricter rate limiting for auth endpoints
        if (requestURI.contains("/api/auth/login") || requestURI.contains("/api/auth/register")) {
            if (!rateLimitService.tryConsumeAuth(clientIp)) {
                long retryAfter = rateLimitService.getSecondsUntilRefill("auth:" + clientIp);
                log.warn("Rate limit exceeded for auth endpoint. IP: {}, URI: {}", clientIp, requestURI);
                throw new RateLimitExceededException(
                    "Too many authentication attempts. Please try again later.",
                    retryAfter
                );
            }
        } else {
            // General API rate limiting
            if (!rateLimitService.tryConsume(clientIp)) {
                long retryAfter = rateLimitService.getSecondsUntilRefill(clientIp);
                log.warn("Rate limit exceeded. IP: {}, URI: {}", clientIp, requestURI);
                throw new RateLimitExceededException(
                    "Rate limit exceeded. Please slow down your requests.",
                    retryAfter
                );
            }
        }
        
        return true;
    }

    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header (for proxy/load balancer scenarios)
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
