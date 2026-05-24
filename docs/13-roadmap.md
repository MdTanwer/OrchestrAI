# 13 — Roadmap

## 12-Week MVP Plan

---

### Week 1–2: Foundation

**Goal:** Project skeleton + YAML parser working

**Tasks:**

- [ ] Setup multi-module Maven project
- [ ] Configure Quarkus for API server
- [ ] Define all domain model classes
- [ ] Implement YAML parser (Jackson YAML)
- [ ] Write Flow validation logic
- [ ] Unit tests for parser (80%+ coverage)
- [ ] Setup PostgreSQL schema + Flyway migrations
- [ ] Configure Docker Compose (Postgres + Kafka)

**Deliverable:** Parse and validate a YAML flow file

---

### Week 3–4: Core Execution Engine

**Goal:** Execute a basic flow end-to-end

**Tasks:**

- [ ] Implement Execution state machine
- [ ] Build expression resolver (JEXL)
- [ ] Create sequential task executor
- [ ] Implement retry logic
- [ ] Build execution context (inputs/outputs/variables)
- [ ] Persist executions + task runs to DB
- [ ] Unit + integration tests

**Deliverable:** Run a 3-task flow and see results in DB

---

### Week 5–6: Kafka Integration

**Goal:** Distributed task execution

**Tasks:**

- [ ] Setup Kafka topics
- [ ] Implement Kafka producer (API → Worker)
- [ ] Implement Kafka consumer (Worker)
- [ ] Build Worker Node service
- [ ] Plugin SDK interface
- [ ] Idempotency + at-least-once delivery
- [ ] Dead letter queue handling
- [ ] Multi-worker test (2 workers, parallel tasks)

**Deliverable:** Tasks distributed across multiple workers

---

### Week 7: AI Plugins

**Goal:** Real LLM calls working

**Tasks:**

- [ ] Implement `openai.chat` plugin
- [ ] Implement `anthropic.chat` plugin
- [ ] Implement `google.gemini` plugin
- [ ] Token + cost tracking
- [ ] SSE token streaming (`stream: true`, `GET /executions/{id}/stream`)
- [ ] Tool-calling loop (`tools`, `maxToolRounds`, `tool_call_*` SSE)
- [ ] Model fallback logic
- [ ] Secret injection for API keys
- [ ] Test with real API calls

**Deliverable:** Multi-LLM flow running with cost tracking

---

### Week 8: Integration Plugins + Triggers

**Goal:** Connect to external systems

**Tasks:**

- [ ] Implement `http.request` plugin
- [ ] Implement `core.parallel` plugin
- [ ] Implement `core.if` plugin
- [ ] Build cron scheduler (Quartz)
- [ ] Build webhook trigger endpoint
- [ ] Manual trigger via API

**Deliverable:** Webhook triggers a flow; parallel tasks work

---

### Week 9–10: REST API + Persistence

**Goal:** Full API working, data persisted

**Tasks:**

- [ ] Flow CRUD endpoints
- [ ] Execution endpoints
- [ ] Log endpoints + SSE streaming
- [ ] Secrets management API
- [ ] Metrics endpoints
- [ ] API authentication (JWT)
- [ ] RBAC implementation
- [ ] API validation + error handling
- [ ] Postman collection

**Deliverable:** Full REST API documented and tested

---

### Week 11: Web Dashboard (UI)

**Goal:** Visual interface working

**Tasks:**

- [ ] Setup Next.js project
- [ ] Flow list + YAML editor
- [ ] Execution trigger + history
- [ ] Real-time log viewer (SSE)
- [ ] Cost dashboard
- [ ] Plugin catalog page
- [ ] Basic auth UI (login)

**Deliverable:** Full dashboard working locally

---

### Week 12: Polish + Launch

**Goal:** Production-ready, public launch

**Tasks:**

- [ ] End-to-end integration tests
- [ ] Performance testing (100 concurrent flows)
- [ ] Security review
- [ ] Complete all documentation
- [ ] GitHub README with screenshots/GIFs
- [ ] Docker Compose one-command setup
- [ ] Record demo video
- [ ] Publish on GitHub
- [ ] Post on LinkedIn

**Deliverable:** Public GitHub repo, demo video, LinkedIn post

---

## Post-MVP (Phase 2)

| Feature | Why |
|---------|-----|
| Visual flow editor (drag-and-drop) | Better UX |
| Flow marketplace | Share flows |
| Kubernetes operator | Enterprise deployment |
| gRPC support | Performance |
| LangChain integration | Python interop |
| Multi-tenant SaaS | Commercial path |
| OpenTelemetry tracing | Observability |
| Flow testing framework | Quality |
| Custom plugin marketplace | Community |
| GraphQL API | Flexible queries |

---

## Career Milestones (Personal)

| Week | Career Action |
|------|---------------|
| 1 | Post on LinkedIn: "I'm building an AI orchestration platform" |
| 2 | Share architecture diagram |
| 4 | Share first working execution demo |
| 6 | Post Kafka distributed execution deep-dive |
| 8 | Share AI plugin demo (GPT + Claude) |
| 10 | Share API design decisions |
| 12 | Full launch post — GitHub, demo video |
| 12+ | Start applying for Senior Java/Backend roles |
| 12+ | Use project as system design interview example |

---

## Success Metrics

| Metric | Target |
|--------|--------|
| GitHub Stars | 100+ |
| LinkedIn post views | 10K+ |
| Test Coverage | 80%+ |
| Concurrent executions | 100+ |
| Documentation pages | 15 |
| Plugins built | 10+ |
| Interview calls | 5+ |
