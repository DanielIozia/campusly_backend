-- liquibase formatted sql
-- changeset campusly:005-status-length-50

-- Modifica la lunghezza della colonna status a 50 caratteri
ALTER TABLE users ALTER COLUMN status TYPE VARCHAR(50);
