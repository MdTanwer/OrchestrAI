# 04 — YAML Schema Design

Workflows in OrchestrAI are written in declarative YAML. The schema enforces static validations before execution and structures tasks, inputs, variables, triggers, and error routing.

---

## Flow YAML Structure

```yaml
# Required top-level identifier and folder grouping
id: content-review-workflow
namespace: production.marketing

# Optional human documentation and tag filters
description: "Pipes input query to AI agents, reviews toxicity, and posts output."
labels:
  team: content
  tier: production

# Inputs requested at execution time
inputs:
  - id: articleText
    type: STRING
    required: true
    description: "The text content to moderate and publish."

# Shared variables resolved at runtime
variables:
  defaultModel: gpt-4o
  minConfidence: 0.85

# Automatic event triggers
triggers:
  - id: daily-cleanup
    type: schedule.cron
    cron: "0 0 * * *"

# Ordered sequence of tasks
tasks:
  - id: initial-draft
    type: openai.chat
    prompt: "Proofread and polish this text: {{ inputs.articleText }}"

  - id: parallel-checks
    type: core.parallel
    tasks:
      - id: toxicity
        type: openai.moderation
        input: "{{ outputs.initial-draft.response }}"
      - id: spelling
        type: custom.spellcheck
        text: "{{ outputs.initial-draft.response }}"

  - id: filter-decision
    type: core.if
    condition: "{{ outputs.parallel-checks.toxicity.flagged || outputs.parallel-checks.spelling.errors > 3 }}"
    then:
      - id: notify-reject
        type: http.request
        method: POST
        url: "https://alerts.example.com/reject"
    else:
      - id: post-success
        type: http.request
        method: POST
        url: "https://api.notion.com/v1/pages"
        body: "{{ outputs.initial-draft.response }}"

# Executed if any root task fails and exhausts retries
onFailure:
  - id: alert-slack
    type: http.request
    method: POST
    url: "https://hooks.slack.com/services/..."
    body: "Execution failed!"
```

---

## Field Reference

### Top-Level Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | Unique workflow name (regex: `^[a-z0-9-]+$`). |
| `namespace` | string | Yes | Dot-separated folder namespace (regex: `^[a-z0-9.-]+$`). |
| `description` | string | No | Human description of flow. |
| `labels` | map | No | Tags for filtering execution metrics. |
| `inputs` | list | No | Request parameters for execution. |
| `variables` | map | No | Immutable flow-wide variables. |
| `triggers` | list | No | Event-based trigger triggers. |
| `tasks` | list | Yes | List of steps to execute sequentially by default. |
| `onFailure` | list | No | Sequence executed if a flow run fails. |

### Input Schema
```yaml
inputs:
  - id: <string>
    type: STRING | INT | FLOAT | BOOL | JSON | FILE
    required: <bool>
    defaults: <any>
    description: <string>
```

### Trigger Schema

Flows start via **manual API**, **webhook**, **cron**, or **Kafka**. Each trigger creates a separate `Execution` (`trigger_type`: `MANUAL`, `WEBHOOK`, `CRON`, `EVENT`).

#### Cron (`schedule.cron`)

```yaml
triggers:
  - id: monday-morning
    type: schedule.cron
    cron: "0 8 * * 1"
    timezone: "America/New_York"
```

See [`examples/11-weekly-ops-digest.yaml`](../examples/11-weekly-ops-digest.yaml).

#### Webhook

```yaml
triggers:
  - id: helpdesk-webhook
    type: webhook
    path: /hooks/support-ticket
```

See [`examples/04-support-ticket-router.yaml`](../examples/04-support-ticket-router.yaml), [`examples/10-incident-triage.yaml`](../examples/10-incident-triage.yaml).

#### Kafka event (`kafka`) — **starts flow on message**

OrchestrAI runs a **trigger consumer** (not a worker) that subscribes to your business topic. **One Kafka message → one Execution.** Payload fields map to flow `inputs` by name, or via `inputsMapping` (JSONPath).

```yaml
triggers:
  - id: on-order-created
    type: kafka
    topic: ecommerce.orders.created      # Required: topic to subscribe
    consumerGroup: orchestrai-fulfillment-v1  # Required: consumer group id
    description: <string>                  # Optional
    offsetReset: earliest | latest         # Optional: default latest
    inputsMapping:                         # Optional: rename nested JSON fields
      orderId: "$.order_id"
      customerEmail: "$.customer.email"
```

