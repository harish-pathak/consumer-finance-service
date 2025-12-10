package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loan Application Entity
 *
 * Represents a consumer's loan application request.
 * Tracks the lifecycle: PENDING → APPROVED/REJECTED
 *
 * Database Table: loan_applications
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "loan_applications",
    indexes = {
        @Index(name = "idx_app_consumer", columnList = "consumer_id"),
        @Index(name = "idx_app_status", columnList = "status"),
        @Index(name = "idx_app_created_at", columnList = "created_at"),
        @Index(name = "idx_app_consumer_status", columnList = "consumer_id, status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", length = 36)
    private String id;

    @Column(name = "consumer_id", nullable = false, columnDefinition = "VARCHAR(36)", length = 36)
    private String consumerId;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private LoanApplicationStatus status;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "term_in_months")
    private Integer termInMonths;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = LoanApplicationStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "LoanApplication{" +
                "id=" + id +
                ", consumerId=" + consumerId +
                ", status=" + status +
                ", requestedAmount=" + requestedAmount +
                ", termInMonths=" + termInMonths +
                ", createdAt=" + createdAt +
                '}';
    }
}
