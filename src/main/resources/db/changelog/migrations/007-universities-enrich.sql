-- liquibase formatted sql
-- changeset campusly:007-universities-enrich

-- title: Universities — arricchimento dati (updated_at, short_name, logo_url, website_url) --
-- tabelle modificate: universities --

ALTER TABLE universities ADD COLUMN updated_at   TIMESTAMP;
ALTER TABLE universities ADD COLUMN short_name   VARCHAR(50);
ALTER TABLE universities ADD COLUMN logo_url     VARCHAR(500);
ALTER TABLE universities ADD COLUMN website_url  VARCHAR(255);