| Field | Required | Description |
|-------|----------|-------------|
| `type` | Yes | Must be `kafka` |
| `topic` | Yes | External topic (e.g. from storefront, billing, IoT) |
| `consumerGroup` | Yes | Isolated offset cursor per flow/deployment |
| `inputsMapping` | No | JSONPath from message `value` → `inputs.*` |
| `offsetReset` | No | Where to start when no committed offset exists |

**Not the same as** internal topics `executions` / `task-runs` (platform plumbing). Example flow: [`examples/17-order-fulfillment-kafka-trigger.yaml`](../examples/17-order-fulfillment-kafka-trigger.yaml). Sample message: [`kafka-trigger-message.json`](../examples/sample-output/kafka-trigger-message.json).

### Task Schema (Standard Task Parameters)
```yaml
tasks:
  - id: <string>           # Required: unique within immediate block
    type: <plugin-type>    # Required: plugin descriptor (e.g. openai.chat)
    description: <string>  # Optional
    timeout: <duration>    # Optional: e.g. "30s", "10m"
    retry:                 # Optional retry parameters
      maxAttempts: 3
      backoff: exponential
      initialDelay: "1s"
    stream: <bool>         # Optional: AI chat plugins only — emit token_delta SSE events to client (default false)
    if: <expression>       # Optional: Skip task dynamically if condition resolves false
    fallback:              # Optional: Run alternative task if this task fails
      type: <plugin-type>  # Required: plugin type of the fallback task
      # Plugin-specific config and fallback conditions
    tools:                 # Optional: AI chat only — LLM-invoked subtasks (see below)
    maxToolRounds: <int>   # Optional: Max LLM ↔ tool iterations (default 10)
```

### Tool definitions (`tools` on AI chat tasks)

Each entry in `tools` is a **named function** the model can call plus a **plugin payload** the worker executes.

```yaml
tools:
  - name: lookup_account          # Required: function name for the model
    description: "..."            # Required: when to use this tool
    parameters:                   # Required: JSON Schema for tool arguments
      type: object
      required: [accountId]
      properties:
        accountId: { type: string }
    type: http.request            # Required: plugin type for the subtask
    method: GET
    url: "https://crm.example.com/v1/accounts/{{ tool.args.accountId }}"
```

At runtime the engine resolves `{{ tool.args.<field> }}` from the model's `tool_call` arguments, runs the plugin, appends the result to the agent conversation, and may invoke the model again until `maxToolRounds` is reached.

