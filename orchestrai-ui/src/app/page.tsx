/**
 * TODO: OrchestrAI dashboard home — nav links to flows, executions, plugins, metrics
 * Stack: Next.js, TanStack Query, Tailwind, SSE (@microsoft/fetch-event-source)
 */
import Link from 'next/link';

export default function Home() {
  const links = [
    { href: '/flows', label: 'Flows', desc: 'Create and edit YAML workflows' },
    { href: '/executions', label: 'Executions', desc: 'Run history and live logs' },
    { href: '/plugins', label: 'Plugins', desc: 'LLM and integration catalog' },
    { href: '/metrics', label: 'Metrics', desc: 'Token usage and cost dashboard' },
  ];

  return (
    <main className="mx-auto flex min-h-screen max-w-3xl flex-col justify-center gap-8 p-8">
      <div>
        <h1 className="text-3xl font-bold">OrchestrAI</h1>
        <p className="mt-2 text-zinc-500">AI Agent Orchestration Platform — UI scaffold</p>
      </div>
      <ul className="grid gap-4 sm:grid-cols-2">
        {links.map((l) => (
          <li key={l.href}>
            <Link
              href={l.href}
              className="block rounded-lg border p-4 hover:bg-zinc-50 dark:hover:bg-zinc-900"
            >
              <span className="font-medium">{l.label}</span>
              <span className="mt-1 block text-sm text-zinc-500">{l.desc}</span>
            </Link>
          </li>
        ))}
      </ul>
      <p className="text-xs text-zinc-400">TODO: Wire TanStack Query + API client — see src/lib/api.ts</p>
    </main>
  );
}
