-- Add pan_number column to consumers table
-- PAN (Permanent Account Number) is mandatory for Indian consumers
-- Format: ABCDE1234F (5 letters, 4 digits, 1 letter)
-- Must be unique across all consumers

-- Step 1: Add pan_number column (allow NULL initially for existing records)
ALTER TABLE consumers
ADD COLUMN pan_number VARCHAR(10) COMMENT 'Indian PAN (Permanent Account Number) - Format: ABCDE1234F';

-- Step 2: Add unique constraint on pan_number
-- This ensures no two consumers can have the same PAN
ALTER TABLE consumers
ADD CONSTRAINT uk_pan_number UNIQUE (pan_number);

-- Step 3: Add index for performance (queries by PAN number)
CREATE INDEX idx_pan_number ON consumers(pan_number);

-- Note: For new consumer onboarding, pan_number will be mandatory at application level
-- Existing consumers (if any) will have NULL pan_number until they update their profile
-- In production, you may want to set NOT NULL after backfilling existing data