Outputs on the parent task include `response`, `toolCalls[]`, and `toolRounds`. See [Examples — Tool calling](./15-examples.md#14-sales-rep-copilot--tool-calling).

---

## Control Flow Task Schemas

Unlike standard worker plugins, control flow tasks structure the execution layout reactively.

### 1. Parallel Task (`core.parallel`)
Runs all nested tasks concurrently.
```yaml
- id: check-services
  type: core.parallel
  tasks:
    - id: server-a
      type: http.request
      url: "https://a.com/health"
    - id: server-b
      type: http.request
      url: "https://b.com/health"
```
*   **Schema:** Requires `tasks` (a nested list of standard Tasks).
*   **Namespace Resolution:** Nested task outputs are isolated under the parent parallel block to prevent naming collisions:
    `{{ outputs.check-services.server-a.body }}`

### 2. Conditional Task (`core.if`)
Branches execution path based on a boolean condition.
```yaml
- id: routing-gate
  type: core.if
  condition: "{{ outputs.check-services.server-a.statusCode == 200 }}"
  then:
    - id: status-ok
      type: custom.logger
      message: "Server is green"
  else:
    - id: status-error
      type: custom.logger
      message: "Server is down"
```
*   **Schema:** Requires `condition` (expression resolving to boolean) and `then` (list of tasks). Optional: `else` (list of tasks).

### 3. Loop Task (`core.foreach`)
Runs the nested `tasks` list **once per element** in `items`. Use for batch operations (personalized emails per account, API call per row, etc.) instead of duplicating YAML.

```yaml
- id: personalize-outreach
  type: core.foreach
  items: "{{ outputs.fetch-at-risk-accounts.body.accounts }}"
  tasks:
    - id: draft-save-email
      type: openai.chat
      prompt: "Account {{ taskrun.value.companyName }} ({{ taskrun.index + 1 }}/{{ taskrun.total }})"
    - id: log-crm-touch
      type: http.request
      url: "https://crm.example.com/v1/accounts/{{ taskrun.value.accountId }}/activities"
```

| Field | Required | Description |
|-------|----------|-------------|
| `items` | Yes | Expression → JSON **array** (empty array skips the loop) |
| `tasks` | Yes | Steps executed per item, in order within each iteration |

**Loop-scoped expressions** (only inside nested tasks):

| Expression | Meaning |
|------------|---------|
| `{{ taskrun.value }}` | Current list element (object or scalar) |
| `{{ taskrun.value.accountId }}` | Field on object items |
| `{{ taskrun.index }}` | 0-based iteration index |
| `{{ taskrun.total }}` | `items.length` |

**Parent task outputs** after the loop completes:

| Output field | Description |
|--------------|-------------|
| `results` | Array of per-iteration outputs (nested task results) |
| `count` | Number of iterations run |
| `totalCostUsd` / `totalTokens` | Rolled-up AI usage across iterations |

Example flow: [`examples/15-churn-outreach-foreach.yaml`](../examples/15-churn-outreach-foreach.yaml). Sample: [`foreach-results.json`](../examples/sample-output/foreach-results.json).

---

## Expression & Variable Resolution

All dynamic evaluation in OrchestrAI is enclosed in `{{ }}` brackets and computed using JEXL.

### Resolution Hierarchy
*   **Inputs:** `{{ inputs.myParameter }}`
*   **Variables:** `{{ vars.myVariable }}`
*   **Outputs:** `{{ outputs.stepId.response }}`
*   **Nested Outputs (Parallel):** `{{ outputs.parallelParentId.childStepId.response }}`
*   **Loop (foreach):** `{{ taskrun.value }}`, `{{ taskrun.index }}`, `{{ taskrun.total }}` — see [Loop Task](#3-loop-task-coreforeach)
*   **Built-in Functions:**
    *   `{{ now() }}` - ISO time string
    *   `{{ uuid() }}` - Random UUID
    *   `{{ env('SYS_ENV_VAR') }}` - Retrieves environment variable safely from host context

### AI Task Outputs (Cost & Token Metrics)

Every AI plugin (`openai.chat`, `anthropic.chat`, `google.gemini`, `ollama.chat`, etc.) attaches usage metrics to its output. Downstream tasks and expressions can read them; the execution engine sums them into `total_cost_usd` and `total_tokens` on the run.

| Output field | Type | Description |
|--------------|------|-------------|
| `response` | string | Model text (or structured JSON if requested) |
| `model` | string | Model id used for pricing |
| `tokensUsed` | int | Total tokens (prompt + completion) |
| `promptTokens` | int | Input tokens |
| `completionTokens` | int | Output tokens |
| `costUsd` | decimal | Estimated USD for this TaskRun |

**Expression examples:**

```yaml
# Log or branch on a prior step's cost
condition: "{{ outputs.draft.costUsd > 0.10 }}"

# Sum costs across steps (see examples/08-cost-tracking.yaml)
condition: "{{ (outputs.draft.costUsd + outputs.refine.costUsd) > vars.maxBudgetUsd }}"
```

Indirect cost control: set `maxTokens` on the task config. Flow-level `labels` (e.g. `cost-center: marketing`) filter metrics in `GET /metrics/costs`. See [Examples — Cost Tracking](./15-examples.md#8-cost-tracking--tokens-and-usd-per-step-and-per-run).

### AI Task Streaming (`stream: true`)

When `stream: true` on an AI chat task, the worker consumes the provider's streaming completion API and publishes each chunk to the API server's SSE bus. Clients connect with `GET /executions/{id}/stream`; non-streaming tasks in the same flow behave unchanged.

```yaml
- id: stream-answer
  type: openai.chat
  stream: true
  model: gpt-4o
  prompt: "{{ inputs.userMessage }}"

- id: save
  type: http.request
  url: "https://api.example.com/messages"
  body: "{{ outputs.stream-answer.response }}"  # full text available after stream ends
```

See [Examples — Streaming copilot](./15-examples.md#13-streaming-product-copilot--sse-token-stream).

### Secure Secret Resolution Pattern
To prevent credential leaks in Kafka pipelines, **never use secrets as JEXL expressions** (e.g. `apiKey: "{{ secret('X') }}"` is deprecated). Instead, plugins automatically read API keys directly from the worker environment context, or map custom keys securely:

```yaml
# Good: The plugin loads OPENAI_API_KEY securely from context.
- id: ask-gpt
  type: openai.chat
  prompt: "Hello"

# Good: If you need a custom secret key, pass the KEY REFERENCE, not the secret expression.
- id: ask-gpt-alt
  type: openai.chat
  secretKeyRef: "OPENAI_API_KEY_MARKETING"  # Worker resolves this securely at runtime
  prompt: "Hello"
```
