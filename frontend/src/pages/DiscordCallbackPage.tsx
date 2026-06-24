import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function DiscordCallbackPage() {
  const navigate = useNavigate()
  const { refreshUser } = useAuth()

  useEffect(() => {
    refreshUser().then(() => {
      navigate('/dashboard', { replace: true })
    }).catch(() => {
      navigate('/login', { replace: true })
    })
  }, [refreshUser, navigate])

  return (
    <div className="min-h-screen bg-bg flex items-center justify-center px-4">
      <div className="text-center">
        <div className="w-8 h-8 border-2 border-accent-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-text-secondary text-sm">Completing Discord login...</p>
      </div>
    </div>
  )
}
