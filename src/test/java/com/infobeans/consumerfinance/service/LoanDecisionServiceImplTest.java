package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.LoanApplication;
import com.infobeans.consumerfinance.domain.LoanApplicationDecision;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import com.infobeans.consumerfinance.dto.request.SubmitLoanDecisionRequest;
import com.infobeans.consumerfinance.dto.response.LoanApplicationResponse;
import com.infobeans.consumerfinance.dto.response.LoanDecisionResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.LoanApplicationDecisionRepository;
import com.infobeans.consumerfinance.repository.LoanApplicationRepository;
import com.infobeans.consumerfinance.service.impl.LoanDecisionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanDecisionServiceImpl.
 * Tests decision submission, state transitions, duplicate detection, and event publishing.
 *
 * Test Coverage:
 * 1. Get application status (success and not found)
 * 2. Submit decision - approve (success, event published)
 * 3. Submit decision - reject (success, no event)
 * 4. Application not found returns 404
 * 5. Application not PENDING returns 409
 * 6. Duplicate decision attempt returns 409
 * 7. Race condition handling (DataIntegrityViolationException)
 * 8. Successful state transitions (PENDING -> APPROVED/REJECTED)
 * 9. Decision audit record persistence
 */
@DisplayName("LoanDecisionService Tests")
class LoanDecisionServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApplicationDecisionRepository loanApplicationDecisionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private LoanDecisionServiceImpl loanDecisionService;

    private String testApplicationId;
    private String testConsumerId;
    private String testStaffId;
    private LoanApplication pendingApplication;
    private LoanApplicationDecision approvalDecision;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testApplicationId = "550e8400-e29b-41d4-a716-446655441000";
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testStaffId = "loan_officer_001";

        pendingApplication = LoanApplication.builder()
            .id(testApplicationId)
            .consumerId(testConsumerId)
            .status(LoanApplicationStatus.PENDING)
            .requestedAmount(new BigDecimal("50000.00"))
            .termInMonths(60)
            .purpose("Home renovation")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        approvalDecision = LoanApplicationDecision.builder()
            .id("550e8400-e29b-41d4-a716-446655442000")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.APPROVED)
            .staffId(testStaffId)
            .reason("Income verified, credit score 750+, approved for full amount")
            .createdAt(LocalDateTime.now())
            .build();
    }

    // ============ GET APPLICATION STATUS TESTS ============

    @Test
    @DisplayName("Should successfully retrieve application status")
    void testGetApplicationStatus_Success() {
        // Arrange
        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));

        // Act
        LoanApplicationResponse response = loanDecisionService.getApplicationStatus(testApplicationId);

        // Assert
        assertNotNull(response);
        assertEquals(testApplicationId, response.getId());
        assertEquals(testConsumerId, response.getConsumerId());
        assertEquals(LoanApplicationStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("50000.00"), response.getRequestedAmount());

        verify(loanApplicationRepository).findById(testApplicationId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when application not found")
    void testGetApplicationStatus_NotFound() {
        // Arrange
        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> loanDecisionService.getApplicationStatus(testApplicationId)
        );

        assertTrue(exception.getMessage().contains(testApplicationId));
        verify(loanApplicationRepository).findById(testApplicationId);
    }

    // ============ SUBMIT DECISION - APPROVE TESTS ============

    @Test
    @DisplayName("Should successfully approve pending application and publish event")
    void testSubmitDecision_Approve_Success() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("Income verified, credit score 750+, approved for full amount")
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(approvalDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        LoanDecisionResponse response = loanDecisionService.submitDecision(
            testApplicationId, request, testStaffId);

        // Assert
        assertNotNull(response);
        assertEquals(approvalDecision.getId(), response.getId());
        assertEquals(testApplicationId, response.getApplicationId());
        assertEquals(LoanDecisionStatus.APPROVED, response.getDecision());
        assertEquals(testStaffId, response.getStaffId());

        // Verify persistence
        verify(loanApplicationDecisionRepository).save(any(LoanApplicationDecision.class));
        verify(loanApplicationRepository).save(any(LoanApplication.class));

        // Verify event published for approval
        verify(applicationEventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Should successfully reject pending application without publishing event")
    void testSubmitDecision_Reject_Success() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.REJECTED)
            .reason("Income insufficient for requested amount")
            .build();

        LoanApplicationDecision rejectionDecision = LoanApplicationDecision.builder()
            .id("550e8400-e29b-41d4-a716-446655442001")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.REJECTED)
            .staffId(testStaffId)
            .reason("Income insufficient for requested amount")
            .createdAt(LocalDateTime.now())
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.REJECTED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(rejectionDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        LoanDecisionResponse response = loanDecisionService.submitDecision(
            testApplicationId, request, testStaffId);

        // Assert
        assertNotNull(response);
        assertEquals(rejectionDecision.getId(), response.getId());
        assertEquals(LoanDecisionStatus.REJECTED, response.getDecision());

        // Verify persistence
        verify(loanApplicationDecisionRepository).save(any(LoanApplicationDecision.class));
        verify(loanApplicationRepository).save(any(LoanApplication.class));

        // Verify NO event published for rejection
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    // ============ APPLICATION NOT FOUND TESTS ============

    @Test
    @DisplayName("Should throw ResourceNotFoundException when application not found during decision")
    void testSubmitDecision_ApplicationNotFound() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains(testApplicationId));
        verify(loanApplicationRepository).findById(testApplicationId);
        verify(loanApplicationDecisionRepository, never()).save(any());
    }

    // ============ APPLICATION STATE VALIDATION TESTS ============

    @Test
    @DisplayName("Should return 409 when application is already APPROVED")
    void testSubmitDecision_ApplicationAlreadyApproved() {
        // Arrange
        pendingApplication.setStatus(LoanApplicationStatus.APPROVED);
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains("not in PENDING status"));
        verify(loanApplicationDecisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 409 when application is REJECTED")
    void testSubmitDecision_ApplicationAlreadyRejected() {
        // Arrange
        pendingApplication.setStatus(LoanApplicationStatus.REJECTED);
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains("not in PENDING status"));
        verify(loanApplicationDecisionRepository, never()).save(any());
    }

    // ============ DUPLICATE DECISION TESTS ============

    @Test
    @DisplayName("Should return 409 when duplicate APPROVED decision already exists")
    void testSubmitDecision_DuplicateApprovalDecision() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("Income verified")
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(true);
        when(loanApplicationDecisionRepository.findByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(Optional.of(approvalDecision));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains("already has a"));
        assertTrue(exception.getMessage().contains("decision"));
        verify(loanApplicationDecisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 409 when duplicate REJECTED decision already exists")
    void testSubmitDecision_DuplicateRejectionDecision() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.REJECTED)
            .reason("Income insufficient")
            .build();

        LoanApplicationDecision rejectionDecision = LoanApplicationDecision.builder()
            .id("550e8400-e29b-41d4-a716-446655442001")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.REJECTED)
            .staffId("another_officer")
            .createdAt(LocalDateTime.now())
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.REJECTED)).thenReturn(true);
        when(loanApplicationDecisionRepository.findByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.REJECTED)).thenReturn(Optional.of(rejectionDecision));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains("already has a"));
        verify(loanApplicationDecisionRepository, never()).save(any());
    }

    // ============ RACE CONDITION TESTS ============

    @Test
    @DisplayName("Should handle race condition (DataIntegrityViolationException) for duplicate decision")
    void testSubmitDecision_RaceCondition_DuplicateConstraint() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("Income verified")
            .build();

        // First check: duplicate doesn't exist yet
        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);

        // Then save fails with DataIntegrityViolationException
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate entry for application_id, decision"));

        // Re-check after race condition: duplicate now exists
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED))
            .thenReturn(true)
            .thenReturn(true);  // Subsequent calls also return true
        when(loanApplicationDecisionRepository.findByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(Optional.of(approvalDecision));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains("already has a"));
        verify(loanApplicationDecisionRepository, times(1)).save(any(LoanApplicationDecision.class));
    }

    @Test
    @DisplayName("Should re-throw DataIntegrityViolationException for non-duplicate constraint failures")
    void testSubmitDecision_RaceCondition_OtherConstraintViolation() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenThrow(new DataIntegrityViolationException("Foreign key constraint failed"));

        // Still no duplicate after race condition
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);

        // Act & Assert
        DataIntegrityViolationException exception = assertThrows(
            DataIntegrityViolationException.class,
            () -> loanDecisionService.submitDecision(testApplicationId, request, testStaffId)
        );

        assertTrue(exception.getMessage().contains("Foreign key constraint failed"));
    }

    // ============ STATE TRANSITION TESTS ============

    @Test
    @DisplayName("Should transition application status from PENDING to APPROVED")
    void testSubmitDecision_StateTransition_PendingToApproved() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(approvalDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        loanDecisionService.submitDecision(testApplicationId, request, testStaffId);

        // Assert - capture saved application to verify status change
        ArgumentCaptor<LoanApplication> applicationCaptor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(applicationCaptor.capture());

        LoanApplication savedApplication = applicationCaptor.getValue();
        assertEquals(LoanApplicationStatus.APPROVED, savedApplication.getStatus());
    }

    @Test
    @DisplayName("Should transition application status from PENDING to REJECTED")
    void testSubmitDecision_StateTransition_PendingToRejected() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.REJECTED)
            .build();

        LoanApplicationDecision rejectionDecision = LoanApplicationDecision.builder()
            .id("550e8400-e29b-41d4-a716-446655442001")
            .applicationId(testApplicationId)
            .decision(LoanDecisionStatus.REJECTED)
            .staffId(testStaffId)
            .createdAt(LocalDateTime.now())
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.REJECTED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(rejectionDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        loanDecisionService.submitDecision(testApplicationId, request, testStaffId);

        // Assert - capture saved application to verify status change
        ArgumentCaptor<LoanApplication> applicationCaptor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(applicationCaptor.capture());

        LoanApplication savedApplication = applicationCaptor.getValue();
        assertEquals(LoanApplicationStatus.REJECTED, savedApplication.getStatus());
    }

    // ============ AUDIT TRAIL TESTS ============

    @Test
    @DisplayName("Should capture staff ID in decision audit record")
    void testSubmitDecision_AuditTrail_CapturesStaffId() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason("Income verified")
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(approvalDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        loanDecisionService.submitDecision(testApplicationId, request, testStaffId);

        // Assert - capture saved decision to verify staff ID
        ArgumentCaptor<LoanApplicationDecision> decisionCaptor = ArgumentCaptor.forClass(LoanApplicationDecision.class);
        verify(loanApplicationDecisionRepository).save(decisionCaptor.capture());

        LoanApplicationDecision savedDecision = decisionCaptor.getValue();
        assertEquals(testStaffId, savedDecision.getStaffId());
    }

    @Test
    @DisplayName("Should capture reason in decision audit record")
    void testSubmitDecision_AuditTrail_CapturesReason() {
        // Arrange
        String reason = "Income verified, credit score 750+, approved for full amount";
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason(reason)
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(approvalDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        loanDecisionService.submitDecision(testApplicationId, request, testStaffId);

        // Assert - capture saved decision to verify reason
        ArgumentCaptor<LoanApplicationDecision> decisionCaptor = ArgumentCaptor.forClass(LoanApplicationDecision.class);
        verify(loanApplicationDecisionRepository).save(decisionCaptor.capture());

        LoanApplicationDecision savedDecision = decisionCaptor.getValue();
        assertEquals(reason, savedDecision.getReason());
    }

    @Test
    @DisplayName("Should allow null reason in decision audit record")
    void testSubmitDecision_AuditTrail_NullReason() {
        // Arrange
        SubmitLoanDecisionRequest request = SubmitLoanDecisionRequest.builder()
            .decision(LoanDecisionStatus.APPROVED)
            .reason(null)  // No reason provided
            .build();

        when(loanApplicationRepository.findById(testApplicationId)).thenReturn(Optional.of(pendingApplication));
        when(loanApplicationDecisionRepository.existsByApplicationIdAndDecision(
            testApplicationId, LoanDecisionStatus.APPROVED)).thenReturn(false);
        when(loanApplicationDecisionRepository.save(any(LoanApplicationDecision.class)))
            .thenReturn(approvalDecision);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingApplication);

        // Act
        loanDecisionService.submitDecision(testApplicationId, request, testStaffId);

        // Assert - should succeed even with null reason
        ArgumentCaptor<LoanApplicationDecision> decisionCaptor = ArgumentCaptor.forClass(LoanApplicationDecision.class);
        verify(loanApplicationDecisionRepository).save(decisionCaptor.capture());

        LoanApplicationDecision savedDecision = decisionCaptor.getValue();
        assertNull(savedDecision.getReason());
    }
}
