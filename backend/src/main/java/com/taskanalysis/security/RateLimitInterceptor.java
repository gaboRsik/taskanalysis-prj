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
        String clientIp = ClientIpResolver.resolve(request);

        // Apply stricter rate limiting for auth endpoints
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            if (!rateLimitService.tryConsumeAuth(clientIp)) {
                long retryAfter = rateLimitService.getSecondsUntilRefill(RateLimitService.AUTH_KEY_PREFIX + clientIp);
                log.warn("Rate limit exceeded for auth endpoint. IP: {}, URI: {}", clientIp, requestURI);
                throw new RateLimitExceededException(
                    "Túl sok hitelesítési kísérlet. Kis idő múlva újra lehet próbálni.",
                    retryAfter
                );
            }
        } else if (path.equals("/auth/forgot-password") || path.equals("/auth/reset-password")) {
            // Both endpoints share one budget so alternating between them cannot double it
            if (!rateLimitService.tryConsumePasswordReset(clientIp)) {
                long retryAfter = rateLimitService.getSecondsUntilRefill(RateLimitService.PASSWORD_RESET_KEY_PREFIX + clientIp);
                log.warn("Rate limit exceeded for password reset endpoint. IP: {}, URI: {}", clientIp, requestURI);
                throw new RateLimitExceededException(
                    "Túl sok jelszó-visszaállítási kérés. Kis idő múlva újra lehet próbálni.",
                    retryAfter
                );
            }
        } else {
            // General API rate limiting
            if (!rateLimitService.tryConsume(clientIp)) {
                long retryAfter = rateLimitService.getSecondsUntilRefill(clientIp);
                log.warn("Rate limit exceeded. IP: {}, URI: {}", clientIp, requestURI);
                throw new RateLimitExceededException(
                    "Túl sok kérés. Kis idő múlva újra lehet próbálni.",
                    retryAfter
                );
            }
        }
        
        return true;
    }
}
