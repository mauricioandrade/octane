import { useState, type FormEvent } from 'react'
import { Fuel } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/context/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
    } catch {
      setError('Usuário ou senha inválidos')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 dark:bg-slate-950">
      <div className="w-full max-w-sm rounded-xl border bg-white dark:bg-slate-900 p-8 shadow-sm">
        <div className="mb-6 flex items-center gap-2">
          <Fuel size={24} className="text-orange-600" />
          <span className="text-xl font-extrabold tracking-tight text-orange-600">Octane</span>
        </div>
        <h1 className="mb-1 text-lg font-bold text-slate-900 dark:text-slate-100">Entrar</h1>
        <p className="mb-6 text-sm text-slate-500 dark:text-slate-400">
          Acesse o sistema de gestão do posto
        </p>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <Label htmlFor="username">Usuário</Label>
            <Input
              id="username"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
          </div>
          <div>
            <Label htmlFor="password">Senha</Label>
            <Input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
          </div>
          {error && <p className="text-sm text-red-500">{error}</p>}
          <Button
            type="submit"
            disabled={loading || !username || !password}
            className="bg-orange-600 hover:bg-orange-700"
          >
            {loading ? 'Entrando…' : 'Entrar'}
          </Button>
        </form>
      </div>
    </div>
  )
}
