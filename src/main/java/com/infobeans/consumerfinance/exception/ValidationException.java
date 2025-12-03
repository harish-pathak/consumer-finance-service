package com.infobeans.consumerfinance.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails for business rules or data constraints.
 * This exception can carry multiple field-level validation errors.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }

    /**
     * Constructs a new ValidationException with the specified detail message and errors map.
     *
     * @param message the detail message
     * @param errors  map of field names to error messages
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors != null ? new HashMap<>(errors) : new HashMap<>();
    }

    /**
     * Constructs a new ValidationException with a single field error.
     *
     * @param message   the detail message
     * @param fieldName the name of the field that failed validation
     * @param error     the validation error message
     */
    public ValidationException(String message, String fieldName, String error) {
        super(message);
        this.errors = new HashMap<>();
        this.errors.put(fieldName, error);
    }

    /**
     * Constructs a new ValidationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new HashMap<>();
    }

    /**
     * Gets the validation errors map.
     *
     * @return map of field names to error messages
     */
    public Map<String, String> getErrors() {
        return new HashMap<>(errors);
    }

    /**
     * Checks if there are any validation errors.
     *
     * @return true if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Adds a validation error for a specific field.
     *
     * @param fieldName the name of the field
     * @param error     the error message
     */
    public void addError(String fieldName, String error) {
        this.errors.put(fieldName, error);
    }
}
