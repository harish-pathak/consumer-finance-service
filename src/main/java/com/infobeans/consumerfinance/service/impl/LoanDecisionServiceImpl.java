package com.infobeans.consumerfinance.service.impl;

import com.infobeans.consumerfinance.domain.LoanApplication;
import com.infobeans.consumerfinance.domain.LoanApplicationDecision;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import com.infobeans.consumerfinance.dto.request.SubmitLoanDecisionRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.dto.response.LoanDecisionResponse;
import com.infobeans.consumerfinance.event.LoanApplicationApprovedEvent;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.LoanApplicationDecisionRepository;
import com.infobeans.consumerfinance.repository.LoanApplicationRepository;
import com.infobeans.consumerfinance.service.LoanDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for loan application decision operations.
 *
 * Handles state transitions, duplicate prevention, audit trail persistence, and event publishing.
 *
 * Key Features:
 * - Atomic transactions: status update and decision record persist together
 * - Idempotent error handling: duplicate decisions return 409 Conflict
 * - Comprehensive audit trail: captures who decided, when, and why
 * - Event-driven: publishes LoanApplicationApprovedEvent for downstream processing
 * - Race condition handling: uses database constraints and transactional checks
 *
 * Concurrency:
 * - Database unique constraint on (application_id, decision) prevents duplicate decisions
 * - DataIntegrityViolationException caught for race conditions
 * - Service-level check before persistence for early validation
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDecisionServiceImpl implements LoanDecisionService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationDecisionRepository loanApplicationDecisionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Retrieve the current status of a loan application.
     *
     * @param applicationId the loan application ID
     * @return LoanApplicationResponse with current status
     * @throws ResourceNotFoundException if application not found
     */
    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getApplicationStatus(String applicationId) {
        log.info("Retrieving status for loan application: {}", applicationId);

        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> {
                log.error("Loan application not found: {}", applicationId);
                return new ResourceNotFoundException("LoanApplication", "id", applicationId);
            });

        LoanApplicationResponse response = mapToResponse(application);
        log.info("Application status retrieved: {} (status: {})", applicationId, application.getStatus());
        return response;
    }

    /**
     * Submit a decision (APPROVE or REJECT) on a loan application.
     *
     * Atomic Transaction Flow:
     * 1. Validate application exists and is PENDING
     * 2. Check no duplicate decision already exists
     * 3. Persist decision audit record
     * 4. Update application status to match decision
     * 5. Publish approval event if decision is APPROVED
     *
     * @param applicationId the loan application ID
     * @param request the decision request (decision + reason)
     * @param staffId the staff member making decision (from JWT)
     * @return LoanDecisionResponse with audit metadata
     * @throws ResourceNotFoundException if application not found
     * @throws DuplicateResourceException if duplicate decision exists
     */
    @Override
    @Transactional
    public LoanDecisionResponse submitDecision(
        String applicationId,
        SubmitLoanDecisionRequest request,
        String staffId
    ) {
        log.info("Received decision request for application: {} from staff: {} (decision: {})",
            applicationId, staffId, request.getDecision());

        // Step 1: Validate application exists
        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> {
                log.error("Loan application not found: {}", applicationId);
                return new ResourceNotFoundException("LoanApplication", "id", applicationId);
            });

        // Step 2: Validate application is in PENDING state (can still be decided)
        if (!LoanApplicationStatus.PENDING.equals(application.getStatus())) {
            log.warn("Cannot decide on application {} - status is {} (must be PENDING)",
                applicationId, application.getStatus());
            throw new DuplicateResourceException(
                "Application with ID '" + applicationId + "' is not in PENDING status. " +
                "Current status: " + application.getStatus()
            );
        }

        // Step 3: Check no duplicate decision already exists
        if (loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            applicationId, request.getDecision())) {
            var existingDecision = loanApplicationDecisionRepository
                .findByApplicationIdAndDecision(applicationId, request.getDecision())
                .orElseThrow();
            log.warn("Duplicate decision attempted for application {} - {} decision already exists ({})",
                applicationId, request.getDecision(), existingDecision.getId());
            throw new DuplicateResourceException(
                "Application with ID '" + applicationId + "' already has a " +
                request.getDecision().getDisplayName().toLowerCase() + " decision " +
                "(ID: " + existingDecision.getId() + ", created: " + existingDecision.getCreatedAt() + ")"
            );
        }

        // Step 4: Create and persist decision audit record
        LoanApplicationDecision decision = LoanApplicationDecision.builder()
            .applicationId(applicationId)
            .decision(request.getDecision())
            .staffId(staffId)
            .reason(request.getReason())
            .build();

        try {
            LoanApplicationDecision savedDecision = loanApplicationDecisionRepository.save(decision);
            log.info("Decision record created: {} (decision: {}, staff: {})",
                savedDecision.getId(), savedDecision.getDecision(), staffId);

            // Step 5: Update application status to match decision
            LoanApplicationStatus newStatus = LoanDecisionStatus.APPROVED.equals(request.getDecision())
                ? LoanApplicationStatus.APPROVED
                : LoanApplicationStatus.REJECTED;

            application.setStatus(newStatus);
            loanApplicationRepository.save(application);
            log.info("Application status updated: {} (status: {} -> {})",
                applicationId, LoanApplicationStatus.PENDING, newStatus);

            // Step 6: Publish approval event if approved (for downstream consumers)
            if (LoanDecisionStatus.APPROVED.equals(request.getDecision())) {
                LoanApplicationApprovedEvent event = new LoanApplicationApprovedEvent(
                    this,
                    applicationId,
                    application.getConsumerId(),
                    application.getRequestedAmount(),
                    staffId,
                    savedDecision.getCreatedAt()
                );
                applicationEventPublisher.publishEvent(event);
                log.info("LoanApplicationApprovedEvent published for application: {} (consumer: {})",
                    applicationId, application.getConsumerId());
            }

            return mapToResponse(savedDecision);

        } catch (DataIntegrityViolationException e) {
            // Race condition: duplicate decision created between check and save
            log.warn("Race condition detected - duplicate decision for application {} (decision: {})",
                applicationId, request.getDecision());

            // Re-check if duplicate decision now exists
            if (loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
                applicationId, request.getDecision())) {
                var conflictingDecision = loanApplicationDecisionRepository
                    .findByApplicationIdAndDecision(applicationId, request.getDecision())
                    .orElseThrow();
                throw new DuplicateResourceException(
                    "Application with ID '" + applicationId + "' already has a " +
                    request.getDecision().getDisplayName().toLowerCase() + " decision " +
                    "(ID: " + conflictingDecision.getId() + ")"
                );
            }
            // If not duplicate constraint, re-throw original exception
            throw e;
        }
    }

    /**
     * Map LoanApplication entity to response DTO.
     */
    private LoanApplicationResponse mapToResponse(LoanApplication application) {
        return LoanApplicationResponse.builder()
            .id(application.getId())
            .consumerId(application.getConsumerId())
            .status(application.getStatus())
            .requestedAmount(application.getRequestedAmount())
            .termInMonths(application.getTermInMonths())
            .purpose(application.getPurpose())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .build();
    }

    /**
     * Map LoanApplicationDecision entity to response DTO.
     */
    private LoanDecisionResponse mapToResponse(LoanApplicationDecision decision) {
        return LoanDecisionResponse.builder()
            .id(decision.getId())
            .applicationId(decision.getApplicationId())
            .decision(decision.getDecision())
            .staffId(decision.getStaffId())
            .reason(decision.getReason())
            .createdAt(decision.getCreatedAt())
            .build();
    }
}
