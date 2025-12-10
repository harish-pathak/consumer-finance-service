package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.converter.EncryptedFieldConverter;
import com.infobeans.consumerfinance.converter.UUIDConverter;
import com.infobeans.consumerfinance.domain.embedded.EmploymentDetails;
import com.infobeans.consumerfinance.domain.embedded.IncomeDetails;
import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consumer entity representing a consumer profile with onboarding data.
 *
 * Stores personal, identity, employment, and financial information.
 * Sensitive fields (national_id, document_number, employer_name, income details) are encrypted
 * at the application level using JPA AttributeConverter before persistence.
 *
 * Uniqueness is enforced on email and national_id to detect duplicate onboarding attempts.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Entity
@Table(
    name = "consumers",
    indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_national_id", columnList = "national_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_email_national_id",
            columnNames = {"email", "national_id"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumer {

    /**
     * Unique consumer identifier (UUID string).
     */
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", length = 36)
    private String id;

    // ==================== PERSONAL INFORMATION ====================

    /**
     * Consumer's first name.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * Consumer's last name.
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Consumer's email address. Unique to detect duplicate registrations.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Consumer's phone number (optional).
     */
    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    /**
     * Consumer's date of birth (optional).
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // ==================== IDENTITY INFORMATION (ENCRYPTED) ====================

    /**
     * National ID or government-issued identifier (ENCRYPTED).
     * Unique to detect duplicate registrations.
     */
    @Column(name = "national_id", unique = true, length = 500)
    @Convert(converter = EncryptedFieldConverter.class)
    private String nationalId;

    /**
     * Type of identity document (e.g., PASSPORT, NATIONAL_ID, DRIVER_LICENSE).
     */
    @Column(name = "document_type", length = 50)
    private String documentType;

    /**
     * Identity document number (ENCRYPTED).
     */
    @Column(name = "document_number", unique = true, length = 500)
    @Convert(converter = EncryptedFieldConverter.class)
    private String documentNumber;

    /**
     * Indian PAN (Permanent Account Number).
     * Format: ABCDE1234F (5 letters, 4 digits, 1 letter).
     * Unique to detect duplicate registrations.
     */
    @Column(name = "pan_number", unique = true, length = 10)
    private String panNumber;

    // ==================== EMPLOYMENT INFORMATION (EMBEDDED) ====================

    /**
     * Employment details including employer name, position, years of experience, industry.
     * Sensitive fields are encrypted via embedded object converter.
     */
    @Embedded
    private EmploymentDetails employmentDetails;

    // ==================== FINANCIAL INFORMATION (EMBEDDED) ====================

    /**
     * Income details including monthly/annual income, source, currency.
     * Sensitive fields are encrypted via embedded object converter.
     */
    @Embedded
    private IncomeDetails incomeDetails;

    // ==================== STATUS AND METADATA ====================

    /**
     * Consumer account status (ACTIVE, DISABLED, ARCHIVED).
     */
    @Column(name = "status", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Timestamp when the consumer record was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the consumer record was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User or system that created the record.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * User or system that last updated the record.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Lifecycle hook: set createdAt timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }

    /**
     * Lifecycle hook: update updatedAt timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
