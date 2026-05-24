# OrchestrAI — Example Flows

Production-style YAML workflows scoped to [Vision & Goals](../docs/01-vision-and-goals.md). See **[GOALS.md](./GOALS.md)** for how each example maps to project goals and what we intentionally omit (built-in vector DB, training, visual editor).

## By real-world scenario

| Scenario | File | Primary goals |
|----------|------|----------------|
| In-app copilot from release notes | [01-hello-agent.yaml](./01-hello-agent.yaml) | G1, G6 |
| Competitive intel exec brief | [02-research-and-summarize.yaml](./02-research-and-summarize.yaml) | Pipelines, multi-LLM, cost |
| Marketplace listing moderation | [03-content-moderation.yaml](./03-content-moderation.yaml) | G2, G5 |
| Helpdesk ticket routing (webhook) | [04-support-ticket-router.yaml](./04-support-ticket-router.yaml) | Triggers, G6 |
| Invoice extraction + fallback | [05-model-fallback.yaml](./05-model-fallback.yaml) | G2 |
| Regulated email + compliance HITL | [06-human-approval.yaml](./06-human-approval.yaml) | HITL |
| Interview screen + recruiter coach | [07-shared-context.yaml](./07-shared-context.yaml) | Shared context |
| Blog pipeline budget gate | [08-cost-tracking.yaml](./08-cost-tracking.yaml) | G3 |
| **RAG support (external Pinecone)** | [09-rag-customer-support.yaml](./09-rag-customer-support.yaml) | G4, G6 |
| **On-call incident triage (webhook)** | [10-incident-triage.yaml](./10-incident-triage.yaml) | G2, G5, triggers |
| **Monday ops digest (cron)** | [11-weekly-ops-digest.yaml](./11-weekly-ops-digest.yaml) | G3, cron |
| **Vendor MSA review (HITL)** | [12-contract-review-hitl.yaml](./12-contract-review-hitl.yaml) | HITL, legal |
| **In-app copilot (SSE token stream)** | [13-streaming-copilot.yaml](./13-streaming-copilot.yaml) | G3, F3.9 |
| **Sales copilot (LLM tool calling)** | [14-sales-agent-with-tools.yaml](./14-sales-agent-with-tools.yaml) | G4, G6, F3.7 |
| **Churn save batch (core.foreach)** | [15-churn-outreach-foreach.yaml](./15-churn-outreach-foreach.yaml) | F2.4, G6 |
| **Distributed parallel review** | [16-distributed-document-review.yaml](./16-distributed-document-review.yaml) | F2.8, G5 |
| **Kafka / multi-worker guide** | [DISTRIBUTED.md](./DISTRIBUTED.md) | Architecture ↔ YAML |
| **Order fulfillment (Kafka trigger)** | [17-order-fulfillment-kafka-trigger.yaml](./17-order-fulfillment-kafka-trigger.yaml) | F5.4 |

## By pattern (quick reference)

| Pattern | Files |
|---------|--------|
| Single agent | `01` |
| Sequential chain | `02`, `08`, `09`, `12` |
| Parallel + branch | `03`, `10` |
| **Loop (`core.foreach`)** | `15` |
| **Distributed (Kafka workers)** | `16`, [DISTRIBUTED.md](./DISTRIBUTED.md) |
| Router | `04` |
| Fallback / retry | `05`, `10` |
| Human approval | `06`, `12` |
| Shared `contextKey` | `07` |
| Cost / budget | `02`, `08`, `09`, `11` |
| Webhook trigger | `04`, `10` |
| Cron trigger | `11` |
| **Kafka event trigger** | `17` |
| External RAG (HTTP) | `09` |
| **Streaming tokens (SSE)** | `13` |
| **Tool calling (LLM → subtasks)** | `14` |

**Sample payloads:** [`sample-output/`](./sample-output/) — metrics, [token SSE](sample-output/token-stream-sse.txt), [tool roundtrip](sample-output/tool-calling-roundtrip.json), [foreach results](sample-output/foreach-results.json), [Kafka task-run](sample-output/kafka-task-run-message.json), [Kafka trigger message](sample-output/kafka-trigger-message.json)

**Walkthrough:** [docs/15-examples.md](../docs/15-examples.md)
