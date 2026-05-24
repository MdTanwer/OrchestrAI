# Examples ↔ Project Goals

Every example in this folder is scoped to [Vision & Goals](../docs/01-vision-and-goals.md). We **do not** demonstrate out-of-scope items (custom LLM training, built-in vector DB, visual editor).

## Primary goals

| Goal | What it means in examples |
|------|---------------------------|
| **G1 Simplicity** | `01` stays minimal; most flows are &lt; 60 lines and read top-to-bottom |
| **G2 Reliability** | `05`, `10` use retry, fallback, `onFailure` |
| **G3 Observability** | `08`, `11`, `13` expose tokens/cost + live SSE token stream |
| **G4 Extensibility** | `09` uses **your** Pinecone via `http.request`; `03` uses `custom.spellcheck` |
| **G5 Performance** | `10` uses `core.parallel` for independent I/O + LLM steps |
| **G6 Agent logic, not plumbing** | Prompts/tasks only—no hand-rolled retry loops in YAML |

## In-scope capabilities (value proposition)

| Capability | Examples |
|------------|----------|
| Connected agents (pipelines) | `02`, `08`, `09`, `12` |
| **Loops (`core.foreach`)** | `15` — one LLM + CRM step per account |
| **Distributed execution (Kafka)** | `16`, [DISTRIBUTED.md](./DISTRIBUTED.md) — parallel tasks → worker pool |
| Multi-LLM plugins | `02`, `05`, `07`, `09` |
| Retry & fallback | `05`, `10` |
| Cost tracking | `02`, `08`, `11` |
| Human-in-the-loop | `06`, `12` |
| Shared context | `07` |
| Triggers (REST / webhook / cron / **Kafka**) | `04`, `10`, `11`, `17` |
| RAG via **external** vector DB | `09` only |
| Observability hooks | All `production.*` and `operations.*` namespaces |
| **Streaming tokens (SSE)** | `13` — in-app copilot |
| **Tool calling (LLM subtasks)** | `14` — CRM/ERP tools chosen by model |

## Real-world scenarios by audience

| Audience | Examples |
|----------|----------|
| **AI engineers** | `09` RAG support, `07` interview coach, `02` intel briefing, `15` batch outreach |
| **Backend developers** | `04` ticket router, `12` contract review, `01` API-backed Q&A, `13` SSE chat UI, `14` tool calling |
| **DevOps / platform** | `10` incident triage, `11` weekly digest, `05` resilient extraction, `16` + DISTRIBUTED |
| **Startups shipping fast** | `01`, `06`, `08` — copy, approve, ship with cost guardrails |

## Out of scope (intentionally absent)

- Training or fine-tuning models  
- Embedded Pinecone/Weaviate server — only HTTP to an existing index (`09`)  
- Drag-and-drop UI — flows are YAML files in git  
