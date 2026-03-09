package com.spunit.performance.controller;

import com.spunit.performance.model.PerformanceMetric;
import com.spunit.performance.service.PerformanceMonitoringService;
import com.spunit.performance.service.RateLimitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performance Dashboard Controller for real-time performance metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Tag(name = "Performance Dashboard", description = "Real-time performance monitoring and metrics")
public class PerformanceDashboardController {

    private final PerformanceMonitoringService performanceMonitoringService;
    private final RateLimitingService rateLimitingService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get performance dashboard data")
    public ResponseEntity<PerformanceMonitoringService.DashboardData> getDashboard() {
        log.info("Fetching performance dashboard data");
        return ResponseEntity.ok(performanceMonitoringService.getDashboardData());
    }

    @GetMapping("/metrics/summary")
    @Operation(summary = "Get metrics summary")
    public ResponseEntity<PerformanceMonitoringService.MetricsSummary> getMetricsSummary() {
        log.info("Fetching metrics summary");
        return ResponseEntity.ok(performanceMonitoringService.getMetricsSummary());
    }

    @GetMapping("/metrics/{type}")
    @Operation(summary = "Get metrics by type")
    public ResponseEntity<List<PerformanceMetric>> getMetricsByType(@PathVariable String type) {
        log.info("Fetching metrics for type: {}", type);
        return ResponseEntity.ok(performanceMonitoringService.getMetricsByType(type));
    }

    @GetMapping("/metrics/average/{type}")
    @Operation(summary = "Get average metric value by type")
    public ResponseEntity<Map<String, Object>> getAverageValue(@PathVariable String type) {
        log.info("Fetching average value for type: {}", type);
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("averageValue", performanceMonitoringService.getAverageValue(type));
        response.put("unit", "ms");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/slow-queries")
    @Operation(summary = "Get slow queries")
    public ResponseEntity<List<PerformanceMetric>> getSlowQueries(
            @RequestParam(defaultValue = "1000") double threshold) {
        log.info("Fetching slow queries with threshold: {} ms", threshold);
        return ResponseEntity.ok(performanceMonitoringService.getSlowQueries(threshold));
    }

    @GetMapping("/metrics/slow-requests")
    @Operation(summary = "Get slow requests")
    public ResponseEntity<List<PerformanceMetric>> getSlowRequests(
            @RequestParam(defaultValue = "3000") double threshold) {
        log.info("Fetching slow requests with threshold: {} ms", threshold);
        return ResponseEntity.ok(performanceMonitoringService.getSlowRequests(threshold));
    }

    @GetMapping("/rate-limit/status")
    @Operation(summary = "Get rate limit status for user")
    public ResponseEntity<RateLimitingService.RateLimitStatus> getRateLimitStatus(
            @RequestParam String userId) {
        log.info("Fetching rate limit status for user: {}", userId);
        return ResponseEntity.ok(rateLimitingService.getStatus(userId));
    }

    @PostMapping("/rate-limit/reset")
    @Operation(summary = "Reset rate limit for user")
    public ResponseEntity<String> resetRateLimit(@RequestParam String userId) {
        log.info("Resetting rate limit for user: {}", userId);
        rateLimitingService.resetUserLimit(userId);
        return ResponseEntity.ok("Rate limit reset for user: " + userId);
    }

    @DeleteMapping("/metrics/clear")
    @Operation(summary = "Clear old metrics")
    public ResponseEntity<String> clearOldMetrics(
            @RequestParam(defaultValue = "24") int hours) {
        log.info("Clearing metrics older than {} hours", hours);
        performanceMonitoringService.clearOldMetrics(hours);
        return ResponseEntity.ok("Cleared metrics older than " + hours + " hours");
    }

    @GetMapping("/health")
    @Operation(summary = "Performance health check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        PerformanceMonitoringService.MetricsSummary summary =
                performanceMonitoringService.getMetricsSummary();

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", summary.getTimestamp());
        health.put("averageRequestTime", summary.getAverageRequestTime());
        health.put("averageQueryTime", summary.getAverageQueryTime());
        health.put("cacheHitRate", summary.getCacheHitRate());

        // Determine health status
        boolean healthy = summary.getAverageRequestTime() < 3000 &&
                         summary.getAverageQueryTime() < 1000;
        health.put("status", healthy ? "HEALTHY" : "DEGRADED");

        return ResponseEntity.ok(health);
    }
}

