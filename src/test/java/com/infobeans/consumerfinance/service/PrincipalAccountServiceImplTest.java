package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.PrincipalAccount;
import com.infobeans.consumerfinance.dto.request.CreatePrincipalAccountRequest;
import com.infobeans.consumerfinance.dto.response.PrincipalAccountResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.repository.PrincipalAccountRepository;
import com.infobeans.consumerfinance.service.impl.PrincipalAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PrincipalAccountServiceImpl.
 * Tests principal account creation, retrieval, duplicate detection, and error handling.
 */
@DisplayName("PrincipalAccountService Tests")
class PrincipalAccountServiceImplTest {

    @Mock
    private PrincipalAccountRepository principalAccountRepository;

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private PrincipalAccountServiceImpl principalAccountService;

    private CreatePrincipalAccountRequest validRequest;
    private String testConsumerId;
    private PrincipalAccount testAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";

        validRequest = CreatePrincipalAccountRequest.builder()
            .consumerId(testConsumerId)
            .accountType("PRIMARY")
            .build();

        testAccount = PrincipalAccount.builder()
            .id("account-id-123")
            .consumerId(testConsumerId)
            .accountType("PRIMARY")
            .build();
    }

    @Test
    @DisplayName("Should successfully create principal account for existing consumer")
    void testCreatePrincipalAccount_Success() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(false);
        when(principalAccountRepository.save(any(PrincipalAccount.class))).thenReturn(testAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.createPrincipalAccount(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testAccount.getId(), response.getId());
        assertEquals(testConsumerId, response.getConsumerId());
        assertEquals("PRIMARY", response.getAccountType());

        verify(consumerRepository).findById(testConsumerId);
        verify(principalAccountRepository).existsByConsumerId(testConsumerId);
        verify(principalAccountRepository).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when consumer does not exist")
    void testCreatePrincipalAccount_ConsumerNotFound() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> principalAccountService.createPrincipalAccount(validRequest)
        );

        assertTrue(exception.getMessage().contains(testConsumerId));
        verify(consumerRepository).findById(testConsumerId);
        verify(principalAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when principal account already exists")
    void testCreatePrincipalAccount_DuplicatePrincipalAccount() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> principalAccountService.createPrincipalAccount(validRequest)
        );

        assertTrue(exception.getMessage().contains(testConsumerId));
        verify(principalAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException and throw DuplicateResourceException")
    void testCreatePrincipalAccount_ConcurrentCreationRaceCondition() {
        // Arrange - Simulate race condition where account is created between check and save
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(false);
        when(principalAccountRepository.save(any(PrincipalAccount.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate entry for consumer_id"));
        when(principalAccountRepository.findByConsumerId(testConsumerId))
            .thenReturn(Optional.of(testAccount));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> principalAccountService.createPrincipalAccount(validRequest)
        );

        assertTrue(exception.getMessage().contains(testConsumerId));
    }

    @Test
    @DisplayName("Should successfully retrieve principal account by consumer ID")
    void testGetPrincipalAccountByConsumerId_Success() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.findByConsumerId(testConsumerId)).thenReturn(Optional.of(testAccount));

        // Act
        PrincipalAccountResponse response = principalAccountService.getPrincipalAccountByConsumerId(testConsumerId);

        // Assert
        assertNotNull(response);
        assertEquals(testAccount.getId(), response.getId());
        assertEquals(testConsumerId, response.getConsumerId());

        verify(consumerRepository).findById(testConsumerId);
        verify(principalAccountRepository).findByConsumerId(testConsumerId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when consumer does not exist during retrieval")
    void testGetPrincipalAccountByConsumerId_ConsumerNotFound() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> principalAccountService.getPrincipalAccountByConsumerId(testConsumerId)
        );

        assertTrue(exception.getMessage().contains(testConsumerId));
        verify(principalAccountRepository, never()).findByConsumerId(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when principal account does not exist for consumer")
    void testGetPrincipalAccountByConsumerId_PrincipalAccountNotFound() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.findByConsumerId(testConsumerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> principalAccountService.getPrincipalAccountByConsumerId(testConsumerId)
        );

        assertTrue(exception.getMessage().contains("PrincipalAccount"));
        verify(consumerRepository).findById(testConsumerId);
        verify(principalAccountRepository).findByConsumerId(testConsumerId);
    }

    @Test
    @DisplayName("Should correctly check if principal account exists for consumer")
    void testExistsPrincipalAccountForConsumer() {
        // Arrange
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(true);

        // Act
        boolean exists = principalAccountService.existsPrincipalAccountForConsumer(testConsumerId);

        // Assert
        assertTrue(exists);
        verify(principalAccountRepository).existsByConsumerId(testConsumerId);
    }

    @Test
    @DisplayName("Should correctly check when principal account does not exist for consumer")
    void testExistsPrincipalAccountForConsumer_NotExists() {
        // Arrange
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(false);

        // Act
        boolean exists = principalAccountService.existsPrincipalAccountForConsumer(testConsumerId);

        // Assert
        assertFalse(exists);
        verify(principalAccountRepository).existsByConsumerId(testConsumerId);
    }

    @Test
    @DisplayName("Should create principal account with custom account type")
    void testCreatePrincipalAccount_CustomAccountType() {
        // Arrange
        CreatePrincipalAccountRequest customRequest = CreatePrincipalAccountRequest.builder()
            .consumerId(testConsumerId)
            .accountType("SECONDARY")
            .build();

        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(false);

        PrincipalAccount secondaryAccount = PrincipalAccount.builder()
            .id("account-id-123")
            .consumerId(testConsumerId)
            .accountType("SECONDARY")
            .build();
        when(principalAccountRepository.save(any(PrincipalAccount.class))).thenReturn(secondaryAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.createPrincipalAccount(customRequest);

        // Assert
        assertEquals("SECONDARY", response.getAccountType());
    }

    @Test
    @DisplayName("Should handle null account type and default to PRIMARY")
    void testCreatePrincipalAccount_NullAccountType() {
        // Arrange
        CreatePrincipalAccountRequest nullTypeRequest = CreatePrincipalAccountRequest.builder()
            .consumerId(testConsumerId)
            .accountType(null)
            .build();

        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(principalAccountRepository.existsByConsumerId(testConsumerId)).thenReturn(false);
        when(principalAccountRepository.save(any(PrincipalAccount.class))).thenReturn(testAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.createPrincipalAccount(nullTypeRequest);

        // Assert
        assertNotNull(response);
        verify(principalAccountRepository).save(any(PrincipalAccount.class));
    }
}
