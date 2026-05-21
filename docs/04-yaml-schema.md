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
    if: <expression>       # Optional: Skip task dynamically if condition resolves false
```

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
Iterates tasks over a list.
```yaml
- id: process-users
  type: core.foreach
  items: "{{ outputs.get-users.list }}"
  tasks:
    - id: notify-user
      type: http.request
      url: "https://api.com/user/{{ taskrun.value.id }}/notify"
```
*   **Schema:** Requires `items` (expression resolving to a JSON array) and `tasks` (list of tasks executing for each item).

---

## Expression & Variable Resolution

All dynamic evaluation in OrchestrAI is enclosed in `{{ }}` brackets and computed using JEXL.

### Resolution Hierarchy
*   **Inputs:** `{{ inputs.myParameter }}`
*   **Variables:** `{{ vars.myVariable }}`
*   **Outputs:** `{{ outputs.stepId.response }}`
*   **Nested Outputs (Parallel):** `{{ outputs.parallelParentId.childStepId.response }}`
*   **Loop Variable:** `{{ taskrun.value }}` (refers to the current element in a loop iteration)
*   **Built-in Functions:**
    *   `{{ now() }}` - ISO time string
    *   `{{ uuid() }}` - Random UUID
    *   `{{ env('SYS_ENV_VAR') }}` - Retrieves environment variable safely from host context

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
