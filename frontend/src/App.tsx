import heroImg from './assets/hero.png'
import './App.css'
import { HealthBadge } from './components/HealthBadge'

function App() {
  return (
    <div className="min-h-screen w-full bg-slate-50 text-slate-900">
      <div className="mx-auto flex max-w-2xl flex-col items-center gap-6 px-6 py-24 text-center">
        <h1 className="text-4xl font-bold tracking-tight">
          DocsAssistant: Your AI-powered document companion
        </h1>
        <p className="max-w-md text-slate-600">
          A simple and easy-to-use multimodal knowledge tool to help you read and understand your documents.
        </p>
        <HealthBadge />
      </div>
    </div>
  )
}

export default App
