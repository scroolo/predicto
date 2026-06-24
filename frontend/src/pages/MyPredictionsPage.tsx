import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'
import { SkeletonTable } from '../components/Skeleton'

type FilterMode = 'ALL' | 'PENDING' | 'SETTLED'
type Tab = 'bets' | 'f1'

interface PredictionItem {
  id: string
  match: { id: string; game: string; leagueName: string; teamAName: string; teamBName: string; format: string; stage: string; startsAt: string; status: string }
  winnerTeamId: string | null; predictedWinnerName: string | null; stake: number; potentialReturn: number
  exactScore: string | null; scoreStake: number | null; status: string
  pointsAwarded: number; actualReturn: number; createdAt: string; settledAt: string | null
}

interface F1PredictionItem {
  id: string
  sessionId: string
  sessionName: string
  sessionType: string
  meetingName: string
  meetingId: string
  dateStart: string
  predictedPoleDriverNumber: number | null
  predictedP1DriverNumber: number | null
  predictedP2DriverNumber: number | null
  predictedP3DriverNumber: number | null
  poleDriverName: string | null
  p1DriverName: string | null
  p2DriverName: string | null
  p3DriverName: string | null
  pointsEarned: number
  status: string
  settledAt: string | null
  locked: boolean
  predictionsLocked: boolean
}

