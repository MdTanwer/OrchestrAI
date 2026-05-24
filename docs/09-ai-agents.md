# 09 — AI Agents

## What is an AI Agent in OrchestrAI?

An **AI Agent** is a Task that uses an LLM to make decisions, generate content, or call tools. Agents are first-class citizens in OrchestrAI.

---

## Agent Capabilities

### 1. Chat Completion

Standard LLM call with prompt → response.

### 2. Tool Calling

LLM decides which tools to invoke. Example: [`14-sales-agent-with-tools.yaml`](../examples/14-sales-agent-with-tools.yaml).

### 3. Multi-turn Conversation

Stateful chat with memory.

### 4. RAG (Retrieval Augmented Generation)

Search documents → inject into prompt.

### 5. Streaming

Stream completion **tokens to the client in real time** via SSE while the worker still calls the provider's streaming API. The final text and usage metrics are stored on the TaskRun when the model finishes—downstream tasks use `outputs.<taskId>.response` as usual.

**YAML** — set `stream: true` on any AI chat plugin:

```yaml
- id: stream-answer
  type: openai.chat
  model: gpt-4o
  stream: true
  prompt: "{{ inputs.userMessage }}"
```

**Client** — after `POST .../execute`, open:

```
GET /v1/executions/{executionId}/stream
Accept: text/event-stream
```

