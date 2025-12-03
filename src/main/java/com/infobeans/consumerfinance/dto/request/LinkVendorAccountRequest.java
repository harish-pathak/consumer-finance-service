package com.infobeans.consumerfinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for linking a vendor account to a consumer.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkVendorAccountRequest {

    @NotNull(message = "Consumer ID is required")
    private UUID consumerId;

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    @NotBlank(message = "Vendor account ID is required")
    @Size(max = 100, message = "Vendor account ID must not exceed 100 characters")
    private String vendorAccountId;

    @Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    private String metadata;
}
