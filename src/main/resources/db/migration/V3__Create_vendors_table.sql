-- Create vendors table
-- Represents vendor/partner organizations that consumers can link their accounts with
-- Vendors can be activated or deactivated system-wide

CREATE TABLE IF NOT EXISTS vendors (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique identifier (UUID)',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Vendor name (unique)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Vendor status: ACTIVE, INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Vendor creation timestamp',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',

    -- Indexes for performance
    INDEX idx_vendor_status (status),
    INDEX idx_vendor_name (name)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vendor partners that can be linked to consumer accounts';
