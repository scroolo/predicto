import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'
import RankBadge from '../components/RankBadge'
import { SkeletonTable } from '../components/Skeleton'

interface SeasonItem { id: string; name: string; game: string; type: string; startsAt: string; endsAt: string; status: string }
interface LeaderboardEntry {
  rankPosition: number; userId: string; displayName: string; avatarUrl: string | null
  points: number; correctPicks: number; mvpCorrect: number; scoreCorrect: number
}

const rankColors: Record<string, string> = {
  '1': 'text-rank-gold', '2': 'text-rank-silver', '3': 'text-rank-challenger',
}

const trophies: Record<number, string> = {
  1: '🥇', 2: '🥈', 3: '🥉',
}

export default function LeaderboardPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const [seasons, setSeasons] = useState<SeasonItem[]>([])
  const [selectedSeasonId, setSelectedSeasonId] = useState('')
  const [entries, setEntries] = useState<LeaderboardEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get('/api/seasons')
        const data: SeasonItem[] = res.data ?? []
        setSeasons(data)
        const active = data.find((s) => s.status === 'ACTIVE')
        if (active) setSelectedSeasonId(active.id)
        else if (data.length > 0) setSelectedSeasonId(data[0].id)
      } catch { /* ignore */ }
    })()
  }, [])

  useEffect(() => {
    if (!selectedSeasonId) { setEntries([]); setLoading(false); return }
    setLoading(true)
    ;(async () => {
      try { const res = await api.get(`/api/seasons/${selectedSeasonId}/leaderboard`); setEntries(res.data ?? []) }
      catch { setEntries([]) }
      finally { setLoading(false) }
    })()
  }, [selectedSeasonId])

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5 space-y-4">
        {/* Season selector */}
        <div className="flex items-center gap-3">
          <select value={selectedSeasonId} onChange={(e) => setSelectedSeasonId(e.target.value)} className="select-field w-56 text-sm">
            {seasons.map((s) => (
              <option key={s.id} value={s.id}>{s.name} ({s.status})</option>
            ))}
          </select>
        </div>

        {/* Table */}
        {loading ? (
          <SkeletonTable rows={8} />
        ) : entries.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
            <span className="text-3xl mb-3">🏆</span>
            <p className="text-sm">{t('leaderboard.ziadne_data')}</p>
          </div>
        ) : (
          <div className="bg-surface border border-border rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-text-secondary text-xs uppercase">
                  <th className="table-header w-12">{t('leaderboard.hash')}</th>
                  <th className="table-header">{t('leaderboard.hrac')}</th>
                  <th className="table-header text-right">{t('leaderboard.body')}</th>
                  <th className="table-header text-right hidden sm:table-cell">{t('leaderboard.spravne')}</th>
                  <th className="table-header text-right hidden sm:table-cell">{t('leaderboard.mvp')}</th>
                  <th className="table-header text-right hidden sm:table-cell">{t('leaderboard.score')}</th>
                </tr>
              </thead>
              <tbody>
                {entries.map((e) => {
                  const isMe = user?.id === e.userId
                  return (
                    <tr key={e.userId} className={`border-b border-border/30 transition ${
                      isMe ? 'bg-accent-glow' : 'hover:bg-surface-elevated/30'
                    }`}>
                      <td className="table-cell font-mono">
                        <span className={`flex items-center gap-1 ${rankColors[String(e.rankPosition)] || 'text-text-secondary'}`}>
                          {trophies[e.rankPosition] && <span>{trophies[e.rankPosition]}</span>}
                          {e.rankPosition}
                        </span>
                      </td>
                      <td className="table-cell">
                        <div className="flex items-center gap-2">
                          {e.avatarUrl ? (
                            <img src={e.avatarUrl} alt="" className="w-6 h-6 rounded-full" />
                          ) : (
                            <div className="w-6 h-6 rounded-full bg-surface-elevated flex items-center justify-center text-xs text-text-secondary">
                              {e.displayName?.charAt(0)?.toUpperCase() ?? '?'}
                            </div>
                          )}
                          {isMe && user?.lolRank && (
                            <RankBadge rank={user.lolRank} game="LOL" size={22} />
                          )}
                          <span className={isMe ? 'text-accent-primary font-semibold' : ''}>{e.displayName}</span>
                        </div>
                      </td>
                      <td className="table-cell text-right font-mono text-accent-primary font-semibold">{e.points.toLocaleString()}</td>
                      <td className="table-cell text-right font-mono text-text-primary hidden sm:table-cell">{e.correctPicks}</td>
                      <td className="table-cell text-right font-mono text-text-primary hidden sm:table-cell">{e.mvpCorrect}</td>
                      <td className="table-cell text-right font-mono text-text-primary hidden sm:table-cell">{e.scoreCorrect}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </PageLayout>
  )
}
