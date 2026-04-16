-- liquibase formatted sql
-- changeset campusly:015-create-spotted-images

-- title: Crea tabella spotted_images per la gestione delle immagini degli spotted --
-- tabelle create: spotted_images --

CREATE TABLE spotted_images (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    spotted_id    UUID NOT NULL REFERENCES spotted(id) ON DELETE CASCADE,
    file_name     VARCHAR(255) NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_spotted_images_spotted_id ON spotted_images(spotted_id);
