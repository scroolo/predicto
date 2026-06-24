import { Navigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { t } = useTranslation()
  const { user, loading } = useAuth()
  if (loading) return <div className="flex items-center justify-center min-h-screen text-gray-400">{t('common.nacitavam')}</div>
  if (!user) return <Navigate to="/login" replace />
  return <>{children}</>
}
