/**
 * TODO: Plugins page — Plugin catalog from GET /plugins
 */
import Link from 'next/link';

export default function Page() {
  return (
    <main className="mx-auto max-w-5xl p-8">
      <Link href="/" className="text-sm text-zinc-500 hover:underline">← Home</Link>
      <h1 className="mt-4 text-2xl font-semibold">Plugins</h1>
      <p className="mt-2 text-zinc-500">TODO: Implement this page. Plugin catalog from GET /plugins</p>
      {/* TODO: plugin catalog grid */}
      <div className="mt-6 rounded border border-dashed p-8 text-center text-zinc-400">
        Placeholder — assign to frontend developer
      </div>
    </main>
  );
}
