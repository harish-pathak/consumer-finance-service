package com.infobeans.consumerfinance.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Domain event published when consumer onboarding is completed successfully.
 *
 * This event serves as an integration point for downstream operations such as:
 * - Principal Account creation
 * - KYC verification
 * - Credit limit assignment
 * - Welcome communication
 *
 * Usage:
 * Listeners can subscribe to this event using @EventListener annotation
 * or implement ApplicationListener<OnboardingCompletedEvent> interface.
 *
 * Example listener:
 * @Component
 * public class PrincipalAccountCreationListener {
 *     @EventListener
 *     public void onOnboardingCompleted(OnboardingCompletedEvent event) {
 *         // Create principal account for consumer
 *         principalAccountService.createPrincipalAccount(event.getConsumerId());
 *     }
 * }
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OnboardingCompletedEvent extends ApplicationEvent {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique consumer identifier (UUID string) of the newly onboarded consumer.
     * Required field.
     */
    private String consumerId;

    /**
     * Consumer's email address.
     * Useful for identifying the consumer in event listeners.
     */
    private String email;

    /**
     * Consumer's full name (formatted as "firstName lastName").
     * Useful for logging and communication.
     */
    private String consumerName;

    /**
     * Timestamp when the onboarding was completed.
     * Typically same as consumer creation timestamp.
     */
    private LocalDateTime onboardingCompletedAt;

    /**
     * Source of the onboarding event (e.g., "api", "mobile_app", "batch_import").
     * Useful for tracking different onboarding channels.
     */
    private String eventSource;

    /**
     * Additional metadata as a JSON string or Map.
     * Can contain additional context like IP address, user agent, etc.
     */
    private String metadata;

    /**
     * Default constructor with source parameter.
     * Required by ApplicationEvent base class.
     */
    public OnboardingCompletedEvent(Object source) {
        super(source);
    }

    /**
     * Full constructor for Spring ApplicationEvent.
     * Required for proper event publishing.
     */
    public OnboardingCompletedEvent(
        Object source,
        String consumerId,
        String email,
        String consumerName,
        LocalDateTime onboardingCompletedAt,
        String eventSource,
        String metadata
    ) {
        super(source);
        this.consumerId = consumerId;
        this.email = email;
        this.consumerName = consumerName;
        this.onboardingCompletedAt = onboardingCompletedAt;
        this.eventSource = eventSource;
        this.metadata = metadata;
    }
}
