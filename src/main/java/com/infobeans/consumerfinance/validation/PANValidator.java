package com.infobeans.consumerfinance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for @PAN annotation.
 *
 * Validates Indian PAN (Permanent Account Number) format:
 * - Pattern: [A-Z]{5}[0-9]{4}[A-Z]{1}
 * - Example: ABCDE1234F
 *
 * Format breakdown:
 * - First 5 characters: Uppercase letters
 * - Next 4 characters: Digits (0-9)
 * - Last 1 character: Uppercase letter
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public class PANValidator implements ConstraintValidator<PAN, String> {

    /**
     * Indian PAN regex pattern: 5 letters, 4 digits, 1 letter (all uppercase).
     */
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    @Override
    public void initialize(PAN constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String panNumber, ConstraintValidatorContext context) {
        // Null or empty values are handled by @NotBlank annotation
        // This validator only checks format if value is present
        if (panNumber == null || panNumber.isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        // Validate PAN format
        return PAN_PATTERN.matcher(panNumber).matches();
    }
}
