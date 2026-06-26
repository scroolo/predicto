import { useEffect, useState, useMemo } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'
import AdBanner from '../components/AdBanner'
import OddsButton from '../components/OddsButton'
import { SkeletonTable } from '../components/Skeleton'

interface LeagueItem { id: string; name: string; region: string; logoUrl: string | null }
interface TeamRef { id: string; name: string; logoUrl: string | null }
interface WinnerOddsItem { teamId: string; oddsValue: number }
interface ScoreOddsItem { scoreValue: string; oddsValue: number }
interface OddsData { matchId: string; winnerOdds: WinnerOddsItem[]; scoreOdds: ScoreOddsItem[] }
interface MatchItem {
  id: string; game: string
  league: { id: string; name: string; logoUrl: string | null }
  teamA: TeamRef; teamB: TeamRef; format: string; stage: string
  startsAt: string; status: string
  result: { winnerTeamId: string; score: string } | null
}

export default function DashboardPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const [matches, setMatches] = useState<MatchItem[]>([])
  const [oddsMap, setOddsMap] = useState<Record<string, OddsData>>({})
  const [leagues, setLeagues] = useState<LeagueItem[]>([])
  const [leagueFilter, setLeagueFilter] = useState('all')
  const game = searchParams.get('game') || ''
  const status = searchParams.get('status')?.toLowerCase() || ''
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    (async () => {
      try {
        const matchesParam = game ? `?game=${game}` : ''
        const leaguesParam = game ? `?game=${game}` : ''
        console.log('fetching leagues:', `/api/leagues${leaguesParam}`)
        const [mRes, lRes] = await Promise.all([api.get(`/api/matches${matchesParam}`), api.get(`/api/leagues${leaguesParam}`)])
        const data: MatchItem[] = mRes.data ?? []
        data.sort((a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime())
        setMatches(data)
        setLeagues(lRes.data ?? [])
        const oddsPromises = data.map(async (m) => {
          try {
            const oRes = await api.get(`/api/matches/${m.id}/odds`)
            return { id: m.id, data: oRes.data as OddsData }
          } catch { return null }
        })
        const results = await Promise.all(oddsPromises)
        const map: Record<string, OddsData> = {}
        results.forEach((r) => { if (r) map[r.id] = r.data })
        setOddsMap(map)
      } catch { /* ignore */ }
      setLoading(false)
    })()
  }, [searchParams])

  const formatTime = (iso: string) => {
    const d = new Date(iso)
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  const formatDateGroup = (iso: string) => {
    const d = new Date(iso)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const tomorrow = new Date(today.getTime() + 86400000)
    const dStart = new Date(d.getFullYear(), d.getMonth(), d.getDate())
    if (dStart.getTime() === today.getTime()) return t('matches.dnes')
    if (dStart.getTime() === tomorrow.getTime()) return t('matches.zajtra')
    return d.toLocaleDateString('en-US', { weekday: 'short', day: 'numeric', month: 'short' }).toUpperCase()
  }

  const formatDateSub = (iso: string) => {
    const d = new Date(iso)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const tomorrow = new Date(today.getTime() + 86400000)
    const dStart = new Date(d.getFullYear(), d.getMonth(), d.getDate())
    if (dStart.getTime() === today.getTime()) return ''
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  }

  const shortName = (name: string) => {
    const parts = name.split(' ')
    return parts.length > 1 ? parts[0] : name.substring(0, 8)
  }

  const grouped = useMemo(() => {
    const filtered = matches.filter((m) => {
      if (leagueFilter !== 'all' && m.league.id !== leagueFilter) return false
      if (status === 'live' && m.status !== 'LIVE') return false
      if (status === 'upcoming' && m.status !== 'SCHEDULED') return false
      if (status === 'finished' && m.status !== 'FINISHED') return false
      if (!status && (m.status === 'FINISHED' || m.status === 'CANCELLED')) return false
      if (game && m.game !== game) return false
      return true
    })
    const groups: Record<string, MatchItem[]> = {}
    filtered.forEach((m) => {
      const key = formatDateGroup(m.startsAt)
      if (!groups[key]) groups[key] = []
      groups[key].push(m)
    })
    return groups
  }, [matches, leagueFilter, game, status])

  const handleOddsClick = (matchId: string, teamId: string) => {
    navigate(`/matches/${matchId}?pick=${teamId}`)
  }

  const statusColor = (s: string) => {
    const map: Record<string, string> = {
      SCHEDULED: 'bg-status-scheduled/20 text-status-scheduled',
      LOCKED: 'bg-status-locked/20 text-status-locked',
      LIVE: 'bg-status-live/20 text-status-live',
      FINISHED: 'bg-status-finished/20 text-status-finished',
      CANCELLED: 'bg-status-cancelled/20 text-status-cancelled',
    }
    return map[s] || 'bg-gray-700 text-gray-400'
  }

  const MAIN_LEAGUES = ['LCK', 'LEC', 'LCS', 'LPL', 'Worlds', 'MSI', 'ESL Pro League', 'BLAST Premier', 'IEM', 'PGL Major', 'BLAST.tv Major']
  const mainLeagues = leagues.filter(l => MAIN_LEAGUES.some(name => l.name.includes(name)))
  const otherLeagues = leagues.filter(l => !MAIN_LEAGUES.some(name => l.name.includes(name)))

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5">
        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3 mb-4">
        <select value={leagueFilter} onChange={(e) => setLeagueFilter(e.target.value)} className="select-field w-48 text-sm">
          <option value="all">{t('matches.vsetky_ligy')}</option>
          {mainLeagues.length > 0 && (
            <optgroup label="Hlavné ligy">
              {mainLeagues.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
            </optgroup>
          )}
          {otherLeagues.length > 0 && (
            <optgroup label="Ostatné">
              {otherLeagues.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
            </optgroup>
          )}
        </select>

        <div className="flex gap-1 bg-surface rounded-lg p-0.5">
          {(['all', 'live', 'upcoming', 'finished'] as const).map((s) => (
            <button key={s} onClick={() => { setSearchParams((prev) => { const next = new URLSearchParams(prev); if (s === 'all') next.delete('status'); else next.set('status', s.toUpperCase()); return next }) }}
              className={`px-3 py-1.5 text-xs rounded-md transition ${
                status === s ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'
              }`}
            >
              {s === 'all' ? t('matches.all') : s === 'live' ? t('matches.live') : s === 'upcoming' ? t('matches.upcoming') : t('matches.finished')}
            </button>
          ))}
        </div>
      </div>

      <div style={{ margin: '16px 0' }}>
        <AdBanner size="horizontal" index={2} />
      </div>

      {/* Match list */}
      {loading ? (
        <SkeletonTable rows={6} />
      ) : Object.keys(grouped).length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
          <span className="text-3xl mb-3">📅</span>
          <p className="text-sm">{t('matches.ziadne_zapasy')}</p>
        </div>
      ) : (
        <div className="space-y-6">
          {Object.entries(grouped).map(([dateLabel, groupMatches]) => (
            <div key={dateLabel}>
              <div className="sticky top-0 z-10 bg-bg py-2">
                <span className="text-xs font-semibold text-text-secondary tracking-widest">
                  {dateLabel}
                </span>
              </div>
              <div className="bg-surface border border-border rounded-xl overflow-hidden divide-y divide-border/50">
                {groupMatches.map((m) => {
                  const odds = oddsMap[m.id]
                  return (
                    <div key={m.id} className="match-row card-hover" onClick={() => navigate(`/matches/${m.id}`)}>
                      {/* Time column */}
                      <div className="w-14 shrink-0 text-center">
                        <p className="text-sm font-bold text-accent-primary leading-tight">{formatTime(m.startsAt)}</p>
                        <p className="text-[10px] text-text-secondary leading-tight">
                          {formatDateSub(m.startsAt) || <span className="text-status-live">{t('matches.dnes')}</span>}
                        </p>
                      </div>

                      {/* Match column */}
                      <div className="flex-1 min-w-0">
                        <p className="text-[10px] text-text-secondary mb-0.5 truncate">{m.league.name}</p>
                        <div className="flex items-center gap-1.5">
                          {m.teamA.logoUrl && <img src={m.teamA.logoUrl} alt="" className="w-4 h-4 rounded-full object-contain shrink-0" />}
                          <span className="text-sm font-medium truncate">{m.teamA.name}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          {m.teamB.logoUrl && <img src={m.teamB.logoUrl} alt="" className="w-4 h-4 rounded-full object-contain shrink-0" />}
                          <span className="text-sm font-medium truncate">{m.teamB.name}</span>
                        </div>
                      </div>

                      {/* Odds column */}
                      <div className="flex items-center gap-1.5 shrink-0" onClick={(e) => e.stopPropagation()}>
                        {odds?.winnerOdds[0] && (
                          <OddsButton
                            label={shortName(m.teamA.name)}
                            odds={odds.winnerOdds[0].oddsValue}
                            size="sm"
                            onClick={() => handleOddsClick(m.id, odds.winnerOdds[0].teamId)}
                          />
                        )}
                        {odds?.winnerOdds[1] && (
                          <OddsButton
                            label={shortName(m.teamB.name)}
                            odds={odds.winnerOdds[1].oddsValue}
                            size="sm"
                            onClick={() => handleOddsClick(m.id, odds.winnerOdds[1].teamId)}
                          />
                        )}
                        <div className="bg-surface border border-border rounded-lg px-2 py-1.5 text-center min-h-[44px] flex flex-col items-center justify-center cursor-default">
                          <span className="text-[10px] text-text-secondary">{m.format}</span>
                          <span className="text-xs text-text-secondary">⚡</span>
                        </div>
                      </div>

                      {/* Status */}
                      {m.status !== 'SCHEDULED' && (
                        <span className={`badge shrink-0 ${statusColor(m.status)}`}>{m.status}</span>
                      )}
                    </div>
                  )
                })}
              </div>
            </div>
          ))}
        </div>
      )}
      </div>
    </PageLayout>
  )
}
