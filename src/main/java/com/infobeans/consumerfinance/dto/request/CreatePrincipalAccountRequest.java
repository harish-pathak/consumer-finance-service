package com.infobeans.consumerfinance.dto.request;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a principal account.
 *
 * Accepts consumer ID and optional account type for principal account creation.
 * Validates that consumer ID is a valid UUID format.
 * Account type defaults to PRIMARY if not provided.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrincipalAccountRequest {

    /**
     * Consumer ID (UUID) that this principal account belongs to.
     * Must be a valid UUID in string format (36 characters).
     * Required field.
     */
    @NotBlank(message = "Consumer ID is required")
    @Pattern(
        regexp = "^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$|^[a-f0-9]{36}$",
        message = "Consumer ID must be a valid UUID format (with or without hyphens)"
    )
    private String consumerId;

    /**
     * Account type for the principal account (e.g., PRIMARY, SECONDARY).
     * Optional field. Defaults to PRIMARY if not provided.
     */
    @Builder.Default
    private String accountType = "PRIMARY";

    /**
     * Account status (ACTIVE, INACTIVE, ARCHIVED, SUSPENDED).
     * Optional field. Defaults to ACTIVE if not provided.
     */
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;
}
