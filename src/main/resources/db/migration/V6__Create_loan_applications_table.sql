-- Create loan_applications table
-- Stores consumer loan application submissions
-- Tracks application lifecycle: PENDING -> APPROVED/REJECTED/CANCELLED

CREATE TABLE IF NOT EXISTS loan_applications (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique identifier (UUID)',
    consumer_id VARCHAR(36) NOT NULL COMMENT 'Foreign key to consumers table',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Application status: PENDING, APPROVED, REJECTED, CANCELLED',
    requested_amount DECIMAL(15, 2) NOT NULL COMMENT 'Loan amount requested (in currency units)',
    term_in_months INT COMMENT 'Loan term in months',
    purpose VARCHAR(255) COMMENT 'Purpose of the loan',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Application submission timestamp',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',

    -- Constraints
    CONSTRAINT fk_loan_app_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers(id) ON DELETE CASCADE,

    -- Indexes for performance and duplicate detection
    INDEX idx_app_consumer (consumer_id),
    INDEX idx_app_status (status),
    INDEX idx_app_created_at (created_at),
    INDEX idx_app_consumer_status (consumer_id, status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Loan applications: tracks consumer loan application submissions and lifecycle';
