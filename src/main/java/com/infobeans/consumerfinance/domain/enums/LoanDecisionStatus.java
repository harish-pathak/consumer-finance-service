package com.infobeans.consumerfinance.domain.enums;

/**
 * Enum representing the possible decisions that can be made on a loan application.
 *
 * Values:
 * - APPROVED: Application has been approved for loan disbursement
 * - REJECTED: Application has been rejected, loan will not be provided
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public enum LoanDecisionStatus {
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String displayName;

    LoanDecisionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
