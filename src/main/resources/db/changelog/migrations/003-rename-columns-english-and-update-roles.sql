-- liquibase formatted sql
-- changeset campusly:003-rename-columns-english-and-update-roles

-- title: Rinomina colonne in snake_case inglese su tutte le tabelle e aggiorna i ruoli --
-- tabelle modificate: universities, users --
-- tabelle create: --

-- ---------------------------------------------------------------
-- Tabella: universities
-- ---------------------------------------------------------------
ALTER TABLE universities RENAME COLUMN nome           TO name;
ALTER TABLE universities RENAME COLUMN paese          TO country;
ALTER TABLE universities RENAME COLUMN citta          TO city;
ALTER TABLE universities RENAME COLUMN dominio_email  TO email_domain;
ALTER TABLE universities RENAME COLUMN internazionale TO international;

-- ---------------------------------------------------------------
-- Tabella: users  (colonne aggiunte dalla migration 002)
-- ---------------------------------------------------------------
ALTER TABLE users RENAME COLUMN nome         TO first_name;
ALTER TABLE users RENAME COLUMN cognome      TO last_name;
ALTER TABLE users RENAME COLUMN data_nascita TO birth_date;
ALTER TABLE users RENAME COLUMN telefono     TO phone;

-- Aumenta la dimensione della colonna phone per supportare il formato +XXXX-XXXXXXXXXXXXXXX
ALTER TABLE users ALTER COLUMN phone TYPE VARCHAR(25);

-- Aggiorna il default del ruolo e migra i valori esistenti
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'CAMPUSLY_USER';
UPDATE users SET role = 'CAMPUSLY_USER' WHERE role = 'STUDENT';
