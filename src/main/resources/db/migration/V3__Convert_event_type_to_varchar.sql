-- V3__Convert_event_type_to_varchar.sql
-- Convert PostgreSQL native enum to VARCHAR for better Hibernate compatibility

-- First, alter the column to use VARCHAR
ALTER TABLE audit_event
    ALTER COLUMN event_type TYPE VARCHAR(50)
    USING event_type::text;

-- Drop the old enum type
DROP TYPE IF EXISTS event_type;

