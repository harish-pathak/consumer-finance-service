package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.Consumer;
import com.infobeans.consumerfinance.dto.request.CreateConsumerOnboardingRequest;
import com.infobeans.consumerfinance.dto.response.ConsumerOnboardingResponse;

/**
 * Service interface for consumer onboarding operations.
 * Defines business logic for consumer management.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public interface ConsumerService {

    /**
     * Onboard a new consumer with provided data.
     * Validates request, checks for duplicates, persists consumer, and publishes onboarding event.
     *
     * @param request CreateConsumerOnboardingRequest with consumer data
     * @return ConsumerOnboardingResponse with created consumer ID and status
     * @throws com.infobeans.consumerfinance.exception.ValidationException if request validation fails
     * @throws com.infobeans.consumerfinance.exception.DuplicateResourceException if email/nationalId already exists
     */
    ConsumerOnboardingResponse onboardConsumer(CreateConsumerOnboardingRequest request);

    /**
     * Retrieve a consumer by ID.
     *
     * @param consumerId the consumer ID (UUID as string)
     * @return Consumer entity
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException if consumer not found
     */
    Consumer getConsumerById(String consumerId);

    /**
     * Check if consumer exists by email (for duplicate detection).
     *
     * @param email the email address to check
     * @return true if consumer with this email exists
     */
    boolean consumerExistsByEmail(String email);
}
