# 04 — YAML Schema Design

## Flow YAML Structure

```yaml
# Required fields
id: my-flow-id
namespace: my-namespace

# Optional metadata
description: "What this flow does"
labels:
  team: ai
  env: prod

# Optional inputs (parameters)
inputs:
  - id: userQuery
    type: STRING
    required: true
    defaults: "Hello"

# Optional variables
variables:
  model: "gpt-4"
  temperature: 0.7

# Triggers (how flow starts)
triggers:
  - id: daily
    type: schedule.cron
    cron: "0 9 * * *"

# Tasks (the actual work)
tasks:
  - id: task1
    type: openai.chat
    model: "{{ vars.model }}"
    prompt: "{{ inputs.userQuery }}"

  - id: task2
    type: anthropic.chat
    prompt: "Summarize: {{ outputs.task1.response }}"

# Error handling
onFailure:
  - id: notify
    type: http.request
    url: "https://alerts.example.com"
```

---

## Field Reference

### Top-Level Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | string | Yes | Unique flow identifier |
| namespace | string | Yes | Logical grouping |
| description | string | No | Human-readable description |
| labels | map | No | Key-value tags |
| inputs | list | No | Input parameters |
| variables | map | No | Reusable values |
| triggers | list | No | Auto-start conditions |
| tasks | list | Yes | Execution steps |
| onFailure | list | No | Failure handlers |

### Input Schema

```yaml
inputs:
  - id: <string>
    type: STRING | INT | FLOAT | BOOL | JSON | FILE
    required: <bool>
    defaults: <any>
    description: <string>
```

### Task Schema

```yaml
tasks:
  - id: <string>           # Required: unique within flow
    type: <plugin-type>    # Required: plugin identifier
    description: <string>  # Optional
    timeout: <duration>    # Optional: e.g., "30s", "5m"
    retry:                 # Optional retry config
      maxAttempts: 3
      backoff: exponential
      initialDelay: "1s"
    if: <expression>       # Optional: conditional execution
    # Plugin-specific config follows
```

---

## Expression Syntax

Use `{{ }}` for dynamic values:

```yaml
# Input reference
"{{ inputs.userQuery }}"

# Variable reference
"{{ vars.model }}"

# Output from previous task
"{{ outputs.task1.response }}"

# Built-in functions
"{{ now() }}"
"{{ uuid() }}"
"{{ env('API_KEY') }}"

# Conditionals
"{{ outputs.task1.score > 0.8 ? 'high' : 'low' }}"
```

---

## Complete Examples

### Example 1: Simple AI Chain

```yaml
id: research-assistant
namespace: ai.research

inputs:
  - id: topic
    type: STRING
    required: true

tasks:
  - id: research
    type: openai.chat
    model: gpt-4
    prompt: "Research the topic: {{ inputs.topic }}"

  - id: summarize
    type: anthropic.chat
    model: claude-3-opus
    prompt: "Summarize in 3 bullets: {{ outputs.research.response }}"

  - id: save
    type: http.request
    method: POST
    url: "https://api.notion.com/notes"
    body: "{{ outputs.summarize.response }}"
```

### Example 2: Parallel + Conditional

```yaml
id: content-moderator
namespace: ai.moderation

inputs:
  - id: text
    type: STRING

tasks:
  - id: parallel-check
    type: core.parallel
    tasks:
      - id: toxicity
        type: openai.moderation
        input: "{{ inputs.text }}"
      - id: spam
        type: custom.spamDetector
        input: "{{ inputs.text }}"

  - id: decision
    type: core.if
    condition: "{{ outputs.toxicity.flagged || outputs.spam.flagged }}"
    then:
      - id: reject
        type: http.request
        url: "/api/reject"
    else:
      - id: approve
        type: http.request
        url: "/api/approve"
```

---

## Validation Rules

- `id` must be unique within a flow
- `id` must match regex: `^[a-z0-9-]+$`
- Task references in expressions must exist
- Circular dependencies are forbidden
- Plugin type must be registered
- Required fields must be provided