Listen for `token_delta` (append `delta` to the UI) and `token_done` (final `response`, `tokensUsed`, `costUsd`). See [API Design — Token streaming (SSE)](./10-api-design.md#token-streaming-sse) and [`examples/13-streaming-copilot.yaml`](../examples/13-streaming-copilot.yaml).

| Event | When | Use in UI |
|-------|------|-----------|
| `token_delta` | During `stream: true` task | Typing effect / partial markdown |
| `token_done` | Model finished | Finalize message + show usage |
| `task_completed` | Task closed | Enable follow-up actions |
| `log` | Any task | Debug panel |

**Example flow:** [`examples/13-streaming-copilot.yaml`](../examples/13-streaming-copilot.yaml) — stream to widget, then HTTP persist full transcript. Samples: [`token-stream-sse.txt`](../examples/sample-output/token-stream-sse.txt), [`streaming-client.md`](../examples/sample-output/streaming-client.md).

---

## Cost & Token Tracking

Every AI plugin reports:

```json
{
  "tokensUsed": 1523,
  "promptTokens": 245,
  "completionTokens": 1278,
  "costUsd": 0.04569,
  "model": "gpt-4"
}
```

Costs are aggregated at the execution level.

**Example flow:** [`examples/08-cost-tracking.yaml`](../examples/08-cost-tracking.yaml) — three LLM steps, budget gate, and sample JSON in [`examples/sample-output/`](../examples/sample-output/). Walkthrough: [15 — Examples](./15-examples.md#8-cost-tracking--tokens-and-usd-per-step-and-per-run).

---

## Model Fallback Pattern

Task-level fallback lets you define alternative steps if a primary model provider encounters rate limits, timeouts, or API errors. The Execution Engine automatically swaps the execution configuration to the fallback task upon failure:

```yaml
- id: extract-data
  type: openai.chat
  model: gpt-4o
  prompt: "Extract entities from: {{ inputs.articleText }}"
  fallback:
    type: anthropic.chat
    model: claude-3-5-sonnet
    prompt: "Extract entities from: {{ inputs.articleText }}"
    fallbackOn:
      - RATE_LIMIT
      - TIMEOUT
      - PROVIDER_ERROR
```

---

## Shared Context Between Agents

Agents in the same execution can share memory:

```yaml
tasks:
  - id: agent1
    type: openai.chat
    contextKey: "conversation"
    prompt: "User: {{ inputs.query }}"

  - id: agent2
    type: anthropic.chat
    contextKey: "conversation"  # same context
    prompt: "Continue from previous"
```

Behind the scenes, `contextKey` stores conversation history.

### Memory vs vector stores

| Mechanism | What it does | Built into OrchestrAI? |
|-----------|--------------|------------------------|
| `contextKey` | Shared conversation history between agent tasks in one execution | Yes |
| `outputs.*` / `vars.*` | Pass structured data between any tasks | Yes |
| RAG (retrieval) | Search documents, inject into prompts | Via plugins + **your** vector DB |
| Vector database | Long-term embeddings storage | **No** — use Pinecone, Weaviate, pgvector, etc. via `http.request` or a custom plugin |

OrchestrAI orchestrates **when** agents run and **what** they receive; it does not replace a dedicated vector database. See [Vision & Goals — Out of Scope](./01-vision-and-goals.md#what-we-are-not-building-out-of-scope).

---

## Human-in-the-Loop

```yaml
- id: review
  type: human.approval
  message: "Approve this content?"
  data: "{{ outputs.generate.response }}"
  approvers:
    - user@example.com
  timeout: "24h"
  onTimeout: REJECT

- id: publish
  type: http.request
  if: "{{ outputs.review.approved }}"
  url: "/api/publish"
```

Execution pauses at `human.approval` and resumes on user action.

---

## Tool Calling — LLM-invoked subtasks

An agent task can declare **tools**: each tool is a normal OrchestrAI plugin configuration (e.g. `http.request`, `postgres.query`) that the model may invoke with structured arguments. The execution engine runs the tool loop—you do not write retry/orchestration code in your app.

```
User message
    → LLM (with tool schemas)
    → tool_call(s) → Worker runs nested plugin(s) using {{ tool.args.* }}
    → tool results fed back to LLM
    → (repeat up to maxToolRounds)
    → final natural-language answer
```

**YAML:**

```yaml
- id: sales-agent
  type: openai.chat
  model: gpt-4o
  maxToolRounds: 5
  prompt: "{{ inputs.userMessage }}"
  tools:
    - name: lookup_account
      description: "Fetch CRM account by id"
      parameters:
        type: object
        required: [accountId]
        properties:
          accountId: { type: string }
      type: http.request
      method: GET
      url: "https://crm.example.com/v1/accounts/{{ tool.args.accountId }}"
```

| Field | Description |
|-------|-------------|
| `tools[].name` | Function name exposed to the model |
| `tools[].description` | When the model should use this tool |
| `tools[].parameters` | JSON Schema for arguments the model fills |
| `tools[].type` + … | Plugin config for the subtask (same fields as a standalone task) |
| `maxToolRounds` | Cap on LLM ↔ tool iterations (default 10) |
| `{{ tool.args.<field> }}` | Resolved from the model's tool_call arguments |

**Task output** includes the final answer plus audit metadata:

```json
{
  "response": "...",
  "toolRounds": 2,
  "toolCalls": [{ "name": "lookup_account", "args": {}, "taskRunId": "uuid", "durationMs": 142 }],
  "tokensUsed": 2184,
  "costUsd": 0.018
}
```

Sample: [`tool-calling-roundtrip.json`](../examples/sample-output/tool-calling-roundtrip.json). SSE: [`tool-calling-sse.txt`](../examples/sample-output/tool-calling-sse.txt).

**Example flow:** [`examples/14-sales-agent-with-tools.yaml`](../examples/14-sales-agent-with-tools.yaml) — CRM lookup, open invoices, create follow-up task. Walkthrough: [15 — Examples](./15-examples.md#14-sales-rep-copilot--tool-calling).

**vs fixed pipelines:** In `02` or `09`, tasks run in a fixed order. Tool calling lets the **model decide** which integrations to hit and with which parameters.

---

## Prompt Templates

```yaml
variables:
  systemPrompt: |
    You are a helpful AI assistant.
    Always respond in JSON format.
    Be concise.

tasks:
  - id: chat
    type: openai.chat
    system: "{{ vars.systemPrompt }}"
    prompt: "{{ inputs.query }}"
```

---

## Multi-Agent Patterns

### Pattern 1: Sequential Chain

```
Agent A → Agent B → Agent C
```

### Pattern 2: Parallel Aggregation

```
       ┌─ Agent A ─┐
Input →├─ Agent B ─┼→ Aggregator → Output
       └─ Agent C ─┘
```

### Pattern 3: Router

```
              ┌─ Agent A (if topic=tech)
Input → Router├─ Agent B (if topic=business)
              └─ Agent C (if topic=other)
```

### Pattern 4: Refiner

```
Generator → Critic → Generator → Critic → Final
```
