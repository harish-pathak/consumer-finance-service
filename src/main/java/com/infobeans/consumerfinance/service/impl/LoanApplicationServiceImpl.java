package com.infobeans.consumerfinance.service.impl;

import com.infobeans.consumerfinance.domain.LoanApplication;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import com.infobeans.consumerfinance.dto.request.CreateLoanApplicationRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.repository.LoanApplicationRepository;
import com.infobeans.consumerfinance.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of LoanApplicationService.
 *
 * Handles loan application business logic including creation, retrieval, and duplicate detection.
 * Enforces the constraint that a consumer can only have ONE PENDING application at a time.
 * Provides idempotent behavior with proper error handling and transactional guarantees.
 *
 * Duplicate Detection Strategy:
 * - Checks for existing PENDING applications before persisting new ones
 * - Uses database query with proper indexing for efficient lookup
 * - Handles race conditions with DataIntegrityViolationException handling
 * - Throws DuplicateResourceException (HTTP 409) if duplicate detected
 *
 * Transactional Guarantees:
 * - createLoanApplication is transactional: all operations succeed or all rollback
 * - getLoanApplicationById is read-only
 * - hasPendingLoanApplication is read-only for efficiency
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final ConsumerRepository consumerRepository;

    /**
     * Create a new loan application for a consumer.
     *
     * Process:
     * 1. Verify consumer exists (throws 404 if not found)
     * 2. Check for existing PENDING application (throws 409 if duplicate)
     * 3. Create and persist new loan application with status=PENDING
     * 4. Return application details
     *
     * Transactional: If any step fails, entire transaction is rolled back.
     * Handles DataIntegrityViolationException (concurrent creation race condition) → 409 Conflict.
     *
     * @param consumerId the ID of the consumer submitting the application (from JWT)
     * @param request CreateLoanApplicationRequest with requestedAmount, termInMonths, purpose
     * @return LoanApplicationResponse with created application details
     * @throws ResourceNotFoundException if referenced consumer does not exist
     * @throws DuplicateResourceException if a PENDING application already exists for this consumer
     */
    @Override
    public LoanApplicationResponse createLoanApplication(String consumerId, CreateLoanApplicationRequest request) {
        log.info("Starting loan application creation for consumer: {}", consumerId);

        // Step 1: Verify consumer exists
        if (!consumerRepository.findById(consumerId).isPresent()) {
            log.warn("Consumer not found for ID: {}", consumerId);
            throw new ResourceNotFoundException("Consumer", "id", consumerId);
        }

        // Step 2: Check for existing PENDING application (duplicate detection)
        if (loanApplicationRepository.existsByConsumerIdAndStatus(consumerId, LoanApplicationStatus.PENDING)) {
            log.warn("PENDING loan application already exists for consumer: {}", consumerId);

            // Fetch the existing PENDING application for detailed error response
            var existingApplication = loanApplicationRepository.findMostRecentByConsumerIdAndStatus(
                consumerId,
                LoanApplicationStatus.PENDING
            );

            String errorMessage = String.format(
                "Consumer with ID '%s' already has a pending loan application",
                consumerId
            );
            if (existingApplication.isPresent()) {
                errorMessage = String.format(
                    "Consumer with ID '%s' already has a pending loan application (ID: %s, created: %s)",
                    consumerId,
                    existingApplication.get().getId(),
                    existingApplication.get().getCreatedAt()
                );
            }

            throw new DuplicateResourceException(errorMessage);
        }

        // Step 3: Create loan application
        try {
            LoanApplication loanApplication = LoanApplication.builder()
                .consumerId(consumerId)
                .requestedAmount(request.getRequestedAmount())
                .termInMonths(request.getTermInMonths())
                .purpose(request.getPurpose())
                .status(LoanApplicationStatus.PENDING)
                .build();

            log.debug("Persisting loan application for consumer: {}", consumerId);
            LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

            log.info("Loan application created successfully with ID: {} for consumer: {}",
                savedApplication.getId(), consumerId);

            // Step 4: Return response
            return mapToResponse(savedApplication);

        } catch (DataIntegrityViolationException ex) {
            // Handle race condition: another thread created application simultaneously
            log.warn("DataIntegrityViolationException while creating loan application for consumer: {}. " +
                "Likely concurrent creation attempt.", consumerId);

            // Check if PENDING application now exists (created by concurrent request)
            if (loanApplicationRepository.existsByConsumerIdAndStatus(consumerId, LoanApplicationStatus.PENDING)) {
                log.info("PENDING loan application exists from concurrent request for consumer: {}",
                    consumerId);
                var existingApplication = loanApplicationRepository.findMostRecentByConsumerIdAndStatus(
                    consumerId,
                    LoanApplicationStatus.PENDING
                );

                String errorMessage = String.format(
                    "Consumer with ID '%s' already has a pending loan application",
                    consumerId
                );
                if (existingApplication.isPresent()) {
                    errorMessage = String.format(
                        "Consumer with ID '%s' already has a pending loan application (ID: %s, created: %s)",
                        consumerId,
                        existingApplication.get().getId(),
                        existingApplication.get().getCreatedAt()
                    );
                }

                throw new DuplicateResourceException(errorMessage);
            }

            // If not found, re-throw as it's a different integrity violation
            log.error("Unexpected data integrity violation while creating loan application for consumer: {}",
                consumerId);
            throw ex;
        }
    }

    /**
     * Retrieve a loan application by its ID.
     *
     * Returns the loan application with the given ID.
     * Throws 404 if application does not exist.
     *
     * @param applicationId the application ID to retrieve
     * @return LoanApplicationResponse with application details
     * @throws ResourceNotFoundException if application does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(String applicationId) {
        log.info("Retrieving loan application with ID: {}", applicationId);

        return loanApplicationRepository.findById(applicationId)
            .map(application -> {
                log.info("Loan application found with ID: {}", applicationId);
                return mapToResponse(application);
            })
            .orElseThrow(() -> {
                log.warn("Loan application not found with ID: {}", applicationId);
                return new ResourceNotFoundException("LoanApplication", "id", applicationId);
            });
    }

    /**
     * Check if a consumer has a PENDING loan application.
     *
     * Used for duplicate detection logic before creating new applications.
     *
     * @param consumerId the consumer ID to check
     * @return true if a PENDING application exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingLoanApplication(String consumerId) {
        boolean hasPending = loanApplicationRepository.existsByConsumerIdAndStatus(
            consumerId,
            LoanApplicationStatus.PENDING
        );
        log.debug("Checking PENDING loan application for consumer {}: {}", consumerId, hasPending);
        return hasPending;
    }

    /**
     * Map LoanApplication entity to response DTO.
     *
     * @param application the LoanApplication entity
     * @return LoanApplicationResponse
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
}
