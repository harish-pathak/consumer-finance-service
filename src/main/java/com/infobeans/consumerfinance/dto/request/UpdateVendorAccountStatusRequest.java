package com.infobeans.consumerfinance.dto.request;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating vendor-linked account status.
 *
 * Used by internal/system endpoints to change the lifecycle status of a vendor-linked account.
 * Supports transitions between ACTIVE, DISABLED, and ARCHIVED states.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVendorAccountStatusRequest {

    /**
     * Target status for the vendor-linked account
     * Allowed values: ACTIVE, DISABLED, ARCHIVED
     */
    @NotNull(message = "Status is required")
    private AccountStatus status;

    /**
     * Optional reason for the status change
     * Used for audit trail and tracking purposes
     */
    private String reason;
}
