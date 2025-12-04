package com.infobeans.consumerfinance.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new loan application.
 *
 * Captures the consumer's loan request details including:
 * - Consumer ID (required) - the consumer for whom the application is being created
 * - Requested loan amount (in base currency)
 * - Desired loan term (in months)
 * - Purpose of the loan (optional)
 *
 * Design Philosophy:
 * - Portal login (JWT user) is INDEPENDENT from the consumer for whom the application is created
 * - Allows agents/staff to submit applications on behalf of consumers
 * - JWT authentication validates the user has permission, consumerId in payload specifies the target consumer
 *
 * Validation:
 * - JSR-380 Bean Validation annotations enforce field-level constraints
 * - consumerId: Required, must be a valid UUID format (36 characters)
 * - requestedAmount: Required, must be positive, supports up to 15 digits with 2 decimal places
 * - termInMonths: Optional but if provided, must be between 3 and 360 months (30 years)
 * - purpose: Optional, max 255 characters for flexibility
 *
 * All validation errors are automatically mapped to 400 Bad Request responses
 * with field-level error messages for API clarity.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateLoanApplicationRequest {

    /**
     * The consumer ID for whom the loan application is being created.
     * Required field. Must be a valid UUID (36-character string).
     * This is INDEPENDENT from the portal login user (JWT principal).
     *
     * Use Cases:
     * - Consumer submits own application: consumerId = their own UUID from JWT
     * - Agent submits on behalf of consumer: consumerId = customer's UUID (from JWT, request body can differ)
     * - Admin system integration: consumerId = target consumer UUID
     *
     * Examples:
     * - "550e8400-e29b-41d4-a716-446655440000" (valid)
     * - "invalid-id" (invalid - must be UUID format)
     * - null (invalid - required)
     */
    @NotBlank(message = "Consumer ID is required")
    @Size(min = 36, max = 36, message = "Consumer ID must be a valid UUID (36 characters)")
    private String consumerId;

    /**
     * The requested loan amount.
     * Required field. Must be a positive decimal value.
     * Supports up to 15 digits with 2 decimal places.
     *
     * Examples:
     * - 10000.00 (valid)
     * - 50000.50 (valid)
     * - 0 (invalid - must be greater than 0)
     * - -5000 (invalid - cannot be negative)
     */
    @NotNull(message = "Requested amount is required")
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "Requested amount must be greater than 0"
    )
    @Digits(
        integer = 15,
        fraction = 2,
        message = "Requested amount must be a valid monetary amount (up to 15 digits, 2 decimal places)"
    )
    private BigDecimal requestedAmount;

    /**
     * The desired loan term in months.
     * Optional field. If provided, must be between 3 and 360 months (3 months to 30 years).
     * Allows flexibility: 3 months for short-term, 360 months for long-term loans.
     *
     * Examples:
     * - 12 (1 year - valid)
     * - 60 (5 years - valid)
     * - 360 (30 years - valid)
     * - 2 (too short - invalid)
     * - 400 (too long - invalid)
     * - null (optional, system can use defaults)
     */
    @Min(
        value = 3,
        message = "Loan term must be at least 3 months"
    )
    @Max(
        value = 360,
        message = "Loan term must not exceed 360 months (30 years)"
    )
    private Integer termInMonths;

    /**
     * The purpose of the loan.
     * Optional field. Free-form text describing why the consumer needs the loan.
     *
     * Examples:
     * - "Home renovation" (valid)
     * - "Business expansion" (valid)
     * - "Education fees" (valid)
     * - null (optional, not required)
     *
     * Max 255 characters to maintain database compatibility and UI usability.
     */
    @Size(
        max = 255,
        message = "Loan purpose must not exceed 255 characters"
    )
    private String purpose;
}
