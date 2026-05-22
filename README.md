# OrchestrAI — AI Agent Orchestration Platform

> Build, run, and monitor multi-agent AI workflows using simple YAML.

OrchestrAI is an open-source, Java-based orchestration platform designed specifically for AI agents. Define complex multi-agent pipelines in YAML, execute them at scale, and monitor every step in real time.

---

## Why OrchestrAI?

Modern AI applications need multiple agents working together — but orchestrating them is painful. OrchestrAI provides:

- Declarative YAML workflows
- Multi-LLM support (OpenAI, Claude, Gemini, local models)
- Plugin-based architecture
- Distributed execution via Kafka
- Real-time monitoring dashboard
- Token and cost tracking
- Human-in-the-loop steps

---

## Documentation

Full documentation lives in the [`docs/`](./docs/) directory:

| # | Document | Description |
|---|----------|-------------|
| 00 | [Overview](./docs/00-overview.md) | Project introduction |
| 01 | [Vision & Goals](./docs/01-vision-and-goals.md) | Why we're building this |
| 02 | [Terminology](./docs/02-terminology.md) | Core concepts defined |
| 03 | [Features](./docs/03-features.md) | What OrchestrAI does |
| 04 | [YAML Schema](./docs/04-yaml-schema.md) | Workflow definition format |
| 05 | [Architecture](./docs/05-architecture.md) | System design |
| 06 | [Data Models](./docs/06-data-models.md) | Database schemas |
| 07 | [Execution Engine](./docs/07-execution-engine.md) | How workflows run |
| 08 | [Plugin System](./docs/08-plugin-system.md) | Extensibility model |
| 09 | [AI Agents](./docs/09-ai-agents.md) | LLM integration |
| 10 | [API Design](./docs/10-api-design.md) | REST API reference |
| 11 | [Deployment](./docs/11-deployment.md) | Running OrchestrAI |
| 12 | [Security](./docs/12-security.md) | Auth and secrets |
| 13 | [Roadmap](./docs/13-roadmap.md) | Development plan |
| 14 | [Native Image](./docs/14-native-image.md) | GraalVM native builds |
| 15 | [Examples](./docs/15-examples.md) | What we build — sample workflows |
| — | [`examples/`](./examples/) | Copy-pasteable YAML flow files |

See also [docs/README.md](./docs/README.md) for the documentation index and quick start.

**New developers:** start with [docs/15-examples.md](./docs/15-examples.md) and the YAML files in [`examples/`](./examples/) to see concrete workflows before reading architecture docs.

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
