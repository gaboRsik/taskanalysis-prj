package com.taskanalysis.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor to log all HTTP requests
 * Adds request context to MDC for structured logging
 */
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        // Add client IP to MDC
        String clientIp = getClientIP(request);
        MDC.put("ip", clientIp);
        
        // Log request details
        log.info("Incoming request: {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                clientIp);
        
        // Store start time for duration calculation
        request.setAttribute("startTime", System.currentTimeMillis());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Calculate request duration
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        // Log response details
        log.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);
        
        // Log error if present
        if (ex != null) {
            log.error("Request failed with exception: {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex);
        }
        
        // Clear MDC
        MDC.clear();
    }

    /**
     * Extract client IP address from request
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
