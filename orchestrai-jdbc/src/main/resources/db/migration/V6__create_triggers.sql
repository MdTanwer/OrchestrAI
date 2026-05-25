CREATE TABLE triggers (
    id              UUID PRIMARY KEY,
    flow_id         UUID NOT NULL REFERENCES flows (id) ON DELETE CASCADE,
    trigger_id      VARCHAR(100) NOT NULL,
    type            VARCHAR(50) NOT NULL,
    config          JSONB NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    last_triggered  TIMESTAMP,
    next_trigger    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_triggers_flow ON triggers (flow_id);
