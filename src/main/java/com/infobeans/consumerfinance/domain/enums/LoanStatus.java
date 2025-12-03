package com.infobeans.consumerfinance.domain.enums;

/**
 * Enum for Loan Status
 *
 * Represents the lifecycle stages of a disbursed loan:
 * - ACTIVE: Loan is active and in repayment phase
 * - CLOSED: Loan fully repaid and closed
 * - DEFAULTED: Loan in default due to missed payments
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
public enum LoanStatus {
    ACTIVE("Active"),
    CLOSED("Closed"),
    DEFAULTED("Defaulted");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
