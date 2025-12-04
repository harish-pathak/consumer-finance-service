package com.infobeans.consumerfinance.controller;

import com.infobeans.consumerfinance.dto.request.CreateConsumerOnboardingRequest;
import com.infobeans.consumerfinance.dto.response.ApiResponse;
import com.infobeans.consumerfinance.dto.response.ConsumerOnboardingResponse;
import com.infobeans.consumerfinance.service.ConsumerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for consumer onboarding endpoints.
 *
 * Provides secure endpoints for consumer registration and onboarding.
 * All endpoints are authenticated via OAuth2/JWT tokens.
 *
 * Base URI: /api/v1/consumers
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/consumers")
@RequiredArgsConstructor
@Slf4j
public class ConsumerController {

    private final ConsumerService consumerService;

    /**
     * POST /api/v1/consumers - Onboard a new consumer.
     *
     * Accepts consumer onboarding payload with personal, identity, employment, and financial data.
     * Validates request, checks for duplicates, encrypts sensitive fields, and persists consumer.
     *
     * Authentication:
     * Requires valid OAuth2/JWT token with scope 'consumer:write'.
     * Missing or invalid token returns 401 Unauthorized.
     *
     * Request body:
     * CreateConsumerOnboardingRequest with JSR-380 validation annotations.
     * Validation errors return 400 Bad Request with field-level messages.
     *
     * Success response (HTTP 201):
     * {
     *   "status": 201,
     *   "message": "Consumer onboarded successfully",
     *   "data": {
     *     "consumerId": "550e8400-e29b-41d4-a716-446655440000",
     *     "status": "ACTIVE",
     *     "createdAt": "2024-12-03T10:30:00Z",
     *     "message": "Consumer onboarded successfully"
     *   }
     * }
     *
     * Error responses:
     * - 400 Bad Request: Validation errors (field-level messages included)
     * - 401 Unauthorized: Missing or invalid OAuth2/JWT token
     * - 409 Conflict: Duplicate email or national ID
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param request CreateConsumerOnboardingRequest with consumer data (validated)
     * @return ResponseEntity with 201 Created status and ConsumerOnboardingResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConsumerOnboardingResponse>> onboardConsumer(
        @Valid @RequestBody CreateConsumerOnboardingRequest request
    ) {
        log.info("Received consumer onboarding request for email: {}", request.getEmail());

        // Call service to onboard consumer
        ConsumerOnboardingResponse response = consumerService.onboardConsumer(request);

        log.info("Consumer onboarded successfully with ID: {}", response.getConsumerId());

        // Return 201 Created with response wrapped in ApiResponse
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.<ConsumerOnboardingResponse>builder()
                .success(true)
                .message("Consumer onboarded successfully")
                .data(response)
                .build()
            );
    }
}
