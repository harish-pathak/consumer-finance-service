-- Create consumers table with all required columns for onboarding data
-- This table stores consumer profile information across personal, identity, employment, and financial dimensions

CREATE TABLE IF NOT EXISTS consumers (
    -- Primary Key and Identifiers
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique consumer identifier (UUID)',

    -- Personal Information
    first_name VARCHAR(100) NOT NULL COMMENT 'Consumer first name',
    last_name VARCHAR(100) NOT NULL COMMENT 'Consumer last name',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Consumer email (unique for duplicate detection)',
    phone VARCHAR(20) UNIQUE COMMENT 'Consumer phone number',
    date_of_birth DATE COMMENT 'Consumer date of birth',

    -- Identity Information (Encrypted)
    national_id VARCHAR(255) UNIQUE COMMENT 'National ID or government-issued identifier (encrypted)',
    document_type VARCHAR(50) COMMENT 'Type of identity document (e.g., PASSPORT, NATIONAL_ID, DRIVER_LICENSE)',
    document_number VARCHAR(255) UNIQUE COMMENT 'Identity document number (encrypted)',

    -- Employment Information (Encrypted)
    employer_name VARCHAR(255) COMMENT 'Name of employer (encrypted)',
    position VARCHAR(100) COMMENT 'Job position/title',
    employment_type VARCHAR(50) COMMENT 'Type of employment (e.g., FULL_TIME, PART_TIME, SELF_EMPLOYED)',
    years_of_experience BIGINT COMMENT 'Years of work experience',
    industry VARCHAR(100) COMMENT 'Industry sector',

    -- Financial Information (Encrypted)
    monthly_income DECIMAL(15, 2) COMMENT 'Monthly income in base currency (encrypted)',
    annual_income DECIMAL(15, 2) COMMENT 'Annual income in base currency (encrypted)',
    income_source VARCHAR(255) COMMENT 'Source of income',
    currency VARCHAR(3) DEFAULT 'USD' COMMENT 'Currency code for financial amounts',

    -- Status and Metadata
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Consumer account status (ACTIVE, DISABLED, ARCHIVED)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when record was created',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when record was last updated',
    created_by VARCHAR(100) COMMENT 'User or system that created the record',
    updated_by VARCHAR(100) COMMENT 'User or system that last updated the record',

    -- Indexes for Performance
    INDEX idx_email (email) COMMENT 'Index on email for duplicate detection and lookups',
    INDEX idx_national_id (national_id) COMMENT 'Index on national ID for identity verification',
    INDEX idx_status (status) COMMENT 'Index on status for filtering active consumers',
    INDEX idx_created_at (created_at) COMMENT 'Index on creation timestamp for chronological queries',

    -- Constraints
    CONSTRAINT check_email_format CHECK (email LIKE '%@%'),
    CONSTRAINT check_positive_income CHECK (monthly_income IS NULL OR monthly_income >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Consumer profiles with onboarding data (personal, identity, employment, financial)'
;

-- Create index on combination of email and national_id for comprehensive uniqueness
CREATE UNIQUE INDEX idx_email_national_id ON consumers (email, national_id);

-- Add comment to table noting encrypted fields
ALTER TABLE consumers COMMENT = 'Consumer profiles with onboarding data (personal, identity, employment, financial). Encrypted fields: national_id, document_number, employer_name, monthly_income, annual_income, income_source'
;
