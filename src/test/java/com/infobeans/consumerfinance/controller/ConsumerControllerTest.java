package com.infobeans.consumerfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobeans.consumerfinance.dto.request.CreateConsumerOnboardingRequest;
import com.infobeans.consumerfinance.dto.response.ConsumerOnboardingResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.service.ConsumerService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ConsumerController.
 * Tests REST endpoints for consumer onboarding with authentication and error handling.
 *
 * Test Coverage:
 * 1. POST /api/v1/consumers - successful onboarding
 * 2. POST /api/v1/consumers - validation errors (missing fields, invalid formats)
 * 3. POST /api/v1/consumers - duplicate email
 * 4. POST /api/v1/consumers - duplicate national ID
 * 5. POST /api/v1/consumers - duplicate phone
 * 6. POST /api/v1/consumers - duplicate document number
 * 7. Authentication requirements (403 Forbidden)
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ConsumerController Integration Tests")
class ConsumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumerService consumerService;

    private CreateConsumerOnboardingRequest validRequest;
    private ConsumerOnboardingResponse validResponse;
    private String validJwt;

    @BeforeEach
    void setUp() {
        // Create valid request
        validRequest = CreateConsumerOnboardingRequest.builder()
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789012")
            .phone("+1234567890")
            .documentType("PASSPORT")
            .documentNumber("AB1234567")
            .panNumber("ABCDE1234F")
            .employerName("ABC Corp")
            .position("Software Engineer")
            .employmentType("FULL_TIME")
            .yearsOfExperience(5)
            .industry("Technology")
            .monthlyIncome(new BigDecimal("5000.00"))
            .annualIncome(new BigDecimal("60000.00"))
            .incomeSource("SALARY")
            .currency("USD")
            .build();

        // Create valid response
        validResponse = ConsumerOnboardingResponse.builder()
            .consumerId("550e8400-e29b-41d4-a716-446655440000")
            .status("ACTIVE")
            .message("Consumer onboarded successfully")
            .createdAt(LocalDateTime.now())
            .build();

        validJwt = "valid.jwt.token";
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Onboard consumer successfully")
    void testOnboardConsumer_Success() throws Exception {
        // Arrange
        when(consumerService.onboardConsumer(any(CreateConsumerOnboardingRequest.class)))
            .thenReturn(validResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", containsString("successfully")))
            .andExpect(jsonPath("$.data.consumerId", is(validResponse.getConsumerId())))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")))
            .andExpect(jsonPath("$.data.message", is("Consumer onboarded successfully")));

        verify(consumerService).onboardConsumer(any(CreateConsumerOnboardingRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 409 when email already exists")
    void testOnboardConsumer_DuplicateEmail() throws Exception {
        // Arrange
        when(consumerService.onboardConsumer(any()))
            .thenThrow(new DuplicateResourceException(
                "A consumer with email '" + validRequest.getEmail() + "' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("email")));

        verify(consumerService).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 409 when national ID already exists")
    void testOnboardConsumer_DuplicateNationalId() throws Exception {
        // Arrange
        when(consumerService.onboardConsumer(any()))
            .thenThrow(new DuplicateResourceException(
                "A consumer with national ID '" + validRequest.getNationalId() + "' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("national ID")));

        verify(consumerService).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 409 when phone already exists")
    void testOnboardConsumer_DuplicatePhone() throws Exception {
        // Arrange
        when(consumerService.onboardConsumer(any()))
            .thenThrow(new DuplicateResourceException(
                "A consumer with phone '" + validRequest.getPhone() + "' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("phone")));

        verify(consumerService).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 409 when document number already exists")
    void testOnboardConsumer_DuplicateDocumentNumber() throws Exception {
        // Arrange
        when(consumerService.onboardConsumer(any()))
            .thenThrow(new DuplicateResourceException(
                "A consumer with document number '" + validRequest.getDocumentNumber() + "' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("document number")));

        verify(consumerService).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 400 for validation errors (missing email)")
    void testOnboardConsumer_ValidationError_MissingEmail() throws Exception {
        // Arrange - request without email
        CreateConsumerOnboardingRequest invalidRequest = CreateConsumerOnboardingRequest.builder()
            .email(null)
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789012")
            .phone("+1234567890")
            .documentType("PASSPORT")
            .documentNumber("AB1234567")
            .employerName("ABC Corp")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(consumerService, never()).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 400 for validation errors (invalid email format)")
    void testOnboardConsumer_ValidationError_InvalidEmailFormat() throws Exception {
        // Arrange - request with invalid email format
        CreateConsumerOnboardingRequest invalidRequest = CreateConsumerOnboardingRequest.builder()
            .email("invalid-email")
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789012")
            .phone("+1234567890")
            .documentType("PASSPORT")
            .documentNumber("AB1234567")
            .employerName("ABC Corp")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(consumerService, never()).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 400 for validation errors (missing first name)")
    void testOnboardConsumer_ValidationError_MissingFirstName() throws Exception {
        // Arrange - request without first name
        CreateConsumerOnboardingRequest invalidRequest = CreateConsumerOnboardingRequest.builder()
            .email("john.doe@example.com")
            .firstName(null)
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789012")
            .phone("+1234567890")
            .documentType("PASSPORT")
            .documentNumber("AB1234567")
            .employerName("ABC Corp")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(consumerService, never()).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 400 when PAN number is missing")
    void testOnboardConsumer_ValidationError_MissingPanNumber() throws Exception {
        // Arrange - request without PAN number
        CreateConsumerOnboardingRequest invalidRequest = CreateConsumerOnboardingRequest.builder()
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789012")
            .phone("+1234567890")
            .documentType("PASSPORT")
            .documentNumber("AB1234567")
            .panNumber(null) // Missing PAN number
            .employerName("ABC Corp")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("Validation Error")))
            .andExpect(jsonPath("$.message", containsString("validation failed")));

        verify(consumerService, never()).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 400 when PAN number format is invalid")
    void testOnboardConsumer_ValidationError_InvalidPanFormat() throws Exception {
        // Arrange - request with invalid PAN format
        CreateConsumerOnboardingRequest invalidRequest = CreateConsumerOnboardingRequest.builder()
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789012")
            .phone("+1234567890")
            .documentType("PASSPORT")
            .documentNumber("AB1234567")
            .panNumber("INVALID123") // Invalid format (not matching ABCDE1234F pattern)
            .employerName("ABC Corp")
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("Validation Error")))
            .andExpect(jsonPath("$.details", containsString("PAN")));

        verify(consumerService, never()).onboardConsumer(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/consumers - Return 409 when PAN number already exists")
    void testOnboardConsumer_DuplicatePanNumber() throws Exception {
        // Arrange
        when(consumerService.onboardConsumer(any()))
            .thenThrow(new DuplicateResourceException(
                "A consumer with PAN number '" + validRequest.getPanNumber() + "' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
            .header("Authorization", "Bearer " + validJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")))
            .andExpect(jsonPath("$.message", containsString("PAN number")));

        verify(consumerService).onboardConsumer(any());
    }

    @Test
    @DisplayName("POST /api/v1/consumers - Return 403 when not authenticated")
    void testOnboardConsumer_Forbidden() throws Exception {
        // Act & Assert - no authentication header
        // Spring Security returns 403 Forbidden when no authentication is provided
        mockMvc.perform(post("/api/v1/consumers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isForbidden());

        verify(consumerService, never()).onboardConsumer(any());
    }
}
