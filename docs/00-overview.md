# 00 — Project Overview

## What is OrchestrAI?

OrchestrAI is an **AI Agent Orchestration Platform** that allows developers to define, execute, and monitor multi-step AI workflows using a simple YAML-based DSL (Domain Specific Language).

Think of it as **"GitHub Actions for AI Agents"** — but more powerful, distributed, and built for production AI systems.

---

## The Problem

Building AI-powered applications today requires:

1. Calling multiple LLMs (GPT-4, Claude, Gemini)
2. Chaining outputs between agents
3. Handling failures, retries, and fallbacks
4. Tracking tokens, costs, and performance
5. Adding human approval steps
6. Scheduling and triggering workflows

**Current solutions are fragmented:**

- LangChain → Python only, code-heavy
- Custom scripts → Hard to maintain, not reusable
- Generic orchestrators (Airflow, Kestra) → Not AI-first

**Result:** Teams spend 60% of time on plumbing, not AI logic.

---

## The Solution

OrchestrAI provides:

- **Declarative YAML** — Define workflows, not write code
- **Plugin Architecture** — Add any LLM or tool
- **Distributed Execution** — Scale with Kafka
- **Built-in Observability** — Logs, metrics, traces
- **Cost Tracking** — Know what every workflow costs
- **Human-in-the-Loop** — Approval steps when needed

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
