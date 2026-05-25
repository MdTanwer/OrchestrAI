CREATE TABLE executions (
    id              UUID PRIMARY KEY,
    flow_id         UUID NOT NULL REFERENCES flows (id) ON DELETE CASCADE,
    namespace       VARCHAR(100) NOT NULL,
    state           VARCHAR(20) NOT NULL,
    inputs          JSONB,
    outputs         JSONB,
    trigger_type    VARCHAR(20),
    trigger_data    JSONB,
    started_at      TIMESTAMP,
    ended_at        TIMESTAMP,
    duration_ms     BIGINT,
    total_cost_usd  DECIMAL(10, 6),
    total_tokens    BIGINT,
    error_message   TEXT,
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_executions_state ON executions (state);
CREATE INDEX idx_executions_flow ON executions (flow_id);
