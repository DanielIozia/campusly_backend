-- liquibase formatted sql
-- changeset campusly:009-create-otp-tokens

-- title: Crea tabella otp_tokens, rimuove campi OTP da users --
-- tabelle create: otp_tokens --
-- tabelle modificate: users --

CREATE TABLE otp_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_type   VARCHAR(30) NOT NULL,
    otp_code     VARCHAR(10) NOT NULL,
    expires_at   TIMESTAMP NOT NULL,
    used         BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_otp_tokens_user_id   ON otp_tokens(user_id);
CREATE INDEX idx_otp_tokens_type_used ON otp_tokens(token_type, used);

-- Rimuove i campi OTP dalla tabella users (ora gestiti in otp_tokens)
ALTER TABLE users DROP COLUMN IF EXISTS otp_code;
ALTER TABLE users DROP COLUMN IF EXISTS otp_expires_at;
