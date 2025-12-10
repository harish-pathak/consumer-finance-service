package com.infobeans.consumerfinance.domain;

import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing the decision made on a loan application.
 *
 * Audit Trail:
 * - Captures who made the decision (staffId), when, and why (reason)
 * - Immutable record: once created, decision cannot be modified or deleted
 * - Prevents duplicate decisions: one decision per application
 *
 * Design:
 * - Composite unique constraint on (application_id, decision) prevents duplicate decisions
 * - Foreign key to loan_applications with cascade delete
 * - Decision timestamp captured at persistence
 * - Staff ID extracted from JWT principal (actor performing the decision)
 *
 * Lifecycle:
 * - Created when decision is submitted
 * - Related loan_application status updated atomically in same transaction
 * - Event emitted on approval for downstream consumers
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Entity
@Table(
    name = "loan_application_decisions",
    indexes = {
        @Index(name = "idx_decision_app", columnList = "application_id"),
        @Index(name = "idx_decision_staff", columnList = "staff_id"),
        @Index(name = "idx_decision_created", columnList = "created_at"),
        @Index(name = "idx_decision_status", columnList = "decision")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_app_decision",
            columnNames = {"application_id", "decision"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LoanApplicationDecision {

    /**
     * Unique identifier for this decision record (UUID).
     * Auto-generated at creation time.
     * Format: 36-character string representation of UUID.
     */
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", length = 36)
    private String id;

    /**
     * Foreign key: Reference to the loan application this decision applies to.
     * Links to loan_applications.id
     * ON DELETE CASCADE: Decision automatically deleted when application is deleted.
     * NOT NULL: Every decision must reference a valid application.
     */
    @Column(name = "application_id", nullable = false, columnDefinition = "VARCHAR(36)", length = 36)
    private String applicationId;

    /**
     * The decision made on the application.
     * Enum stored as VARCHAR(20).
     * Values: APPROVED, REJECTED
     * NOT NULL: Decision type is required.
     *
     * Unique Constraint: Composite with application_id prevents duplicate decisions
     * (only one APPROVED and one REJECTED per application, enforced by DB).
     */
    @Column(name = "decision", nullable = false, columnDefinition = "ENUM('APPROVED', 'REJECTED')")
    @Enumerated(EnumType.STRING)
    private LoanDecisionStatus decision;

    /**
     * The ID/username of the staff member who made this decision.
     * Extracted from JWT authentication principal.
     * Format: username or system identifier (36 chars max for consistency).
     * NOT NULL: Every decision must have an actor.
     */
    @Column(name = "staff_id", nullable = false, columnDefinition = "VARCHAR(100)")
    private String staffId;

    /**
     * Optional reason or notes explaining the decision.
     * Free-form text up to 500 characters.
     * Examples: "Income insufficient for requested amount", "Approved for 50000"
     * NULL allowed: Reason may be omitted.
     */
    @Column(name = "reason", columnDefinition = "VARCHAR(500)")
    private String reason;

    /**
     * Timestamp when this decision was created/recorded.
     * Auto-set by Spring Data @CreatedDate.
     * Immutable: Never changes after creation.
     * Stored as TIMESTAMP with current_timestamp default.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Lifecycle hook: Generate UUID before persisting to database.
     * Called automatically by JPA/Hibernate before INSERT.
     * Sets id as 36-character string UUID if not already set.
     */
    @PrePersist
    protected void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
