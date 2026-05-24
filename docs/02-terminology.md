# 02 — Terminology & Core Concepts

This document defines every term used throughout OrchestrAI. Understanding this vocabulary is essential.

---

## Core Terms

### Flow

A declarative definition of a workflow, written in YAML. A Flow is a template — it defines what should happen but doesn't run until executed.

```yaml
id: my-flow
tasks:
  - id: step1
    type: openai.chat
```

### Task

A single step within a Flow. Each task has a type (which plugin to use) and configuration. Tasks run sequentially by default.

### Execution

A runtime instance of a Flow. When you "run" a Flow, you create an Execution. Each Execution has a unique ID, state, and history.

| Concept | Analogy |
|---------|---------|
| Flow (template) | Recipe |
| Execution (instance) | Cooked meal |

### TaskRun

A runtime instance of a single Task within an Execution. If a Flow has 5 tasks and you execute it, you get 5 TaskRuns.

### Loop (`core.foreach`)

A control-flow task that runs nested tasks for **each element** in a list (`items`). Inside the loop, `taskrun.value` is the current item and `taskrun.index` is the position. After completion, `outputs.<foreachTaskId>.results` holds per-iteration outputs. See [`examples/15-churn-outreach-foreach.yaml`](../examples/15-churn-outreach-foreach.yaml).

### Trigger

An event that starts an Execution. Types:

| Type | Description |
|------|-------------|
| Manual | User clicks "Run" |
| Cron | Scheduled time |
| Webhook | HTTP request |
| Event | Kafka message on **your** topic (`triggers[].type: kafka`) — see [`examples/17-order-fulfillment-kafka-trigger.yaml`](../examples/17-order-fulfillment-kafka-trigger.yaml) |

### Plugin

A reusable, pluggable component that defines a task type. Examples:

- `openai.chat` — Call OpenAI
- `http.request` — Make HTTP call
- `shell.exec` — Run shell command

### Worker

A process that executes TaskRuns. Workers pull tasks from Kafka queues and execute them. You can run multiple workers for scalability. Workers are stateless—they do not load the full flow graph. See [`examples/DISTRIBUTED.md`](../examples/DISTRIBUTED.md) and [`16-distributed-document-review.yaml`](../examples/16-distributed-document-review.yaml).

### Namespace

A logical grouping of Flows (like a folder). Used for organization and access control.

```
namespace: marketing
  ├── flow: email-campaign
  └── flow: lead-scoring
```

### Input

Parameters passed into a Flow at execution time.

```yaml
inputs:
  - id: userQuery
    type: STRING
    required: true
```

### Output

Data produced by a Task, usable by subsequent tasks.

```yaml
- id: step1
  type: openai.chat
  # produces: outputs.step1.response

- id: step2
  prompt: "{{ outputs.step1.response }}"
```

### Variable

A reusable value within a Flow.

```yaml
variables:
  model: "gpt-4"
tasks:
  - id: chat
    model: "{{ vars.model }}"
```

### State (Execution States)

| State | Description |
|-------|-------------|
| CREATED | Execution created, not started |
| RUNNING | Currently executing |
| SUCCESS | All tasks completed |
| FAILED | A task failed |
| CANCELLED | Manually stopped |
| PAUSED | Waiting for human input |

### Expression

Dynamic value resolved at runtime, using `{{ }}` syntax.

```yaml
prompt: "Hello {{ inputs.name }}, today is {{ now() }}"
```

---

## AI-Specific Terms

### Agent

An AI-powered Task that uses an LLM to make decisions or generate output.

### Prompt

The input text sent to an LLM.

### Token

A unit of text (~4 characters) used by LLMs. Tokens drive cost.

### Cost (TaskRun & Execution)

Each AI TaskRun records `tokensUsed` and `costUsd` (estimated from the provider's model pricing). The platform rolls these up to `totalCostUsd` and `totalTokens` on the Execution. Visible in the dashboard, REST API, and SSE streams—not configured manually in YAML. See [Examples — Cost Tracking](./15-examples.md#8-cost-tracking--tokens-and-usd-per-step-and-per-run).

### Context Window

The maximum tokens an LLM can process at once.

### Streaming (token SSE)

When a task sets `stream: true`, the worker forwards each completion chunk to the API, which emits `token_delta` events on `GET /executions/{id}/stream`. The UI renders partial text in real time; the full `response` is still stored on the TaskRun for downstream tasks. Distinct from **log streaming** (`/logs/stream`), which is for ops messages only.

### Tool Calling

An LLM task (`openai.chat`, etc.) declares **tools**—each tool maps to a plugin subtask (`http.request`, `postgres.query`, …). The model chooses which tools to run and fills `tool.args`; the engine executes subtasks and loops until the model returns a final answer or `maxToolRounds` is hit. See [`examples/14-sales-agent-with-tools.yaml`](../examples/14-sales-agent-with-tools.yaml).

### Chain

A sequence of agent tasks where each agent's output feeds the next.

### Human-in-the-Loop (HITL)

A workflow step that pauses for human approval before continuing.
