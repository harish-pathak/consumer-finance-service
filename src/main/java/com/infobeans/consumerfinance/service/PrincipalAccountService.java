package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.dto.request.CreatePrincipalAccountRequest;
import com.infobeans.consumerfinance.dto.response.PrincipalAccountResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;

/**
 * Service interface for principal account operations.
 *
 * Defines business logic for creating and retrieving principal accounts.
 * Enforces uniqueness (one principal account per consumer) and handles idempotency.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public interface PrincipalAccountService {

    /**
     * Create a principal account for a consumer.
     *
     * Creates a new principal account linked to an existing consumer.
     * Enforces uniqueness: only one principal account per consumer is allowed.
     * Returns 409 Conflict if a principal account already exists for the consumer.
     *
     * Idempotency behavior:
     * - If a principal account already exists for the consumer, throws DuplicateResourceException.
     * - Clients must implement retry-with-idempotency-key or check for 409 response.
     *
     * Transactional: atomic creation with referential integrity checks.
     *
     * @param request CreatePrincipalAccountRequest containing consumerId and optional accountType
     * @return PrincipalAccountResponse with created principal account details
     * @throws ResourceNotFoundException if referenced consumer does not exist
     * @throws DuplicateResourceException if principal account already exists for this consumer
     */
    PrincipalAccountResponse createPrincipalAccount(CreatePrincipalAccountRequest request)
            throws ResourceNotFoundException, DuplicateResourceException;

    /**
     * Retrieve a principal account by consumer ID.
     *
     * Fetches the principal account associated with a specific consumer.
     * Returns 404 Not Found if consumer does not exist or has no principal account.
     *
     * @param consumerId the consumer ID to retrieve the principal account for
     * @return PrincipalAccountResponse with principal account details
     * @throws ResourceNotFoundException if consumer does not exist or has no principal account
     */
    PrincipalAccountResponse getPrincipalAccountByConsumerId(String consumerId)
            throws ResourceNotFoundException;

    /**
     * Check if a principal account exists for a consumer.
     *
     * @param consumerId the consumer ID to check
     * @return true if principal account exists, false otherwise
     */
    boolean existsPrincipalAccountForConsumer(String consumerId);
}
