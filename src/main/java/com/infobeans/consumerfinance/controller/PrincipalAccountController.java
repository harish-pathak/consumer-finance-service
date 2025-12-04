package com.infobeans.consumerfinance.controller;

import com.infobeans.consumerfinance.dto.request.CreatePrincipalAccountRequest;
import com.infobeans.consumerfinance.dto.response.ApiResponse;
import com.infobeans.consumerfinance.dto.response.PrincipalAccountResponse;
import com.infobeans.consumerfinance.service.PrincipalAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for principal account endpoints.
 *
 * Provides secure endpoints for creating and retrieving principal accounts.
 * All endpoints are authenticated via OAuth2/JWT tokens.
 *
 * Base URI: /api/v1/principal-accounts
 * Consumer-specific URI: /api/v1/consumers/{consumerId}/principal-account
 *
 * Endpoints:
 * - POST /api/v1/principal-accounts - Create principal account
 * - GET /api/v1/consumers/{consumerId}/principal-account - Retrieve principal account
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PrincipalAccountController {

    private final PrincipalAccountService principalAccountService;

    /**
     * POST /api/v1/principal-accounts - Create a principal account for a consumer.
     *
     * Creates a new principal account linked to an existing consumer.
     * Enforces uniqueness: only one principal account per consumer is allowed.
     * Returns 409 Conflict if a principal account already exists for the consumer.
     *
     * Authentication:
     * Requires valid OAuth2/JWT token. Missing or invalid token returns 401 Unauthorized.
     *
     * Request body:
     * CreatePrincipalAccountRequest with consumerId (required) and optional accountType.
     * Validation errors return 400 Bad Request with field-level messages.
     *
     * Success response (HTTP 201):
     * {
     *   "success": true,
     *   "message": "Principal account created successfully",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655440000",
     *     "consumerId": "123e4567-e89b-12d3-a456-426614174000",
     *     "accountType": "PRIMARY",
     *     "status": "ACTIVE",
     *     "createdAt": "2024-12-04T10:30:00",
     *     "updatedAt": "2024-12-04T10:30:00"
     *   },
     *   "timestamp": "2024-12-04T10:30:00"
     * }
     *
     * Error responses:
     * - 400 Bad Request: Validation errors (field-level messages included)
     * - 401 Unauthorized: Missing or invalid OAuth2/JWT token
     * - 404 Not Found: Referenced consumer does not exist
     * - 409 Conflict: Principal account already exists for the consumer
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param request CreatePrincipalAccountRequest with consumerId and optional accountType (validated)
     * @return ResponseEntity with 201 Created status and PrincipalAccountResponse
     */
    @PostMapping("/principal-accounts")
    public ResponseEntity<ApiResponse<PrincipalAccountResponse>> createPrincipalAccount(
        @Valid @RequestBody CreatePrincipalAccountRequest request
    ) {
        log.info("Received request to create principal account for consumer: {}", request.getConsumerId());

        PrincipalAccountResponse response = principalAccountService.createPrincipalAccount(request);

        log.info("Principal account created successfully with ID: {} for consumer: {}",
            response.getId(), request.getConsumerId());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.<PrincipalAccountResponse>builder()
                .success(true)
                .message("Principal account created successfully")
                .data(response)
                .build()
            );
    }

    /**
     * GET /api/v1/consumers/{consumerId}/principal-account - Retrieve principal account for a consumer.
     *
     * Fetches the principal account associated with a specific consumer.
     * Returns 404 Not Found if consumer does not exist or has no principal account.
     *
     * Authentication:
     * Requires valid OAuth2/JWT token. Missing or invalid token returns 401 Unauthorized.
     *
     * Success response (HTTP 200):
     * {
     *   "success": true,
     *   "message": "Principal account retrieved successfully",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655440000",
     *     "consumerId": "123e4567-e89b-12d3-a456-426614174000",
     *     "accountType": "PRIMARY",
     *     "status": "ACTIVE",
     *     "createdAt": "2024-12-04T10:30:00",
     *     "updatedAt": "2024-12-04T10:30:00"
     *   },
     *   "timestamp": "2024-12-04T10:30:00"
     * }
     *
     * Error responses:
     * - 401 Unauthorized: Missing or invalid OAuth2/JWT token
     * - 404 Not Found: Consumer does not exist or has no principal account
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param consumerId the consumer ID (path variable, UUID format)
     * @return ResponseEntity with 200 OK status and PrincipalAccountResponse
     */
    @GetMapping("/consumers/{consumerId}/principal-account")
    public ResponseEntity<ApiResponse<PrincipalAccountResponse>> getPrincipalAccount(
        @PathVariable String consumerId
    ) {
        log.info("Received request to retrieve principal account for consumer: {}", consumerId);

        PrincipalAccountResponse response = principalAccountService.getPrincipalAccountByConsumerId(consumerId);

        log.info("Principal account retrieved successfully for consumer: {}", consumerId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.<PrincipalAccountResponse>builder()
                .success(true)
                .message("Principal account retrieved successfully")
                .data(response)
                .build()
            );
    }
}
