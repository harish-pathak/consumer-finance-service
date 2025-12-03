package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Principal Account Entity
 *
 * Represents the primary account for a consumer.
 * Constraint: Exactly ONE principal account per consumer (unique on consumer_id).
 * Auto-created post-consumer onboarding.
 *
 * Database Table: principal_accounts
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "principal_accounts",
    indexes = {
        @Index(name = "idx_principal_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_consumer", columnNames = "consumer_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipalAccount {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "consumer_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID consumerId;

    /**
     * Account Type
     * Examples: DEFAULT, PREMIUM, etc.
     */
    @Column(name = "account_type", length = 50)
    private String accountType;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

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
            this.status = AccountStatus.ACTIVE;
        }
        if (this.accountType == null) {
            this.accountType = "DEFAULT";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PrincipalAccount{" +
                "id=" + id +
                ", consumerId=" + consumerId +
                ", accountType='" + accountType + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
