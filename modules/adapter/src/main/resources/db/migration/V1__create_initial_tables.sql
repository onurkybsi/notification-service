-- notification_template
-- R2dbcEntityTemplate cannot generate write queries for enum types!
--CREATE TYPE notification_channel AS ENUM ('EMAIL');
--CREATE TYPE notification_type AS ENUM ('WELCOME', 'PASSWORD_RESET');
--CREATE TYPE notification_language AS ENUM ('EN');

CREATE TABLE IF NOT EXISTS notification_template (
  id SERIAL PRIMARY KEY,
  modified_by VARCHAR,
  modified_at TIMESTAMP NOT NULL,
  created_by VARCHAR,
  created_at TIMESTAMP NOT NULL,
  channel VARCHAR NOT NULL,
  type VARCHAR NOT NULL,
  "language" VARCHAR NOT NULL,
  subject VARCHAR NOT NULL,
  content VARCHAR NOT NULL,
  UNIQUE(channel, type, language)
);

CREATE INDEX IF NOT EXISTS idx_notification_template_channel_type_language ON notification_template(channel, type, language);
