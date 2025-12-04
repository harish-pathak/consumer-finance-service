package com.infobeans.consumerfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import com.infobeans.consumerfinance.dto.request.CreateLoanApplicationRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.service.LoanApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LoanApplicationController.
 * Tests REST endpoints for loan application creation and retrieval with authentication and error handling.
 *
 * Test Coverage:
 * 1. Successful loan application creation (HTTP 201)
 * 2. Duplicate detection returns 409 Conflict
 * 3. Consumer not found returns 404
 * 4. Validation errors return 400
 * 5. Missing authentication returns 401
 * 6. Successful retrieval by ID (HTTP 200)
 * 7. Retrieval not found returns 404
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("LoanApplicationController Integration Tests")
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanApplicationService loanApplicationService;

    private CreateLoanApplicationRequest validRequest;
    private LoanApplicationResponse validResponse;
    private String testConsumerId;
    private String testApplicationId;
    private String validJwt;

    @BeforeEach
    void setUp() {
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testApplicationId = "550e8400-e29b-41d4-a716-446655441000";

        validRequest = CreateLoanApplicationRequest.builder()
            .consumerId(testConsumerId)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("Home renovation")
            .build();

        validResponse = LoanApplicationResponse.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("Home renovation")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // Mock JWT token
        validJwt = "valid.jwt.token";
    }

    // ============ CREATE LOAN APPLICATION TESTS ============

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Create loan application successfully")
    void testCreateLoanApplication_Success() throws Exception {
        // Arrange
        when(loanApplicationService.createLoanApplication(testConsumerId, validRequest))
            .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("created successfully")))
            .andExpect(jsonPath("$.data.id", is(testApplicationId)))
            .andExpect(jsonPath("$.data.consumerId", is(testConsumerId)))
            .andExpect(jsonPath("$.data.status", is("PENDING")))
            .andExpect(jsonPath("$.data.requestedAmount", is(50000.00)))
            .andExpect(jsonPath("$.data.termInMonths", is(60)))
            .andExpect(jsonPath("$.data.purpose", is("Home renovation")));

        verify(loanApplicationService).createLoanApplication(testConsumerId, validRequest);
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Create with only required fields")
    void testCreateLoanApplication_MinimalRequest_Success() throws Exception {
        // Arrange
        CreateLoanApplicationRequest minimalRequest = CreateLoanApplicationRequest.builder()
            .consumerId(testConsumerId)
            .requestedAmount(new BigDecimal("10000.00"))
            .build();

        LoanApplicationResponse minimalResponse = LoanApplicationResponse.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("10000.00"))
            .termInMonths(null)
            .purpose(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(loanApplicationService.createLoanApplication(testConsumerId, minimalRequest))
            .thenReturn(minimalResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(minimalRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.requestedAmount", is(10000.00)));

        verify(loanApplicationService).createLoanApplication(testConsumerId, minimalRequest);
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 404 when consumer does not exist")
    void testCreateLoanApplication_ConsumerNotFound() throws Exception {
        // Arrange
        when(loanApplicationService.createLoanApplication(anyString(), any(CreateLoanApplicationRequest.class)))
            .thenThrow(new ResourceNotFoundException("Consumer", "id", testConsumerId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Resource Not Found")))
            .andExpect(jsonPath("$.message", containsString("Consumer")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 409 when PENDING application already exists")
    void testCreateLoanApplication_DuplicatePendingApplication() throws Exception {
        // Arrange
        String duplicateMessage = "Consumer with ID '" + testConsumerId + "' already has a pending loan application";
        when(loanApplicationService.createLoanApplication(anyString(), any(CreateLoanApplicationRequest.class)))
            .thenThrow(new DuplicateResourceException(duplicateMessage));

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("already has a pending loan application")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 409 with existing application ID in error message")
    void testCreateLoanApplication_DuplicateWithApplicationId() throws Exception {
        // Arrange
        String duplicateMessage = "Consumer with ID '" + testConsumerId
            + "' already has a pending loan application (ID: " + testApplicationId
            + ", created: 2024-12-04T10:30:00)";
        when(loanApplicationService.createLoanApplication(anyString(), any(CreateLoanApplicationRequest.class)))
            .thenThrow(new DuplicateResourceException(duplicateMessage));

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString(testApplicationId)));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 400 for validation error: missing requestedAmount")
    void testCreateLoanApplication_ValidationError_MissingAmount() throws Exception {
        // Arrange - request without requestedAmount
        String invalidJson = "{ \"termInMonths\": 60, \"purpose\": \"Home renovation\" }";

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 400 for validation error: negative amount")
    void testCreateLoanApplication_ValidationError_NegativeAmount() throws Exception {
        // Arrange
        CreateLoanApplicationRequest invalidRequest = CreateLoanApplicationRequest.builder()
            .consumerId(testConsumerId)
            .requestedAmount(new BigDecimal("-5000.00"))
            .termInMonths(60)
            .purpose("Invalid")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 400 for validation error: invalid term (too short)")
    void testCreateLoanApplication_ValidationError_TermTooShort() throws Exception {
        // Arrange
        CreateLoanApplicationRequest invalidRequest = CreateLoanApplicationRequest.builder()
            .consumerId(testConsumerId)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(2)  // Minimum is 3
            .purpose("Invalid")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 400 for validation error: invalid term (too long)")
    void testCreateLoanApplication_ValidationError_TermTooLong() throws Exception {
        // Arrange
        CreateLoanApplicationRequest invalidRequest = CreateLoanApplicationRequest.builder()
            .consumerId(testConsumerId)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(400)  // Maximum is 360
            .purpose("Invalid")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("POST /api/v1/loan-applications - Return 400 for validation error: purpose too long")
    void testCreateLoanApplication_ValidationError_PurposeTooLong() throws Exception {
        // Arrange
        CreateLoanApplicationRequest invalidRequest = CreateLoanApplicationRequest.builder()
            .consumerId(testConsumerId)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("A".repeat(300))  // Exceeds max 255
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    // ============ RETRIEVE LOAN APPLICATION TESTS ============

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/loan-applications/{id} - Retrieve loan application successfully")
    void testGetLoanApplication_Success() throws Exception {
        // Arrange
        when(loanApplicationService.getLoanApplicationById(testApplicationId))
            .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId)
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(testApplicationId)))
            .andExpect(jsonPath("$.data.consumerId", is(testConsumerId)))
            .andExpect(jsonPath("$.data.status", is("PENDING")))
            .andExpect(jsonPath("$.data.requestedAmount", is(50000.00)));

        verify(loanApplicationService).getLoanApplicationById(testApplicationId);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/loan-applications/{id} - Return 404 when application does not exist")
    void testGetLoanApplication_NotFound() throws Exception {
        // Arrange
        String nonExistentId = "00000000-0000-0000-0000-000000000000";
        when(loanApplicationService.getLoanApplicationById(nonExistentId))
            .thenThrow(new ResourceNotFoundException("LoanApplication", "id", nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/loan-applications/" + nonExistentId)
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Resource Not Found")))
            .andExpect(jsonPath("$.message", containsString("LoanApplication")));
    }

    // ============ AUTHENTICATION TESTS ============

    @Test
    @DisplayName("POST /api/v1/loan-applications - Return 403 without authentication")
    void testCreateLoanApplication_NoAuthentication() throws Exception {
        // Act & Assert - No @WithMockUser, so request should be forbidden (403)
        mockMvc.perform(post("/api/v1/loan-applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isForbidden());

        // Service should not be called
        verify(loanApplicationService, never()).createLoanApplication(anyString(), any());
    }

    @Test
    @DisplayName("GET /api/v1/loan-applications/{id} - Return 403 without authentication")
    void testGetLoanApplication_NoAuthentication() throws Exception {
        // Act & Assert - No @WithMockUser, so request should be forbidden (403)
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        // Service should not be called
        verify(loanApplicationService, never()).getLoanApplicationById(anyString());
    }
}
