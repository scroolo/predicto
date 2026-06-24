import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import RankBadge from '../components/RankBadge'

interface LeaderboardEntry {
  rankPosition: number
  userId: string
  username: string
  displayName: string
  avatarUrl: string | null
  lolElo: number
  lolRank: string | null
  cs2Elo: number
  cs2Rank: string | null
}

const rankColors: Record<string, string> = {
  '1': 'text-rank-gold', '2': 'text-rank-silver', '3': 'text-rank-challenger',
}

const trophies: Record<number, string> = {
  1: '🥇', 2: '🥈', 3: '🥉',
}

const rankBadgeColors: Record<string, string> = {
  Rookie: 'text-rank-rookie',
  Silver: 'text-rank-silver',
  Gold: 'text-rank-gold',
  Diamond: 'text-rank-diamond',
  Master: 'text-rank-master',
  Challenger: 'text-rank-challenger',
  'FACEIT 1': 'text-rank-rookie',
  'FACEIT 2': 'text-rank-silver',
  'FACEIT 4': 'text-rank-gold',
  'FACEIT 6': 'text-rank-diamond',
  'FACEIT 8': 'text-rank-master',
  'FACEIT 10': 'text-rank-challenger',
}

export default function AdminLeaderboardPage() {
  const { t } = useTranslation()
  const [game, setGame] = useState<'LOL' | 'CS2'>('LOL')
  const [entries, setEntries] = useState<LeaderboardEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    (async () => {
      setLoading(true)
      try {
        const res = await api.get(`/api/admin/leaderboard/${game.toLowerCase()}`)
        setEntries(res.data ?? [])
      } catch {
        setEntries([])
      }
      setLoading(false)
    })()
  }, [game])

  return (
    <div>
      <div className="flex items-center gap-4 mb-6">
        <h1 className="text-xl font-bold">Leaderboard</h1>
        <div className="flex gap-1 bg-surface rounded-lg p-0.5">
          {(['LOL', 'CS2'] as const).map((g) => (
            <button
              key={g}
              onClick={() => setGame(g)}
              className={`px-4 py-1.5 text-sm rounded-md transition ${
                game === g ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'
              }`}
            >
              {g === 'LOL' ? 'League of Legends' : 'CS2'}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="text-text-secondary text-sm">{t('common.nacitavam')}</div>
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
                <th className="table-header w-12">#</th>
                <th className="table-header">{t('leaderboard.hrac')}</th>
                <th className="table-header text-right">ELO</th>
                <th className="table-header text-right">{t('profile.rank_tiers')}</th>
              </tr>
            </thead>
            <tbody>
              {entries.map((e) => {
                const elo = game === 'LOL' ? e.lolElo : e.cs2Elo
                const rank = game === 'LOL' ? e.lolRank : e.cs2Rank
                const badgeColor = rankBadgeColors[rank ?? ''] || 'text-text-secondary'
                return (
                  <tr key={e.userId} className="border-b border-border/30 transition hover:bg-surface-elevated/30">
                    <td className="table-cell font-mono">
                      <span className={`flex items-center gap-1 ${trophies[e.rankPosition] ? 'text-rank-gold' : 'text-text-secondary'}`}>
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
                        <span>{e.displayName}</span>
                        <span className="text-text-secondary text-xs">(@{e.username})</span>
                      </div>
                    </td>
                    <td className="table-cell text-right font-mono text-accent-primary font-semibold">{elo.toLocaleString()}</td>
                    <td className="table-cell text-right">
                      <div className="flex items-center justify-end gap-2">
                        {rank && <RankBadge rank={rank} game={game} size={28} />}
                        <span className={`font-mono text-xs font-semibold ${badgeColor}`}>{rank ?? t('profile.unranked')}</span>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
