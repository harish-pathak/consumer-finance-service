package com.infobeans.consumerfinance.domain.enums;

/**
 * Enum for Repayment Status
 *
 * Represents the states of a repayment transaction:
 * - PENDING: Repayment due but not yet processed
 * - SUCCESS: Repayment successfully processed
 * - FAILED: Repayment transaction failed
 * - REVERSED: Repayment transaction reversed/refunded
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
public enum RepaymentStatus {
    PENDING("Pending"),
    SUCCESS("Successful"),
    FAILED("Failed"),
    REVERSED("Reversed");

    private final String displayName;

    RepaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
