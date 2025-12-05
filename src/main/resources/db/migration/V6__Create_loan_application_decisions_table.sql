-- Create loan_application_decisions table
-- Audit trail for all decisions made on loan applications (approve, reject)
-- Immutable record: once created, decisions cannot be modified or deleted
-- Enables full traceability: who decided, when, what reason

CREATE TABLE IF NOT EXISTS loan_application_decisions (
    id VARCHAR(36) COLLATE utf8mb4_0900_ai_ci NOT NULL PRIMARY KEY,
    application_id VARCHAR(36) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    decision ENUM('APPROVED', 'REJECTED') NOT NULL,
    staff_id VARCHAR(100) NOT NULL,
    reason VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
