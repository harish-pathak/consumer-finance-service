package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.dto.request.CreateLoanApplicationRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;

/**
 * Service interface for loan application operations.
 *
 * Defines business logic for creating and retrieving loan applications.
 * Enforces duplicate prevention (one PENDING application per consumer at a time)
 * and handles idempotency with proper error handling.
 *
 * Key Features:
 * - Create new loan applications with automatic ID generation
 * - Detect and prevent duplicate PENDING applications (HTTP 409 Conflict)
 * - Retrieve loan application details
 * - Allow multiple applications once previous ones are resolved
 *
 * Duplicate Prevention Strategy:
 * - A consumer can only have ONE PENDING application at a time
 * - Once an application reaches terminal status (APPROVED/REJECTED/CANCELLED),
 *   the consumer can submit new applications
 * - Prevents application spam and concurrent duplicate submissions
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public interface LoanApplicationService {

    /**
     * Create a new loan application for a consumer.
     *
     * Validates and persists a loan application for the authenticated consumer.
     * Enforces the constraint that a consumer can only have ONE PENDING application at a time.
     *
     * Process:
     * 1. Verify consumer exists (throws 404 if not found)
     * 2. Check for existing PENDING application (throws 409 if duplicate)
     * 3. Create and persist new loan application with initial status=PENDING
     * 4. Return application details with HTTP 201 Created
     *
     * Duplicate Detection:
     * - If a PENDING application already exists for this consumer:
     *   - Throw DuplicateResourceException
     *   - API returns HTTP 409 Conflict
     *   - Error response includes details of existing PENDING application
     * - Once application status changes to APPROVED/REJECTED/CANCELLED,
     *   the consumer can submit new applications
     *
     * Idempotency:
     * - If a PENDING application already exists, consistently returns 409
     * - Clients should implement retry-with-idempotency-key or check for 409
     * - Not idempotent with multiple submission attempts (by design, to prevent spam)
     *
     * Transactional: If any step fails, entire transaction is rolled back.
     * Handles DataIntegrityViolationException (concurrent creation race condition) → 409 Conflict.
     *
     * @param consumerId the ID of the consumer submitting the application (from JWT)
     * @param request CreateLoanApplicationRequest with requestedAmount, termInMonths, purpose
     * @return LoanApplicationResponse with created application details (HTTP 201)
     * @throws ResourceNotFoundException if referenced consumer does not exist
     * @throws DuplicateResourceException if a PENDING application already exists for this consumer
     */
    LoanApplicationResponse createLoanApplication(String consumerId, CreateLoanApplicationRequest request)
            throws ResourceNotFoundException, DuplicateResourceException;

    /**
     * Retrieve a loan application by its ID.
     *
     * Fetches the details of a specific loan application.
     * Returns 404 Not Found if the application does not exist.
     *
     * @param applicationId the application ID to retrieve
     * @return LoanApplicationResponse with application details
     * @throws ResourceNotFoundException if application does not exist
     */
    LoanApplicationResponse getLoanApplicationById(String applicationId)
            throws ResourceNotFoundException;

    /**
     * Check if a consumer has a PENDING loan application.
     *
     * Used for duplicate detection logic before creating new applications.
     *
     * @param consumerId the consumer ID to check
     * @return true if a PENDING application exists, false otherwise
     */
    boolean hasPendingLoanApplication(String consumerId);
}
