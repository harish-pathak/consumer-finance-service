package com.infobeans.consumerfinance.event;

import com.infobeans.consumerfinance.repository.VendorRepository;
import com.infobeans.consumerfinance.service.VendorLinkedAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for consumer onboarding completion.
 *
 * Automatically creates vendor-linked accounts for newly onboarded consumers.
 * Links each new consumer to all active vendors in the system.
 *
 * Triggered by: OnboardingCompletedEvent published during consumer onboarding
 * Action: Creates VendorLinkedAccount records for each active vendor
 *
 * This ensures that every new consumer is automatically linked to available vendors
 * as part of the onboarding process, enabling immediate access to vendor services.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VendorAccountCreationListener {

    private final VendorLinkedAccountService vendorLinkedAccountService;
    private final VendorRepository vendorRepository;

    /**
     * Listen for OnboardingCompletedEvent and automatically link consumer to all active vendors.
     *
     * This method is triggered asynchronously when a consumer completes onboarding.
     * It retrieves all active vendors and creates a vendor-linked account for each one.
     * Uses idempotent creation to ensure no duplicates are created even with concurrent requests.
     *
     * @param event OnboardingCompletedEvent containing consumer information
     */
    @EventListener
    @Transactional
    public void onOnboardingCompleted(OnboardingCompletedEvent event) {
        String consumerId = event.getConsumerId();
        String consumerEmail = event.getEmail();

        try {
            log.info("Processing OnboardingCompletedEvent for consumer: {} ({}). Starting vendor account linking...",
                consumerId, consumerEmail);

            // Get all active vendors from the system
            var activeVendors = vendorRepository.findAll().stream()
                .filter(vendor -> vendor.getStatus().name().equals("ACTIVE"))
                .toList();

            if (activeVendors.isEmpty()) {
                log.info("No active vendors found. Skipping vendor account creation for consumer: {}", consumerId);
                return;
            }

            log.info("Found {} active vendors. Creating vendor-linked accounts for consumer: {}",
                activeVendors.size(), consumerId);

            // Create vendor-linked account for each active vendor (idempotent)
            activeVendors.forEach(vendor -> {
                try {
                    // Use the idempotent service method - if account already exists, it returns existing
                    var request = com.infobeans.consumerfinance.dto.request.LinkVendorAccountRequest.builder()
                        .consumerId(consumerId)
                        .vendorId(vendor.getId())
                        .build();

                    var response = vendorLinkedAccountService.createOrLinkVendorAccount(request);
                    log.debug("Vendor-linked account created/linked for consumer: {} and vendor: {} ({})",
                        consumerId, vendor.getId(), vendor.getName());

                } catch (Exception vendorLinkException) {
                    // Log but don't fail the onboarding process if one vendor link fails
                    log.error("Failed to create vendor-linked account for consumer: {} and vendor: {} ({}). Error: {}",
                        consumerId, vendor.getId(), vendor.getName(), vendorLinkException.getMessage());
                }
            });

            log.info("Vendor account linking completed successfully for consumer: {} ({})", consumerId, consumerEmail);

        } catch (Exception e) {
            // Log the error but don't fail the onboarding process
            // In production, consider sending alert/notification for manual investigation
            log.error("Failed to process vendor account linking for consumer: {} ({}). Error: {}",
                consumerId, consumerEmail, e.getMessage(), e);
        }
    }
}
