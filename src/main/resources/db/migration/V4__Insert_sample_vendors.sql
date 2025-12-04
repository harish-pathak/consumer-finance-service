-- Insert sample vendors for testing vendor account creation
INSERT INTO vendors (id, name, status, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440111', 'Bank of America', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440222', 'Wells Fargo', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440333', 'Chase Bank', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440444', 'Citibank', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440555', 'PayPal', 'INACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status=status;
