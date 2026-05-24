# 10 — REST API Design

## Base URL

```
https://api.orchestrai.io/v1
```

## Authentication

All requests require:

```
Authorization: Bearer <jwt-token>
```

---

## Endpoints

### Flows

#### `POST /flows`

Create a new flow.

**Body:**

```yaml
id: my-flow
namespace: default
tasks: [...]
```

**Response:** `201 Created`

```json
{ "id": "uuid", "version": 1 }
```

#### `GET /flows`

List all flows (returns latest version by default).

**Query params:** `namespace`, `page`, `size`

#### `GET /flows/{namespace}/{id}`

Get the latest version of a specific flow.

#### `GET /flows/{namespace}/{id}/versions`

List all immutable historical versions of a specific flow.

**Response:** `200 OK`
```json
[
  { "version": 2, "createdAt": "2024-01-15T10:00:00Z" },
  { "version": 1, "createdAt": "2024-01-14T09:00:00Z" }
]
```

#### `GET /flows/{namespace}/{id}/versions/{version}`

Retrieve a specific historical version of a flow.

#### `PUT /flows/{namespace}/{id}`

Update a flow (creates new version).

#### `DELETE /flows/{namespace}/{id}`

Delete a flow (cascade deletes all historical versions and associated executions).

---

### Triggers (Kafka, webhook, cron)

When a flow with `triggers:` is **created or updated**, the platform registers listeners:

| Trigger type | Registration behavior |
|--------------|----------------------|
| `kafka` | Join `consumerGroup` on `topic`; each message → `POST` internal execute |
| `webhook` | Expose `path` on API server |
| `schedule.cron` | Scheduler publishes to `executions` at fire time |

#### Kafka trigger — message → execution

**Flow YAML** ([`examples/17-order-fulfillment-kafka-trigger.yaml`](../examples/17-order-fulfillment-kafka-trigger.yaml)):

```yaml
triggers:
  - id: on-order-created
    type: kafka
    topic: ecommerce.orders.created
    consumerGroup: orchestrai-fulfillment-v1
```

**Inbound message** (from your producer): see [`kafka-trigger-message.json`](../examples/sample-output/kafka-trigger-message.json).

**Platform steps:**

1. Consumer reads record from `ecommerce.orders.created`
2. Maps `value` JSON to flow `inputs` (by key name or `inputsMapping`)
3. Publishes to internal `executions` topic → Executor runs tasks → workers consume `task-runs`
4. Commits offset only after execution is accepted (at-least-once; idempotent inputs recommended)

**Manual replay / test** (without producing to Kafka):

```http
POST /v1/flows/production.fulfillment/order-fulfillment-agent/execute
Content-Type: application/json

{
  "inputs": {
    "orderId": "ord_test",
    "customerEmail": "test@example.com",
    "lineItems": [{ "sku": "SKU-1", "qty": 1, "name": "Test" }],
    "shipToCountry": "US"
  },
  "labels": { "trigger": "manual-test" }
}
```

#### `GET /flows/{namespace}/{id}/triggers`

List registered triggers and consumer lag (ops dashboard).

---

### Executions

#### `POST /flows/{namespace}/{id}/execute`

Start a new execution.

**Query params:** `version` (optional, integer. Defaults to latest version if omitted)

**Body:**

```json
{
  "inputs": { "userQuery": "Hello" },
  "labels": { "trigger": "test" }
}
```

**Response:** `202 Accepted`

```json
{ "executionId": "uuid", "state": "CREATED" }
```

#### `GET /executions/{id}`

Get execution details.

**Response:**

```json
{
  "id": "uuid",
  "state": "RUNNING",
  "startedAt": "2024-01-15T10:00:00Z",
  "taskRuns": []
}
```

#### `GET /executions/{id}/logs`

Get logs (paginated).

#### `GET /executions/{id}/logs/stream` (SSE)

Stream structured logs and execution state (no LLM token deltas). Use for ops/debug dashboards.

```
event: log
data: {"level":"INFO","message":"Task started"}

event: state
data: {"state":"RUNNING"}
```

#### `GET /executions/{id}/stream` (SSE) — token streaming

Stream **LLM token deltas** to the client for tasks defined with `stream: true` in the flow YAML. Multiplexes lifecycle events so a single `EventSource` can drive a chat UI.

```
event: token_delta
data: {"taskId":"stream-answer","index":0,"delta":"Our"}

event: token_done
data: {"taskId":"stream-answer","response":"...full text...","model":"gpt-4o","tokensUsed":412,"costUsd":0.00384}

event: tool_call_started
data: {"taskId":"sales-agent","round":1,"name":"lookup_account","args":{"accountId":"acc_1042"}}

event: tool_call_completed
data: {"taskId":"sales-agent","round":1,"name":"lookup_account","taskRunId":"uuid","durationMs":142}

event: execution_completed
data: {"state":"SUCCESS","totalCostUsd":0.00384}
```

Tool events are emitted for tasks with a `tools` list. Sample sequence: [`examples/sample-output/tool-calling-sse.txt`](../examples/sample-output/tool-calling-sse.txt).

**Typical client flow:**

