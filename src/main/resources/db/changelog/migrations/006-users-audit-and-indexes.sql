-- liquibase formatted sql
-- changeset campusly:006-users-audit-and-indexes

-- title: Users — aggiunge updated_at e indici frequenti --
-- tabelle modificate: users --

ALTER TABLE users ADD COLUMN updated_at TIMESTAMP;

CREATE INDEX idx_users_university_id ON users(university_id);
CREATE INDEX idx_users_status         ON users(status);
