package com.infobeans.consumerfinance.domain.enums;

/**
 * Enum for Account Status
 *
 * Represents the states of linked accounts:
 * - ACTIVE: Account is active and operational
 * - DISABLED: Account is disabled but not deleted
 * - ARCHIVED: Account is archived and no longer in use
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
public enum AccountStatus {
    ACTIVE("Active"),
    DISABLED("Disabled"),
    ARCHIVED("Archived");

    private final String displayName;

    AccountStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
