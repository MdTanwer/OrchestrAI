CREATE TABLE secrets (
    id              UUID PRIMARY KEY,
    namespace       VARCHAR(100) NOT NULL,
    key             VARCHAR(100) NOT NULL,
    encrypted_value TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    UNIQUE (namespace, key)
);
