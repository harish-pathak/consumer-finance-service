package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.enums.RepaymentStatus;
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
 * Repayment Entity
 *
 * Represents a repayment transaction against a loan.
 * Tracks payment status: PENDING → SUCCESS/FAILED/REVERSED
 *
 * Database Table: repayments
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "repayments",
    indexes = {
        @Index(name = "idx_repay_loan", columnList = "loan_id"),
        @Index(name = "idx_repay_status", columnList = "status"),
        @Index(name = "idx_repay_due_date", columnList = "due_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repayment {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "loan_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID loanId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RepaymentStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

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
            this.status = RepaymentStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Repayment{" +
                "id=" + id +
                ", loanId=" + loanId +
                ", amount=" + amount +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", paidDate=" + paidDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
