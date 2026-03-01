package com.taskanalysis.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to track failed login attempts and implement account lockout
 * Uses in-memory cache, suitable for small-scale applications
 * For production with multiple servers, consider Redis
 */
@Service
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    // Track failed attempts count
    private final Cache<String, Integer> attemptsCache;
    
    // Track lockout timestamps
    private final ConcurrentHashMap<String, LocalDateTime> lockoutCache;

    public LoginAttemptService() {
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(LOCKOUT_DURATION_MINUTES))
                .maximumSize(10000)
                .build();
        
        this.lockoutCache = new ConcurrentHashMap<>();
    }

    /**
     * Record a failed login attempt
     * @param key User identifier (email or IP)
     */
    public void loginFailed(String key) {
        Integer attempts = attemptsCache.getIfPresent(key);
        attempts = (attempts == null) ? 1 : attempts + 1;
        attemptsCache.put(key, attempts);
        
        log.warn("Failed login attempt #{} for key: {}", attempts, key);
        
        if (attempts >= MAX_ATTEMPTS) {
            LocalDateTime lockoutUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
            lockoutCache.put(key, lockoutUntil);
            log.warn("Account locked for key: {} until {}", key, lockoutUntil);
        }
    }

    /**
     * Clear failed attempts on successful login
     * @param key User identifier (email or IP)
     */
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
        lockoutCache.remove(key);
        log.info("Successful login, cleared attempts for key: {}", key);
    }

    /**
     * Check if account is currently locked
     * @param key User identifier (email or IP)
     * @return true if locked, false otherwise
     */
    public boolean isLocked(String key) {
        LocalDateTime lockoutUntil = lockoutCache.get(key);
        
        if (lockoutUntil == null) {
            return false;
        }
        
        // Check if lockout period has expired
        if (LocalDateTime.now().isAfter(lockoutUntil)) {
            // Unlock the account
            lockoutCache.remove(key);
            attemptsCache.invalidate(key);
            log.info("Account lockout expired for key: {}", key);
            return false;
        }
        
        return true;
    }

    /**
     * Get remaining lockout time in minutes
     * @param key User identifier
     * @return minutes remaining, or 0 if not locked
     */
    public long getRemainingLockoutMinutes(String key) {
        LocalDateTime lockoutUntil = lockoutCache.get(key);
        
        if (lockoutUntil == null || LocalDateTime.now().isAfter(lockoutUntil)) {
            return 0;
        }
        
        return Duration.between(LocalDateTime.now(), lockoutUntil).toMinutes();
    }

    /**
     * Get failed attempts count
     * @param key User identifier
     * @return number of failed attempts
     */
    public int getFailedAttempts(String key) {
        Integer attempts = attemptsCache.getIfPresent(key);
        return attempts != null ? attempts : 0;
    }

    /**
     * Get remaining attempts before lockout
     * @param key User identifier
     * @return remaining attempts
     */
    public int getRemainingAttempts(String key) {
        int failed = getFailedAttempts(key);
        return Math.max(0, MAX_ATTEMPTS - failed);
    }
}
