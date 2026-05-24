# 03 — Features

> **See it in practice:** [Examples](./15-examples.md) and [`examples/`](../examples/) show each capability as a runnable YAML workflow.

## Feature Categories

1. Flow Management
2. Execution Engine
3. AI Capabilities
4. Observability
5. Integration

---

## 1. Flow Management

### F1.1 — YAML Flow Definition

Define workflows in human-readable YAML.

### F1.2 — Flow Versioning

Every flow update creates a new version (immutable).

### F1.3 — Namespaces

Organize flows into hierarchical namespaces.

### F1.4 — Flow Validation

Static validation before execution (schema, types, references).

### F1.5 — Flow Import/Export

Share flows as `.yaml` files.

---

## 2. Execution Engine

### F2.1 — Sequential Execution

Tasks run one after another by default.

### F2.2 — Parallel Execution

Run multiple tasks concurrently using `parallel` blocks.

### F2.3 — Conditional Execution

`if/else` logic based on previous outputs.

### F2.4 — Loops

Iterate over a list with `core.foreach` (`items` + nested `tasks`, `taskrun.value` per row).

See [`examples/15-churn-outreach-foreach.yaml`](../examples/15-churn-outreach-foreach.yaml) and [Examples — Loops](./15-examples.md#15-churn-save-outreach--coreforeach).

### F2.5 — Retry Policies

Configurable retries with backoff strategies.

### F2.6 — Timeouts

Per-task and per-flow timeout configuration.

### F2.7 — Error Handling

`onFailure` blocks to handle errors gracefully.

### F2.8 — Distributed Execution

Workers pull tasks from Kafka — scale horizontally. Same flow YAML with 1 or N workers; `core.parallel` dispatches multiple `task-runs` consumed by the pool.

See [`examples/16-distributed-document-review.yaml`](../examples/16-distributed-document-review.yaml), [`examples/DISTRIBUTED.md`](../examples/DISTRIBUTED.md), and [Architecture](./05-architecture.md).

---

## 3. AI Capabilities

### F3.1 — Multi-LLM Support

Plugins for OpenAI, Anthropic, Google, and local models.

### F3.2 — Model Fallback

If GPT-4 fails, automatically try Claude.

### F3.3 — Token Tracking

Count input and output tokens per task.

### F3.4 — Cost Calculation

Real-time cost in USD per execution.

See [`examples/08-cost-tracking.yaml`](../examples/08-cost-tracking.yaml) and [Examples — Cost Tracking](./15-examples.md#8-cost-tracking--tokens-and-usd-per-step-and-per-run).

### F3.5 — Prompt Templates

Reusable prompt templates with variables.

### F3.6 — Context Passing

Share conversation context between agents.

### F3.7 — Tool Calling

Let LLMs invoke other tasks as tools (nested plugin configs + JSON Schema parameters).

See [`examples/14-sales-agent-with-tools.yaml`](../examples/14-sales-agent-with-tools.yaml) and [Examples — Tool calling](./15-examples.md#14-sales-rep-copilot--tool-calling).

### F3.8 — Human-in-the-Loop

Pause execution and wait for human approval.

### F3.9 — Streaming Completions

Stream LLM tokens to the client in real time via SSE (`stream: true` on AI tasks, `GET /executions/{id}/stream`).

See [`examples/13-streaming-copilot.yaml`](../examples/13-streaming-copilot.yaml) and [Examples — Streaming](./15-examples.md#13-streaming-product-copilot--sse-token-stream).

---

## 4. Observability

### F4.1 — Real-time Logs

Stream task logs via Server-Sent Events (SSE).

### F4.2 — Execution History

Searchable log of all past executions.

### F4.3 — Metrics Dashboard

Prometheus metrics: executions/sec, success rate, latency.

### F4.4 — Cost Dashboard

Daily and monthly cost breakdown per flow.

### F4.5 — Audit Trail

Track who triggered what and when.

### F4.6 — Task Input/Output Visibility

Per TaskRun: inputs, outputs, duration, state, errors, token usage, and cost — visible in the dashboard and via the REST API / SSE stream for debugging and compliance.

---

## 5. Integration

### F5.1 — REST API

Full programmatic control via REST.

### F5.2 — Webhook Triggers

Start flows from external HTTP calls.

### F5.3 — Cron Scheduler

Schedule flows on cron expressions.

### F5.4 — Kafka Triggers

Start flows on Kafka events (`type: kafka` on external topics — one message → one execution).

See [`examples/17-order-fulfillment-kafka-trigger.yaml`](../examples/17-order-fulfillment-kafka-trigger.yaml) and [Examples — Kafka trigger](./15-examples.md#17-order-fulfillment--kafka-event-trigger).

### F5.5 — CLI Tool

Manage flows from the command line.

---

## MVP Feature List (12 Weeks)

| Feature | Week |
|---------|------|
| YAML parser | 1–2 |
| Flow validation | 2 |
| Execution engine (sequential) | 3–4 |
| Kafka integration | 5–6 |
| OpenAI plugin | 7 |
| Claude plugin | 7 |
| HTTP plugin | 8 |
| REST API | 9 |
| PostgreSQL persistence | 9–10 |
| Web dashboard | 11 |
| Docker deployment | 12 |
