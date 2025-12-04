package com.infobeans.consumerfinance.service.impl;

import com.infobeans.consumerfinance.domain.PrincipalAccount;
import com.infobeans.consumerfinance.dto.request.CreatePrincipalAccountRequest;
import com.infobeans.consumerfinance.dto.response.PrincipalAccountResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.repository.PrincipalAccountRepository;
import com.infobeans.consumerfinance.service.PrincipalAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of PrincipalAccountService.
 *
 * Handles principal account business logic including creation, retrieval, and validation.
 * Enforces uniqueness (one principal account per consumer) at both application and database levels.
 * Provides idempotent behavior with proper error handling and transactional guarantees.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrincipalAccountServiceImpl implements PrincipalAccountService {

    private final PrincipalAccountRepository principalAccountRepository;
    private final ConsumerRepository consumerRepository;

    /**
     * Create a principal account for a consumer.
     *
     * Process:
     * 1. Verify consumer exists (throws 404 if not found)
     * 2. Check for existing principal account (throws 409 if duplicate)
     * 3. Create and persist new principal account
     * 4. Return account details
     *
     * Transactional: If any step fails, entire transaction is rolled back.
     * Handles DataIntegrityViolationException (concurrent creation race condition) → 409 Conflict.
     *
     * @param request CreatePrincipalAccountRequest with consumerId and optional accountType
     * @return PrincipalAccountResponse with created account details
     * @throws ResourceNotFoundException if consumer does not exist
     * @throws DuplicateResourceException if principal account already exists for consumer
     */
    @Override
    public PrincipalAccountResponse createPrincipalAccount(CreatePrincipalAccountRequest request) {
        String consumerId = request.getConsumerId();

        log.info("Starting principal account creation for consumer: {}", consumerId);

        // Step 1: Verify consumer exists (check if consumer with this ID exists)
        if (!consumerRepository.findById(consumerId).isPresent()) {
            log.warn("Consumer not found for ID: {}", consumerId);
            throw new ResourceNotFoundException("Consumer", "id", consumerId);
        }

        // Step 2: Check for existing principal account (application-level check for idempotency)
        if (principalAccountRepository.existsByConsumerId(consumerId)) {
            log.warn("Principal account already exists for consumer: {}", consumerId);
            throw new DuplicateResourceException(
                "A principal account already exists for consumer with ID '" + consumerId + "'");
        }

        // Step 3: Create principal account
        try {
            PrincipalAccount principalAccount = PrincipalAccount.builder()
                .consumerId(consumerId)
                .accountType(request.getAccountType())
                .status(request.getStatus())
                .build();

            log.debug("Persisting principal account for consumer: {}", consumerId);
            PrincipalAccount savedAccount = principalAccountRepository.save(principalAccount);

            log.info("Principal account created successfully with ID: {} for consumer: {}",
                savedAccount.getId(), consumerId);

            // Step 4: Return response
            return mapToResponse(savedAccount);

        } catch (DataIntegrityViolationException ex) {
            // Handle race condition: another thread created account simultaneously
            log.warn("DataIntegrityViolationException while creating principal account for consumer: {}. " +
                "Likely concurrent creation attempt.", consumerId);

            // Check if principal account now exists (created by concurrent request)
            var existingAccount = principalAccountRepository.findByConsumerId(consumerId);
            if (existingAccount.isPresent()) {
                log.info("Returning existing principal account created by concurrent request for consumer: {}",
                    consumerId);
                throw new DuplicateResourceException(
                    "A principal account already exists for consumer with ID '" + consumerId + "'");
            }

            // If not found, re-throw as it's a different integrity violation
            log.error("Unexpected data integrity violation while creating principal account for consumer: {}",
                consumerId);
            throw ex;
        }
    }

    /**
     * Retrieve a principal account by consumer ID.
     *
     * Returns the principal account associated with a specific consumer.
     * Throws 404 if consumer does not exist or has no principal account.
     *
     * @param consumerId the consumer ID to retrieve the principal account for
     * @return PrincipalAccountResponse with principal account details
     * @throws ResourceNotFoundException if consumer doesn't exist or has no principal account
     */
    @Override
    @Transactional(readOnly = true)
    public PrincipalAccountResponse getPrincipalAccountByConsumerId(String consumerId) {
        log.info("Retrieving principal account for consumer: {}", consumerId);

        // Step 1: Verify consumer exists (check if consumer with this ID exists)
        if (!consumerRepository.findById(consumerId).isPresent()) {
            log.warn("Consumer not found for ID: {}", consumerId);
            throw new ResourceNotFoundException("Consumer", "id", consumerId);
        }

        // Step 2: Find principal account
        return principalAccountRepository.findByConsumerId(consumerId)
            .map(account -> {
                log.info("Principal account found for consumer: {}", consumerId);
                return mapToResponse(account);
            })
            .orElseThrow(() -> {
                log.warn("Principal account not found for consumer: {}", consumerId);
                return new ResourceNotFoundException("PrincipalAccount", "consumerId", consumerId);
            });
    }

    /**
     * Check if a principal account exists for a consumer.
     *
     * @param consumerId the consumer ID to check
     * @return true if principal account exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsPrincipalAccountForConsumer(String consumerId) {
        return principalAccountRepository.existsByConsumerId(consumerId);
    }

    /**
     * Map PrincipalAccount entity to response DTO.
     *
     * @param account the PrincipalAccount entity
     * @return PrincipalAccountResponse
     */
    private PrincipalAccountResponse mapToResponse(PrincipalAccount account) {
        return PrincipalAccountResponse.builder()
            .id(account.getId())
            .consumerId(account.getConsumerId())
            .accountType(account.getAccountType())
            .status(account.getStatus())
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .build();
    }
}
