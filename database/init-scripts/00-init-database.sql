-- Initialize database for OS Service
-- This script runs when the PostgreSQL container starts

-- Create indexes for better query performance
-- (Tables are auto-created by Hibernate)

-- Note: The tables will be created automatically by JPA/Hibernate
-- This file can be used for additional initialization if needed

SELECT 'OS Service database initialized' AS status;