1. `POST /flows/{namespace}/{id}/execute` → `{ "executionId": "uuid" }`
2. Immediately open `GET /executions/{uuid}/stream` with `Accept: text/event-stream`
3. Append `token_delta.delta` until `token_done` or `task_completed` for that `taskId`

Sample payload sequence: [`examples/sample-output/token-stream-sse.txt`](../examples/sample-output/token-stream-sse.txt). Browser snippet: [`streaming-client.md`](../examples/sample-output/streaming-client.md).

#### `POST /executions/{id}/cancel`

Cancel execution.

#### `POST /executions/{id}/resume`

Resume a paused execution (Human-in-the-Loop).

**Body:**

```json
{
  "taskRunId": "uuid",
  "approved": true,
  "comment": "Looks good!"
}
```

#### `GET /executions`

List executions with filters.

**Query params:**

- `namespace=default`
- `flowId=my-flow`
- `state=RUNNING`
- `from=2024-01-01`
- `to=2024-01-31`
- `page=0`
- `size=20`

---

### Plugins

#### `GET /plugins`

List all registered plugins.

**Response:**

```json
[
  {
    "type": "openai.chat",
    "version": "1.0.0",
    "description": "OpenAI Chat Completion",
    "schema": {}
  }
]
```

#### `GET /plugins/{type}`

Get plugin details and config schema.

---

### Triggers

#### `POST /webhooks/{namespace}/{flowId}`

Trigger a flow via webhook.

No auth required (uses webhook secret in header):

```
X-OrchestrAI-Secret: <webhook-secret>
```

Body: any JSON → passed as trigger data

---

### Secrets

#### `POST /secrets`

Store a secret.

**Body:**

```json
{
  "namespace": "default",
  "key": "OPENAI_API_KEY",
  "value": "sk-..."
}
```

#### `DELETE /secrets/{namespace}/{key}`

Delete a secret.

---

### Metrics

#### `GET /metrics/executions`

Execution statistics.

**Response:**

```json
{
  "total": 1523,
  "success": 1489,
  "failed": 34,
  "successRate": 97.8,
  "avgDurationMs": 4532
}
```

#### `GET /metrics/costs`

Cost breakdown.

**Response:**

```json
{
  "totalCostUsd": 23.45,
  "byFlow": [
    { "flowId": "my-flow", "costUsd": 12.34 }
  ],
  "byModel": [
    { "model": "gpt-4", "costUsd": 18.90 },
    { "model": "claude-3", "costUsd": 4.55 }
  ]
}
```

---

## Error Responses

All errors follow this format:

```json
{
  "error": "FLOW_NOT_FOUND",
  "message": "Flow 'my-flow' not found in namespace 'default'",
  "timestamp": "2024-01-15T10:00:00Z",
  "requestId": "uuid"
}
```

| Code | HTTP Status | Description |
|------|-------------|-------------|
| FLOW_NOT_FOUND | 404 | Flow doesn't exist |
| FLOW_INVALID | 400 | YAML validation failed |
| EXECUTION_NOT_FOUND | 404 | Execution doesn't exist |
| PLUGIN_NOT_FOUND | 400 | Plugin type unknown |
| UNAUTHORIZED | 401 | Missing or invalid token |
| FORBIDDEN | 403 | No access to namespace |
| CONFLICT | 409 | Duplicate flow ID |
| INTERNAL_ERROR | 500 | Server error |

---

## WebSocket / SSE

### Real-time execution updates (logs)

Connect to SSE endpoint:

```
GET /executions/{id}/logs/stream
Accept: text/event-stream
```

**Events received:**

```
event: task_started
data: {"taskId":"step1","startedAt":"..."}

event: log
data: {"level":"INFO","message":"Calling OpenAI..."}

event: task_completed
data: {"taskId":"step1","durationMs":1234}

event: execution_completed
data: {"state":"SUCCESS","totalCostUsd":0.045}
```

### Token streaming (SSE) {#token-streaming-sse}

For chat UIs, use the dedicated stream endpoint (includes token deltas **and** lifecycle events):

```
GET /executions/{id}/stream
Accept: text/event-stream
```

**Additional events (when flow task has `stream: true`):**

```
event: token_delta
data: {"taskId":"stream-answer","index":0,"delta":"Hello"}

event: token_done
data: {"taskId":"stream-answer","response":"Hello world","tokensUsed":12,"costUsd":0.0001,"model":"gpt-4o"}

event: tool_call_started
data: {"taskId":"sales-agent","round":1,"name":"lookup_account","args":{"accountId":"acc_1042"}}

event: tool_call_completed
data: {"taskId":"sales-agent","name":"lookup_account","durationMs":142}
```

| Endpoint | Purpose |
|----------|---------|
| `/executions/{id}/logs/stream` | Ops logs, state changes |
| `/executions/{id}/stream` | **Product UI** — token-by-token LLM output |

Example flow: [`examples/13-streaming-copilot.yaml`](../examples/13-streaming-copilot.yaml).

---

## API Versioning Strategy

- Current version: v1
- Version in URL: `/v1/...`
- Old versions supported for 6 months
- Breaking changes → new version
