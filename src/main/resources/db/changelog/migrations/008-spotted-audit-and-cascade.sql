-- liquibase formatted sql
-- changeset campusly:008-spotted-audit-and-cascade

-- title: Spotted — aggiunge updated_at, ricrea FK con ON DELETE SET NULL, indici feed --
-- tabelle modificate: spotted --

ALTER TABLE spotted ADD COLUMN updated_at TIMESTAMP;

-- Ricrea FK author_id con ON DELETE SET NULL (post resta visibile dopo cancellazione utente)
ALTER TABLE spotted DROP CONSTRAINT IF EXISTS spotted_author_id_fkey;
ALTER TABLE spotted ADD CONSTRAINT spotted_author_id_fkey
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_spotted_author_id        ON spotted(author_id);
CREATE INDEX idx_spotted_status_created   ON spotted(status, created_at DESC);
