package com.spunit.performance.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Performance Monitoring Configuration with Micrometer metrics
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PerformanceMonitoringConfig {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Long> requestTimings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> queryTimings = new ConcurrentHashMap<>();

    @Bean
    public Counter cacheHitCounter() {
        return Counter.builder("cache.hits")
                .description("Number of cache hits")
                .tag("type", "redis")
                .register(meterRegistry);
    }

    @Bean
    public Counter cacheMissCounter() {
        return Counter.builder("cache.misses")
                .description("Number of cache misses")
                .tag("type", "redis")
                .register(meterRegistry);
    }

    @Bean
    public Timer requestTimer() {
        return Timer.builder("http.requests")
                .description("HTTP request processing time")
                .register(meterRegistry);
    }

    @Bean
    public Timer queryTimer() {
        return Timer.builder("database.queries")
                .description("Database query execution time")
                .register(meterRegistry);
    }

    @Bean
    public Counter rateLimitCounter() {
        return Counter.builder("rate.limit.exceeded")
                .description("Number of rate limit exceeded errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter asyncTaskCounter() {
        return Counter.builder("async.tasks")
                .description("Number of async tasks executed")
                .register(meterRegistry);
    }

    /**
     * Record request timing
     */
    public void recordRequestTiming(String endpoint, long durationMs) {
        requestTimings.put(endpoint, durationMs);
        requestTimer().record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Record query timing
     */
    public void recordQueryTiming(String query, long durationMs) {
        queryTimings.put(query, durationMs);
        queryTimer().record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Get average request time
     */
    public double getAverageRequestTime() {
        if (requestTimings.isEmpty()) {
            return 0.0;
        }
        return requestTimings.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Export metrics periodically
     */
    @Scheduled(fixedDelayString = "${performance.monitoring.metrics-export-interval:60000}")
    public void exportMetrics() {
        log.info("Performance Metrics - Avg Request Time: {} ms, Total Requests: {}",
                getAverageRequestTime(), requestTimings.size());

        // Log slow queries
        queryTimings.entrySet().stream()
                .filter(entry -> entry.getValue() > 1000)
                .forEach(entry -> log.warn("Slow query detected: {} - {} ms",
                        entry.getKey(), entry.getValue()));
    }
}

