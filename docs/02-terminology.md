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

### Trigger

An event that starts an Execution. Types:

| Type | Description |
|------|-------------|
| Manual | User clicks "Run" |
| Cron | Scheduled time |
| Webhook | HTTP request |
| Event | Kafka message |

### Plugin

A reusable, pluggable component that defines a task type. Examples:

- `openai.chat` — Call OpenAI
- `http.request` — Make HTTP call
- `shell.exec` — Run shell command

### Worker

A process that executes TaskRuns. Workers pull tasks from Kafka queues and execute them. You can run multiple workers for scalability.

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

### Tool Calling

An LLM's ability to invoke functions or APIs as part of its response.

### Chain

A sequence of agent tasks where each agent's output feeds the next.

### Human-in-the-Loop (HITL)

A workflow step that pauses for human approval before continuing.
