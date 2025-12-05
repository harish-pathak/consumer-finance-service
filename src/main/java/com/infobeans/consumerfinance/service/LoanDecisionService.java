package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.dto.request.SubmitLoanDecisionRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.dto.response.LoanDecisionResponse;

/**
 * Service interface for loan application decision operations.
 *
 * Handles:
 * - Retrieving current loan application status
 * - Submitting decisions (approve/reject) with atomic state transitions
 * - Decision audit trail persistence
 * - Event publishing for downstream consumers
 *
 * Design Principles:
 * - Atomicity: status update and decision record persist together or not at all
 * - Idempotency: duplicate decision attempts return 409 Conflict instead of failing
 * - Auditability: all decisions recorded with staff identity and timestamp
 * - Event-Driven: approval triggers LoanApplicationApprovedEvent for async processing
 *
 * Authorization:
 * - Decision endpoints require staff authorization (checked at controller level)
 * - Service assumes valid staff ID passed from authenticated principal
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public interface LoanDecisionService {

    /**
     * Retrieve the current status of a loan application.
     *
     * Returns basic application information without sensitive data.
     * Useful for checking if application is ready for decision.
     *
     * @param applicationId the loan application ID (UUID string)
     * @return LoanApplicationResponse with current status and metadata
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException
     *         if application does not exist (404 Not Found)
     */
    LoanApplicationResponse getApplicationStatus(String applicationId);

    /**
     * Submit a decision (APPROVE or REJECT) on a loan application.
     *
     * Atomically:
     * 1. Validates application exists and is in PENDING state
     * 2. Checks no duplicate decision already exists
     * 3. Persists decision audit record with staff identity
     * 4. Updates application status to match decision (APPROVED/REJECTED)
     * 5. Publishes LoanApplicationApprovedEvent if approved (for downstream processing)
     *
     * Authorization:
     * - Requires staff authorization (enforced at controller level)
     * - Staff ID extracted from JWT authentication principal
     *
     * Validation:
     * - Application must exist: returns 404 if not found
     * - Application must be PENDING: returns 409 if already decided
     * - Decision must be APPROVED or REJECTED: validated by DTO
     * - Reason optional but max 500 chars: validated by DTO
     *
     * Duplicate Prevention:
     * - Database unique constraint on (application_id, decision) prevents duplicates
     * - Service checks for existing decision before attempting save
     * - Concurrent duplicate attempts fail with 409 Conflict
     *
     * @param applicationId the loan application ID (UUID string)
     * @param request the decision request containing decision type and optional reason
     * @param staffId the ID/username of staff making the decision (from JWT principal)
     * @return LoanDecisionResponse with decision audit record and metadata
     *
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException
     *         if application does not exist (404 Not Found)
     * @throws com.infobeans.consumerfinance.exception.DuplicateResourceException
     *         if duplicate decision already exists (409 Conflict)
     * @throws com.infobeans.consumerfinance.exception.BusinessRuleException
     *         if application is not in PENDING state (409 Conflict)
     */
    LoanDecisionResponse submitDecision(
        String applicationId,
        SubmitLoanDecisionRequest request,
        String staffId
    );
}
