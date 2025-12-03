package com.infobeans.consumerfinance.domain.enums;

/**
 * Enum for Loan Application Status
 *
 * Represents the lifecycle stages of a loan application:
 * - PENDING: Application submitted, awaiting review
 * - APPROVED: Application approved, ready for disbursement
 * - REJECTED: Application rejected after review
 * - CANCELLED: Application cancelled by consumer or system
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
public enum LoanApplicationStatus {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    private final String displayName;

    LoanApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
