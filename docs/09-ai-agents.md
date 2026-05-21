# 09 — AI Agents

## What is an AI Agent in OrchestrAI?

An **AI Agent** is a Task that uses an LLM to make decisions, generate content, or call tools. Agents are first-class citizens in OrchestrAI.

---

## Agent Capabilities

### 1. Chat Completion

Standard LLM call with prompt → response.

### 2. Tool Calling

LLM decides which tools to invoke.

### 3. Multi-turn Conversation

Stateful chat with memory.

### 4. RAG (Retrieval Augmented Generation)

Search documents → inject into prompt.

### 5. Streaming

Stream tokens as they're generated.

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

---

## Model Fallback Pattern

```yaml
- id: ai-task
  type: ai.chat
  primary:
    provider: openai
    model: gpt-4
  fallback:
    - provider: anthropic
      model: claude-3-opus
    - provider: google
      model: gemini-pro
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

## Tool Calling Pattern

```yaml
- id: agent-with-tools
  type: openai.chat
  model: gpt-4
  prompt: "{{ inputs.userQuery }}"
  tools:
    - name: searchWeb
      description: "Search the web"
      type: http.request
      url: "https://api.search.com"
    - name: queryDb
      description: "Query database"
      type: postgres.query
      sql: "SELECT * FROM users WHERE..."
```

The LLM decides which tool to call; the engine executes the tool and returns the result.

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
