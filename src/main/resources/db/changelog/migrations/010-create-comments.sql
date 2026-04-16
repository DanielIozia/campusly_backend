-- liquibase formatted sql
-- changeset campusly:010-create-comments

-- title: Crea tabella comments (commenti su spotted, con supporto thread) --
-- tabelle create: comments --

CREATE TABLE comments (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    spotted_id        UUID NOT NULL REFERENCES spotted(id) ON DELETE CASCADE,
    author_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    parent_comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    content           TEXT NOT NULL,
    is_anonymous      BOOLEAN NOT NULL DEFAULT false,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE INDEX idx_comments_spotted_id ON comments(spotted_id);
CREATE INDEX idx_comments_author_id  ON comments(author_id);
CREATE INDEX idx_comments_parent_id  ON comments(parent_comment_id);
