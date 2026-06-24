import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import api from '../lib/api'
import { AxiosError } from 'axios'

export default function RegisterPage() {
  const { t } = useTranslation()
  const { register } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (password.length < 4) { setError(t('auth.heslo_kratke')); return }
    setSubmitting(true)
    try {
      await register(username, password)
      navigate('/dashboard')
    } catch (err) {
      const axiosErr = err as AxiosError<{ message?: string }>
      setError(axiosErr.response?.data?.message || 'Registration failed')
    } finally { setSubmitting(false) }
  }

  const discordRegister = async () => {
    try {
      const res = await api.get('/api/auth/discord')
      window.location.href = res.data.url
    } catch {
      setError('Failed to initiate Discord registration')
    }
  }

  return (
    <div className="min-h-screen bg-bg flex items-center justify-center px-4 relative overflow-hidden">
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[400px] bg-accent-primary/10 rounded-full blur-[120px]" />
      <div className="w-full max-w-sm relative">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-accent-primary">Predicto</h1>
          <p className="text-sm text-text-secondary mt-1">{t('auth.tagline')}</p>
        </div>
        <form onSubmit={handleSubmit} className="card p-6 space-y-4">
          <h2 className="text-lg font-semibold">{t('auth.registracia')}</h2>
          {error && <div className="bg-red-900/30 border border-red-800/50 text-red-400 px-3 py-2 rounded text-sm">{error}</div>}
          <div>
            <label className="block text-xs text-text-secondary mb-1">{t('auth.username')}</label>
            <input className="input-field" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div>
            <label className="block text-xs text-text-secondary mb-1">{t('auth.password')}</label>
            <input type="password" className="input-field" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button type="submit" disabled={submitting} className="btn-primary w-full">{submitting ? t('auth.registrujem') : t('auth.registrovat_sa')}</button>
          <p className="text-xs text-text-secondary text-center">
            {t('auth.mas_ucet')} <Link to="/login" className="text-accent-primary hover:underline">{t('auth.login_link')}</Link>
          </p>
        </form>
        <div className="relative my-4">
          <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-border"></div></div>
          <div className="relative flex justify-center text-xs"><span className="bg-bg px-2 text-text-secondary">{t('auth.alebo')}</span></div>
        </div>
        <button onClick={discordRegister} className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium text-white transition" style={{background: '#5865F2'}}>
          <DiscordIcon />
          {t('auth.discord_registracia')}
        </button>
      </div>
    </div>
  )
}

function DiscordIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 127.14 96.36" width="18" height="18" fill="currentColor">
      <path d="M107.7,8.07A105.15,105.15,0,0,0,81.47,0a72.06,72.06,0,0,0-3.36,6.83A97.68,97.68,0,0,0,49,6.83,72.37,72.37,0,0,0,45.64,0,105.89,105.89,0,0,0,19.39,8.09C2.79,32.65-1.71,56.6.54,80.21h0A105.73,105.73,0,0,0,32.71,96.36,77.7,77.7,0,0,0,39.6,85.25a68.42,68.42,0,0,1-10.85-5.18c.91-.66,1.8-1.34,2.66-2a75.57,75.57,0,0,0,64.32,0c.87.71,1.76,1.39,2.66,2a68.68,68.68,0,0,1-10.87,5.19,77,77,0,0,0,6.89,11.1A105.25,105.25,0,0,0,126.6,80.22h0C129.24,52.84,122.09,29.11,107.7,8.07ZM42.45,65.69C36.18,65.69,31,60,31,53s5-12.74,11.43-12.74S54,46,53.89,53,48.84,65.69,42.45,65.69Zm42.24,0C78.41,65.69,73.25,60,73.25,53s5-12.74,11.44-12.74S96.14,46,96,53,91.08,65.69,84.69,65.69Z"/>
    </svg>
  )
}
