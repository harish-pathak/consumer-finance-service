package com.infobeans.consumerfinance.controller;

import com.infobeans.consumerfinance.dto.request.CreateLoanApplicationRequest;
import com.infobeans.consumerfinance.dto.request.SubmitLoanDecisionRequest;
import com.infobeans.consumerfinance.dto.response.ApiResponse;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.dto.response.LoanDecisionResponse;
import com.infobeans.consumerfinance.service.LoanApplicationService;
import com.infobeans.consumerfinance.service.LoanDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for loan application endpoints.
 *
 * Provides secure endpoints for creating, retrieving, and deciding on loan applications.
 * All endpoints are authenticated via OAuth2/JWT tokens.
 *
 * Base URI: /api/v1/loan-applications
 *
 * Endpoints:
 * - POST /api/v1/loan-applications - Create loan application (authenticated user)
 * - GET /api/v1/loan-applications/{applicationId} - Retrieve loan application details
 * - GET /api/v1/loan-applications/{applicationId}/status - Get application status (authenticated)
 * - POST /api/v1/loan-applications/{applicationId}/decisions - Submit decision (staff only)
 *
 * Key Features:
 * - Duplicate detection: prevents multiple PENDING applications per consumer
 * - Idempotent error handling: consistent 409 responses for duplicates
 * - Atomic state transitions: status and decision persist together
 * - Audit trail: decisions recorded with staff ID, timestamp, and reason
 * - Event-driven: publishes LoanApplicationApprovedEvent for downstream processing
 * - Staff authorization: decision endpoints require staff role/claims
 * - JSR-380 Bean Validation on request DTOs
 * - Global exception handling with standard error responses
 *
 * @author Consumer Finance Service
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final LoanDecisionService loanDecisionService;

    /**
     * POST /api/v1/loan-applications - Create a new loan application.
     *
     * Creates a new loan application for the specified consumer.
     * Enforces uniqueness: a consumer can only have ONE PENDING application at a time.
     * Returns 409 Conflict if a PENDING application already exists for the consumer.
     *
     * Design:
     * - Portal login user (JWT) is INDEPENDENT from the consumer for whom the application is created
     * - Allows agents/staff to submit applications on behalf of consumers
     * - Consumer ID is provided in request payload, not extracted from JWT
     * - JWT authentication validates user has permission to submit applications
     *
     * Authentication:
     * Requires valid OAuth2/JWT token. Missing or invalid token returns 401 Unauthorized.
     *
     * Request body:
     * CreateLoanApplicationRequest with:
     * - consumerId (required): UUID of the consumer for whom application is created (36 chars)
     * - requestedAmount (required): Loan amount, must be > 0
     * - termInMonths (optional): Loan term in months (3-360)
     * - purpose (optional): Loan purpose, max 255 characters
     *
     * Validation errors return 400 Bad Request with field-level messages.
     *
     * Success response (HTTP 201 Created):
     * {
     *   "success": true,
     *   "message": "Loan application created successfully",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655441000",
     *     "consumerId": "550e8400-e29b-41d4-a716-446655440000",
     *     "status": "PENDING",
     *     "requestedAmount": 50000.00,
     *     "termInMonths": 60,
     *     "purpose": "Home renovation",
     *     "createdAt": "2024-12-04T10:30:00",
     *     "updatedAt": "2024-12-04T10:30:00"
     *   },
     *   "timestamp": "2024-12-04T10:30:00"
     * }
     *
     * Error responses:
     * - 400 Bad Request: Validation errors (field-level messages included)
     *   Example: "Consumer ID must be a valid UUID (36 characters)"
     *   Example: "Requested amount must be greater than 0"
     * - 401 Unauthorized: Missing or invalid OAuth2/JWT token
     * - 404 Not Found: Referenced consumer does not exist
     * - 409 Conflict: PENDING application already exists for the consumer
     *   Example error: "Consumer with ID '....' already has a pending loan application (ID: ..., created: ...)"
     * - 500 Internal Server Error: Unexpected server error
     *
     * Example request:
     * POST /api/v1/loan-applications
     * Content-Type: application/json
     * Authorization: Bearer <JWT_TOKEN>
     *
     * {
     *   "consumerId": "550e8400-e29b-41d4-a716-446655440000",
     *   "requestedAmount": 50000.00,
     *   "termInMonths": 60,
     *   "purpose": "Home renovation"
     * }
     *
     * Use Cases:
     * 1. Consumer self-submission: JWT user = consumer, consumerId in payload = same consumer
     * 2. Agent on behalf of consumer: JWT user = agent, consumerId in payload = customer UUID
     * 3. System integration: JWT user = system account, consumerId in payload = target consumer
     *
     * @param request CreateLoanApplicationRequest with consumerId, requestedAmount, termInMonths, purpose (validated)
     * @param authentication the authenticated principal from JWT token (portal login user)
     * @return ResponseEntity with 201 Created status and LoanApplicationResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> createLoanApplication(
        @Valid @RequestBody CreateLoanApplicationRequest request,
        Authentication authentication
    ) {
        String consumerId = request.getConsumerId();
        String portalUser = authentication.getName();

        log.info("Received request to create loan application for consumer: {} from portal user: {} with amount: {}",
            consumerId, portalUser, request.getRequestedAmount());

        LoanApplicationResponse response = loanApplicationService.createLoanApplication(consumerId, request);

        log.info("Loan application created successfully with ID: {} for consumer: {} by portal user: {}",
            response.getId(), consumerId, portalUser);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .message("Loan application created successfully")
                .data(response)
                .build()
            );
    }

    /**
     * GET /api/v1/loan-applications/{applicationId} - Retrieve loan application details.
     *
     * Fetches the details of a specific loan application.
     * Returns 404 Not Found if the application does not exist.
     *
     * Authentication:
     * Requires valid OAuth2/JWT token. Missing or invalid token returns 401 Unauthorized.
     *
     * Success response (HTTP 200 OK):
     * {
     *   "success": true,
     *   "message": "Loan application retrieved successfully",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655441000",
     *     "consumerId": "123e4567-e89b-12d3-a456-426614174000",
     *     "status": "PENDING",
     *     "requestedAmount": 50000.00,
     *     "termInMonths": 60,
     *     "purpose": "Home renovation",
     *     "createdAt": "2024-12-04T10:30:00",
     *     "updatedAt": "2024-12-04T10:30:00"
     *   },
     *   "timestamp": "2024-12-04T10:30:00"
     * }
     *
     * Error responses:
     * - 401 Unauthorized: Missing or invalid OAuth2/JWT token
     * - 404 Not Found: Application does not exist
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param applicationId the application ID (path variable, UUID format)
     * @param authentication the authenticated principal from JWT token
     * @return ResponseEntity with 200 OK status and LoanApplicationResponse
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getLoanApplication(
        @PathVariable String applicationId,
        Authentication authentication
    ) {
        log.info("Received request to retrieve loan application: {}", applicationId);

        LoanApplicationResponse response = loanApplicationService.getLoanApplicationById(applicationId);

        log.info("Loan application retrieved successfully for ID: {}", applicationId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .message("Loan application retrieved successfully")
                .data(response)
                .build()
            );
    }

    /**
     * GET /api/v1/loan-applications/{applicationId}/status - Retrieve application status.
     *
     * Returns the current status of a loan application.
     * Useful for checking if application is ready for decision.
     *
     * Authentication:
     * Requires valid OAuth2/JWT token. Missing or invalid token returns 401 Unauthorized.
     *
     * Success response (HTTP 200 OK):
     * {
     *   "success": true,
     *   "message": "Loan application status retrieved successfully",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655441000",
     *     "consumerId": "550e8400-e29b-41d4-a716-446655440000",
     *     "status": "PENDING",
     *     "requestedAmount": 50000.00,
     *     "termInMonths": 60,
     *     "purpose": "Home renovation",
     *     "createdAt": "2024-12-04T10:30:00",
     *     "updatedAt": "2024-12-04T10:30:00"
     *   },
     *   "timestamp": "2024-12-05T14:30:00"
     * }
     *
     * Error responses:
     * - 401 Unauthorized: Missing or invalid OAuth2/JWT token
     * - 404 Not Found: Application does not exist
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param applicationId the loan application ID (path variable, UUID format)
     * @param authentication the authenticated principal from JWT token
     * @return ResponseEntity with 200 OK status and LoanApplicationResponse
     */
    @GetMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplicationStatus(
        @PathVariable String applicationId,
        Authentication authentication
    ) {
        log.info("Received request to get status for loan application: {}", applicationId);

        LoanApplicationResponse response = loanDecisionService.getApplicationStatus(applicationId);

        log.info("Application status retrieved: {} (status: {})", applicationId, response.getStatus());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .message("Loan application status retrieved successfully")
                .data(response)
                .build()
            );
    }

    /**
     * POST /api/v1/loan-applications/{applicationId}/decisions - Submit a decision on a loan application.
     *
     * Submits an approval or rejection decision for a PENDING loan application.
     * Only authorized staff members can submit decisions.
     *
     * Design:
     * - Staff authorization required: checked via JWT claims/roles (Spring Security)
     * - Staff ID extracted from JWT authentication principal (who made the decision)
     * - Decision type (APPROVE/REJECT) provided in request body
     * - Optional reason field for audit trail documentation
     *
     * Authorization:
     * Requires valid OAuth2/JWT token with staff authorization.
     * Returns:
     * - 401 Unauthorized: Missing or invalid JWT token
     * - 403 Forbidden: Authenticated but lacking staff authorization
     *
     * Atomicity & Idempotency:
     * - Application status and decision record update in single transaction
     * - Duplicate decision attempts return 409 Conflict (not allowed)
     * - Database unique constraint on (application_id, decision) prevents duplicates
     *
     * Event Publishing:
     * - On approval: LoanApplicationApprovedEvent published for downstream consumers
     * - On rejection: No event published (internal decision only)
     *
     * Request body:
     * SubmitLoanDecisionRequest with:
     * - decision (required): APPROVED or REJECTED
     * - reason (optional): max 500 characters
     *
     * Success response (HTTP 200 OK):
     * {
     *   "success": true,
     *   "message": "Decision submitted successfully",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655442000",
     *     "applicationId": "550e8400-e29b-41d4-a716-446655441000",
     *     "decision": "APPROVED",
     *     "staffId": "loan_officer_001",
     *     "reason": "Income verified, credit score acceptable, approved for full amount",
     *     "createdAt": "2024-12-05T14:30:00"
     *   },
     *   "timestamp": "2024-12-05T14:30:00"
     * }
     *
     * Error responses:
     * - 400 Bad Request: Validation errors (missing decision, reason too long, etc.)
     * - 401 Unauthorized: Missing or invalid JWT token
     * - 403 Forbidden: Authenticated but not a staff member (missing staff authorization)
     * - 404 Not Found: Application does not exist
     * - 409 Conflict: Application not PENDING or duplicate decision exists
     * - 500 Internal Server Error: Unexpected server error
     *
     * Example request:
     * POST /api/v1/loan-applications/550e8400-e29b-41d4-a716-446655441000/decisions
     * Content-Type: application/json
     * Authorization: Bearer <STAFF_JWT_TOKEN>
     *
     * {
     *   "decision": "APPROVED",
     *   "reason": "Income verified, credit score 750+, approved for full requested amount"
     * }
     *
     * @param applicationId the loan application ID (path variable, UUID format)
     * @param request SubmitLoanDecisionRequest with decision and optional reason
     * @param authentication the authenticated principal from JWT token (staff member)
     * @return ResponseEntity with 200 OK status and LoanDecisionResponse
     */
    @PostMapping("/{applicationId}/decisions")
    public ResponseEntity<ApiResponse<LoanDecisionResponse>> submitDecision(
        @PathVariable String applicationId,
        @Valid @RequestBody SubmitLoanDecisionRequest request,
        Authentication authentication
    ) {
        String staffId = authentication.getName();

        log.info("Received decision request for application: {} from staff: {} (decision: {})",
            applicationId, staffId, request.getDecision());

        LoanDecisionResponse response = loanDecisionService.submitDecision(applicationId, request, staffId);

        log.info("Decision submitted successfully for application: {} (decision: {}, staff: {})",
            applicationId, response.getDecision(), staffId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.<LoanDecisionResponse>builder()
                .success(true)
                .message("Decision submitted successfully")
                .data(response)
                .build()
            );
    }
}
