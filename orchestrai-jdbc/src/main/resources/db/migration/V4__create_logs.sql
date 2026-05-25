CREATE TABLE logs (
    id              BIGSERIAL PRIMARY KEY,
    execution_id    UUID NOT NULL REFERENCES executions (id) ON DELETE CASCADE,
    task_run_id     UUID REFERENCES task_runs (id) ON DELETE CASCADE,
    level           VARCHAR(10) NOT NULL,
    message         TEXT NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_logs_exec ON logs (execution_id);
CREATE INDEX idx_logs_taskrun ON logs (task_run_id);
