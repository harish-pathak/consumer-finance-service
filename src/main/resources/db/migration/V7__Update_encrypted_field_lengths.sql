-- Migration to update column lengths for encrypted fields
-- Encrypted data (Base64 encoded with IV and GCM tag) requires more space than plaintext
-- AES-GCM encrypted data: IV (12 bytes) + ciphertext (variable) + tag (16 bytes)
-- Base64 encoding increases size by ~33%

-- Update consumers table - identity fields
ALTER TABLE consumers
    MODIFY COLUMN national_id VARCHAR(500) COMMENT 'National ID (AES-GCM encrypted)',
    MODIFY COLUMN document_number VARCHAR(500) COMMENT 'Document number (AES-GCM encrypted)';

-- Update consumers table - employment fields
ALTER TABLE consumers
    MODIFY COLUMN employer_name VARCHAR(500) COMMENT 'Employer name (AES-GCM encrypted)',
    MODIFY COLUMN income_source VARCHAR(500) COMMENT 'Income source (AES-GCM encrypted)';
