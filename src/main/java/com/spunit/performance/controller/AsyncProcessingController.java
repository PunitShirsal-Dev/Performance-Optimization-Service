package com.spunit.performance.controller;

import com.spunit.performance.service.AsyncProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Async Processing Controller for non-blocking operations
 */
@Slf4j
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
@Tag(name = "Async Processing", description = "Non-blocking async operations with CompletableFuture")
public class AsyncProcessingController {

    private final AsyncProcessingService asyncProcessingService;

    @PostMapping("/process")
    @Operation(summary = "Execute async task")
    public CompletableFuture<ResponseEntity<String>> processAsync(@RequestBody String data) {
        log.info("Received async processing request");

        return asyncProcessingService.executeAsync(() -> {
            // Simulate processing
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Processed: " + data;
        }).thenApply(ResponseEntity::ok);
    }

    @PostMapping("/process/long-running")
    @Operation(summary = "Execute long-running async task")
    public CompletableFuture<ResponseEntity<String>> processLongRunning(@RequestBody String data) {
        log.info("Received long-running async processing request");

        return asyncProcessingService.executeLongRunningTask(() -> {
            // Simulate long processing
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Long processing completed: " + data;
        }).thenApply(ResponseEntity::ok);
    }

    @PostMapping("/process/batch")
    @Operation(summary = "Process data batch asynchronously")
    public CompletableFuture<ResponseEntity<Integer>> processBatch(@RequestBody List<String> data) {
        log.info("Received batch processing request with {} items", data.size());

        return asyncProcessingService.processBatch(data)
                .thenApply(count -> ResponseEntity.ok(count));
    }

    @PostMapping("/process/chain")
    @Operation(summary = "Chain multiple async operations")
    public CompletableFuture<ResponseEntity<String>> chainOperations(@RequestBody String input) {
        log.info("Received chained async processing request");

        return asyncProcessingService.chainOperations(input)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/process/timeout")
    @Operation(summary = "Execute async task with timeout")
    public CompletableFuture<ResponseEntity<String>> processWithTimeout(
            @RequestBody String data,
            @RequestParam(defaultValue = "3000") long timeoutMs) {
        log.info("Received async processing request with timeout: {} ms", timeoutMs);

        return asyncProcessingService.executeWithTimeout(() -> {
            // Simulate processing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Processed: " + data;
        }, timeoutMs).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/status")
    @Operation(summary = "Get async processing status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Async processing service is running");
    }
}

