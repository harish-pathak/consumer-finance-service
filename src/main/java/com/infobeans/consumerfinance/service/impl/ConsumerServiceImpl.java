package com.infobeans.consumerfinance.service.impl;

import com.infobeans.consumerfinance.domain.Consumer;
import com.infobeans.consumerfinance.domain.embedded.EmploymentDetails;
import com.infobeans.consumerfinance.domain.embedded.IncomeDetails;
import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.CreateConsumerOnboardingRequest;
import com.infobeans.consumerfinance.dto.response.ConsumerOnboardingResponse;
import com.infobeans.consumerfinance.event.OnboardingCompletedEvent;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.service.ConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of ConsumerService.
 * Handles consumer onboarding business logic, validation, persistence, and event publishing.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsumerServiceImpl implements ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Onboard a new consumer.
     * Validates for duplicates, persists data with encrypted sensitive fields,
     * and publishes OnboardingCompletedEvent for downstream processing.
     *
     * @param request CreateConsumerOnboardingRequest with consumer data
     * @return ConsumerOnboardingResponse with created consumer ID and status
     * @throws DuplicateResourceException if email or national ID already exists
     */
    @Override
    public ConsumerOnboardingResponse onboardConsumer(CreateConsumerOnboardingRequest request) {
        log.info("Starting consumer onboarding for email: {}", request.getEmail());

        // Check for duplicate email
        if (consumerRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email detected during onboarding: {}", request.getEmail());
            throw new DuplicateResourceException(
                "A consumer with email '" + request.getEmail() + "' already exists. Please use a different email address."
            );
        }

        // Check for duplicate national ID if provided
        if (request.getNationalId() != null && !request.getNationalId().isEmpty()) {
            if (consumerRepository.existsByNationalId(request.getNationalId())) {
                log.warn("Duplicate national ID detected during onboarding: {}", request.getNationalId());
                throw new DuplicateResourceException(
                    "A consumer with national ID '" + request.getNationalId() + "' already exists. Please verify the national ID and try again."
                );
            }
        }

        // Check for duplicate phone if provided
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (consumerRepository.existsByPhone(request.getPhone())) {
                log.warn("Duplicate phone number detected during onboarding: {}", request.getPhone());
                throw new DuplicateResourceException(
                    "A consumer with phone number '" + request.getPhone() + "' already exists. Please use a different phone number."
                );
            }
        }

        // Check for duplicate document number if provided
        if (request.getDocumentNumber() != null && !request.getDocumentNumber().isEmpty()) {
            if (consumerRepository.existsByDocumentNumber(request.getDocumentNumber())) {
                log.warn("Duplicate document number detected during onboarding: {}", request.getDocumentNumber());
                throw new DuplicateResourceException(
                    "A consumer with document number '" + request.getDocumentNumber() + "' already exists. Please use a different document number."
                );
            }
        }

        // Create consumer entity from request
        Consumer consumer = buildConsumerFromRequest(request);

        // Persist consumer (encryption happens automatically via JPA converter)
        Consumer savedConsumer = consumerRepository.save(consumer);
        log.info("Consumer persisted successfully with ID: {}", savedConsumer.getId());

        // Publish onboarding completed event for downstream operations (e.g., principal account creation)
        publishOnboardingCompletedEvent(savedConsumer);

        // Build and return response
        return ConsumerOnboardingResponse.builder()
            .consumerId(savedConsumer.getId())
            .status(savedConsumer.getStatus().name())
            .createdAt(savedConsumer.getCreatedAt())
            .message("Consumer onboarded successfully")
            .build();
    }

    /**
     * Retrieve a consumer by ID.
     *
     * @param consumerId the consumer ID (UUID string)
     * @return Consumer entity
     * @throws ResourceNotFoundException if consumer not found
     */
    @Override
    @Transactional(readOnly = true)
    public Consumer getConsumerById(String consumerId) {
        return consumerRepository.findById(consumerId)
            .orElseThrow(() -> {
                log.warn("Consumer not found with ID: {}", consumerId);
                return new ResourceNotFoundException("Consumer", "id", consumerId);
            });
    }

    /**
     * Check if consumer exists by email.
     *
     * @param email the email address to check
     * @return true if consumer with this email exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean consumerExistsByEmail(String email) {
        return consumerRepository.existsByEmail(email);
    }

    /**
     * Build Consumer entity from onboarding request.
     * Maps request DTOs to domain objects, organizing data by groups.
     *
     * @param request CreateConsumerOnboardingRequest
     * @return Consumer entity ready for persistence
     */
    private Consumer buildConsumerFromRequest(CreateConsumerOnboardingRequest request) {
        // Build employment details
        EmploymentDetails employmentDetails = null;
        if (request.getEmployerName() != null || request.getPosition() != null ||
            request.getEmploymentType() != null || request.getYearsOfExperience() != null) {
            employmentDetails = EmploymentDetails.builder()
                .employerName(request.getEmployerName())
                .position(request.getPosition())
                .employmentType(request.getEmploymentType())
                .yearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience().longValue() : null)
                .industry(request.getIndustry())
                .build();
        }

        // Build income details
        IncomeDetails incomeDetails = null;
        if (request.getMonthlyIncome() != null || request.getAnnualIncome() != null ||
            request.getIncomeSource() != null) {
            incomeDetails = IncomeDetails.builder()
                .monthlyIncome(request.getMonthlyIncome())
                .annualIncome(request.getAnnualIncome())
                .incomeSource(request.getIncomeSource())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .build();
        }

        // Build consumer entity
        return Consumer.builder()
            .id(UUID.randomUUID().toString())
            // Personal
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .dateOfBirth(request.getDateOfBirth())
            // Identity (encrypted via converter)
            .nationalId(request.getNationalId())
            .documentType(request.getDocumentType())
            .documentNumber(request.getDocumentNumber())
            // Employment
            .employmentDetails(employmentDetails)
            // Financial
            .incomeDetails(incomeDetails)
            // Status
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("api")
            .updatedBy("api")
            .build();
    }

    /**
     * Publish OnboardingCompletedEvent for downstream listeners.
     * Listeners can subscribe to handle operations like principal account creation.
     *
     * @param consumer the newly onboarded consumer
     */
    private void publishOnboardingCompletedEvent(Consumer consumer) {
        String consumerName = consumer.getFirstName() + " " + consumer.getLastName();
        OnboardingCompletedEvent event = new OnboardingCompletedEvent(
            this,
            consumer.getId(),
            consumer.getEmail(),
            consumerName,
            consumer.getCreatedAt(),
            "api",
            null
        );
        eventPublisher.publishEvent(event);
        log.info("OnboardingCompletedEvent published for consumer ID: {}", consumer.getId());
    }
}
