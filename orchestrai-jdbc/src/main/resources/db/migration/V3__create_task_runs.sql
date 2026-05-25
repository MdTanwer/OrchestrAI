CREATE TABLE task_runs (
    id              UUID PRIMARY KEY,
    execution_id    UUID NOT NULL REFERENCES executions (id) ON DELETE CASCADE,
    task_id         VARCHAR(100) NOT NULL,
    task_type       VARCHAR(100) NOT NULL,
    state           VARCHAR(20) NOT NULL,
    attempt         INT NOT NULL DEFAULT 1,
    inputs          JSONB,
    outputs         JSONB,
    started_at      TIMESTAMP,
    ended_at        TIMESTAMP,
    duration_ms     BIGINT,
    tokens_used     INT,
    cost_usd        DECIMAL(10, 6),
    error_message   TEXT,
    worker_id       VARCHAR(100),
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_taskruns_exec ON task_runs (execution_id);
