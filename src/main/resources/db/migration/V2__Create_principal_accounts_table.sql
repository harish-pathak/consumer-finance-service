-- Create principal_accounts table for storing principal account records linked to consumers
-- Each consumer can have exactly ONE principal account (enforced by unique constraint on consumer_id)
-- Principal account serves as the main account holder record for financial operations

CREATE TABLE IF NOT EXISTS principal_accounts (
    -- Primary Key and Identifiers
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique principal account identifier (UUID)',
    consumer_id VARCHAR(36) NOT NULL UNIQUE COMMENT 'Foreign key reference to consumers.id - unique to enforce one account per consumer',

    -- Account Information
    account_type VARCHAR(50) DEFAULT 'PRIMARY' COMMENT 'Type of principal account (PRIMARY, SECONDARY, etc.)',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Account status (ACTIVE, INACTIVE, ARCHIVED, SUSPENDED)',

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when principal account was created',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when principal account was last updated',
    created_by VARCHAR(100) COMMENT 'User or system that created the principal account',
    updated_by VARCHAR(100) COMMENT 'User or system that last updated the principal account',

    -- Foreign Key Constraint for referential integrity with consumers table
    CONSTRAINT fk_principal_account_consumer FOREIGN KEY (consumer_id) REFERENCES consumers(id) ON DELETE CASCADE,

    -- Indexes for Performance
    UNIQUE INDEX uk_principal_account_consumer_id (consumer_id) COMMENT 'Enforce one principal account per consumer',
    INDEX idx_status (status) COMMENT 'Index on status for filtering by account status',
    INDEX idx_created_at (created_at) COMMENT 'Index on creation timestamp for chronological queries'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Principal accounts linked to consumers (one account per consumer)';
