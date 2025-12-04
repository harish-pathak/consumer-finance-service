package com.infobeans.consumerfinance.event;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.CreatePrincipalAccountRequest;
import com.infobeans.consumerfinance.service.PrincipalAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for consumer onboarding completion.
 *
 * Automatically creates a principal account for each newly onboarded consumer.
 * This ensures that every consumer has an associated principal account as part of the onboarding process.
 *
 * Triggered by: OnboardingCompletedEvent published during consumer onboarding
 * Action: Creates a new PrincipalAccount linked to the consumer
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrincipalAccountCreationListener {

    private final PrincipalAccountService principalAccountService;

    /**
     * Listen for OnboardingCompletedEvent and automatically create principal account.
     *
     * This method is triggered asynchronously when a consumer completes onboarding.
     * It creates a new principal account with DEFAULT type and ACTIVE status.
     *
     * @param event OnboardingCompletedEvent containing consumer information
     */
    @EventListener
    @Transactional
    public void onOnboardingCompleted(OnboardingCompletedEvent event) {
        String consumerId = event.getConsumerId();
        String consumerEmail = event.getEmail();

        try {
            log.info("Processing OnboardingCompletedEvent for consumer: {} ({})", consumerId, consumerEmail);

            // Create principal account for the newly onboarded consumer
            CreatePrincipalAccountRequest request = CreatePrincipalAccountRequest.builder()
                .consumerId(consumerId)
                .accountType("PRIMARY")
                .status(AccountStatus.ACTIVE)
                .build();

            principalAccountService.createPrincipalAccount(request);

            log.info("Principal account created successfully for consumer: {} ({})", consumerId, consumerEmail);

        } catch (Exception e) {
            // Log the error but don't fail the onboarding process
            // In production, consider sending alert/notification for manual investigation
            log.error("Failed to create principal account for consumer: {} ({}). Error: {}",
                consumerId, consumerEmail, e.getMessage(), e);
        }
    }
}
