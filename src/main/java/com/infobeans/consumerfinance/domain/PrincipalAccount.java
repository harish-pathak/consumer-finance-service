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
 * PrincipalAccount entity representing a principal account linked to a consumer.
 *
 * Each consumer can have exactly one principal account (enforced via unique constraint).
 * The principal account serves as the main account holder record for financial operations.
 *
 * Uniqueness is enforced on consumer_id to ensure one principal account per consumer.
 * Referential integrity is enforced via foreign key constraint to consumers table.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Entity
@Table(
    name = "principal_accounts",
    indexes = {
        @Index(name = "idx_consumer_id", columnList = "consumer_id", unique = true),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_principal_account_consumer_id",
            columnNames = {"consumer_id"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipalAccount {

    /**
     * Unique principal account identifier (UUID string).
     */
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", length = 36)
    private String id;

    /**
     * Reference to the consumer this principal account belongs to (foreign key).
     * Unique constraint enforces one principal account per consumer.
     */
    @Column(name = "consumer_id", nullable = false, columnDefinition = "VARCHAR(36)", length = 36)
    private String consumerId;

    /**
     * Type of principal account (e.g., PRIMARY, SECONDARY).
     */
    @Column(name = "account_type", length = 50, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private String accountType = "PRIMARY";

    /**
     * Status of the principal account (ACTIVE, INACTIVE, ARCHIVED, SUSPENDED).
     */
    @Column(name = "status", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Timestamp when the principal account was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the principal account was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User or system that created this principal account.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * User or system that last updated this principal account.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Lifecycle hook: set id and timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
        if (accountType == null) {
            accountType = "PRIMARY";
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
