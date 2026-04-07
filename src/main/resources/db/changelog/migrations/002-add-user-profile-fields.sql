-- liquibase formatted sql
-- changeset campusly:002-add-user-profile-fields

ALTER TABLE users ADD COLUMN nome        VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN cognome     VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN data_nascita DATE;
ALTER TABLE users ADD COLUMN telefono    VARCHAR(20);

-- Rimuovi i default temporanei usati per la migrazione su colonne esistenti
ALTER TABLE users ALTER COLUMN nome DROP DEFAULT;
ALTER TABLE users ALTER COLUMN cognome DROP DEFAULT;
