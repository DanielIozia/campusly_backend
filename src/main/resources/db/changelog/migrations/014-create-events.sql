-- liquibase formatted sql
-- changeset campusly:014-create-events

-- title: Crea tabelle events e event_attendees (eventi universitari) --
-- tabelle create: events, event_attendees --

CREATE TABLE events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    organizer_id    UUID REFERENCES users(id) ON DELETE SET NULL,
    university_id   UUID NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    location        VARCHAR(255),
    event_date      TIMESTAMP NOT NULL,
    end_date        TIMESTAMP,
    cover_image_url VARCHAR(500),
    category        VARCHAR(50),
    max_attendees   INTEGER,
    attendee_count  INTEGER NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE event_attendees (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id   UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rsvp       VARCHAR(20) NOT NULL DEFAULT 'ATTENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_event_attendees UNIQUE (event_id, user_id)
);

CREATE INDEX idx_events_university    ON events(university_id, event_date);
CREATE INDEX idx_events_organizer     ON events(organizer_id);
CREATE INDEX idx_event_attendees_usr  ON event_attendees(user_id);
