package com.infobeans.consumerfinance.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for loan application decision information.
 *
 * Represents a decision audit record with full metadata for transparency and traceability.
 * Includes who made the decision, what decision, when, and why.
 *
 * Used in:
 * - POST /api/v1/loan-applications/{applicationId}/decisions response (201 Created)
 * - Embedded in status responses showing recent decisions
 *
 * Example response:
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655442000",
 *   "applicationId": "550e8400-e29b-41d4-a716-446655441000",
 *   "decision": "APPROVED",
 *   "staffId": "loan_officer_001",
 *   "reason": "Income verified, credit score 750+, disbursement approved",
 *   "createdAt": "2024-12-05T14:30:00"
 * }
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDecisionResponse {

    /**
     * Unique identifier for this decision record (UUID).
     * Format: 36-character string.
     * Used to identify the specific decision for audit/reference.
     */
    private String id;

    /**
     * Reference to the loan application this decision applies to.
     * Format: 36-character string (UUID).
     * Links decision to its corresponding loan application.
     */
    private String applicationId;

    /**
     * The decision made: APPROVED or REJECTED.
     * Enum value representing the approval status outcome.
     */
    private LoanDecisionStatus decision;

    /**
     * ID/username of the staff member who made this decision.
     * Captured from JWT authentication principal.
     * Provides traceability for audit and compliance purposes.
     */
    private String staffId;

    /**
     * Optional reason or notes explaining the decision.
     * Free-form text (up to 500 characters).
     * May be null if no reason was provided.
     *
     * Examples:
     * - "Income verified, credit score 750+, full amount approved"
     * - "Debt-to-income ratio 45%, requested term too long for income level"
     */
    private String reason;

    /**
     * Timestamp when the decision was created/recorded.
     * ISO 8601 format: yyyy-MM-dd'T'HH:mm:ss
     * Immutable: Reflects the decision timestamp, never updated.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
