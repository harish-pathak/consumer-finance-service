package com.infobeans.consumerfinance.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.infobeans.consumerfinance.validation.PAN;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for consumer onboarding.
 *
 * Contains all required and optional fields for onboarding a new consumer.
 * Organized into logical groups: personal, identity, employment, and financial.
 * All sensitive fields are marked with documentation for encryption handling.
 *
 * Validation:
 * - JSR-380 Bean Validation annotations enforce field-level constraints
 * - Validation errors are automatically mapped to 400 Bad Request responses
 * - Custom error messages provide clear guidance for field-level failures
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateConsumerOnboardingRequest {

    // ==================== PERSONAL INFORMATION ====================

    /**
     * Consumer's first name.
     * Required field.
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    /**
     * Consumer's last name.
     * Required field.
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    /**
     * Consumer's email address.
     * Required field. Must be unique to detect duplicate registrations.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * Consumer's phone number.
     * Optional field.
     */
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(
        regexp = "^[+]?[0-9]{10,20}$",
        message = "Phone number must be a valid format (10-20 digits, optional + prefix)"
    )
    private String phone;

    /**
     * Consumer's date of birth.
     * Optional field. Must be in the past and indicate age >= 18.
     */
    @PastOrPresent(message = "Date of birth cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    // ==================== IDENTITY INFORMATION ====================

    /**
     * National ID or government-issued identifier.
     * Required field. Must be unique to detect duplicate registrations.
     * SENSITIVE: Will be encrypted before persistence.
     */
    @NotBlank(message = "National ID is required")
    @Size(min = 5, max = 50, message = "National ID must be between 5 and 50 characters")
    private String nationalId;

    /**
     * Type of identity document.
     * Required field. Example values: PASSPORT, NATIONAL_ID, DRIVER_LICENSE, PAN, AADHAR.
     */
    @NotBlank(message = "Document type is required")
    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    /**
     * Identity document number.
     * Optional field.
     * SENSITIVE: Will be encrypted before persistence.
     */
    @Size(max = 100, message = "Document number must not exceed 100 characters")
    private String documentNumber;

    /**
     * Indian PAN (Permanent Account Number).
     * Required field. Must be unique to detect duplicate registrations.
     * Format: ABCDE1234F (5 letters, 4 digits, 1 letter).
     * Example values: ABCDE1234F, XYZPA5678B
     */
    @NotBlank(message = "PAN number is required")
    @PAN(message = "PAN number must be exactly 10 characters in format ABCDE1234F (5 letters, 4 digits, 1 letter)")
    private String panNumber;

    // ==================== EMPLOYMENT INFORMATION ====================

    /**
     * Name of employer.
     * Optional field.
     * SENSITIVE: Will be encrypted before persistence.
     */
    @Size(max = 255, message = "Employer name must not exceed 255 characters")
    private String employerName;

    /**
     * Job position or title.
     * Optional field.
     */
    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    /**
     * Type of employment.
     * Optional field. Example values: FULL_TIME, PART_TIME, SELF_EMPLOYED, CONTRACT, UNEMPLOYED.
     */
    @Size(max = 50, message = "Employment type must not exceed 50 characters")
    private String employmentType;

    /**
     * Years of work experience.
     * Optional field. Must be a non-negative integer.
     */
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 80, message = "Years of experience must be realistic")
    private Integer yearsOfExperience;

    /**
     * Industry sector.
     * Optional field.
     */
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    // ==================== FINANCIAL INFORMATION ====================

    /**
     * Monthly income in base currency.
     * Optional field. Must be a positive decimal value.
     * SENSITIVE: Will be encrypted before persistence.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly income must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Monthly income must be a valid monetary amount (up to 15 digits, 2 decimal places)")
    private BigDecimal monthlyIncome;

    /**
     * Annual income in base currency.
     * Optional field. Must be a positive decimal value.
     * SENSITIVE: Will be encrypted before persistence.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Annual income must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Annual income must be a valid monetary amount (up to 15 digits, 2 decimal places)")
    private BigDecimal annualIncome;

    /**
     * Source of income.
     * Optional field. Example values: SALARY, BUSINESS, INVESTMENT, RENTAL, PENSION.
     * SENSITIVE: Will be encrypted before persistence.
     */
    @Size(max = 255, message = "Income source must not exceed 255 characters")
    private String incomeSource;

    /**
     * Currency code for financial amounts.
     * Optional field. Defaults to USD if not provided.
     * Example values: USD, EUR, INR, GBP.
     */
    @Size(min = 3, max = 3, message = "Currency code must be a 3-letter ISO 4217 code")
    private String currency;
}
