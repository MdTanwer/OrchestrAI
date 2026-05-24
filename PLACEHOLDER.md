# OrchestrAI — Developer placeholder guide

This repository is a **scaffold**. Every empty or stub file contains `TODO` comments describing what to implement.

## Start here

1. `docs/README.md` — full documentation index
2. `docs/13-roadmap.md` — 12-week MVP plan
3. `docs/05-architecture.md` — system design

## Module map

| Module | Role |
|--------|------|
| orchestrai-core | Domain models |
| orchestrai-yaml-parser | YAML → Flow |
| orchestrai-engine | Execution logic (library) |
| orchestrai-executor | Kafka brain (deployable) |
| orchestrai-api-server | REST + SSE |
| orchestrai-worker | Task execution |
| orchestrai-scheduler | Cron + Kafka triggers |
| orchestrai-jdbc | PostgreSQL |
| orchestrai-messaging | Kafka DTOs |
| orchestrai-plugin-sdk | Plugin contract |
| orchestrai-plugins | Built-in plugins |
| orchestrai-ui | Next.js dashboard |
| orchestrai-cli | CLI tool |

## Reference

`kestra/` is gitignored local reference only — do not copy blindly; OrchestrAI uses Quarkus + Kafka.
