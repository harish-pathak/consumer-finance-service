-- Create loan_application_decisions table
-- Audit trail for all decisions made on loan applications (approve, reject)
-- Immutable record: once created, decisions cannot be modified or deleted
-- Enables full traceability: who decided, when, what reason

CREATE TABLE IF NOT EXISTS loan_application_decisions (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique identifier (UUID)',
    application_id VARCHAR(36) NOT NULL COMMENT 'Foreign key to loan_applications table',
    decision ENUM('APPROVED', 'REJECTED') NOT NULL COMMENT 'Decision outcome',
    staff_id VARCHAR(100) NOT NULL COMMENT 'ID of staff member who made decision',
    reason VARCHAR(500) COMMENT 'Reason for the decision',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Decision timestamp',

    -- Constraints
    CONSTRAINT fk_decision_app FOREIGN KEY (application_id)
        REFERENCES loan_applications(id) ON DELETE CASCADE,

    -- Unique constraint: prevent duplicate decisions
    UNIQUE KEY uq_app_decision (application_id, decision),

    -- Indexes for query performance
    INDEX idx_decision_app (application_id),
    INDEX idx_decision_staff (staff_id),
    INDEX idx_decision_created (created_at),
    INDEX idx_decision_status (decision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Loan application decisions: immutable audit trail of approval/rejection decisions';
