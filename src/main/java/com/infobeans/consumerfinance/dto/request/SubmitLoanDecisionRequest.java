package com.infobeans.consumerfinance.dto.request;

import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a decision on a loan application.
 *
 * Captures the staff decision (approve or reject) with optional reasoning.
 * Validates that decision is one of the allowed enum values.
 * Reason is optional but limited to 500 characters for database compatibility.
 *
 * Design:
 * - Decoupled from staff identity: staff ID extracted from JWT authentication
 * - Supports audit trail: reason field explains the decision rationale
 * - Idempotent error handling: duplicate decision attempts return 409 Conflict
 *
 * Validation:
 * - JSR-380 annotations enforced by Spring validation framework
 * - decision: Required, must be APPROVED or REJECTED
 * - reason: Optional, max 500 characters
 *
 * Example request:
 * {
 *   "decision": "APPROVED",
 *   "reason": "Income verified, credit score acceptable, disbursement approved"
 * }
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitLoanDecisionRequest {

    /**
     * The decision to be made on the loan application.
     * Required field. Must be one of the enum values: APPROVED, REJECTED.
     *
     * Examples:
     * - "APPROVED" - Application approved, loan will be disbursed
     * - "REJECTED" - Application rejected, loan will not be provided
     *
     * Invalid values result in 400 Bad Request response.
     */
    @NotNull(message = "Decision is required (APPROVED or REJECTED)")
    private LoanDecisionStatus decision;

    /**
     * Optional reason or notes explaining the decision.
     * Free-form text up to 500 characters.
     * Used for audit trail and staff documentation.
     *
     * Examples:
     * - For APPROVED: "Income verified, credit score 750+, approved for full amount"
     * - For REJECTED: "Income insufficient for requested amount, debt-to-income ratio too high"
     *
     * Null/empty allowed: Reason may be omitted, staff can add notes separately if needed.
     * Over 500 chars results in 400 Bad Request.
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
