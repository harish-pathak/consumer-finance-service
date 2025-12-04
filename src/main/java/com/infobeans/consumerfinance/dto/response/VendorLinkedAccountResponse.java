package com.infobeans.consumerfinance.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Vendor-Linked Account operations.
 *
 * Represents a vendor-linked account in API responses. This is a read-only view
 * for consumers; lifecycle changes are managed by system/internal endpoints.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorLinkedAccountResponse {

    /**
     * Unique identifier for this vendor-linked account (UUID as String)
     */
    private String id;

    /**
     * Consumer ID who owns this vendor-linked account (UUID as String)
     */
    private String consumerId;

    /**
     * Principal account ID associated with this vendor link (UUID as String)
     */
    private String principalAccountId;

    /**
     * Vendor ID that this account is linked to (UUID as String)
     */
    private String vendorId;

    /**
     * Name of the vendor (for display purposes)
     */
    private String vendorName;

    /**
     * Lifecycle status: ACTIVE, DISABLED, or ARCHIVED
     */
    private AccountStatus status;

    /**
     * External reference ID in the vendor system (if available)
     */
    private String externalAccountRef;

    /**
     * Internal linkage identifier (if available)
     */
    private String linkageId;

    /**
     * Timestamp when this account was created
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Timestamp when this account was last updated
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
