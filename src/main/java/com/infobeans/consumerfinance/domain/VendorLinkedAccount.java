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
 * VendorLinkedAccount Entity
 *
 * Represents a system-managed vendor-linked account tied to a consumer's principal account.
 * This is a read-only entity for consumers; lifecycle is managed by the system.
 *
 * Key characteristics:
 * - One-to-many relationship: each consumer can have multiple vendor-linked accounts
 * - Uniqueness enforced on (consumer_id, vendor_id) to prevent duplicate links
 * - Lifecycle states: ACTIVE, DISABLED, ARCHIVED
 * - Audit fields: created_by, updated_by track who made changes
 *
 * Database Table: vendor_linked_accounts
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Entity
@Table(
    name = "vendor_linked_accounts",
    indexes = {
        @Index(name = "idx_vendor_linked_account_consumer", columnList = "consumer_id"),
        @Index(name = "idx_vendor_linked_account_vendor", columnList = "vendor_id"),
        @Index(name = "idx_vendor_linked_account_principal", columnList = "principal_account_id"),
        @Index(name = "idx_vendor_linked_account_status", columnList = "status"),
        @Index(name = "idx_vendor_linked_account_created", columnList = "created_at"),
        @Index(name = "idx_vendor_linked_account_consumer_status", columnList = "consumer_id, status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_vendor_linked_account_consumer_vendor",
            columnNames = {"consumer_id", "vendor_id"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorLinkedAccount {

    /**
     * Primary Key: Unique identifier (UUID as String for consistency)
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Foreign Key: Reference to consumer who owns this vendor-linked account
     * ON DELETE CASCADE: When consumer is deleted, all vendor-linked accounts are deleted
     */
    @Column(name = "consumer_id", nullable = false, length = 36)
    private String consumerId;

    /**
     * Foreign Key: Optional reference to the principal account
     * ON DELETE SET NULL: If principal account is deleted, this reference becomes null
     */
    @Column(name = "principal_account_id", length = 36)
    private String principalAccountId;

    /**
     * Foreign Key: Reference to the vendor this account is linked to
     * ON DELETE RESTRICT: Prevents deletion of vendor if linked accounts exist
     */
    @Column(name = "vendor_id", nullable = false, length = 36)
    private String vendorId;

    /**
     * Lifecycle status of this vendor-linked account
     * Possible states: ACTIVE, DISABLED, ARCHIVED
     * Default: ACTIVE
     */
    @Column(name = "status", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    /**
     * External reference ID in the vendor's system
     * Used for integration and reconciliation with vendor systems
     */
    @Column(name = "external_account_ref", length = 255)
    private String externalAccountRef;

    /**
     * Internal linkage identifier for system tracking
     */
    @Column(name = "linkage_id", length = 100)
    private String linkageId;

    /**
     * Timestamp when this vendor-linked account was created
     * Auto-set on persist, not updatable
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this vendor-linked account was last updated
     * Auto-updated on each modification
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Audit field: User or system that created this account
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Audit field: User or system that last updated this account
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * JPA lifecycle hook: executed before persist operation
     * Auto-generates ID and sets default values if not provided
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
    }

    /**
     * JPA lifecycle hook: executed before update operation
     * Updates the updatedAt timestamp to current time
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "VendorLinkedAccount{" +
                "id='" + id + '\'' +
                ", consumerId='" + consumerId + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
