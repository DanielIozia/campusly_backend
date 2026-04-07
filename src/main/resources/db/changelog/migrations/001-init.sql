-- liquibase formatted sql
-- changeset campusly:001-init

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE universities (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome           VARCHAR(255) NOT NULL,
    paese          VARCHAR(100) NOT NULL,
    citta          VARCHAR(100) NOT NULL,
    dominio_email  VARCHAR(255) UNIQUE,
    internazionale BOOLEAN DEFAULT false,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(50) UNIQUE NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255),
    university_id       UUID REFERENCES universities(id),
    erasmus_univ_id     UUID REFERENCES universities(id),
    is_erasmus          BOOLEAN DEFAULT false,
    photo_url           TEXT,
    bio                 VARCHAR(300),
    auth_provider       VARCHAR(20) DEFAULT 'LOCAL',
    role                VARCHAR(20) DEFAULT 'STUDENT',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE spotted (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id      UUID REFERENCES users(id),
    content        TEXT NOT NULL,
    category       VARCHAR(30) NOT NULL,
    university_id  UUID NOT NULL REFERENCES universities(id),
    is_anonymous   BOOLEAN DEFAULT false,
    like_count     INTEGER DEFAULT 0,
    comment_count  INTEGER DEFAULT 0,
    status         VARCHAR(20) DEFAULT 'ACTIVE',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_spotted_university ON spotted(university_id);
CREATE INDEX idx_spotted_created_at ON spotted(created_at DESC);