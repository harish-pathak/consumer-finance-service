package com.infobeans.consumerfinance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test API Controller
 * 
 * Simple endpoints to verify the application is running correctly.
 * No authentication required.
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    /**
     * Simple ping endpoint
     * GET /api/v1/test/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Pong!");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Consumer Finance Service");
        response.put("status", "RUNNING");
        return ResponseEntity.ok(response);
    }

    /**
     * Echo endpoint - returns the input message
     * POST /api/v1/test/echo
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        String message = request.getOrDefault("message", "No message provided");
        response.put("echo", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("received", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Info endpoint - returns service information
     * GET /api/v1/test/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("serviceName", "Consumer Finance Service");
        response.put("version", "1.0.0");
        response.put("environment", "Development");
        response.put("apiVersion", "v1");
        response.put("timestamp", LocalDateTime.now());
        response.put("baseUrl", "/api/v1");
        response.put("description", "Spring Boot microservice for consumer lending operations");
        return ResponseEntity.ok(response);
    }

    /**
     * Ready endpoint - indicates application is ready to serve requests
     * GET /api/v1/test/ready
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ready", true);
        response.put("message", "Application is ready to accept requests");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
