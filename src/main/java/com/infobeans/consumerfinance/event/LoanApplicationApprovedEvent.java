package com.infobeans.consumerfinance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when a loan application is approved.
 *
 * Design:
 * - Extends Spring ApplicationEvent for integration with ApplicationEventPublisher
 * - Immutable after creation: captures snapshot of approval state
 * - Decoupled from implementation: listeners subscribe independently
 * - Synchronous by default: published within same transaction as decision persistence
 *
 * Purpose:
 * Signals downstream consumers that a loan has been approved and is ready for:
 * - Disbursement processing
 * - Account setup
 * - Notification delivery (email, SMS)
 * - Risk assessment
 * - Reporting and analytics
 *
 * Publishing:
 * - Published by LoanDecisionService.submitDecision() after successful APPROVED decision persistence
 * - Within same @Transactional context: event published before transaction commit
 * - If listener fails, decision persistence may rollback (depends on listener configuration)
 *
 * Example usage:
 * applicationEventPublisher.publishEvent(new LoanApplicationApprovedEvent(
 *     this,
 *     applicationId,
 *     consumerId,
 *     requestedAmount,
 *     staffId,
 *     approvedAt
 * ));
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Getter
public class LoanApplicationApprovedEvent extends ApplicationEvent {

    /**
     * The unique identifier of the loan application that was approved.
     * Format: 36-character UUID string.
     * Used by listeners to fetch full application details if needed.
     */
    private String applicationId;

    /**
     * The consumer ID whose loan application was approved.
     * Format: 36-character UUID string.
     * Used for routing: which consumer's accounts/notifications to update.
     */
    private String consumerId;

    /**
     * The approved loan amount.
     * Decimal value with 2 decimal places.
     * Used for disbursement and accounting purposes.
     */
    private java.math.BigDecimal approvedAmount;

    /**
     * The staff member who approved the application.
     * Contains the staff ID/username extracted from JWT.
     * Used for audit and compliance tracking.
     */
    private String approvedBy;

    /**
     * Timestamp when the approval was recorded.
     * ISO 8601 format.
     * Marks the official approval decision time.
     */
    private LocalDateTime approvedAt;

    /**
     * Constructor for Spring ApplicationEvent.
     * Builder will call this via the object construction.
     *
     * @param source the object on which the event initially occurred
     */
    public LoanApplicationApprovedEvent(Object source, String applicationId, String consumerId,
                                       BigDecimal approvedAmount, String approvedBy, LocalDateTime approvedAt) {
        super(source);
        this.applicationId = applicationId;
        this.consumerId = consumerId;
        this.approvedAmount = approvedAmount;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }
}
