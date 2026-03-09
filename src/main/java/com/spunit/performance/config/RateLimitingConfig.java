package com.spunit.performance.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration using Bucket4j
 */
@Slf4j
@Configuration
public class RateLimitingConfig {

    @Value("${rate-limiting.global.capacity:1000}")
    private long globalCapacity;

    @Value("${rate-limiting.api.capacity:100}")
    private long apiCapacity;

    @Value("${rate-limiting.user.capacity:50}")
    private long userCapacity;

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        Map<String, Bucket> buckets = new ConcurrentHashMap<>();

        // Global rate limit bucket
        Bucket globalBucket = Bucket.builder()
                .addLimit(Bandwidth.classic(globalCapacity, Refill.intervally(globalCapacity, Duration.ofMinutes(1))))
                .build();
        buckets.put("global", globalBucket);

        // API rate limit bucket
        Bucket apiBucket = Bucket.builder()
                .addLimit(Bandwidth.classic(apiCapacity, Refill.intervally(apiCapacity, Duration.ofMinutes(1))))
                .build();
        buckets.put("api", apiBucket);

        // User rate limit bucket (default)
        Bucket userBucket = Bucket.builder()
                .addLimit(Bandwidth.classic(userCapacity, Refill.intervally(userCapacity, Duration.ofMinutes(1))))
                .build();
        buckets.put("user", userBucket);

        log.info("Rate limiting buckets initialized - Global: {}, API: {}, User: {}",
                globalCapacity, apiCapacity, userCapacity);

        return buckets;
    }

    /**
     * Create a bucket for a specific user
     */
    public Bucket createUserBucket(String userId) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(userCapacity, Refill.intervally(userCapacity, Duration.ofMinutes(1))))
                .build();
    }

    /**
     * Create a bucket for a specific API key
     */
    public Bucket createApiBucket(String apiKey, long capacity, Duration duration) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, duration)))
                .build();
    }
}