export default function MyPredictionsPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [tab, setTab] = useState<Tab>('bets')
  const [predictions, setPredictions] = useState<PredictionItem[]>([])
  const [f1Predictions, setF1Predictions] = useState<F1PredictionItem[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<FilterMode>('ALL')

  useEffect(() => {
    (async () => {
      try {
        const [betsRes, f1Res] = await Promise.all([
          api.get('/api/users/me/bets'),
          api.get('/api/users/me/predictions'),
        ])
        setPredictions(betsRes.data ?? [])
        setF1Predictions(f1Res.data ?? [])
      } catch {
        setPredictions([])
        setF1Predictions([])
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const filteredPredictions = predictions.filter((pred) => {
    if (filter === 'ALL') return true
    const settled = pred.status === 'WON' || pred.status === 'LOST' || pred.status === 'VOID'
    return filter === 'SETTLED' ? settled : !settled
  })

  const f1Filtered = f1Predictions.filter((p) => {
    if (filter === 'ALL') return true
    const settled = p.status === 'SETTLED'
    return filter === 'SETTLED' ? settled : !settled
  })

  const statusColor = (s: string) => {
    const map: Record<string, string> = {
      WON: 'bg-green-900/30 text-green-400',
      LOST: 'bg-red-900/30 text-red-400',
      VOID: 'bg-gray-700 text-gray-400',
    }
    return map[s] || 'bg-amber-900/30 text-amber-400'
  }

  const formatTime = (iso: string) => new Date(iso).toLocaleString()

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5 space-y-4">
        <div className="flex gap-1 bg-surface rounded-lg p-0.5 w-fit">
          <button
            onClick={() => setTab('bets')}
            className={`px-3 py-1.5 text-xs rounded-md transition ${tab === 'bets' ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'}`}
          >
            {t('predictions.bets')} ({predictions.length})
          </button>
          <button
            onClick={() => setTab('f1')}
            className={`px-3 py-1.5 text-xs rounded-md transition ${tab === 'f1' ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'}`}
          >
            {t('predictions.f1')} ({f1Predictions.length})
          </button>
        </div>

        <div className="flex gap-1 bg-surface rounded-lg p-0.5 w-fit">
          {(['ALL', 'PENDING', 'SETTLED'] as FilterMode[]).map((mode) => (
            <button key={mode} onClick={() => setFilter(mode)} className={`px-3 py-1.5 text-xs rounded-md transition ${
              filter === mode ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'
            }`}>
              {mode === 'ALL' ? t('predictions.all') : mode === 'PENDING' ? t('predictions.pending') : t('predictions.settled')}
            </button>
          ))}
        </div>

        {loading ? (
          <SkeletonTable rows={5} />
        ) : tab === 'f1' && f1Filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
            <span className="text-3xl mb-3">🏎️</span>
            <p className="text-sm">{t('predictions.emptyF1')}</p>
          </div>
        ) : tab === 'bets' && filteredPredictions.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
            <span className="text-3xl mb-3">📋</span>
            <p className="text-sm">{t('predictions.empty')}</p>
          </div>
        ) : tab === 'bets' ? (
          <div className="bg-surface border border-border rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-text-secondary text-xs uppercase">
                  <th className="table-header">{t('predictions.match')}</th>
                  <th className="table-header hidden sm:table-cell">{t('predictions.tvoj_tip')}</th>
                  <th className="table-header text-right">{t('predictions.vklad')}</th>
                  <th className="table-header text-right hidden sm:table-cell">{t('predictions.potential')}</th>
                  <th className="table-header text-right">{t('predictions.status')}</th>
                  <th className="table-header text-right hidden sm:table-cell">{t('predictions.body')}</th>
                </tr>
              </thead>
              <tbody>
                {filteredPredictions.map((pred) => {
                  const isSettled = pred.status === 'WON' || pred.status === 'LOST' || pred.status === 'VOID'
                  return (
                    <tr key={pred.id} onClick={() => navigate(`/matches/${pred.match.id}`)} className="border-b border-border/30 hover:bg-surface-elevated/30 transition cursor-pointer">
                      <td className="table-cell">
                        <p className="font-medium">{pred.match.teamAName} vs {pred.match.teamBName}</p>
                        <p className="text-[10px] text-text-secondary">{pred.match.leagueName}</p>
                      </td>
                      <td className="table-cell text-text-secondary hidden sm:table-cell">{pred.predictedWinnerName ?? pred.winnerTeamId ?? '—'}</td>
                      <td className="table-cell text-right font-mono">{pred.stake.toLocaleString()}</td>
                      <td className="table-cell text-right font-mono text-accent-primary hidden sm:table-cell">{pred.potentialReturn.toLocaleString()}</td>
                      <td className="table-cell text-right"><span className={`badge ${statusColor(pred.status)}`}>{pred.status}</span></td>
                      <td className="table-cell text-right font-mono text-text-secondary hidden sm:table-cell">{isSettled ? pred.pointsAwarded : '—'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="bg-surface border border-border rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-text-secondary text-xs uppercase">
                  <th className="table-header">Session</th>
                  <th className="table-header">Pole</th>
                  <th className="table-header">P1</th>
                  <th className="table-header">P2</th>
                  <th className="table-header">P3</th>
                  <th className="table-header text-right">Body</th>
                  <th className="table-header text-right">Status</th>
                </tr>
              </thead>
              <tbody>
                {f1Filtered.map((p) => (
                  <tr key={p.id} onClick={() => navigate(`/f1/sessions/${p.sessionId}`)} className="border-b border-border/30 hover:bg-surface-elevated/30 transition cursor-pointer">
                    <td className="table-cell">
                      <p className="font-medium">{p.meetingName}</p>
                      <p className="text-[10px] text-text-secondary">{p.sessionName}</p>
                    </td>
                    <td className="table-cell text-text-secondary">{p.poleDriverName ?? p.predictedPoleDriverNumber ?? '—'}</td>
                    <td className="table-cell text-text-secondary">{p.p1DriverName ?? p.predictedP1DriverNumber ?? '—'}</td>
                    <td className="table-cell text-text-secondary">{p.p2DriverName ?? p.predictedP2DriverNumber ?? '—'}</td>
                    <td className="table-cell text-text-secondary">{p.p3DriverName ?? p.predictedP3DriverNumber ?? '—'}</td>
                    <td className="table-cell text-right font-mono">{p.pointsEarned}</td>
                    <td className="table-cell text-right"><span className={`badge ${statusColor(p.status)}`}>{p.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </PageLayout>
  )
}
