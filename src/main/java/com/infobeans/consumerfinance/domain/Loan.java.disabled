package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loan Entity
 *
 * Represents a disbursed loan to a consumer.
 * Tracks loan lifecycle: ACTIVE → CLOSED/DEFAULTED
 *
 * Database Table: loans
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "loans",
    indexes = {
        @Index(name = "idx_loan_application", columnList = "loan_application_id"),
        @Index(name = "idx_loan_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "loan_application_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID loanApplicationId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column(name = "principal", nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "outstanding_balance", precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
            this.status = LoanStatus.ACTIVE;
        }
        if (this.outstandingBalance == null && this.principal != null) {
            this.outstandingBalance = this.principal;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", loanApplicationId=" + loanApplicationId +
                ", status=" + status +
                ", principal=" + principal +
                ", outstandingBalance=" + outstandingBalance +
                ", createdAt=" + createdAt +
                '}';
    }
}
