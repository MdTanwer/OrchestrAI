# API Design

## Overview

OrchestrAI provides multiple API interfaces for interacting with the system: REST API, GraphQL, and Webhooks. This document describes the API design and usage.

## REST API

### Base URL
```
https://api.orchestrai.com/v1
```

### Authentication
All API requests require authentication via Bearer token:
```
Authorization: Bearer <your-api-key>
```

### Endpoints

#### Workflows

##### List Workflows
```http
GET /workflows
```
**Response:**
```json
{
  "workflows": [
    {
      "id": "uuid",
      "name": "workflow-name",
      "version": "1.0",
      "status": "active"
    }
  ],
  "total": 10,
  "page": 1
}
```

##### Get Workflow
```http
GET /workflows/{workflow_id}
```

##### Create Workflow
```http
POST /workflows
Content-Type: application/json

{
  "name": "workflow-name",
  "version": "1.0",
  "definition": "yaml: ..."
}
```

##### Update Workflow
```http
PUT /workflows/{workflow_id}
Content-Type: application/json

{
  "definition": "yaml: ..."
}
```

##### Delete Workflow
```http
DELETE /workflows/{workflow_id}
```

#### Executions

##### List Executions
```http
GET /executions?workflow_id={workflow_id}
```

##### Get Execution
```http
GET /executions/{execution_id}
```

##### Trigger Execution
```http
POST /executions
Content-Type: application/json

{
  "workflow_id": "uuid",
  "input": {}
}
```

##### Cancel Execution
```http
POST /executions/{execution_id}/cancel
```

#### Agents

##### List Agents
```http
GET /agents
```

##### Get Agent
```http
GET /agents/{agent_id}
```

##### Create Agent
```http
POST /agents
Content-Type: application/json

{
  "name": "agent-name",
  "type": "llm",
  "model": "gpt-4",
  "configuration": {}
}
```

## GraphQL API

### Endpoint
```
https://api.orchestrai.com/graphql
```

### Example Query
```graphql
query GetWorkflow($id: ID!) {
  workflow(id: $id) {
    id
    name
    version
    definition
    executions(limit: 10) {
      id
      status
      startedAt
    }
  }
}
```

### Example Mutation
```graphql
mutation CreateWorkflow($input: WorkflowInput!) {
  createWorkflow(input: $input) {
    id
    name
    version
  }
}
```

### Subscriptions
```graphql
subscription ExecutionUpdates($executionId: ID!) {
  executionUpdates(executionId: $executionId) {
    id
    status
    currentStep
    output
  }
}
```

## Webhooks

### Configuration
Webhooks can be configured to receive notifications about events.

#### Create Webhook
```http
POST /webhooks
Content-Type: application/json

{
  "url": "https://your-domain.com/webhook",
  "events": ["execution.completed", "execution.failed"],
  "secret": "webhook-secret"
}
```

### Webhook Events

#### execution.completed
```json
{
  "event": "execution.completed",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "execution_id": "uuid",
    "workflow_id": "uuid",
    "status": "completed",
    "output": {}
  }
}
```

#### execution.failed
```json
{
  "event": "execution.failed",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "execution_id": "uuid",
    "workflow_id": "uuid",
    "error": "Error message"
  }
}
```

### Signature Verification
Webhooks include a signature for verification:
```python
import hmac
import hashlib

def verify_signature(payload, signature, secret):
    expected = hmac.new(
        secret.encode(),
        payload,
        hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(expected, signature)
```

## Error Handling

### Error Response Format
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {}
  }
}
```

### Common Error Codes
- `INVALID_REQUEST`: Malformed request
- `UNAUTHORIZED`: Missing or invalid authentication
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `VALIDATION_ERROR`: Input validation failed
- `RATE_LIMIT_EXCEEDED`: Too many requests
- `INTERNAL_ERROR`: Server error

### Rate Limiting
- Default: 1000 requests per hour
- Headers included: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

## SDKs

### Python SDK
```python
from orchestrAI import Client

client = Client(api_key="your-key")

workflow = client.workflows.get("workflow-id")
execution = workflow.execute(input={"data": "value"})
```

### JavaScript SDK
```javascript
import { Client } from 'orchestrai';

const client = new Client({ apiKey: 'your-key' });

const workflow = await client.workflows.get('workflow-id');
const execution = await workflow.execute({ data: 'value' });
```

### Go SDK
```go
import "github.com/orchestrai/go-sdk"

client := orchestrai.NewClient("your-key")
workflow, err := client.Workflows.Get("workflow-id")
execution, err := workflow.Execute(map[string]interface{}{"data": "value"})
```
