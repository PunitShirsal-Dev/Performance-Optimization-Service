package com.spunit.performance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Performance Metric Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    private String metricName;
    private String metricType; // request, query, cache, async
    private Double value;
    private String unit; // ms, count, percentage
    private LocalDateTime timestamp;
    private String endpoint;
    private String details;
}

