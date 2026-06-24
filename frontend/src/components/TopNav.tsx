import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import LanguageSwitcher from './LanguageSwitcher'

export default function TopNav() {
  const { t } = useTranslation()
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [expandedSection, setExpandedSection] = useState<string | null>(null)

  const rankColor = (badge: string | null) => {
    const map: Record<string, string> = {
      ROOKIE: 'text-rank-rookie', SILVER: 'text-rank-silver', GOLD: 'text-rank-gold',
      DIAMOND: 'text-rank-diamond', MASTER: 'text-rank-master', CHALLENGER: 'text-rank-challenger',
    }
    return map[badge ?? ''] || 'text-text-secondary'
  }

  const navItems = [
    { label: t('nav.domov'), to: '/', dropdown: null },
    {
      label: t('nav.clanky'), to: '/articles', dropdown: [
        { label: t('nav.novinky'), to: '/articles?category=NEWS' },
        { label: t('nav.analyzy'), to: '/articles?category=ANALYSIS' },
        { label: t('nav.tipy'), to: '/articles?category=TIPS' },
      ],
    },
    {
      label: t('nav.zapasy'), to: '/matches', dropdown: [
        { label: t('nav.lol'), to: '/matches?game=LOL' },
        { label: t('nav.cs2'), to: '/matches?game=CS2' },
        { label: t('nav.live'), to: '/matches?status=LIVE' },
      ],
    },
    { label: t('nav.f1'), to: '/f1', dropdown: null },
    { label: t('nav.leaderboard'), to: '/leaderboard', dropdown: null },
    {
      label: t('nav.predikcie'), to: '#', dropdown: [
        { label: t('nav.moje_predikcie'), to: '/predictions' },
      ],
    },
  ]

  const navLink = (
    <div className="flex items-center gap-1">
      {navItems.map((item) => {
        if (!user && item.label === t('nav.predikcie')) return null
        if (item.dropdown) {
          return (
            <div key={item.label} className="relative group">
              <Link
                to={item.to}
                className="px-3 py-2 text-sm text-text-secondary hover:text-text-primary transition rounded-lg block"
              >
                {item.label}
              </Link>
              <div className="absolute top-full left-0 pt-1 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150 translate-y-[-4px] group-hover:translate-y-0 z-50">
                <div className="bg-[#1a2235] border border-[#1e2d45] rounded-lg shadow-[0_8px_32px_rgba(0,0,0,0.4)] py-1 min-w-[180px]">
                  {item.dropdown.map((sub) => (
                    <Link
                      key={sub.to}
                      to={sub.to}
                      className="flex items-center gap-2 px-4 py-2.5 text-sm text-[#f0f4ff] hover:bg-[#22263a] transition"
                    >
                      {sub.label}
                    </Link>
                  ))}
                </div>
              </div>
            </div>
          )
        }
        return (
          <Link
            key={item.label}
            to={item.to}
            className="px-3 py-2 text-sm text-text-secondary hover:text-text-primary transition rounded-lg"
          >
            {item.label}
          </Link>
        )
      })}
    </div>
  )

  const drawerNav = (
    <div className="h-full flex flex-col bg-[#0a0e1a]">
      <div className="flex items-center justify-between px-5 h-14 border-b border-[#1e2d45] shrink-0">
        <span className="text-lg font-bold text-accent-primary">Predicto</span>
        <button onClick={() => setDrawerOpen(false)} className="btn-ghost p-1 text-xl">×</button>
      </div>
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        {navItems.map((item) => {
          if (!user && item.label === t('nav.predikcie')) return null
          if (item.dropdown) {
            const open = expandedSection === item.label
            return (
              <div key={item.label}>
                <button
                  onClick={() => setExpandedSection(open ? null : item.label)}
                  className="w-full flex items-center justify-between px-4 py-2.5 rounded-lg text-sm text-text-secondary hover:text-text-primary hover:bg-surface-elevated/50 transition"
                >
                  <span>{item.label}</span>
                  <span className={`transition-transform duration-150 ${open ? 'rotate-180' : ''}`}>▾</span>
                </button>
                {open && (
                  <div className="ml-2 mt-1 space-y-1">
                    {item.dropdown.map((sub) => (
                      <Link
                        key={sub.to}
                        to={sub.to}
                        onClick={() => setDrawerOpen(false)}
                        className="block px-4 py-2 rounded-lg text-sm text-[#f0f4ff] hover:bg-[#22263a] transition"
                      >
                        {sub.label}
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            )
          }
          return (
            <Link
              key={item.label}
              to={item.to}
              onClick={() => setDrawerOpen(false)}
              className="block px-4 py-2.5 rounded-lg text-sm text-text-secondary hover:text-text-primary hover:bg-surface-elevated/50 transition"
            >
              {item.label}
            </Link>
          )
        })}
      </div>
      {user && (
        <div className="border-t border-[#1e2d45] p-4">
          <div className="flex items-center gap-3 mb-3">
            {user.avatarUrl ? (
              <img src={user.avatarUrl} alt={user.username} style={{ width: '32px', height: '32px', borderRadius: '50%', objectFit: 'cover' }}
                onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
            ) : (
              <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: '#e10600', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '14px' }}>
                {user.username?.charAt(0).toUpperCase()}
              </div>
            )}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">{user.username}</p>
              <p className={`text-xs font-mono ${rankColor(user.badge)}`}>{user.badge ?? 'UNRANKED'}</p>
            </div>
          </div>
          <Link to="/profile" onClick={() => setDrawerOpen(false)} className="block text-xs text-text-secondary hover:text-text-primary py-1">{t('nav.moj_profil')}</Link>
          <Link to="/settings" onClick={() => setDrawerOpen(false)} className="block text-xs text-text-secondary hover:text-text-primary py-1">{t('nav.nastavenia')}</Link>
          {(user.role === 'ADMIN' || user.role === 'EDITOR') && (
            <Link to={user.role === 'EDITOR' ? '/admin/articles' : '/admin/matches'} onClick={() => setDrawerOpen(false)} className="block text-xs text-text-secondary hover:text-text-primary py-1">{t('nav.admin_panel')}</Link>
          )}
          <button onClick={() => { logout(); setDrawerOpen(false) }} className="text-xs text-text-secondary hover:text-text-primary py-1">{t('nav.odhlasit')}</button>
        </div>
      )}
    </div>
  )

  return (
    <>
      <header className="fixed top-0 left-0 right-0 z-40 h-16 bg-[#0a0e1a] border-b border-[#1e2d45] flex items-center justify-between px-4 lg:px-8">
        <div className="flex items-center gap-3">
          <Link to="/" className="flex items-center gap-2 shrink-0">
            <img src="/logo.png" alt="Predicto" className="h-9 w-auto" />
            <span className="text-lg font-bold text-white hidden sm:inline">{t('nav.logo_text')}</span>
          </Link>
        </div>

        <nav className="hidden md:flex items-center">
          {navLink}
        </nav>

        <div className="flex items-center gap-3">
          <LanguageSwitcher />
          {!user ? (
            <>
              <Link to="/login" className="text-sm text-text-secondary hover:text-text-primary transition px-3 py-2">{t('nav.prihlasit')}</Link>
              <Link to="/register" className="btn-primary text-sm !px-4 !py-2">{t('nav.registrovat')}</Link>
            </>
          ) : (
            <div className="flex items-center gap-3">
              <span className="text-sm font-mono text-accent-primary bg-accent-glow rounded-full px-3 py-0.5 hidden sm:inline">
                🪙 {(user?.wallet?.balance ?? 0).toLocaleString()}
              </span>
              <div className="relative group">
                <div className="flex items-center gap-2 cursor-pointer">
                  {user?.avatarUrl ? (
                      <img src={user.avatarUrl} alt={user.username} style={{ width: '32px', height: '32px', borderRadius: '50%', objectFit: 'cover' }}
                        onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
                    ) : (
                      <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: '#e10600', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '14px' }}>
                        {user?.username?.charAt(0).toUpperCase()}
                      </div>
                    )}
                  <span className="text-sm font-medium hidden sm:inline">{user?.username}</span>
                </div>
                <div className="absolute top-full right-0 pt-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150 translate-y-[-4px] group-hover:translate-y-0 z-50">
                  <div className="bg-[#1a2235] border border-[#1e2d45] rounded-lg shadow-[0_8px_32px_rgba(0,0,0,0.4)] py-1 min-w-[160px]">
                    <Link to="/profile" className="block px-4 py-2.5 text-sm text-[#f0f4ff] hover:bg-[#22263a] transition">{t('nav.moj_profil')}</Link>
                    <Link to="/settings" className="block px-4 py-2.5 text-sm text-[#f0f4ff] hover:bg-[#22263a] transition">{t('nav.nastavenia')}</Link>
                    {(user?.role === 'ADMIN' || user?.role === 'EDITOR') && (
                      <Link to={user?.role === 'EDITOR' ? '/admin/articles' : '/admin/matches'} className="block px-4 py-2.5 text-sm text-[#f0f4ff] hover:bg-[#22263a] transition">{t('nav.admin_panel')}</Link>
                    )}
                    <button onClick={logout} className="w-full text-left px-4 py-2.5 text-sm text-[#f0f4ff] hover:bg-[#22263a] transition">{t('nav.odhlasit')}</button>
                  </div>
                </div>
              </div>
            </div>
          )}
          <button onClick={() => setDrawerOpen(true)} className="md:hidden btn-ghost p-1 text-xl ml-1">☰</button>
        </div>
      </header>

      {/* Mobile drawer */}
      {drawerOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div className="absolute inset-0 bg-black/60" onClick={() => setDrawerOpen(false)} />
          <aside className="relative w-72 h-full shadow-2xl overflow-y-auto">
            {drawerNav}
          </aside>
        </div>
      )}
    </>
  )
}
