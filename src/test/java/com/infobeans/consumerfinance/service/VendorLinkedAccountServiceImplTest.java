package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.Consumer;
import com.infobeans.consumerfinance.domain.Vendor;
import com.infobeans.consumerfinance.domain.VendorLinkedAccount;
import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.LinkVendorAccountRequest;
import com.infobeans.consumerfinance.dto.response.VendorLinkedAccountResponse;
import com.infobeans.consumerfinance.exception.BusinessRuleException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.repository.VendorLinkedAccountRepository;
import com.infobeans.consumerfinance.repository.VendorRepository;
import com.infobeans.consumerfinance.service.impl.VendorLinkedAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VendorLinkedAccountServiceImpl.
 * Tests idempotent creation, retrieval, status transitions, and error handling.
 */
@DisplayName("VendorLinkedAccountService Tests")
class VendorLinkedAccountServiceImplTest {

    @Mock private VendorLinkedAccountRepository vendorLinkedAccountRepository;
    @Mock private ConsumerRepository consumerRepository;
    @Mock private VendorRepository vendorRepository;

    @InjectMocks private VendorLinkedAccountServiceImpl service;

    private String testConsumerId;
    private String testVendorId;
    private String testAccountId;
    private LinkVendorAccountRequest validRequest;
    private VendorLinkedAccount testAccount;
    private Consumer testConsumer;
    private Vendor testVendor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testVendorId = UUID.randomUUID().toString();
        testAccountId = UUID.randomUUID().toString();

        validRequest = LinkVendorAccountRequest.builder()
            .consumerId(testConsumerId)
            .vendorId(testVendorId)
            .externalAccountRef("EXT-12345")
            .linkageId("LINK-001")
            .build();

        testConsumer = Consumer.builder().id(testConsumerId).build();
        testVendor = Vendor.builder().id(testVendorId).name("Test Vendor").build();

        testAccount = VendorLinkedAccount.builder()
            .id(testAccountId)
            .consumerId(testConsumerId)
            .vendorId(testVendorId)
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should create vendor-linked account successfully")
    void testCreateOrLinkVendorAccount_Success() {
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(testConsumer));
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.of(testVendor));
        when(vendorLinkedAccountRepository.findByConsumerIdAndVendorId(testConsumerId, testVendorId))
            .thenReturn(Optional.empty());
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class))).thenReturn(testAccount);

        VendorLinkedAccountResponse response = service.createOrLinkVendorAccount(validRequest);

        assertNotNull(response);
        assertEquals(testAccountId, response.getId());
        assertEquals(testConsumerId, response.getConsumerId());
        assertEquals(testVendorId, response.getVendorId());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals("Test Vendor", response.getVendorName());

        verify(consumerRepository).findById(testConsumerId);
        verify(vendorRepository).findById(testVendorId);
        verify(vendorLinkedAccountRepository).save(any());
    }

    @Test
    @DisplayName("Should return existing account if already linked (idempotent)")
    void testCreateOrLinkVendorAccount_Idempotent() {
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(testConsumer));
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.of(testVendor));
        when(vendorLinkedAccountRepository.findByConsumerIdAndVendorId(testConsumerId, testVendorId))
            .thenReturn(Optional.of(testAccount));

        VendorLinkedAccountResponse response = service.createOrLinkVendorAccount(validRequest);

        assertNotNull(response);
        assertEquals(testAccountId, response.getId());
        verify(vendorLinkedAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception if consumer not found")
    void testCreateOrLinkVendorAccount_ConsumerNotFound() {
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.createOrLinkVendorAccount(validRequest));
    }

    @Test
    @DisplayName("Should throw exception if vendor not found")
    void testCreateOrLinkVendorAccount_VendorNotFound() {
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(testConsumer));
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.createOrLinkVendorAccount(validRequest));
    }

    @Test
    @DisplayName("Should handle race condition and return existing account")
    void testCreateOrLinkVendorAccount_RaceCondition() {
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(testConsumer));
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.of(testVendor));
        when(vendorLinkedAccountRepository.findByConsumerIdAndVendorId(testConsumerId, testVendorId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(testAccount));
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        VendorLinkedAccountResponse response = service.createOrLinkVendorAccount(validRequest);

        assertNotNull(response);
        assertEquals(testAccountId, response.getId());
    }

    @Test
    @DisplayName("Should retrieve account by ID")
    void testGetVendorLinkedAccountById_Success() {
        when(vendorLinkedAccountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.of(testVendor));

        VendorLinkedAccountResponse response = service.getVendorLinkedAccountById(testAccountId);

        assertNotNull(response);
        assertEquals(testAccountId, response.getId());
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void testGetVendorLinkedAccountById_NotFound() {
        when(vendorLinkedAccountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.getVendorLinkedAccountById(testAccountId));
    }

    @Test
    @DisplayName("Should list all accounts for consumer")
    void testListVendorAccountsByConsumerId_Success() {
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(testConsumer));
        when(vendorLinkedAccountRepository.findByConsumerId(testConsumerId))
            .thenReturn(List.of(testAccount));
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.of(testVendor));

        List<VendorLinkedAccountResponse> accounts = service.listVendorAccountsByConsumerId(testConsumerId);

        assertNotNull(accounts);
        assertEquals(1, accounts.size());
    }

    @Test
    @DisplayName("Should update account status from ACTIVE to DISABLED")
    void testUpdateVendorAccountStatus_ActiveToDisabled() {
        when(vendorLinkedAccountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class))).thenReturn(testAccount);
        when(vendorRepository.findById(testVendorId)).thenReturn(Optional.of(testVendor));

        VendorLinkedAccountResponse response = service.updateVendorAccountStatus(testAccountId, AccountStatus.DISABLED, "system");

        assertNotNull(response);
        verify(vendorLinkedAccountRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception on invalid transition from ARCHIVED")
    void testUpdateVendorAccountStatus_InvalidTransitionFromArchived() {
        testAccount.setStatus(AccountStatus.ARCHIVED);
        when(vendorLinkedAccountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        assertThrows(BusinessRuleException.class,
            () -> service.updateVendorAccountStatus(testAccountId, AccountStatus.DISABLED, "system"));
    }

    @Test
    @DisplayName("Should check if account exists")
    void testVendorAccountExists() {
        when(vendorLinkedAccountRepository.existsByConsumerIdAndVendorId(testConsumerId, testVendorId))
            .thenReturn(true);

        boolean exists = service.vendorAccountExists(testConsumerId, testVendorId);

        assertTrue(exists);
    }
}
