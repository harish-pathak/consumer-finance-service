-- Create vendor_linked_accounts table
-- Manages system-managed vendor-linked accounts for consumers
-- Enforces uniqueness on (consumer_id, vendor_id) to prevent duplicate links

CREATE TABLE IF NOT EXISTS vendor_linked_accounts (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique identifier (UUID)',
    consumer_id VARCHAR(36) NOT NULL COMMENT 'Foreign key to consumers table',
    vendor_id VARCHAR(36) NOT NULL COMMENT 'Foreign key to vendors table',
    principal_account_id VARCHAR(36) COMMENT 'Reference to principal account',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Account lifecycle status: ACTIVE, DISABLED, ARCHIVED',
    external_account_ref VARCHAR(255) COMMENT 'Reference ID in external vendor system',
    linkage_id VARCHAR(100) COMMENT 'Internal linkage identifier',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation timestamp',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    created_by VARCHAR(100) COMMENT 'User/system that created this account',
    updated_by VARCHAR(100) COMMENT 'User/system that last updated this account',

    -- Constraints
    CONSTRAINT fk_vendor_linked_account_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers(id) ON DELETE CASCADE,
    CONSTRAINT fk_vendor_linked_account_vendor FOREIGN KEY (vendor_id)
        REFERENCES vendors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_vendor_linked_account_principal FOREIGN KEY (principal_account_id)
        REFERENCES principal_accounts(id) ON DELETE SET NULL,

    -- Uniqueness: only one active link per consumer-vendor pair
    CONSTRAINT uk_consumer_vendor_link UNIQUE KEY (consumer_id, vendor_id),

    -- Indexes for performance
    INDEX idx_consumer_id (consumer_id),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_principal_account_id (principal_account_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_consumer_status (consumer_id, status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vendor-linked accounts: system-managed records tied to consumer principal accounts';
