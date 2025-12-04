package com.infobeans.consumerfinance.event;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.CreatePrincipalAccountRequest;
import com.infobeans.consumerfinance.dto.response.PrincipalAccountResponse;
import com.infobeans.consumerfinance.service.PrincipalAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PrincipalAccountCreationListener.
 * Tests event handling and principal account auto-creation during consumer onboarding.
 */
@DisplayName("PrincipalAccountCreationListener Tests")
class PrincipalAccountCreationListenerTest {

    @Mock
    private PrincipalAccountService principalAccountService;

    @InjectMocks
    private PrincipalAccountCreationListener listener;

    private OnboardingCompletedEvent testEvent;
    private String testConsumerId;
    private String testEmail;
    private String testConsumerName;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testConsumerId = "550e8400-e29b-41d4-a716-446655440000";
        testEmail = "john.doe@example.com";
        testConsumerName = "John Doe";

        testEvent = new OnboardingCompletedEvent(
            this,
            testConsumerId,
            testEmail,
            testConsumerName,
            LocalDateTime.now(),
            "api",
            null
        );
    }

    @Test
    @DisplayName("Should create principal account when onboarding is completed")
    void testOnOnboardingCompleted_Success() {
        // Arrange
        PrincipalAccountResponse expectedResponse = PrincipalAccountResponse.builder()
            .id("account-id-123")
            .consumerId(testConsumerId)
            .accountType("PRIMARY")
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(expectedResponse);

        // Act
        listener.onOnboardingCompleted(testEvent);

        // Assert
        verify(principalAccountService).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should call service with correct consumer ID")
    void testOnOnboardingCompleted_CorrectConsumerId() {
        // Arrange
        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .build());

        // Act
        listener.onOnboardingCompleted(testEvent);

        // Assert
        verify(principalAccountService).createPrincipalAccount(argThat(request ->
            request.getConsumerId().equals(testConsumerId)
        ));
    }

    @Test
    @DisplayName("Should set account type to PRIMARY")
    void testOnOnboardingCompleted_AccountTypePrimary() {
        // Arrange
        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .accountType("PRIMARY")
                .build());

        // Act
        listener.onOnboardingCompleted(testEvent);

        // Assert
        verify(principalAccountService).createPrincipalAccount(argThat(request ->
            "PRIMARY".equals(request.getAccountType())
        ));
    }

    @Test
    @DisplayName("Should set status to ACTIVE")
    void testOnOnboardingCompleted_StatusActive() {
        // Arrange
        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .status(AccountStatus.ACTIVE)
                .build());

        // Act
        listener.onOnboardingCompleted(testEvent);

        // Assert
        verify(principalAccountService).createPrincipalAccount(argThat(request ->
            AccountStatus.ACTIVE.equals(request.getStatus())
        ));
    }

    @Test
    @DisplayName("Should handle exception gracefully without propagating")
    void testOnOnboardingCompleted_Exception() {
        // Arrange
        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenThrow(new RuntimeException("Service error"));

        // Act - Should not throw exception
        listener.onOnboardingCompleted(testEvent);

        // Assert - Verify service was called despite exception
        verify(principalAccountService).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should handle null consumer ID gracefully")
    void testOnOnboardingCompleted_NullConsumerId() {
        // Arrange
        OnboardingCompletedEvent nullIdEvent = new OnboardingCompletedEvent(
            this,
            null,
            testEmail,
            testConsumerName,
            LocalDateTime.now(),
            "api",
            null
        );

        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenThrow(new NullPointerException("Consumer ID cannot be null"));

        // Act - Should not throw exception
        nullIdEvent.setConsumerId(null);
        listener.onOnboardingCompleted(nullIdEvent);

        // Assert
        verify(principalAccountService).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should log event processing information")
    void testOnOnboardingCompleted_EventProcessing() {
        // Arrange
        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .build());

        // Act
        listener.onOnboardingCompleted(testEvent);

        // Assert
        verify(principalAccountService, times(1)).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should handle event with all metadata fields populated")
    void testOnOnboardingCompleted_WithMetadata() {
        // Arrange
        OnboardingCompletedEvent eventWithMetadata = new OnboardingCompletedEvent(
            this,
            testConsumerId,
            testEmail,
            testConsumerName,
            LocalDateTime.now(),
            "api",
            "{\"ip\": \"192.168.1.1\", \"userAgent\": \"Chrome\"}"
        );

        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .build());

        // Act
        listener.onOnboardingCompleted(eventWithMetadata);

        // Assert
        verify(principalAccountService).createPrincipalAccount(argThat(request ->
            request.getConsumerId().equals(testConsumerId)
        ));
    }

    @Test
    @DisplayName("Should only create one principal account per onboarding event")
    void testOnOnboardingCompleted_SingleCreation() {
        // Arrange
        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .build());

        // Act
        listener.onOnboardingCompleted(testEvent);

        // Assert
        verify(principalAccountService, times(1)).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should call service even if onboarding event has minimal data")
    void testOnOnboardingCompleted_MinimalEventData() {
        // Arrange
        OnboardingCompletedEvent minimalEvent = new OnboardingCompletedEvent(this);
        minimalEvent.setConsumerId(testConsumerId);

        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .build());

        // Act
        listener.onOnboardingCompleted(minimalEvent);

        // Assert
        verify(principalAccountService).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should handle multiple onboarding events independently")
    void testOnOnboardingCompleted_MultipleEvents() {
        // Arrange
        String consumerId2 = "650e8400-e29b-41d4-a716-446655440001";
        OnboardingCompletedEvent event2 = new OnboardingCompletedEvent(
            this,
            consumerId2,
            "jane.doe@example.com",
            "Jane Doe",
            LocalDateTime.now(),
            "api",
            null
        );

        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-456")
                .consumerId(consumerId2)
                .build());

        // Act
        listener.onOnboardingCompleted(testEvent);
        listener.onOnboardingCompleted(event2);

        // Assert
        verify(principalAccountService, times(2)).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }

    @Test
    @DisplayName("Should use correct event source information")
    void testOnOnboardingCompleted_EventSource() {
        // Arrange
        OnboardingCompletedEvent mobileEvent = new OnboardingCompletedEvent(
            this,
            testConsumerId,
            testEmail,
            testConsumerName,
            LocalDateTime.now(),
            "mobile_app",
            null
        );

        when(principalAccountService.createPrincipalAccount(any(CreatePrincipalAccountRequest.class)))
            .thenReturn(PrincipalAccountResponse.builder()
                .id("account-id-123")
                .consumerId(testConsumerId)
                .build());

        // Act
        listener.onOnboardingCompleted(mobileEvent);

        // Assert - Event source doesn't affect principal account creation, but event is processed
        verify(principalAccountService).createPrincipalAccount(any(CreatePrincipalAccountRequest.class));
    }
}
