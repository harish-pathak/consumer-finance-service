package com.infobeans.consumerfinance.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for Indian PAN (Permanent Account Number).
 *
 * Validates that a string follows the standard Indian PAN format:
 * - Exactly 10 characters
 * - Pattern: ABCDE1234F (5 letters, 4 digits, 1 letter)
 * - Fourth character must be 'P' for individual PANs (optional check, can be customized)
 *
 * Example valid PANs: ABCDE1234F, XYZPA5678B
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = PANValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PAN {

    /**
     * Error message when validation fails.
     */
    String message() default "PAN number must be in valid Indian PAN format (e.g., ABCDE1234F)";

    /**
     * Validation groups (for conditional validation).
     */
    Class<?>[] groups() default {};

    /**
     * Additional payload data.
     */
    Class<? extends Payload>[] payload() default {};
}
