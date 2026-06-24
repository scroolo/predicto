import { Navigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'

interface AdminRouteProps {
  children: React.ReactNode
  requiredRole?: 'ADMIN' | 'EDITOR'
}

export default function AdminRoute({ children, requiredRole }: AdminRouteProps) {
  const { t } = useTranslation()
  const { user, loading } = useAuth()

  if (loading) return <div className="flex items-center justify-center min-h-screen text-gray-400">{t('common.nacitavam')}</div>
  if (!user) return <Navigate to="/login" replace />

  if (!user || (user.role !== 'ADMIN' && user.role !== 'EDITOR')) return <Navigate to="/" replace />

  if (requiredRole === 'ADMIN' && user.role !== 'ADMIN') return <Navigate to="/admin/articles" replace />

  return <>{children}</>
}
