package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.embedded.EmploymentDetails;
import com.infobeans.consumerfinance.domain.embedded.IncomeDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consumer Entity
 *
 * Represents a consumer in the system with personal, identity, employment, and financial information.
 * Sensitive fields (nationalId, employmentDetails, incomeDetails) are encrypted at application level
 * using JPA AttributeConverters.
 *
 * Database Table: consumers
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "consumers",
    indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumer {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    /**
     * National ID - Encrypted at application level
     * Converter: EncryptedStringConverter
     */
    @Column(name = "national_id", length = 500)
    private String nationalId;

    /**
     * Employment Details - Encrypted at application level
     * This is an embedded entity containing employer, position, etc.
     */
    @Embedded
    @AttributeOverride(name = "employerName", column = @Column(name = "employment_details"))
    private EmploymentDetails employmentDetails;

    /**
     * Income Details - Encrypted at application level
     * This is an embedded entity containing monthly/annual income
     */
    @Embedded
    @AttributeOverride(name = "monthlyIncome", column = @Column(name = "income_details"))
    private IncomeDetails incomeDetails;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ConsumerStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ConsumerStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Consumer Status Enum
     * ACTIVE: Consumer account is active
     * INACTIVE: Consumer account is inactive
     * SUSPENDED: Consumer account is suspended
     */
    public enum ConsumerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }

    @Override
    public String toString() {
        return "Consumer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
