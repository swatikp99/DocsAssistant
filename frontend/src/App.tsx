import heroImg from './assets/hero.png'
import './App.css'
import { HealthBadge } from './components/HealthBadge'
import { DocumentUploader } from './components/DocumentUploader'
import { DocumentList } from './components/DocumentList'

function App() {
  return (
    <div className="min-h-screen w-full bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-6 py-4">
          <h1 className="text-lg font-bold tracking-tight">DocsAssistant: Your AI-powered document companion</h1>
          <HealthBadge />
        </div>
      </header>

      <main className="mx-auto flex max-w-3xl flex-col gap-8 px-6 py-10">
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Upload</h2>
          <DocumentUploader />
        </section>
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Library</h2>
          <DocumentList />
        </section>
      </main>
    </div>
  )
}

export default App
