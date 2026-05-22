# OrchestrAI — AI Agent Orchestration Platform

> Build, run, and monitor multi-agent AI workflows using simple YAML.

OrchestrAI is an open-source, Java-based orchestration platform designed specifically for AI agents. Define complex multi-agent pipelines in YAML, execute them at scale, and monitor every step in real time.

---

## Why OrchestrAI?

Most teams build AI agents in **isolation** — hard to chain them into reliable, observable production workflows. OrchestrAI connects them into **multi-agent pipelines** defined in YAML (Agent A → Agent B).

- **Declarative YAML** — chain agents; outputs flow to the next step
- **Swappable LLM plugins** — OpenAI, Claude, Gemini, Ollama/LLaMA, custom models
- **Retries and model fallback** — auto-retry or switch provider on timeout/rate limit
- **Cost tracking** — tokens and USD per execution
- **Human-in-the-loop** — pause for approval, then continue
- **Shared context** — conversation history across steps (RAG via external vector DBs)
- **Triggers** — REST API, webhooks, cron, Kafka events
- **Full observability** — real-time dashboard, logs, per-task I/O, latency, errors
- **Distributed execution** via Kafka

See [Overview — Value Proposition](./00-overview.md#value-proposition) for the full breakdown.

---

## Documentation Index

| # | Document | Description |
|---|----------|-------------|
| 00 | [Overview](./00-overview.md) | Project introduction |
| 01 | [Vision & Goals](./01-vision-and-goals.md) | Why we're building this |
| 02 | [Terminology](./02-terminology.md) | Core concepts defined |
| 03 | [Features](./03-features.md) | What OrchestrAI does |
| 04 | [YAML Schema](./04-yaml-schema.md) | Workflow definition format |
| 05 | [Architecture](./05-architecture.md) | System design |
| 06 | [Data Models](./06-data-models.md) | Database schemas |
| 07 | [Execution Engine](./07-execution-engine.md) | How workflows run |
| 08 | [Plugin System](./08-plugin-system.md) | Extensibility model |
| 09 | [AI Agents](./09-ai-agents.md) | LLM integration |
| 10 | [API Design](./10-api-design.md) | REST API reference |
| 11 | [Deployment](./11-deployment.md) | Running OrchestrAI |
| 12 | [Security](./12-security.md) | Auth and secrets |
| 13 | [Roadmap](./13-roadmap.md) | Development plan |
| 14 | [Native Image](./14-native-image.md) | GraalVM native builds |
| 15 | [Examples](./15-examples.md) | What we build — sample workflows |
| — | [`examples/`](../examples/) | Production-style YAML flows |
| — | [`examples/GOALS.md`](../examples/GOALS.md) | Examples ↔ vision & goals |

---

## New here?

Read [Overview](./00-overview.md), then **[Examples](./15-examples.md)** + **[GOALS.md](../examples/GOALS.md)** before architecture or code.

---

## Quick Start

```bash
git clone https://github.com/yourname/orchestrai.git
cd orchestrai
docker-compose up
```

Open http://localhost:8080 and create your first flow.

---

## Tech Stack

Java 17 · Quarkus · Apache Kafka · PostgreSQL · Next.js · Docker

---

## License

Apache 2.0
