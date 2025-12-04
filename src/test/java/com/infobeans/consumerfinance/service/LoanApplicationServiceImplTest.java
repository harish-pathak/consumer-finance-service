package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.LoanApplication;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import com.infobeans.consumerfinance.dto.request.CreateLoanApplicationRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.repository.LoanApplicationRepository;
import com.infobeans.consumerfinance.service.impl.LoanApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanApplicationServiceImpl.
 * Tests loan application creation, retrieval, duplicate detection, and error handling.
 *
 * Test Coverage:
 * 1. Successful loan application creation
 * 2. Duplicate detection for PENDING applications
 * 3. Consumer not found validation
 * 4. Race condition handling (DataIntegrityViolationException)
 * 5. Successful retrieval by ID
 * 6. Not found during retrieval
 * 7. PENDING application check
 */
@DisplayName("LoanApplicationService Tests")
class LoanApplicationServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private LoanApplicationServiceImpl loanApplicationService;

    private CreateLoanApplicationRequest validRequest;
    private String testConsumerId;
    private String testApplicationId;
    private LoanApplication testApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testApplicationId = "550e8400-e29b-41d4-a716-446655441000";

        validRequest = CreateLoanApplicationRequest.builder()
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("Home renovation")
            .build();

        testApplication = LoanApplication.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("Home renovation")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // ============ CREATE LOAN APPLICATION TESTS ============

    @Test
    @DisplayName("Should successfully create loan application for existing consumer with no pending application")
    void testCreateLoanApplication_Success() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(testApplication);

        // Act
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(testConsumerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testApplicationId, response.getId());
        assertEquals(testConsumerId, response.getConsumerId());
        assertEquals(LoanApplicationStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("50000.00"), response.getRequestedAmount());
        assertEquals(60, response.getTermInMonths());
        assertEquals("Home renovation", response.getPurpose());

        verify(consumerRepository).findById(testConsumerId);
        verify(loanApplicationRepository).existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Should create loan application with only required amount field (optional fields null)")
    void testCreateLoanApplication_OnlyRequiredAmount_Success() {
        // Arrange
        CreateLoanApplicationRequest minimalRequest = CreateLoanApplicationRequest.builder()
            .requestedAmount(new BigDecimal("10000.00"))
            .build();

        LoanApplication createdApp = LoanApplication.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("10000.00"))
            .termInMonths(null)
            .purpose(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(createdApp);

        // Act
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(testConsumerId, minimalRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("10000.00"), response.getRequestedAmount());
        assertNull(response.getTermInMonths());
        assertNull(response.getPurpose());

        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when consumer does not exist")
    void testCreateLoanApplication_ConsumerNotFound() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> loanApplicationService.createLoanApplication(testConsumerId, validRequest)
        );

        assertTrue(exception.getMessage().contains(testConsumerId));
        verify(consumerRepository).findById(testConsumerId);
        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when PENDING application already exists")
    void testCreateLoanApplication_DuplicatePendingApplication() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(true);
        when(loanApplicationRepository.findMostRecentByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING))
            .thenReturn(Optional.of(testApplication));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanApplicationService.createLoanApplication(testConsumerId, validRequest)
        );

        assertTrue(exception.getMessage().contains(testConsumerId));
        assertTrue(exception.getMessage().contains("already has a pending loan application"));
        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should include existing application ID in duplicate error message when available")
    void testCreateLoanApplication_DuplicateApplicationIncludesId() {
        // Arrange
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(true);
        when(loanApplicationRepository.findMostRecentByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING))
            .thenReturn(Optional.of(testApplication));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanApplicationService.createLoanApplication(testConsumerId, validRequest)
        );

        // Verify error message includes the existing application ID
        assertTrue(exception.getMessage().contains(testApplicationId));
        assertTrue(exception.getMessage().contains(testConsumerId));
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException from race condition and throw DuplicateResourceException")
    void testCreateLoanApplication_ConcurrentCreationRaceCondition() {
        // Arrange - Simulate race condition where application is created between check and save
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);
        when(loanApplicationRepository.save(any(LoanApplication.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate entry for consumer_id, status"));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(true);
        when(loanApplicationRepository.findMostRecentByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING))
            .thenReturn(Optional.of(testApplication));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanApplicationService.createLoanApplication(testConsumerId, validRequest)
        );

        assertTrue(exception.getMessage().contains("already has a pending loan application"));
    }

    @Test
    @DisplayName("Should re-throw DataIntegrityViolationException if not due to duplicate PENDING application")
    void testCreateLoanApplication_OtherDataIntegrityViolation() {
        // Arrange - Simulate a non-duplicate integrity violation (e.g., invalid consumer_id FK)
        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);
        when(loanApplicationRepository.save(any(LoanApplication.class)))
            .thenThrow(new DataIntegrityViolationException("Foreign key constraint failed"));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);

        // Act & Assert
        DataIntegrityViolationException exception = assertThrows(
            DataIntegrityViolationException.class,
            () -> loanApplicationService.createLoanApplication(testConsumerId, validRequest)
        );

        assertTrue(exception.getMessage().contains("Foreign key constraint failed"));
    }

    // ============ RETRIEVE LOAN APPLICATION TESTS ============

    @Test
    @DisplayName("Should successfully retrieve loan application by ID")
    void testGetLoanApplicationById_Success() {
        // Arrange
        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(testApplication));

        // Act
        LoanApplicationResponse response = loanApplicationService.getLoanApplicationById(testApplicationId);

        // Assert
        assertNotNull(response);
        assertEquals(testApplicationId, response.getId());
        assertEquals(testConsumerId, response.getConsumerId());
        assertEquals(LoanApplicationStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("50000.00"), response.getRequestedAmount());

        verify(loanApplicationRepository).findById(testApplicationId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when application does not exist")
    void testGetLoanApplicationById_NotFound() {
        // Arrange
        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> loanApplicationService.getLoanApplicationById(testApplicationId)
        );

        assertTrue(exception.getMessage().contains(testApplicationId));
        verify(loanApplicationRepository).findById(testApplicationId);
    }

    // ============ CHECK PENDING APPLICATION TESTS ============

    @Test
    @DisplayName("Should return true when consumer has PENDING loan application")
    void testHasPendingLoanApplication_HasPending() {
        // Arrange
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(true);

        // Act
        boolean hasPending = loanApplicationService.hasPendingLoanApplication(testConsumerId);

        // Assert
        assertTrue(hasPending);
        verify(loanApplicationRepository).existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("Should return false when consumer has no PENDING loan application")
    void testHasPendingLoanApplication_NoPending() {
        // Arrange
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);

        // Act
        boolean hasPending = loanApplicationService.hasPendingLoanApplication(testConsumerId);

        // Assert
        assertFalse(hasPending);
        verify(loanApplicationRepository).existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING);
    }

    // ============ EDGE CASE TESTS ============

    @Test
    @DisplayName("Should allow new application after previous PENDING application is APPROVED")
    void testCreateLoanApplication_AllowedAfterApprovedApplication() {
        // This test verifies the business rule: only ONE PENDING allowed, but multiple historical allowed
        // Arrange
        LoanApplication approvedApp = LoanApplication.builder()
            .id("approved-app-id")
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.APPROVED)
            .requestedAmount(new BigDecimal("30000.00"))
            .build();

        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(testApplication);

        // Act
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(testConsumerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(LoanApplicationStatus.PENDING, response.getStatus());
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Should handle large loan amounts correctly")
    void testCreateLoanApplication_LargeLoanAmount() {
        // Arrange
        CreateLoanApplicationRequest largeRequest = CreateLoanApplicationRequest.builder()
            .requestedAmount(new BigDecimal("999999999999.99"))
            .termInMonths(360)
            .purpose("Large project financing")
            .build();

        LoanApplication largeApp = LoanApplication.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("999999999999.99"))
            .termInMonths(360)
            .purpose("Large project financing")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(consumerRepository.findById(testConsumerId)).thenReturn(Optional.of(mock(com.infobeans.consumerfinance.domain.Consumer.class)));
        when(loanApplicationRepository.existsByConsumerIdAndStatus(testConsumerId, LoanApplicationStatus.PENDING)).thenReturn(false);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(largeApp);

        // Act
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(testConsumerId, largeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("999999999999.99"), response.getRequestedAmount());
        assertEquals(360, response.getTermInMonths());
    }
}
