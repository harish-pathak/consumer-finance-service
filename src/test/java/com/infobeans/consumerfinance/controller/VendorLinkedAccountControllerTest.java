package com.infobeans.consumerfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.LinkVendorAccountRequest;
import com.infobeans.consumerfinance.dto.request.UpdateVendorAccountStatusRequest;
import com.infobeans.consumerfinance.dto.response.VendorLinkedAccountResponse;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.service.VendorLinkedAccountService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for VendorLinkedAccountController.
 * Tests REST endpoints for vendor-linked account management with authentication and error handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("VendorLinkedAccountController Integration Tests")
class VendorLinkedAccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private VendorLinkedAccountService vendorLinkedAccountService;

    private String testConsumerId;
    private String testVendorId;
    private String testAccountId;
    private VendorLinkedAccountResponse validResponse;

    @BeforeEach
    void setUp() {
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testVendorId = UUID.randomUUID().toString();
        testAccountId = UUID.randomUUID().toString();

        validResponse = VendorLinkedAccountResponse.builder()
            .id(testAccountId)
            .consumerId(testConsumerId)
            .vendorId(testVendorId)
            .vendorName("Test Vendor")
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("GET /api/v1/consumers/{consumerId}/vendor-accounts - List accounts successfully")
    void testListVendorAccounts_Success() throws Exception {
        when(vendorLinkedAccountService.listVendorAccountsByConsumerId(testConsumerId))
            .thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/v1/consumers/{consumerId}/vendor-accounts", testConsumerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data[0].id", is(testAccountId)))
            .andExpect(jsonPath("$.data[0].vendorName", is("Test Vendor")));

        verify(vendorLinkedAccountService).listVendorAccountsByConsumerId(testConsumerId);
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("GET /api/v1/consumers/{consumerId}/vendor-accounts/{accountId} - Get account successfully")
    void testGetVendorAccount_Success() throws Exception {
        when(vendorLinkedAccountService.getVendorLinkedAccountById(testAccountId))
            .thenReturn(validResponse);

        mockMvc.perform(get("/api/v1/consumers/{consumerId}/vendor-accounts/{accountId}", testConsumerId, testAccountId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.id", is(testAccountId)))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        verify(vendorLinkedAccountService).getVendorLinkedAccountById(testAccountId);
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("GET /api/v1/consumers/{consumerId}/vendor-accounts/{accountId} - Return 404 when not found")
    void testGetVendorAccount_NotFound() throws Exception {
        when(vendorLinkedAccountService.getVendorLinkedAccountById(testAccountId))
            .thenThrow(new ResourceNotFoundException("VendorLinkedAccount", "id", testAccountId));

        mockMvc.perform(get("/api/v1/consumers/{consumerId}/vendor-accounts/{accountId}", testConsumerId, testAccountId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_STAFF")
    @DisplayName("PATCH /api/v1/vendor-accounts/{id}/status - Update status successfully")
    void testUpdateVendorAccountStatus_Success() throws Exception {
        UpdateVendorAccountStatusRequest request = UpdateVendorAccountStatusRequest.builder()
            .status(AccountStatus.DISABLED)
            .reason("Vendor inactive")
            .build();

        VendorLinkedAccountResponse updatedResponse = VendorLinkedAccountResponse.builder()
            .id(testAccountId)
            .status(AccountStatus.DISABLED)
            .build();

        when(vendorLinkedAccountService.updateVendorAccountStatus(eq(testAccountId), eq(AccountStatus.DISABLED), anyString()))
            .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/vendor-accounts/{id}/status", testAccountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("DISABLED")));

        verify(vendorLinkedAccountService).updateVendorAccountStatus(eq(testAccountId), eq(AccountStatus.DISABLED), anyString());
    }

    @Test
    @DisplayName("GET /api/v1/consumers/{consumerId}/vendor-accounts - Return 403 without authorization")
    void testListVendorAccounts_NoAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/consumers/{consumerId}/vendor-accounts", testConsumerId))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/vendor-accounts/{id}/status - Return 403 without authorization")
    void testUpdateVendorAccountStatus_NoAuthorization() throws Exception {
        UpdateVendorAccountStatusRequest request = UpdateVendorAccountStatusRequest.builder()
            .status(AccountStatus.DISABLED)
            .build();

        mockMvc.perform(patch("/api/v1/vendor-accounts/{id}/status", testAccountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/v1/vendor-accounts/{id}/status - Return 400 for invalid status")
    void testUpdateVendorAccountStatus_ValidationError() throws Exception {
        UpdateVendorAccountStatusRequest request = new UpdateVendorAccountStatusRequest();
        request.setStatus(null);

        mockMvc.perform(patch("/api/v1/vendor-accounts/{id}/status", testAccountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("GET /api/v1/consumers/{consumerId}/vendor-accounts - Return 404 for non-existent consumer")
    void testListVendorAccounts_ConsumerNotFound() throws Exception {
        when(vendorLinkedAccountService.listVendorAccountsByConsumerId(testConsumerId))
            .thenThrow(new ResourceNotFoundException("Consumer", "id", testConsumerId));

        mockMvc.perform(get("/api/v1/consumers/{consumerId}/vendor-accounts", testConsumerId))
            .andExpect(status().isNotFound());
    }
}
