/**
 * TODO: Add <Providers> wrapper with QueryClientProvider
 * Update metadata title/description for OrchestrAI
 */
import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';
// TODO: import { Providers } from '@/components/Providers';

const geistSans = Geist({ variable: '--font-geist-sans', subsets: ['latin'] });
const geistMono = Geist_Mono({ variable: '--font-geist-mono', subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'OrchestrAI',
  description: 'AI Agent Orchestration Platform',
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col bg-white text-zinc-900 dark:bg-zinc-950 dark:text-zinc-50">
        {/* TODO: wrap with <Providers>{children}</Providers> */}
        {children}
      </body>
    </html>
  );
}
