import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import { useAuth } from '../context/AuthContext'
import PageLayout from '../components/PageLayout'
import AdBanner from '../components/AdBanner'

interface F1SessionItem {
  id: string
  sessionName: string
  meetingName: string
  dateStart: string
}

interface ArticleCard {
  id: string
  title: string
  slug: string
  summary: string
  coverImageUrl: string | null
  category: string
  game: string | null
  authorDisplayName: string
  publishedAt: string
  featured: boolean
}

interface MatchItem {
  id: string
  game: string
  league: { id: string; name: string; logoUrl: string | null }
  teamA: { id: string; name: string; logoUrl: string | null }
  teamB: { id: string; name: string; logoUrl: string | null }
  format: string
  stage: string
  startsAt: string
  status: string
  result: { winnerTeamId: string | null; score: string | null } | null
}

const categoryLabels: Record<string, string> = {
  NEWS: 'Správy',
  ANALYSIS: 'Analýza',
  TIPS: 'Tipy',
}

const getCategoryLabel = (cat: string, t: (key: string, opts?: Record<string, unknown>) => string) => {
  const map: Record<string, string> = { NEWS: t('home.správy'), ANALYSIS: t('home.analýza'), TIPS: t('home.tipy') }
  return map[cat] || cat
}

const categoryColors: Record<string, string> = {
  NEWS: 'bg-blue-500/20 text-blue-400',
  ANALYSIS: 'bg-purple-500/20 text-purple-400',
  TIPS: 'bg-green-500/20 text-green-400',
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatDate(iso: string, t: (key: string, opts?: Record<string, unknown>) => string) {
  const d = new Date(iso)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const tomorrow = new Date(today.getTime() + 86400000)
  const dStart = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  if (dStart.getTime() === today.getTime()) return t('home.dnes')
  if (dStart.getTime() === tomorrow.getTime()) return t('home.zajtra')
  return d.toLocaleDateString('sk-SK', { day: 'numeric', month: 'short' })
}

function timeAgo(iso: string, t: (key: string, opts?: Record<string, unknown>) => string) {
  const diff = Date.now() - new Date(iso).getTime()
  const hours = Math.floor(diff / 3600000)
  if (hours < 1) return t('home.prave_teraz')
  if (hours < 24) return t('home.pred_h', { hours })
  const days = Math.floor(hours / 24)
  if (days < 7) return t('home.pred_d', { days })
  return new Date(iso).toLocaleDateString('sk-SK', { day: 'numeric', month: 'short' })
}

export default function HomePage() {
  const { t, i18n } = useTranslation()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [featured, setFeatured] = useState<ArticleCard[]>([])
  const [recentArticles, setRecentArticles] = useState<ArticleCard[]>([])
  const [upcomingMatches, setUpcomingMatches] = useState<MatchItem[]>([])
  const [liveMatches, setLiveMatches] = useState<MatchItem[]>([])
  const [f1Upcoming, setF1Upcoming] = useState<F1SessionItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    ;(async () => {
      try {
        const lang = i18n.language
        const [featuredRes, articlesRes, matchesRes, liveRes] = await Promise.all([
          api.get('/api/articles/featured', { params: { language: lang } }),
          api.get('/api/articles', { params: { page: 0, size: 9, language: lang } }),
          api.get('/api/matches', { params: { status: 'SCHEDULED' } }),
          api.get('/api/matches', { params: { status: 'LIVE' } }),
        ])
        setFeatured(featuredRes.data ?? [])
        setRecentArticles(articlesRes.data?.content ?? [])
        const matches: MatchItem[] = matchesRes.data ?? []
        matches.sort((a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime())
        setUpcomingMatches(matches.slice(0, 3))
        setLiveMatches(liveRes.data ?? [])
        const f1Res = await api.get('/api/f1/sessions/upcoming', { params: { limit: 2 } })
        setF1Upcoming(f1Res.data ?? [])
      } catch {
        setFeatured([])
        setRecentArticles([])
        setUpcomingMatches([])
        setLiveMatches([])
        setF1Upcoming([])
      } finally {
        setLoading(false)
      }
    })()
  }, [i18n.language])
  
  const adBannerUrl = import.meta.env.VITE_AD_BANNER_URL

  const heroArticles = featured.length >= 3 ? featured : [...featured, ...recentArticles].slice(0, 3)
  const heroMain = heroArticles[0]
  const heroSide = heroArticles.slice(1, 3)
  const remainingArticles = recentArticles.filter(
    (a) => a.id !== heroMain?.id && !heroSide.find((s) => s.id === a.id)
  )

  return (
    <PageLayout>
      <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
        {loading ? (
          /* Skeleton */
          <div className="grid grid-cols-1 lg:grid-cols-5 gap-4 mb-10">
            <div className="lg:col-span-3 rounded-xl bg-surface border border-border overflow-hidden animate-skeleton">
              <div className="aspect-[16/9] bg-surface-elevated" />
              <div className="p-4 space-y-2">
                <div className="h-3 w-16 bg-surface-elevated rounded" />
                <div className="h-5 w-3/4 bg-surface-elevated rounded" />
              </div>
            </div>
            <div className="lg:col-span-2 space-y-4">
              {[1, 2].map((i) => (
                <div key={i} className="rounded-xl bg-surface border border-border overflow-hidden animate-skeleton">
                  <div className="aspect-[16/7] bg-surface-elevated" />
                  <div className="p-3 space-y-2">
                    <div className="h-3 w-14 bg-surface-elevated rounded" />
                    <div className="h-4 w-full bg-surface-elevated rounded" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : heroMain ? (
          <>
            {/* Hero section */}
            <div className="grid grid-cols-1 lg:grid-cols-5 gap-4 mb-10">
              <Link
                to={`/articles/${heroMain.slug}`}
                className="lg:col-span-3 rounded-xl bg-surface border border-border overflow-hidden group hover:border-accent-primary/40 transition"
              >
                <div className="aspect-[16/9] bg-surface-elevated relative overflow-hidden">
                  {heroMain.coverImageUrl ? (
                    <img
                      src={heroMain.coverImageUrl}
                      alt={heroMain.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition duration-500"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-text-secondary text-4xl">📰</div>
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
                  <div className="absolute top-3 left-3 flex items-center gap-2">
                    <span className={`badge text-[10px] ${categoryColors[heroMain.category] || 'bg-gray-500/20 text-gray-400'}`}>
                      {getCategoryLabel(heroMain.category, t)}
                    </span>
                    {heroMain.game && (
                      <span className="badge text-[10px] bg-accent-primary/20 text-accent-primary">{heroMain.game}</span>
                    )}
                  </div>
                  <div className="absolute bottom-0 left-0 right-0 p-5">
                    <p className="text-xs text-white/60 mb-1">
                      {new Date(heroMain.publishedAt).toLocaleDateString('sk-SK', { day: 'numeric', month: 'long', year: 'numeric' })}
                    </p>
                    <h2 className="text-2xl font-bold text-white leading-tight mb-1">{heroMain.title}</h2>
                    {heroMain.summary && (
                      <p className="text-sm text-white/80 line-clamp-2 hidden sm:block">{heroMain.summary}</p>
                    )}
                    <span className="inline-block mt-2 text-sm text-accent-primary font-semibold">{t('home.citat_viac')}</span>
                  </div>
                </div>
              </Link>

              <div className="lg:col-span-2 space-y-4">
                {heroSide.map((article) => (
                  <Link
                    key={article.id}
                    to={`/articles/${article.slug}`}
                    className="block rounded-xl bg-surface border border-border overflow-hidden group hover:border-accent-primary/40 transition"
                  >
                    <div className="aspect-[16/7] bg-surface-elevated relative overflow-hidden">
                      {article.coverImageUrl ? (
                        <img
                          src={article.coverImageUrl}
                          alt={article.title}
                          className="w-full h-full object-cover group-hover:scale-105 transition duration-500"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-text-secondary text-3xl">📰</div>
                      )}
                      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent" />
                      <div className="absolute top-2 left-2">
                        <span className={`badge text-[10px] ${categoryColors[article.category] || 'bg-gray-500/20 text-gray-400'}`}>
                          {getCategoryLabel(article.category, t)}
                        </span>
                      </div>
                      <div className="absolute bottom-0 left-0 right-0 p-3">
                        <p className="text-xs text-white/60 mb-0.5">
                          {new Date(article.publishedAt).toLocaleDateString('sk-SK', { day: 'numeric', month: 'short' })}
                        </p>
                        <h3 className="text-sm font-bold text-white leading-snug line-clamp-2">{article.title}</h3>
                        {article.summary && (
                          <p className="text-xs text-white/70 line-clamp-1 mt-0.5">{article.summary}</p>
                        )}
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            </div>

            <div style={{ margin: '16px 0' }}>
              <AdBanner size="horizontal" index={0} />
            </div>

            {/* Three columns below hero */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              {/* Left column — Live matches */}
              <div className="lg:col-span-3">
                <div className="flex items-center gap-2 mb-4">
                  <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
                  <h2 className="text-lg font-bold text-text-primary">{t('home.live_zapasy')}</h2>
                </div>

                {/* F1 Upcoming widget */}
                {f1Upcoming.length > 0 && (
                  <div className="mb-6">
                    <h3 className="text-sm font-semibold text-text-primary mb-3">🏎️ F1 {t('home.nadchadzajuce')}</h3>
                    <div className="space-y-2">
                      {f1Upcoming.map((s) => (
                        <Link
                          key={s.id}
                          to={`/f1/sessions/${s.id}`}
                          className="flex items-center gap-2 bg-surface border border-border rounded-xl px-3 py-2 hover:bg-surface-elevated/50 transition"
                        >
                          <span className="text-lg">🏎️</span>
                          <div className="flex-1 min-w-0">
                            <p className="text-xs font-medium truncate">{s.sessionName}</p>
                            <p className="text-[10px] text-text-secondary truncate">{s.meetingName}</p>
                          </div>
                          <span className="text-[10px] text-text-secondary shrink-0">
                            {new Date(s.dateStart).toLocaleDateString('sk-SK', { day: 'numeric', month: 'short' })}
                          </span>
                        </Link>
                      ))}
                    </div>
                    <Link to="/f1" className="text-xs text-accent-primary hover:underline mt-2 inline-block">
                      Zobraziť všetko →
                    </Link>
                  </div>
                )}
                {liveMatches.length > 0 ? (
                  <div className="space-y-2 mb-6">
                    {liveMatches.map((m) => (
                      <div
                        key={m.id}
                        onClick={() => navigate(`/matches/${m.id}`)}
                        className="bg-surface border border-border rounded-xl p-3 hover:bg-surface-elevated/50 transition cursor-pointer"
                      >
                        <p className="text-[10px] text-text-secondary truncate mb-1">{m.league.name}</p>
                        <div className="flex items-center justify-between gap-2">
                          <span className="text-xs font-medium truncate max-w-[80px]">{m.teamA.name}</span>
                          <span className="text-sm font-bold text-accent-primary shrink-0">{m.result?.score || 'vs'}</span>
                          <span className="text-xs font-medium truncate max-w-[80px]">{m.teamB.name}</span>
                        </div>
                        <div className="mt-1 text-center">
                          <span className="text-[10px] text-text-secondary bg-surface-elevated border border-border rounded px-1.5 py-0.5">{m.format}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex items-center gap-2 bg-surface border border-border rounded-xl p-4 mb-6">
                    <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
                    <p className="text-sm text-text-secondary">{t('home.ziadne_live')}</p>
                  </div>
                )}

                <h3 className="text-sm font-semibold text-text-primary mb-3">{t('home.nadchadzajuce')}</h3>
                {upcomingMatches.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-8 text-text-secondary bg-surface border border-border rounded-xl">
                    <span className="text-2xl mb-1">📅</span>
                    <p className="text-xs">{t('home.ziadne_zapasy')}</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {upcomingMatches.map((m) => (
                      <div
                        key={m.id}
                        onClick={() => navigate(`/matches/${m.id}`)}
                        className="flex items-center gap-2 bg-surface border border-border rounded-xl px-3 py-2 hover:bg-surface-elevated/50 transition cursor-pointer"
                      >
                        <div className="text-center w-9 shrink-0">
                          <p className="text-xs font-bold text-accent-primary leading-tight">{formatTime(m.startsAt)}</p>
                          <p className="text-[9px] text-text-secondary leading-tight">{formatDate(m.startsAt, t)}</p>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-[10px] text-text-secondary truncate">{m.league.name}</p>
                          <p className="text-xs font-medium truncate">{m.teamA.name} vs {m.teamB.name}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Center column — Articles */}
              <div className="lg:col-span-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-bold text-text-primary">{t('home.dalsie_clanky')}</h2>
                  <Link to="/articles" className="text-sm text-accent-primary hover:underline">{t('home.vsetky_clanky')}</Link>
                </div>
                {remainingArticles.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-text-secondary bg-surface border border-border rounded-xl">
                    <span className="text-3xl mb-2">📰</span>
                    <p className="text-sm">{t('home.ziadne_clanky')}</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {remainingArticles.slice(0, 6).map((article) => (
                      <Link
                        key={article.id}
                        to={`/articles/${article.slug}`}
                        className="flex gap-3 bg-surface border border-border rounded-xl p-3 card-hover group"
                      >
                        <div className="w-20 h-20 shrink-0 rounded-lg bg-surface-elevated overflow-hidden">
                          {article.coverImageUrl ? (
                            <img
                              src={article.coverImageUrl}
                              alt={article.title}
                              className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                            />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-text-secondary text-xl">📰</div>
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <span className={`badge text-[10px] ${categoryColors[article.category] || 'bg-gray-500/20 text-gray-400'}`}>
                              {categoryLabels[article.category] || article.category}
                            </span>
                            <span className="text-[10px] text-text-secondary">{timeAgo(article.publishedAt, t)}</span>
                          </div>
                          <h3 className="text-sm font-semibold text-text-primary line-clamp-2">{article.title}</h3>
                          {article.summary && (
                            <p className="text-xs text-text-secondary mt-0.5 line-clamp-1">{article.summary}</p>
                          )}
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </div>

              {/* Right column — Ad banner */}
              <div className="lg:col-span-3">
                <p className="text-xs text-text-secondary mb-3 font-medium uppercase tracking-wider">{t('home.partneri')}</p>
                {adBannerUrl ? (
                  <img src={adBannerUrl} alt={t('home.reklamny_priestor')} style={{ width: '100%', borderRadius: '8px' }} />
                ) : (
                  <div style={{ border: '2px dashed #1e2d45', background: '#111827', borderRadius: '8px', padding: '32px', textAlign: 'center' }}>
                    <p className="text-sm text-text-secondary">{t('home.reklamny_priestor')}</p>
                    <p className="text-xs text-text-secondary mt-1">{t('home.kontakt_reklama')}</p>
                  </div>
                )}
                {/* Second smaller ad unit */}
                <div style={{ border: '2px dashed #1e2d45', background: '#111827', borderRadius: '8px', padding: '24px', textAlign: 'center', height: '150px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', marginTop: '16px' }}>
                  <p className="text-sm text-text-secondary">{t('home.reklamny_priestor')}</p>
                  <p className="text-xs text-text-secondary mt-1">{t('home.kontakt_reklama')}</p>
                </div>
              </div>
            </div>

            {/* Bottom CTA for non-logged-in users */}
            {!user && (
              <div className="mt-12 rounded-xl bg-surface border border-accent-primary/20 p-8 text-center">
                <h2 className="text-2xl font-bold text-text-primary mb-2">{t('home.cta_nadpis')}</h2>
                <p className="text-text-secondary mb-6 max-w-md mx-auto">{t('home.cta_text')}</p>
                <div className="flex items-center justify-center gap-3">
                  <Link to="/register" className="btn-primary">{t('home.registrovat')}</Link>
                  <Link to="/login" className="btn-secondary">{t('home.prihlasit')}</Link>
                </div>
              </div>
            )}
          </>
        ) : (
          /* Empty state — no articles at all */
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <span className="text-5xl mb-4">🎮</span>
            <h1 className="text-2xl font-bold text-text-primary mb-2">{t('home.welcome')}</h1>
            <p className="text-text-secondary mb-6">{t('home.subtitle')}</p>
            {!user && (
              <div className="flex gap-3">
                <Link to="/login" className="btn-primary">{t('auth.login')}</Link>
                <Link to="/register" className="btn-secondary">{t('auth.register')}</Link>
              </div>
            )}
          </div>
        )}
      </div>
    </PageLayout>
  )
}
