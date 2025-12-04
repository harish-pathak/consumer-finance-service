package com.infobeans.consumerfinance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for development and testing purposes.
 * Provides utility endpoints for testing the API.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {

    /**
     * Get test information for API testing.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTestInfo() {
        log.info("Test info requested");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "For testing POST /api/v1/consumers");
        response.put("note", "Configure OAuth2 issuer URI for real JWT validation");
        response.put("test_endpoint", "/api/v1/consumers");
        response.put("method", "POST");
        response.put("auth_required", "Yes - JWT Bearer token");

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for testing.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("message", "Test endpoint is working");
        return ResponseEntity.ok(response);
    }
}
