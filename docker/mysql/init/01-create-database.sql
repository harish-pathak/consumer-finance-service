-- =====================================================
-- Database Initialization Script
-- =====================================================
-- This script creates the consumer_finance database
-- if it doesn't already exist.
--
-- Usage:
-- 1. For Docker: This runs automatically on first container start
-- 2. For Local MySQL: Run manually with:
--    mysql -u root -p < docker/mysql/init/01-create-database.sql
-- =====================================================

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS `consumer_finance`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Display confirmation
SELECT CONCAT('Database "consumer_finance" is ready!') AS Status;

-- Grant privileges to the application user (if not running as root)
-- Note: In Docker, this user is created automatically by MYSQL_USER env var
-- For local setup, you may need to create the user manually:
-- CREATE USER IF NOT EXISTS 'financeuser'@'%' IDENTIFIED BY 'financepass';
-- GRANT ALL PRIVILEGES ON consumer_finance.* TO 'financeuser'@'%';
-- FLUSH PRIVILEGES;
