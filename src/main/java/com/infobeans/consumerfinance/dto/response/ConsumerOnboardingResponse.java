package com.infobeans.consumerfinance.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for consumer onboarding.
 *
 * Returned on successful onboarding (HTTP 201 Created).
 * Contains minimal created resource metadata (ID, status, timestamp).
 * Sensitive fields are NOT included in the response for security reasons.
 *
 * Usage:
 * Wrapped in ApiResponse<ConsumerOnboardingResponse> for consistent API responses.
 *
 * Example response:
 * {
 *   "status": 201,
 *   "message": "Consumer onboarded successfully",
 *   "data": {
 *     "consumerId": "550e8400-e29b-41d4-a716-446655440000",
 *     "status": "ACTIVE",
 *     "createdAt": "2024-12-03T10:30:00Z"
 *   }
 * }
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsumerOnboardingResponse {

    /**
     * Unique consumer identifier (UUID string).
     * This ID is required for all subsequent consumer operations.
     */
    private String consumerId;

    /**
     * Consumer account status.
     * Newly onboarded consumers are in ACTIVE status.
     */
    private String status;

    /**
     * Timestamp when the consumer record was created (onboarded).
     * Format: ISO 8601 with timezone information.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    /**
     * Onboarding summary message.
     * Provides human-readable confirmation of successful onboarding.
     */
    private String message;
}
