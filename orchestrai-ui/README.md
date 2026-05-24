# orchestrai-ui

Next.js dashboard for OrchestrAI.

## TODO (Week 11 — see docs/13-roadmap.md)

- [ ] `src/lib/api.ts` — REST client
- [ ] `src/lib/sse.ts` — log + token SSE streams
- [ ] `src/components/Providers.tsx` — TanStack Query provider
- [ ] `/flows` — YAML editor
- [ ] `/executions` — history + live logs
- [ ] `/metrics` — cost dashboard

## Run

```bash
pnpm install
pnpm dev
```

Env: `NEXT_PUBLIC_API_URL=http://localhost:8080/v1`
