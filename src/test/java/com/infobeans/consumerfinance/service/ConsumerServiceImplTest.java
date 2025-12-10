package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.Consumer;
import com.infobeans.consumerfinance.dto.request.CreateConsumerOnboardingRequest;
import com.infobeans.consumerfinance.dto.response.ConsumerOnboardingResponse;
import com.infobeans.consumerfinance.exception.DuplicateResourceException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.service.impl.ConsumerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConsumerServiceImpl
 * Tests consumer onboarding logic, duplicate detection, and validation
 */
@DisplayName("ConsumerServiceImpl Tests")
class ConsumerServiceImplTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    private CreateConsumerOnboardingRequest validRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validRequest = createValidRequest();
    }

    @Test
    @DisplayName("Should successfully onboard new consumer with unique data")
    void testOnboardConsumer_Success() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByPhone(validRequest.getPhone())).thenReturn(false);
        when(consumerRepository.existsByDocumentNumber(validRequest.getDocumentNumber())).thenReturn(false);
        when(consumerRepository.existsByPanNumber(validRequest.getPanNumber())).thenReturn(false);

        Consumer savedConsumer = new Consumer();
        savedConsumer.setId("test-id-123");
        savedConsumer.setEmail(validRequest.getEmail());
        when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

        // Act
        ConsumerOnboardingResponse response = consumerService.onboardConsumer(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test-id-123", response.getConsumerId());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("Consumer onboarded successfully", response.getMessage());

        // Verify repository interactions
        verify(consumerRepository).existsByEmail(validRequest.getEmail());
        verify(consumerRepository).existsByNationalId(validRequest.getNationalId());
        verify(consumerRepository).existsByPhone(validRequest.getPhone());
        verify(consumerRepository).existsByDocumentNumber(validRequest.getDocumentNumber());
        verify(consumerRepository).existsByPanNumber(validRequest.getPanNumber());
        verify(consumerRepository).save(any(Consumer.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testOnboardConsumer_DuplicateEmail() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> consumerService.onboardConsumer(validRequest)
        );

        assertTrue(exception.getMessage().contains(validRequest.getEmail()));
        assertTrue(exception.getMessage().contains("email"));

        // Verify save was not called
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when national ID already exists")
    void testOnboardConsumer_DuplicateNationalId() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> consumerService.onboardConsumer(validRequest)
        );

        assertTrue(exception.getMessage().contains("national ID"));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when phone number already exists")
    void testOnboardConsumer_DuplicatePhone() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByPhone(validRequest.getPhone())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> consumerService.onboardConsumer(validRequest)
        );

        assertTrue(exception.getMessage().contains(validRequest.getPhone()));
        assertTrue(exception.getMessage().contains("phone"));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when document number already exists")
    void testOnboardConsumer_DuplicateDocumentNumber() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByPhone(validRequest.getPhone())).thenReturn(false);
        when(consumerRepository.existsByDocumentNumber(validRequest.getDocumentNumber())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> consumerService.onboardConsumer(validRequest)
        );

        assertTrue(exception.getMessage().contains(validRequest.getDocumentNumber()));
        assertTrue(exception.getMessage().contains("document number"));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when PAN number already exists")
    void testOnboardConsumer_DuplicatePanNumber() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByPhone(validRequest.getPhone())).thenReturn(false);
        when(consumerRepository.existsByDocumentNumber(validRequest.getDocumentNumber())).thenReturn(false);
        when(consumerRepository.existsByPanNumber(validRequest.getPanNumber())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> consumerService.onboardConsumer(validRequest)
        );

        assertTrue(exception.getMessage().contains(validRequest.getPanNumber()));
        assertTrue(exception.getMessage().contains("PAN number"));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null phone number (optional field)")
    void testOnboardConsumer_NullPhone() {
        // Arrange
        validRequest.setPhone(null);
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByDocumentNumber(validRequest.getDocumentNumber())).thenReturn(false);
        when(consumerRepository.existsByPanNumber(validRequest.getPanNumber())).thenReturn(false);

        Consumer savedConsumer = new Consumer();
        savedConsumer.setId("test-id-456");
        when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

        // Act
        ConsumerOnboardingResponse response = consumerService.onboardConsumer(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test-id-456", response.getConsumerId());

        // Verify phone check was skipped (never called)
        verify(consumerRepository, never()).existsByPhone(any());
    }

    @Test
    @DisplayName("Should handle empty phone number (optional field)")
    void testOnboardConsumer_EmptyPhone() {
        // Arrange
        validRequest.setPhone("");
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByDocumentNumber(validRequest.getDocumentNumber())).thenReturn(false);
        when(consumerRepository.existsByPanNumber(validRequest.getPanNumber())).thenReturn(false);

        Consumer savedConsumer = new Consumer();
        savedConsumer.setId("test-id-789");
        when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

        // Act
        ConsumerOnboardingResponse response = consumerService.onboardConsumer(validRequest);

        // Assert
        assertNotNull(response);
        verify(consumerRepository, never()).existsByPhone("");
    }

    @Test
    @DisplayName("Should check email existence before checking other fields")
    void testOnboardConsumer_ValidationOrder() {
        // Arrange - Email exists (should fail before checking other fields)
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> consumerService.onboardConsumer(validRequest));

        // Verify that only email check was performed
        verify(consumerRepository).existsByEmail(validRequest.getEmail());
        verify(consumerRepository, never()).existsByNationalId(any());
        verify(consumerRepository, never()).existsByPhone(any());
    }

    @Test
    @DisplayName("Should validate all fields when email is unique")
    void testOnboardConsumer_AllFieldsValidated() {
        // Arrange
        when(consumerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(consumerRepository.existsByNationalId(validRequest.getNationalId())).thenReturn(false);
        when(consumerRepository.existsByPhone(validRequest.getPhone())).thenReturn(false);
        when(consumerRepository.existsByDocumentNumber(validRequest.getDocumentNumber())).thenReturn(false);
        when(consumerRepository.existsByPanNumber(validRequest.getPanNumber())).thenReturn(false);

        Consumer savedConsumer = new Consumer();
        savedConsumer.setId("test-id-all");
        when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

        // Act
        consumerService.onboardConsumer(validRequest);

        // Assert - Verify all checks were performed in order
        InOrder inOrder = inOrder(consumerRepository);
        inOrder.verify(consumerRepository).existsByEmail(validRequest.getEmail());
        inOrder.verify(consumerRepository).existsByNationalId(validRequest.getNationalId());
        inOrder.verify(consumerRepository).existsByPhone(validRequest.getPhone());
        inOrder.verify(consumerRepository).existsByDocumentNumber(validRequest.getDocumentNumber());
        inOrder.verify(consumerRepository).existsByPanNumber(validRequest.getPanNumber());
        inOrder.verify(consumerRepository).save(any());
    }

    // Helper method to create valid request
    private CreateConsumerOnboardingRequest createValidRequest() {
        return CreateConsumerOnboardingRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .nationalId("123-45-6789")
                .documentType("NATIONAL_ID")
                .documentNumber("ID123456789")
                .panNumber("ABCDE1234F")
                .employerName("Tech Company")
                .position("Senior Engineer")
                .employmentType("FULL_TIME")
                .yearsOfExperience(8)
                .industry("Technology")
                .monthlyIncome(new BigDecimal("5000.00"))
                .annualIncome(new BigDecimal("60000.00"))
                .incomeSource("SALARY")
                .currency("USD")
                .build();
    }
}
