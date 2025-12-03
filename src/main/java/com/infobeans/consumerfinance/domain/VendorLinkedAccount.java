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
 * Vendor Linked Account Entity
 *
 * Represents a consumer's account linked to a vendor/partner.
 * Constraint: One vendor-linked account per vendor per consumer (composite unique on consumer_id + vendor_id).
 * When vendor becomes inactive, linked accounts are disabled.
 *
 * Database Table: vendor_linked_accounts
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "vendor_linked_accounts",
    indexes = {
        @Index(name = "idx_consumer_linked", columnList = "consumer_id"),
        @Index(name = "idx_vendor_linked", columnList = "vendor_id"),
        @Index(name = "idx_linked_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_consumer_vendor", columnNames = {"consumer_id", "vendor_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorLinkedAccount {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "consumer_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID consumerId;

    @Column(name = "principal_account_id", columnDefinition = "VARCHAR(36)")
    private UUID principalAccountId;

    @Column(name = "vendor_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID vendorId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    /**
     * External account reference from vendor system
     */
    @Column(name = "external_account_ref", length = 255)
    private String externalAccountRef;

    /**
     * Linkage ID from vendor system
     */
    @Column(name = "linkage_id", length = 255)
    private String linkageId;

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
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "VendorLinkedAccount{" +
                "id=" + id +
                ", consumerId=" + consumerId +
                ", vendorId=" + vendorId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
