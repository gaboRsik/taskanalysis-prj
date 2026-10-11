package com.taskanalysis.security;

import com.taskanalysis.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

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
        // Path without the servlet context path (/api)
        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        String clientIp = getClientIP(request);

        // Apply stricter rate limiting for auth endpoints
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
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
     * Uses X-Real-IP (set by Nginx to $remote_addr), falling back to the connection address.
     * X-Forwarded-For is ignored because its first entry is client-controlled and spoofable.
     */
    private String getClientIP(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
