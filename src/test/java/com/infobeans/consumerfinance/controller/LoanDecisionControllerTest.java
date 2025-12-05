package com.infobeans.consumerfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import com.infobeans.consumerfinance.dto.request.SubmitLoanDecisionRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.dto.response.LoanDecisionResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.service.LoanDecisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * Integration tests for loan decision endpoints.
 * Tests REST endpoints for getting application status and submitting decisions.
 *
 * Test Coverage:
 * 1. GET /loan-applications/{id}/status - retrieve status (authenticated)
 * 2. POST /loan-applications/{id}/decisions - submit approval (authenticated, staff)
 * 3. POST /loan-applications/{id}/decisions - submit rejection (authenticated, staff)
 * 4. Error cases: 401 (no auth), 403 (no staff), 404 (not found), 409 (conflict)
 * 5. Validation errors for decision request (missing decision, reason too long)
 * 6. Success responses with audit metadata and event publishing
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("LoanDecision Controller Integration Tests")
class LoanDecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanDecisionService loanDecisionService;

    private String testApplicationId;
    private String testConsumerId;
    private String testStaffId;
    private String validJwt;

    private LoanApplicationResponse pendingApplicationResponse;
    private LoanDecisionResponse approvalDecisionResponse;
    private LoanDecisionResponse rejectionDecisionResponse;

    @BeforeEach
    void setUp() {
        testApplicationId = "550e8400-e29b-41d4-a716-446655441000";
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testStaffId = "loan_officer_001";
        validJwt = "valid.jwt.token";

        pendingApplicationResponse = LoanApplicationResponse.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("Home renovation")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        approvalDecisionResponse = LoanDecisionResponse.builder()
            .id("550e8400-e29b-41d4-a716-446655442000")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.APPROVED)
            .staffId(testStaffId)
            .reason("Income verified, credit score 750+, approved for full amount")
            .createdAt(LocalDateTime.now())
            .build();

        rejectionDecisionResponse = LoanDecisionResponse.builder()
            .id("550e8400-e29b-41d4-a716-446655442001")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.REJECTED)
            .staffId(testStaffId)
            .reason("Income insufficient for requested amount")
            .createdAt(LocalDateTime.now())
            .build();
    }

    // ============ GET STATUS TESTS ============

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("GET /loan-applications/{id}/status - Retrieve application status successfully")
    void testGetApplicationStatus_Success() throws Exception {
        // Arrange
        when(loanDecisionService.getApplicationStatus(testApplicationId))
            .thenReturn(pendingApplicationResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId + "/status")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(testApplicationId)))
            .andExpect(jsonPath("$.data.consumerId", is(testConsumerId)))
            .andExpect(jsonPath("$.data.status", is("PENDING")))
            .andExpect(jsonPath("$.data.requestedAmount", is(50000.00)));

        verify(loanDecisionService).getApplicationStatus(testApplicationId);
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("GET /loan-applications/{id}/status - Return 404 when application not found")
    void testGetApplicationStatus_NotFound() throws Exception {
        // Arrange
        when(loanDecisionService.getApplicationStatus(testApplicationId))
            .thenThrow(new ResourceNotFoundException("LoanApplication", "id", testApplicationId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId + "/status")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Resource Not Found")))
            .andExpect(jsonPath("$.message", containsString("LoanApplication")));
    }

    @Test
    @DisplayName("GET /loan-applications/{id}/status - Return 403 without authentication")
    void testGetApplicationStatus_NoAuthentication() throws Exception {
        // Act & Assert - No @WithMockUser, so request should be forbidden
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId + "/status")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        // Service should not be called
        verify(loanDecisionService, never()).getApplicationStatus(anyString());
    }

    // ============ SUBMIT DECISION - APPROVE TESTS ============

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Submit approval decision successfully")
    void testSubmitDecision_Approve_Success() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("Income verified, credit score 750+, approved for full amount")
            .build();

        when(loanDecisionService.submitDecision(testApplicationId, request, testStaffId))
            .thenReturn(approvalDecisionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", is("Decision submitted successfully")))
            .andExpect(jsonPath("$.data.id", is(approvalDecisionResponse.getId())))
            .andExpect(jsonPath("$.data.applicationId", is(testApplicationId)))
            .andExpect(jsonPath("$.data.decision", is("APPROVED")))
            .andExpect(jsonPath("$.data.staffId", is(testStaffId)))
            .andExpect(jsonPath("$.data.reason", containsString("Income verified")));

        verify(loanDecisionService).submitDecision(testApplicationId, request, testStaffId);
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Submit rejection decision successfully")
    void testSubmitDecision_Reject_Success() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.REJECTED)
            .reason("Income insufficient for requested amount")
            .build();

        when(loanDecisionService.submitDecision(testApplicationId, request, testStaffId))
            .thenReturn(rejectionDecisionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.decision", is("REJECTED")))
            .andExpect(jsonPath("$.data.reason", is("Income insufficient for requested amount")));

        verify(loanDecisionService).submitDecision(testApplicationId, request, testStaffId);
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Submit decision without reason (optional)")
    void testSubmitDecision_WithoutReason_Success() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason(null)  // No reason provided
            .build();

        LoanDecisionResponse responseNoReason = LoanDecisionResponse.builder()
            .id("550e8400-e29b-41d4-a716-446655442000")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.APPROVED)
            .staffId(testStaffId)
            .reason(null)
            .createdAt(LocalDateTime.now())
            .build();

        when(loanDecisionService.submitDecision(testApplicationId, request, testStaffId))
            .thenReturn(responseNoReason);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.decision", is("APPROVED")));

        verify(loanDecisionService).submitDecision(testApplicationId, request, testStaffId);
    }

    // ============ DECISION ERROR TESTS ============

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Return 404 when application not found")
    void testSubmitDecision_ApplicationNotFound() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanDecisionService.submitDecision(anyString(), any(SubmitLoanDecisionRequest.class), anyString()))
            .thenThrow(new ResourceNotFoundException("LoanApplication", "id", testApplicationId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Resource Not Found")))
            .andExpect(jsonPath("$.message", containsString("LoanApplication")));
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Return 409 when application not PENDING")
    void testSubmitDecision_ApplicationNotPending() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanDecisionService.submitDecision(anyString(), any(SubmitLoanDecisionRequest.class), anyString()))
            .thenThrow(new DuplicateResourceException(
                "Application with ID '" + testApplicationId + "' is not in PENDING status"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("not in PENDING status")));
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Return 409 when duplicate decision exists")
    void testSubmitDecision_DuplicateDecision() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        String duplicateMessage = "Application with ID '" + testApplicationId +
            "' already has a approved decision (ID: 550e8400-e29b-41d4-a716-446655442000, created: 2024-12-05T14:30:00)";

        when(loanDecisionService.submitDecision(anyString(), any(SubmitLoanDecisionRequest.class), anyString()))
            .thenThrow(new DuplicateResourceException(duplicateMessage));

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("already has a")));
    }

    // ============ VALIDATION ERROR TESTS ============

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Return 400 for missing decision")
    void testSubmitDecision_ValidationError_MissingDecision() throws Exception {
        // Arrange - request without decision (null)
        String invalidJson = "{ \"reason\": \"Some reason\" }";

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Return 400 for reason too long")
    void testSubmitDecision_ValidationError_ReasonTooLong() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("A".repeat(501))  // Exceeds max 500
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Validation")));
    }

    // ============ AUTHENTICATION TESTS ============

    @Test
    @DisplayName("POST /loan-applications/{id}/decisions - Return 403 without authentication")
    void testSubmitDecision_NoAuthentication() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        // Act & Assert - No @WithMockUser, so request should be forbidden
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());

        // Service should not be called
        verify(loanDecisionService, never()).submitDecision(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("GET /loan-applications/{id}/status - Return 403 without authentication")
    void testGetApplicationStatus_NoAuthentication_Status() throws Exception {
        // Act & Assert - No @WithMockUser
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId + "/status")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(loanDecisionService, never()).getApplicationStatus(anyString());
    }

    // ============ RESPONSE FORMAT TESTS ============

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("POST /loan-applications/{id}/decisions - Response includes decision metadata")
    void testSubmitDecision_ResponseIncludesMetadata() throws Exception {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("Income verified, credit score 750+")
            .build();

        when(loanDecisionService.submitDecision(testApplicationId, request, testStaffId))
            .thenReturn(approvalDecisionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loan-applications/" + testApplicationId + "/decisions")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id", notNullValue()))
            .andExpect(jsonPath("$.data.applicationId", is(testApplicationId)))
            .andExpect(jsonPath("$.data.decision", notNullValue()))
            .andExpect(jsonPath("$.data.staffId", notNullValue()))
            .andExpect(jsonPath("$.data.createdAt", notNullValue()));
    }

    @Test
    @WithMockUser(username = "loan_officer_001")
    @DisplayName("GET /loan-applications/{id}/status - Response includes application data")
    void testGetApplicationStatus_ResponseIncludesApplicationData() throws Exception {
        // Arrange
        when(loanDecisionService.getApplicationStatus(testApplicationId))
            .thenReturn(pendingApplicationResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/loan-applications/" + testApplicationId + "/status")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id", notNullValue()))
            .andExpect(jsonPath("$.data.consumerId", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()))
            .andExpect(jsonPath("$.data.requestedAmount", notNullValue()))
            .andExpect(jsonPath("$.data.createdAt", notNullValue()));
    }
}
