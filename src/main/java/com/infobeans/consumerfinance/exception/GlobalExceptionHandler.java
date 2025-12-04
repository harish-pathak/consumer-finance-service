package com.infobeans.consumerfinance.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for REST Controllers.
 *
 * Centralized exception handling for all REST endpoints.
 * Maps custom business exceptions and Spring exceptions to appropriate HTTP responses.
 *
 * Handled exceptions:
 * - ValidationException (400 Bad Request)
 * - DuplicateResourceException (409 Conflict)
 * - ResourceNotFoundException (404 Not Found)
 * - UnauthorizedException (401 Unauthorized)
 * - MethodArgumentNotValidException (400 Bad Request) - JSR-380 validation
 * - DataIntegrityViolationException (409 Conflict) - Database constraints
 * - AuthenticationException (401 Unauthorized) - Spring Security
 * - Generic Exception (500 Internal Server Error)
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle DuplicateResourceException (409 Conflict).
     * Returned when attempting to create a resource that violates uniqueness constraints.
     *
     * @param ex DuplicateResourceException
     * @param request WebRequest
     * @return ErrorResponse with 409 Conflict status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("Duplicate resource exception: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .error("Duplicate Resource")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle ResourceNotFoundException (404 Not Found).
     * Returned when requested resource does not exist.
     *
     * @param ex ResourceNotFoundException
     * @param request WebRequest
     * @return ErrorResponse with 404 Not Found status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .error("Resource Not Found")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle ValidationException (400 Bad Request).
     * Returned when business logic validation fails.
     *
     * @param ex ValidationException
     * @param request WebRequest
     * @return ErrorResponse with 400 Bad Request status
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .error("Validation Failed")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UnauthorizedException (401 Unauthorized).
     * Returned when authentication or authorization fails.
     *
     * @param ex UnauthorizedException
     * @param request WebRequest
     * @return ErrorResponse with 401 Unauthorized status
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, WebRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .error("Unauthorized")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle MethodArgumentNotValidException (400 Bad Request).
     * Returned when JSR-380 @Valid validation fails on request body.
     * Includes field-level error messages for each validation failure.
     *
     * @param ex MethodArgumentNotValidException
     * @param request WebRequest
     * @return ErrorResponse with 400 Bad Request status and field-level errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed on request body");

        // Extract field-level validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Request validation failed")
                .error("Validation Error")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details(fieldErrors.toString())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle DataIntegrityViolationException (409 Conflict).
     * Returned when database constraints are violated (unique, foreign key, etc.).
     * Parses the exception message to identify which field caused the violation.
     *
     * @param ex DataIntegrityViolationException
     * @param request WebRequest
     * @return ErrorResponse with 409 Conflict status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        String message = "A resource with the provided data already exists";
        String details = null;

        if (ex.getMessage() != null) {
            String errorMsg = ex.getMessage().toLowerCase();

            // Parse specific constraint violations
            if (errorMsg.contains("duplicate entry")) {
                if (errorMsg.contains("email")) {
                    message = "A consumer with this email already exists";
                    details = "Email must be unique";
                } else if (errorMsg.contains("national_id") || errorMsg.contains("national id")) {
                    message = "A consumer with this national ID already exists";
                    details = "National ID must be unique";
                } else if (errorMsg.contains("phone")) {
                    message = "A consumer with this phone number already exists";
                    details = "Phone number must be unique";
                } else if (errorMsg.contains("document_number") || errorMsg.contains("document number")) {
                    message = "A consumer with this document number already exists";
                    details = "Document number must be unique";
                } else {
                    message = "Duplicate entry: " + extractDuplicateValue(ex.getMessage());
                    details = "One or more fields violate unique constraints";
                }
            } else if (errorMsg.contains("foreign key")) {
                message = "Referenced resource does not exist";
                details = "Check foreign key constraints";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(message)
                .error("Constraint Violation")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details(details)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Extract the duplicate value from the error message.
     * Helps users identify exactly what value caused the conflict.
     *
     * @param errorMessage the full error message from the database
     * @return extracted value or null if cannot be parsed
     */
    private String extractDuplicateValue(String errorMessage) {
        try {
            // MySQL format: "Duplicate entry 'value' for key 'column'"
            if (errorMessage.contains("Duplicate entry")) {
                int start = errorMessage.indexOf("'");
                int end = errorMessage.indexOf("'", start + 1);
                if (start != -1 && end != -1 && start < end) {
                    return errorMessage.substring(start + 1, end);
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract duplicate value from error message", e);
        }
        return null;
    }

    /**
     * Handle Spring Security AuthenticationException (401 Unauthorized).
     * Returned when OAuth2/JWT token is missing, invalid, or expired.
     *
     * @param ex AuthenticationException
     * @param request WebRequest
     * @return ErrorResponse with 401 Unauthorized status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        String message = "Authentication failed";
        if (ex instanceof BadCredentialsException) {
            message = "Invalid OAuth2/JWT token or credentials";
        }
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .error("Authentication Failed")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle generic Exception (500 Internal Server Error).
     * Catch-all handler for unexpected exceptions.
     *
     * @param ex Exception
     * @param request WebRequest
     * @return ErrorResponse with 500 Internal Server Error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred", ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An internal server error occurred")
                .error(ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
