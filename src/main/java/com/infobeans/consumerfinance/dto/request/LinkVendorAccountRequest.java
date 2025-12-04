package com.infobeans.consumerfinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for linking a vendor account to a consumer.
 *
 * This DTO is used to create or link a vendor account for a consumer.
 * The request includes consumer ID, vendor ID, and optional metadata fields.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkVendorAccountRequest {

    /**
     * Consumer ID (UUID format as String)
     * Required for identifying the consumer who owns this account link
     */
    @NotBlank(message = "Consumer ID is required")
    @Pattern(
        regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        message = "Consumer ID must be a valid UUID format",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    private String consumerId;

    /**
     * Vendor ID (UUID format as String)
     * Required for identifying the vendor to link
     */
    @NotBlank(message = "Vendor ID is required")
    @Pattern(
        regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        message = "Vendor ID must be a valid UUID format",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    private String vendorId;

    /**
     * External account reference in the vendor's system
     * Optional field for tracking the account ID in the vendor system
     */
    @Size(max = 255, message = "External account reference must not exceed 255 characters")
    private String externalAccountRef;

    /**
     * Internal linkage identifier
     * Optional field for system-specific tracking
     */
    @Size(max = 100, message = "Linkage ID must not exceed 100 characters")
    private String linkageId;

    /**
     * Additional metadata as JSON string
     * Optional field for storing additional vendor-specific information
     */
    @Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    private String metadata;
}
