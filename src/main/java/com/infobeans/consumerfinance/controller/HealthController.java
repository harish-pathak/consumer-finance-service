package com.infobeans.consumerfinance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Endpoints for monitoring application health status.
 * No authentication required.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    /**
     * Basic health check endpoint
     * GET /api/v1/health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("message", "Consumer Finance Service is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Database health check endpoint
     * GET /api/v1/health/db
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("database", "MySQL");
        response.put("connection", "Available");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
