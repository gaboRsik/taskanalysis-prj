package com.taskanalysis.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate limiting service using Bucket4j and Caffeine cache
 * Implements token bucket algorithm for API rate limiting
 */
@Service
@Slf4j
public class RateLimitService {

    // Cache to store buckets for each IP/user
    private final LoadingCache<String, Bucket> cache;

    public RateLimitService() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(10000) // Max 10k different IPs/users
                .expireAfterAccess(Duration.ofHours(1)) // Clean up after 1 hour of inactivity
                .build(this::createNewBucket);
    }

    /**
     * Create a new bucket for an IP/user
     * Default: 100 requests per minute
     */
    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create a bucket for authentication endpoints (more strict)
     * 5 login attempts per minute
     */
    private Bucket createAuthBucket(String key) {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check if request is allowed for general API endpoints
     * @param key IP address or user identifier
     * @return true if request is allowed, false otherwise
     */
    public boolean tryConsume(String key) {
        Bucket bucket = cache.get(key);
        return bucket.tryConsume(1);
    }

    /**
     * Check if request is allowed for authentication endpoints
     * @param key IP address or user identifier
     * @return true if request is allowed, false otherwise
     */
    public boolean tryConsumeAuth(String key) {
        String authKey = "auth:" + key;
        LoadingCache<String, Bucket> authCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(1))
                .build(this::createAuthBucket);
        
        Bucket bucket = authCache.get(authKey);
        boolean consumed = bucket.tryConsume(1);
        
        if (!consumed) {
            log.warn("Rate limit exceeded for auth endpoint. Key: {}", key);
        }
        
        return consumed;
    }

    /**
     * Get the number of seconds until the bucket is refilled
     * @param key IP address or user identifier
     * @return seconds until next token is available
     */
    public long getSecondsUntilRefill(String key) {
        Bucket bucket = cache.get(key);
        return bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;
    }
}
