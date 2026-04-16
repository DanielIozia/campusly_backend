-- liquibase formatted sql
-- changeset campusly:013-create-notifications

-- title: Crea tabella notifications (notifiche in-app) --
-- tabelle create: notifications --

CREATE TABLE notifications (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    type         VARCHAR(50) NOT NULL,
    target_type  VARCHAR(20),
    target_id    UUID,
    message      TEXT,
    is_read      BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, is_read, created_at DESC);
