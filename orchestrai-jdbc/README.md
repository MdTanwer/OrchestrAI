# orchestrai-jdbc

PostgreSQL persistence: Flyway migrations (`V1`–`V6`) and Panache entities/repositories.

## Tables

| Migration | Table |
|-----------|--------|
| V1 | `flows` |
| V2 | `executions` |
| V3 | `task_runs` |
| V4 | `logs` |
| V5 | `secrets` |
| V6 | `triggers` |

Schema: [docs/06-data-models.md](../docs/06-data-models.md).

## Local dev (migrations on startup)

```bash
# From repo root
docker compose up -d

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn quarkus:dev -pl orchestrai-jdbc
```

Flyway runs at startup; Hibernate uses `database.generation=none`. Health: http://localhost:8090/health

## Tests (requires Postgres on localhost:5433)

```bash
docker compose up -d
mvn test -pl orchestrai-jdbc
```

`FlywaySchemaTest` asserts all six tables exist and six migrations applied.

## Flow ID mapping

| Layer | DB UUID | YAML/API string id |
|-------|---------|-------------------|
| `FlowEntity` | `id` | `flowId` → `flow_id` |
| `ExecutionEntity` | `flowRefId` → `flow_id` (FK to `flows.id`) | — |
