CREATE TABLE flows (
    id              UUID PRIMARY KEY,
    flow_id         VARCHAR(100) NOT NULL,
    namespace       VARCHAR(100) NOT NULL,
    version         INT NOT NULL DEFAULT 1,
    yaml_source     TEXT NOT NULL,
    parsed_json     JSONB NOT NULL,
    description     TEXT,
    labels          JSONB,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    created_by      UUID,
    UNIQUE (namespace, flow_id, version)
);

CREATE INDEX idx_flows_lookup ON flows (namespace, flow_id);
