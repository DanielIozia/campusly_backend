-- liquibase formatted sql
-- changeset campusly:004-registration-flow-refactor

-- title: Refactor flusso registrazione - aggiunge stato utente e OTP --
-- tabelle modificate: users --
-- tabelle create: --

-- Aggiunge la colonna status per la macchina a stati della registrazione
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Rende nullable le colonne che non esistono ancora al momento del primo step
ALTER TABLE users ALTER COLUMN username     DROP NOT NULL;
ALTER TABLE users ALTER COLUMN first_name   DROP NOT NULL;
ALTER TABLE users ALTER COLUMN last_name    DROP NOT NULL;

-- Aggiunge le colonne OTP
ALTER TABLE users ADD COLUMN otp_code       VARCHAR(10);
ALTER TABLE users ADD COLUMN otp_expires_at TIMESTAMP;

-- Rimuove il campo phone (non più necessario)
ALTER TABLE users DROP COLUMN IF EXISTS phone;

-- Indice per velocizzare i lookup per email + status (usati spesso nel flusso)
CREATE INDEX idx_users_email_status ON users(email, status);
