package com.spunit.performance.controller;

import com.spunit.performance.model.PerformanceMetric;
import com.spunit.performance.model.Product;
import com.spunit.performance.service.PerformanceMonitoringService;
import com.spunit.performance.service.ProductService;
import com.spunit.performance.service.RateLimitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Controller with performance optimization
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management with caching and optimization")
public class ProductController {

    private final ProductService productService;
    private final RateLimitingService rateLimitingService;
    private final PerformanceMonitoringService performanceMonitoringService;

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<?> getAllProducts(Pageable pageable,
                                           @RequestHeader(value = "X-User-ID", required = false) String userId) {
        long startTime = System.currentTimeMillis();

        // Rate limiting
        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        if (userId != null && !rateLimitingService.isAllowedUser(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("User rate limit exceeded");
        }

        Page<Product> products = productService.getAllProducts(pageable);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("getAllProducts", duration, "/api/products");

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID (cached)")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        Product product = productService.getProductById(id)
                .orElse(null);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("getProductById", duration, "/api/products/{id}");

        return product != null ?
                ResponseEntity.ok(product) :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name (cached)")
    public ResponseEntity<?> searchProducts(@RequestParam String name) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        List<Product> products = productService.searchProducts(name);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("searchProducts", duration, "/api/products/search");

        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category (cached)")
    public ResponseEntity<?> getProductsByCategory(@PathVariable String category, Pageable pageable) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        Page<Product> products = productService.getProductsByCategory(category, pageable);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("getProductsByCategory", duration, "/api/products/category/{category}");

        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range (cached)")
    public ResponseEntity<?> getProductsByPriceRange(@RequestParam Double minPrice,
                                                     @RequestParam Double maxPrice) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("getProductsByPriceRange", duration, "/api/products/price-range");

        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(summary = "Create new product")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        Product created = productService.createProduct(product);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("createProduct", duration, "/api/products");

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        Product updated = productService.updateProduct(id, product);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("updateProduct", duration, "/api/products/{id}");

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();

        if (!rateLimitingService.isAllowedGlobal()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        productService.deleteProduct(id);

        long duration = System.currentTimeMillis() - startTime;
        recordMetric("deleteProduct", duration, "/api/products/{id}");

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cache")
    @Operation(summary = "Clear product cache")
    public ResponseEntity<String> clearCache() {
        productService.clearCache();
        return ResponseEntity.ok("Product cache cleared");
    }

    private void recordMetric(String operation, long duration, String endpoint) {
        PerformanceMetric metric = PerformanceMetric.builder()
                .metricName(operation)
                .metricType("request")
                .value((double) duration)
                .unit("ms")
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .build();
        performanceMonitoringService.recordMetric(metric);
    }
}

