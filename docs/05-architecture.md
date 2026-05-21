# 05 — System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      USERS                              │
│  (Web UI, CLI, API Clients, Webhooks)                   │
└──────────────────┬──────────────────────────────────────┘
                   │ HTTPS
                   ▼
┌─────────────────────────────────────────────────────────┐
│                  API SERVER                             │
│  (Quarkus REST + WebSocket/SSE)                         │
│  - Flow CRUD                                            │
│  - Execution control                                    │
│  - Real-time updates                                    │
└──────┬────────────────────────────────┬─────────────────┘
       │                                │
       │ writes                         │ publishes
       ▼                                ▼
┌──────────────────┐         ┌──────────────────────┐
│   POSTGRESQL     │         │      KAFKA           │
│  - Flows         │         │  - task-queue        │
│  - Executions    │         │  - task-results      │
│  - TaskRuns      │         │  - execution-events  │
│  - Logs          │         └────────┬─────────────┘
└──────────────────┘                  │
                                      │ consumes
                                      ▼
                          ┌─────────────────────────┐
                          │   WORKER NODES          │
                          │  (Pull tasks, execute)  │
                          │   ┌─────────────────┐   │
                          │   │ Plugin Registry │   │
                          │   │ - OpenAI        │   │
                          │   │ - Claude        │   │
                          │   │ - HTTP, etc.    │   │
                          │   └─────────────────┘   │
                          └─────────────────────────┘
```

---

## Core Components

### 1. API Server

**Tech:** Quarkus + REST + SSE

**Responsibilities:**

- Accept HTTP requests
- Validate flows
- Create executions
- Publish tasks to Kafka
- Stream logs via SSE

### 2. Scheduler

**Tech:** Quartz / custom scheduler

**Responsibilities:**

- Evaluate cron triggers
- Watch for webhook triggers
- Create executions on schedule

### 3. Execution Engine

**Tech:** Java + Kafka

**Responsibilities:**

- Manage execution state
- Resolve task dependencies
- Dispatch tasks to workers
- Aggregate results

### 4. Worker Nodes

**Tech:** Java + Plugin SDK

**Responsibilities:**

- Consume tasks from Kafka
- Load appropriate plugin
- Execute task
- Publish results

### 5. Plugin System

**Tech:** Java Reflection + ServiceLoader

**Responsibilities:**

- Discover plugins at startup
- Provide isolated execution context
- Handle inputs and outputs

### 6. Storage Layer

**Tech:** PostgreSQL + Hibernate

**Responsibilities:**

- Persist flows, executions, logs
- Provide history and search

### 7. Web Dashboard

**Tech:** Next.js + React

**Responsibilities:**

- Flow editor (YAML)
- Execution viewer
- Real-time logs
- Metrics dashboard

---

## Data Flow Example

**User runs a flow:**

1. User → `POST /api/flows/{id}/execute`
2. API Server → Validates flow, creates Execution in DB (state: CREATED)
3. API Server → Publishes first task to Kafka topic `task-queue`
4. API Server → Returns `executionId` to user
5. Worker → Consumes task from Kafka
6. Worker → Loads plugin (e.g., `openai.chat`)
7. Worker → Executes task, gets result
8. Worker → Publishes result to `task-results`
9. Execution Engine → Updates DB, publishes next task
10. Repeats until all tasks done
11. Execution Engine → Marks execution SUCCESS
12. SSE → Pushes update to UI

---

## Kafka Topics

| Topic | Purpose | Producer | Consumer |
|-------|---------|----------|----------|
| `task-queue` | Pending tasks | API / Engine | Workers |
| `task-results` | Completed tasks | Workers | Engine |
| `execution-events` | State changes | Engine | API (for SSE) |
| `dead-letter` | Failed tasks | Workers | Manual review |

---

## Design Principles

1. **Stateless Workers** — Any worker can execute any task
2. **Idempotent Tasks** — Safe to retry
3. **Event-Driven** — Kafka decouples components
4. **Plugin-First** — All capabilities are plugins
5. **Observable** — Every action emits events
6. **Horizontal Scalability** — Add workers to scale
