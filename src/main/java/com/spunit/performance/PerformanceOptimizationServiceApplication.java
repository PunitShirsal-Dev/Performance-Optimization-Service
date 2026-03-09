package com.spunit.performance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Performance Optimization Service Application
 * Features: Redis Caching, Query Optimization, Async Processing, Rate Limiting, CDN Integration, Performance Monitoring
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableAsync
@EnableScheduling
public class PerformanceOptimizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceOptimizationServiceApplication.class, args);
    }
}

