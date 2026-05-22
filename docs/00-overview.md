# 00 — Project Overview

## What is OrchestrAI?

OrchestrAI is an **AI Agent Orchestration Platform** that allows developers to define, execute, and monitor multi-step AI workflows using a simple YAML-based DSL (Domain Specific Language).

Think of it as **"GitHub Actions for AI Agents"** — but more powerful, distributed, and built for production AI systems.

---

## The Problem

Every team is building AI agents, but they often run in **isolation** — separate scripts, services, or notebooks with no reliable way to chain them into a single production workflow with retries, governance, and observability.

Building AI-powered applications today also requires:

1. Calling multiple LLMs (GPT-4, Claude, Gemini, local models)
2. Chaining outputs between agents (Agent A → Agent B)
3. Handling failures, retries, and fallbacks
4. Tracking tokens, costs, and performance
5. Adding human approval steps
6. Scheduling and triggering workflows

**Current solutions are fragmented:**

- LangChain → Python only, code-heavy
- Custom scripts → Hard to maintain, not reusable; agents stay siloed
- Generic orchestrators (Airflow, Kestra) → Not AI-first

**Result:** Teams spend most of their time on plumbing (orchestration, retries, monitoring), not on agent logic and prompts.

---

## Value Proposition

| # | Capability | How OrchestrAI delivers it |
|---|------------|----------------------------|
| 1 | **Connected agents, not silos** | Multi-agent **pipelines** in YAML with reliable execution — not one-off scripts in isolation |
| 2 | **Multi-agent pipelines** | Agent A's output feeds Agent B via `{{ outputs.<taskId>... }}` — see [YAML Schema](./04-yaml-schema.md) and [AI Agents](./09-ai-agents.md) |
| 3 | **Swappable LLM plugins** | Each model/provider is a **plugin** (`openai.chat`, `anthropic.chat`, `google.gemini`, `ollama.chat`, custom JARs) — compose them like building blocks — see [Plugin System](./08-plugin-system.md) |
| 4 | **Retry and fallback** | Per-task retry with backoff; **model fallback** when a provider times out or rate-limits (e.g. GPT-4 → Claude) — see [Execution Engine](./07-execution-engine.md), [AI Agents](./09-ai-agents.md) |
| 5 | **Cost tracking** | Tokens and USD **per task and per execution**; APIs and cost dashboard — see [AI Agents](./09-ai-agents.md), [API Design](./10-api-design.md) |
| 6 | **Human-in-the-loop** | Pause at `human.approval`, wait for a human, then **resume automatically** — see [AI Agents](./09-ai-agents.md) |
| 7 | **Shared context** | Conversation history and variables across steps (`contextKey`, outputs); RAG via **external** vector stores (not a built-in DB) — see [AI Agents](./09-ai-agents.md) |
| 8 | **Triggers** | REST API, webhooks, cron, Kafka events — see [Features](./03-features.md), [API Design](./10-api-design.md) |
| 9 | **Observability** | Real-time logs (SSE), execution history, per-task inputs/outputs, latency, errors, token/cost metrics — see [Features](./03-features.md) |
| 10 | **Ship faster** | Platform handles orchestration, scaling, retries, and ops so teams focus on **agent logic**, not infrastructure |

Some items above are **P1/P2 on the [roadmap](./13-roadmap.md)** (e.g. cost dashboard, human-in-the-loop UI, shared memory polish). Core YAML execution, plugins, and API are **P0**.

---

## The Solution

OrchestrAI provides:

- **Declarative YAML** — Define multi-agent workflows, not orchestration code
- **Plugin architecture** — Swap GPT-4, Claude, Gemini, Ollama/LLaMA, or custom models per task
- **Reliable execution** — Retries, timeouts, fallbacks, and `onFailure` handlers
- **Distributed execution** — Scale workers with Kafka
- **Triggers** — REST, webhooks, cron, and Kafka events
- **Built-in observability** — Logs, metrics, per-task I/O, and execution history
- **Cost tracking** — Tokens and USD per workflow run
- **Human-in-the-loop** — Approval steps when AI output needs a human gate

---

## Target Audience

- **AI Engineers** building multi-agent systems
- **Backend Developers** integrating LLMs
- **DevOps Teams** deploying AI workflows
- **Startups** shipping AI products fast

---

## Project Status

**Active Development** — MVP targeted in 12 weeks

---

## Inspiration

OrchestrAI draws inspiration from:

- **Kestra** — YAML-based orchestration
- **Temporal** — Distributed execution
- **LangChain** — Agent composition
- **GitHub Actions** — Developer experience

We are not a clone — we are AI-first, Java-based, and distributed.

---

## See example workflows

Production-style flows (support RAG, on-call triage, marketplace moderation, invoice AP, legal HITL, cron digests, and more) live in [`examples/`](../examples/)—each mapped to [Vision & Goals](./01-vision-and-goals.md) in [GOALS.md](../examples/GOALS.md). Walkthrough: [15 — Examples](./15-examples.md).
