import { ReactNode } from 'react'
import { Navigate, Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import TopNav from './TopNav'

export default function AdminLayout({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  const { user, loading } = useAuth()
  const location = useLocation()

  const adminTabs = [
    { to: '/admin/matches', label: t('admin.zapasy'), roles: ['ADMIN'] },
    { to: '/admin/seasons', label: t('admin.sezony'), roles: ['ADMIN'] },
    { to: '/admin/articles', label: t('admin.clanky_tab'), roles: ['ADMIN', 'EDITOR'] },
    { to: '/admin/leaderboard', label: t('admin.leaderboard_tab'), roles: ['ADMIN'] },
    { to: '/admin/predictions', label: 'Predikcie', roles: ['ADMIN'] },
    { to: '/admin/f1', label: 'F1', roles: ['ADMIN'] },
    { to: '/admin/sync', label: t('admin.sync'), roles: ['ADMIN'] },
    { to: '/admin/users', label: t('admin.users'), roles: ['ADMIN'] },
    { to: '/admin/academy', label: 'Academy', roles: ['ADMIN'] },
  ]

  if (loading) return <div className="flex items-center justify-center min-h-screen bg-bg text-text-secondary">{t('common.nacitavam')}</div>
  if (!user || (user.role !== 'ADMIN' && user.role !== 'EDITOR')) return <Navigate to="/matches" replace />
  if (user.role === 'EDITOR' && !location.pathname.startsWith('/admin/articles')) return <Navigate to="/admin/articles" replace />

  return (
    <div className="min-h-screen bg-bg">
      <TopNav />
      <div className="pt-16">
        <div className="bg-[#111827] border-b border-[#1e2d45]">
          <div className="max-w-7xl mx-auto px-4 lg:px-8 flex items-center gap-1 overflow-x-auto">
            {adminTabs.filter(t => t.roles.includes(user.role)).map((tab) => {
              const active = location.pathname.startsWith(tab.to)
              return (
                <Link
                  key={tab.to}
                  to={tab.to}
                  className={`px-4 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition ${
                    active
                      ? 'border-accent-primary text-accent-primary'
                      : 'border-transparent text-text-secondary hover:text-text-primary'
                  }`}
                >
                  {tab.label}
                </Link>
              )
            })}
          </div>
        </div>
        <main className="p-5">
          {children}
        </main>
      </div>
    </div>
  )
}
