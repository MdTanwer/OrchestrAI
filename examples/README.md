# OrchestrAI — Example Flows

Copy-pasteable YAML workflows that show what OrchestrAI is meant to run. Each file is a complete flow definition you can read, import, or adapt once the platform is implemented.

| File | Pattern | What it demonstrates |
|------|---------|----------------------|
| [01-hello-agent.yaml](./01-hello-agent.yaml) | Single agent | Minimal flow: one LLM call with an input |
| [02-research-and-summarize.yaml](./02-research-and-summarize.yaml) | Sequential chain | Agent A researches → Agent B summarizes |
| [03-content-moderation.yaml](./03-content-moderation.yaml) | Parallel + branch | Draft → parallel checks → approve or reject |
| [04-support-ticket-router.yaml](./04-support-ticket-router.yaml) | Router | Classify ticket → route to the right specialist agent |
| [05-model-fallback.yaml](./05-model-fallback.yaml) | Resilience | Primary model fails → fallback provider |
| [06-human-approval.yaml](./06-human-approval.yaml) | Human-in-the-loop | Generate → pause for approval → publish |
| [07-shared-context.yaml](./07-shared-context.yaml) | Shared memory | Two agents, one conversation via `contextKey` |
| [08-cost-tracking.yaml](./08-cost-tracking.yaml) | Cost tracking | Per-task tokens/USD, budget gate, flow labels |

**Sample API/output payloads:** [`sample-output/`](./sample-output/) — JSON shapes for TaskRun metrics and execution totals.

For walkthroughs, diagrams, and when to use each pattern, see [docs/15-examples.md](../docs/15-examples.md).
