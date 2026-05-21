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

List all flows.

**Query params:** `namespace`, `page`, `size`

#### `GET /flows/{namespace}/{id}`

Get a specific flow.

#### `PUT /flows/{namespace}/{id}`

Update a flow (creates new version).

#### `DELETE /flows/{namespace}/{id}`

Delete a flow.

---

### Executions

#### `POST /flows/{namespace}/{id}/execute`

Start a new execution.

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

Stream logs in real time.

```
event: log
data: {"level":"INFO","message":"Task started"}

event: state
data: {"state":"RUNNING"}
```

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

### Real-time Execution Updates

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

---

## API Versioning Strategy

- Current version: v1
- Version in URL: `/v1/...`
- Old versions supported for 6 months
- Breaking changes → new version
