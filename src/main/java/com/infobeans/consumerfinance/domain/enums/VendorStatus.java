package com.infobeans.consumerfinance.domain.enums;

/**
 * Enum for Vendor Status
 *
 * Represents the operational state of a vendor:
 * - ACTIVE: Vendor is active and operational
 * - INACTIVE: Vendor is inactive; linked accounts should be disabled
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
public enum VendorStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String displayName;

    VendorStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
