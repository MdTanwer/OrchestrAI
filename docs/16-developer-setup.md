# 16 — Developer Setup

Local build requirements, JDK 17 setup, and common Maven commands for the OrchestrAI monorepo.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|--------|
| **JDK** | **17** (required) | Project `java.version` is 17. Do not use JDK 21/25 for Java modules. |
| **Maven** | 3.8+ | Builds all backend modules. |
| **Node.js** | 20+ | For `orchestrai-ui` only. |
| **pnpm** | 9+ | UI package manager. |
| **Docker** | 24+ | Postgres, Kafka, local stack (`docker compose`). |

---

## JDK 17 (required)

`orchestrai-core` uses **Lombok**. Lombok runs at compile time and is validated against **JDK 17** via the Maven Enforcer plugin. Building on JDK 25 (or other versions) will fail with a compiler or enforcer error.

### Check your Java version

```bash
java -version
```

You should see `17.x`. If you see `21` or `25`, switch before running Maven.

### Option 1 — SDKMAN (recommended)

The repo includes `.sdkmanrc` and `.java-version` (both pin **17**):

```bash
cd orchestrai

# Install & use Java 17 for this directory
sdk env install
# or manually:
sdk use java 17.0.17-tem

java -version
```

### Option 2 — System OpenJDK 17 (Linux)

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

java -version
```

Add those `export` lines to your shell profile if you want them permanent.

### Option 3 — macOS (Homebrew)

```bash
brew install openjdk@17
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

java -version
```

---

## Backend — Maven commands

Run from the **repository root** (`orchestrai/`), with **JDK 17** active.

### Build everything (no tests)

```bash
mvn clean compile -DskipTests
```

### Run all tests

```bash
mvn clean test
```

### Work on one module

```bash
# Domain models + Jackson tests (orchestrai-core)
mvn test -pl orchestrai-core

# YAML parser only
mvn test -pl orchestrai-yaml-parser

# JDBC + Flyway (requires Postgres on localhost:5433)
mvn test -pl orchestrai-jdbc -am

# YAML parser (uses ../examples/*.yaml)
mvn test -pl orchestrai-yaml-parser -am

# Compile a single module
mvn compile -pl orchestrai-api-server -am
```

`-am` also builds required dependencies (e.g. `orchestrai-core`, `orchestrai-jdbc`).

### Run a Quarkus service in dev mode

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64   # if needed
export PATH="$JAVA_HOME/bin:$PATH"

# API server (REST + SSE)
mvn quarkus:dev -pl orchestrai-api-server

# Kafka execution brain
mvn quarkus:dev -pl orchestrai-executor

# Task worker
mvn quarkus:dev -pl orchestrai-worker

# Cron / Kafka triggers
mvn quarkus:dev -pl orchestrai-scheduler
```

### Package a runnable JAR

```bash
mvn package -pl orchestrai-api-server -DskipTests
# JAR: orchestrai-api-server/target/quarkus-app/
```

### CLI (optional native profile)

```bash
mvn package -pl orchestrai-cli -DskipTests
mvn package -pl orchestrai-cli -DskipTests -Dnative
```

---

## Frontend — `orchestrai-ui`

The UI is **not** part of the Maven reactor. Use **pnpm** in `orchestrai-ui/`:

```bash
cd orchestrai-ui

pnpm install
pnpm dev          # http://localhost:3000
pnpm build        # production build
pnpm lint
```

Point the UI at the API (when running locally):

```bash
export NEXT_PUBLIC_API_URL=http://localhost:8080/v1
```

---

## Infrastructure — Docker Compose

Start Postgres and Kafka for integration work (once `docker-compose.yml` is filled in):

```bash
cd orchestrai
docker compose up -d
docker compose ps
docker compose logs -f
```

Stop:

```bash
docker compose down
```

---

## Module map (Maven)

| Module | Type | Run locally |
|--------|------|-------------|
| `orchestrai-core` | Library | `mvn test -pl orchestrai-core` |
| `orchestrai-messaging` | Library | (via dependents) |
| `orchestrai-yaml-parser` | Library | `mvn test -pl orchestrai-yaml-parser` |
| `orchestrai-jdbc` | Library + Flyway | needs Postgres |
| `orchestrai-engine` | Library | (via executor / api-server) |
| `orchestrai-plugins/*` | Libraries | (via worker) |
| `orchestrai-api-server` | **Deployable** | `mvn quarkus:dev -pl orchestrai-api-server` |
| `orchestrai-executor` | **Deployable** | `mvn quarkus:dev -pl orchestrai-executor` |
| `orchestrai-worker` | **Deployable** | `mvn quarkus:dev -pl orchestrai-worker` |
| `orchestrai-scheduler` | **Deployable** | `mvn quarkus:dev -pl orchestrai-scheduler` |
| `orchestrai-cli` | **Deployable** | `mvn package -pl orchestrai-cli` |
| `orchestrai-ui` | Next.js | `pnpm dev` |

---

## IDE setup

### Lombok

Install the **Lombok** plugin for your IDE so annotations resolve in the editor:

- IntelliJ IDEA: bundled / “Lombok” plugin — enable annotation processing
- VS Code / Cursor: “Lombok Annotations Support” extension
- Eclipse: lombok.jar as java agent (see [projectlombok.org](https://projectlombok.org/setup/overview))

### Import as Maven project

Open the **root** `pom.xml` as a Maven multi-module project. JDK **17** must be the project SDK for Java modules.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `requireJavaVersion` / JDK 17 enforcer failure | Switch to JDK 17 (`sdk use java 17.0.17-tem` or `JAVA_HOME` above). |
| Lombok / `TypeTag :: UNKNOWN` on compile | You are on JDK 21/25 — use JDK 17. |
| `Conflicting getter definitions for property "id"` on `Flow` | DB id is JSON field `uuid`; YAML/API flow id is `id` — see [Data Models](./06-data-models.md). |
| Quarkus dev fails: no datasource | Start Postgres or add `application.properties` (see [Deployment](./11-deployment.md)). |

---

## Related docs

- [Roadmap](./13-roadmap.md) — Week 1–2 foundation tasks
- [Deployment](./11-deployment.md) — Docker, K8s, env vars
- [Data Models](./06-data-models.md) — `Flow`, `Execution`, enums
- [`orchestrai-core/README.md`](../orchestrai-core/README.md) — module-specific notes
