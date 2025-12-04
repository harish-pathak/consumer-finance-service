package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.enums.VendorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Vendor Entity
 *
 * Represents a vendor/partner that consumers can link their accounts with.
 * When a vendor becomes inactive, all linked accounts should be disabled.
 *
 * Database Table: vendors
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Entity
@Table(
    name = "vendors",
    indexes = {
        @Index(name = "idx_vendor_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", length = 36)
    private String id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private VendorStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = VendorStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
