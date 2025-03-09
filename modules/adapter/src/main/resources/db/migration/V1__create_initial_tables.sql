-- notification_template
CREATE TYPE notification_channel AS ENUM ('EMAIL');
CREATE TYPE notification_type AS ENUM ('WELCOME', 'PASSWORD_RESET');
CREATE TYPE notification_language AS ENUM ('EN');

CREATE TABLE IF NOT EXISTS notification_template (
  id SERIAL PRIMARY KEY,
  modified_by VARCHAR,
  modified_at TIMESTAMP NOT NULL,
  created_by VARCHAR,
  created_at TIMESTAMP NOT NULL,
  channel notification_channel NOT NULL,
  type notification_type NOT NULL,
  "language" notification_language NOT NULL,
  subject VARCHAR NOT NULL,
  content VARCHAR NOT NULL,
  UNIQUE(channel, type, "language")
);

CREATE INDEX IF NOT EXISTS idx_notification_template_channel_type_language ON notification_template(channel, type, "language");

-- service_task
CREATE TYPE service_task_type AS ENUM ('SEND_EMAIL');
CREATE TYPE service_task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'ERROR', 'COMPLETED', 'FAILED', 'PUBLISHED');

CREATE TABLE IF NOT EXISTS service_task (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  modified_by VARCHAR,
  modified_at TIMESTAMP NOT NULL,
  created_by VARCHAR,
  created_at TIMESTAMP NOT NULL,
  type service_task_type NOT NULL,
  status service_task_status NOT NULL,
  external_id UUID NOT NULL UNIQUE,
  priority SMALLINT NOT NULL,
  execution_count SMALLINT NOT NULL,
  execution_started_at TIMESTAMP,
  execution_scheduled_at TIMESTAMP,
  context JSONB,
  message VARCHAR
);

CREATE INDEX IF NOT EXISTS idx_service_task_external_id ON service_task(external_id);