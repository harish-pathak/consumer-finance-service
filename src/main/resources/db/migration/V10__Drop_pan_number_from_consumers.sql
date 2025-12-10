-- =====================================================
-- Drop pan_number column from consumers table
-- =====================================================
-- This migration reverts the changes made in V9__Add_pan_number_to_consumers.sql
-- It safely drops the column, index, and constraints only if they exist.
-- =====================================================

-- Check if pan_number column exists before attempting to drop
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'consumers'
      AND COLUMN_NAME = 'pan_number'
);

-- Only proceed with drops if column exists
SET @drop_statements = IF(
    @column_exists > 0,
    'ALTER TABLE consumers DROP INDEX IF EXISTS idx_pan_number; ALTER TABLE consumers DROP COLUMN pan_number;',
    'SELECT "Column pan_number does not exist, skipping migration" AS message;'
);

-- Execute the dynamic SQL
PREPARE stmt FROM @drop_statements;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Display completion message
SELECT
    CASE
        WHEN @column_exists > 0
        THEN 'Migration V10 completed: pan_number column, index, and constraints removed'
        ELSE 'Migration V10 completed: No changes needed (pan_number does not exist)'
    END AS Status;
