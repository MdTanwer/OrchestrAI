# Client: consume token SSE from the browser

Start the flow, then open the stream on the returned `executionId`.

## 1. Start execution

```http
POST /v1/flows/production.product/streaming-product-copilot/execute
Content-Type: application/json

{
  "inputs": {
    "sessionId": "sess_abc",
    "userMessage": "Does Pro include SSO?",
    "productContext": "Pro: SSO, audit logs, SLA. Free: up to 5 users."
  }
}
```

Response `202`:

```json
{ "executionId": "550e8400-e29b-41d4-a716-446655440000", "state": "CREATED" }
```

## 2. Subscribe to token stream (SSE)

```http
GET /v1/executions/550e8400-e29b-41d4-a716-446655440000/stream
Accept: text/event-stream
```

Handle `token_delta` to append to the UI; on `token_done` or `task_completed` for the streaming task, stop the typing indicator.

## 3. Minimal JavaScript (`EventSource`)

```javascript
const executionId = "550e8400-e29b-41d4-a716-446655440000";
const source = new EventSource(`/v1/executions/${executionId}/stream`);
let answer = "";

source.addEventListener("token_delta", (e) => {
  const { delta } = JSON.parse(e.data);
  answer += delta;
  renderPartial(answer);
});

source.addEventListener("token_done", (e) => {
  const { response, costUsd, tokensUsed } = JSON.parse(e.data);
  renderFinal(response, { costUsd, tokensUsed });
});

source.addEventListener("execution_completed", () => source.close());
```

## 4. CLI (watch tokens)

```bash
orchestrai execution stream <executionId> --follow
```

Equivalent to log follow, but prints `token_delta` lines as they arrive.
