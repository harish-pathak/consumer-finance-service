package com.infobeans.consumerfinance.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler
 * Tests exception handling and error message generation
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
        when(mockRequest.getDescription(false)).thenReturn("uri=/api/v1/consumers");
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException with custom message")
    void testHandleDuplicateResource() {
        // Arrange
        String customMessage = "Consumer with email 'test@example.com' already exists";
        DuplicateResourceException exception = new DuplicateResourceException(customMessage);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResource(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals(customMessage, response.getBody().getMessage());
        assertEquals("Duplicate Resource", response.getBody().getError());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void testHandleResourceNotFound() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Consumer", "id", "123");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertNotNull(response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle ValidationException")
    void testHandleValidationException() {
        // Arrange
        ValidationException exception = new ValidationException("Email is required");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Email is required", response.getBody().getMessage());
        assertEquals("Validation Failed", response.getBody().getError());
    }

    @Test
    @DisplayName("Should detect duplicate email in DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_DuplicateEmail() {
        // Arrange
        String errorMsg = "Duplicate entry 'test@example.com' for key 'consumers.UK_email'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("email"),
                "Message should contain 'email'");
        assertTrue(response.getBody().getMessage().contains("already exists"));
        assertEquals("Constraint Violation", response.getBody().getError());
    }

    @Test
    @DisplayName("Should detect duplicate phone in DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_DuplicatePhone() {
        // Arrange
        String errorMsg = "Duplicate entry '+1234567890' for key 'consumers.UK_phone'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("phone"),
                "Message should contain 'phone'");
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should detect duplicate national ID in DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_DuplicateNationalId() {
        // Arrange
        String errorMsg = "Duplicate entry '123-45-6789' for key 'consumers.UK_national_id'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("national ID"),
                "Message should contain 'national ID'");
    }

    @Test
    @DisplayName("Should detect duplicate document number in DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_DuplicateDocumentNumber() {
        // Arrange
        String errorMsg = "Duplicate entry 'ID123456789' for key 'consumers.UK_document_number'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("document number"),
                "Message should contain 'document number'");
    }

    @Test
    @DisplayName("Should extract duplicate value from error message")
    void testHandleDataIntegrityViolation_ExtractValue() {
        // Arrange
        String errorMsg = "Duplicate entry 'test.value@example.com' for key 'UK_email'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertNotNull(response.getBody().getDetails());
        assertTrue(response.getBody().getMessage().contains("email"));
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException with null message")
    void testHandleDataIntegrityViolation_NullMessage() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException("error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody().getMessage());
        assertEquals("Constraint Violation", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle foreign key constraint violation")
    void testHandleDataIntegrityViolation_ForeignKey() {
        // Arrange
        String errorMsg = "Cannot delete or update a parent row: a foreign key constraint fails";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Referenced resource"));
    }

    @Test
    @DisplayName("Should include error details in response")
    void testHandleDataIntegrityViolation_ErrorDetails() {
        // Arrange
        String errorMsg = "Duplicate entry 'test@example.com' for key 'UK_email'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        assertNotNull(response.getBody().getDetails());
        assertTrue(response.getBody().getDetails().contains("unique"));
    }

    @Test
    @DisplayName("Should handle generic exception with 500 status")
    void testHandleGlobalException() {
        // Arrange
        Exception exception = new Exception("Unexpected error occurred");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("An internal server error occurred", response.getBody().getMessage());
        assertEquals("Exception", response.getBody().getError());
    }

    @Test
    @DisplayName("Should include path in error response")
    void testHandleException_IncludePath() {
        // Arrange
        when(mockRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
        DuplicateResourceException exception = new DuplicateResourceException("Duplicate data");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResource(exception, mockRequest);

        // Assert
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("Should handle UnauthorizedException")
    void testHandleUnauthorized() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorized(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    @Test
    @DisplayName("Should provide helpful message for duplicate email constraint")
    void testHandleDataIntegrityViolation_HelpfulEmailMessage() {
        // Arrange
        String errorMsg = "Duplicate entry 'john@example.com' for key 'consumers.idx_email'";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMsg);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        // Assert
        String message = response.getBody().getMessage();
        assertTrue(message.contains("consumer"));
        assertTrue(message.contains("email"));
        assertTrue(message.contains("already exists"));
    }
}
