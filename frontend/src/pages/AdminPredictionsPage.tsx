import { useEffect, useState } from 'react'
import api from '../lib/api'

type Tab = 'bets' | 'f1'

interface BetItem {
  id: string
  userId: string
  username: string
  matchId: string
  game: string
  leagueName: string
  teamAName: string
  teamBName: string
  stake: number
  potentialReturn: number
  status: string
  pointsAwarded: number
  actualReturn: number
  createdAt: string
  settledAt: string | null
}

interface F1PredictionItem {
  id: string
  userId: string
  username: string
  sessionId: string
  sessionName: string
  meetingName: string
  dateStart: string
  predictedPoleDriverNumber: number | null
  predictedPoleDriverName: string | null
  predictedP1DriverNumber: number | null
  predictedP1DriverName: string | null
  predictedP2DriverNumber: number | null
  predictedP2DriverName: string | null
  predictedP3DriverNumber: number | null
  predictedP3DriverName: string | null
  pointsEarned: number
  status: string
  settledAt: string | null
  createdAt: string
}

export default function AdminPredictionsPage() {
  const [tab, setTab] = useState<Tab>('bets')
  const [bets, setBets] = useState<BetItem[]>([])
  const [f1Predictions, setF1Predictions] = useState<F1PredictionItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    (async () => {
      setLoading(true)
      try {
        const [betsRes, f1Res] = await Promise.all([
          api.get('/api/admin/bets'),
          api.get('/api/admin/f1/predictions'),
        ])
        setBets(betsRes.data ?? [])
        setF1Predictions(f1Res.data ?? [])
      } catch {
        setBets([])
        setF1Predictions([])
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const statusBadge = (s: string) => {
    const map: Record<string, string> = {
      WON: 'bg-green-900/30 text-green-400',
      LOST: 'bg-red-900/30 text-red-400',
      VOID: 'bg-gray-700 text-gray-400',
      PENDING: 'bg-amber-900/30 text-amber-400',
      SETTLED: 'bg-green-900/30 text-green-400',
    }
    return map[s] || 'bg-amber-900/30 text-amber-400'
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16 text-text-secondary">
        Načítavam...
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex gap-1 bg-surface rounded-lg p-0.5 w-fit">
        <button
          onClick={() => setTab('bets')}
          className={`px-3 py-1.5 text-xs rounded-md transition ${tab === 'bets' ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'}`}
        >
          Predikcie ({bets.length})
        </button>
        <button
          onClick={() => setTab('f1')}
          className={`px-3 py-1.5 text-xs rounded-md transition ${tab === 'f1' ? 'bg-accent-primary text-white font-semibold' : 'text-text-secondary hover:text-text-primary'}`}
        >
          F1 predikcie ({f1Predictions.length})
        </button>
      </div>

      {tab === 'bets' && (
        <div className="bg-surface border border-border rounded-xl overflow-x-auto">
          {bets.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
              <p className="text-sm">Žiadne predikcie</p>
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-text-secondary text-xs uppercase">
                  <th className="table-header">Používateľ</th>
                  <th className="table-header">Zápas</th>
                  <th className="table-header">Liga</th>
                  <th className="table-header text-right">Vklad</th>
                  <th className="table-header text-right">Pot. výhra</th>
                  <th className="table-header text-right">Status</th>
                  <th className="table-header text-right">Body</th>
                </tr>
              </thead>
              <tbody>
                {bets.map((b) => (
                  <tr key={b.id} className="border-b border-border/30 hover:bg-surface-elevated/30 transition">
                    <td className="table-cell font-medium">{b.username}</td>
                    <td className="table-cell">{b.teamAName} vs {b.teamBName}</td>
                    <td className="table-cell text-text-secondary">{b.leagueName}</td>
                    <td className="table-cell text-right font-mono">{b.stake.toLocaleString()}</td>
                    <td className="table-cell text-right font-mono text-accent-primary">{b.potentialReturn.toLocaleString()}</td>
                    <td className="table-cell text-right"><span className={`badge ${statusBadge(b.status)}`}>{b.status}</span></td>
                    <td className="table-cell text-right font-mono text-text-secondary">{b.pointsAwarded}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'f1' && (
        <div className="bg-surface border border-border rounded-xl overflow-x-auto">
          {f1Predictions.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
              <p className="text-sm">Žiadne F1 predikcie</p>
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-text-secondary text-xs uppercase">
                  <th className="table-header">Používateľ</th>
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
                {f1Predictions.map((p) => (
                  <tr key={p.id} className="border-b border-border/30 hover:bg-surface-elevated/30 transition">
                    <td className="table-cell font-medium">{p.username}</td>
                    <td className="table-cell">
                      <p className="font-medium">{p.meetingName}</p>
                      <p className="text-[10px] text-text-secondary">{p.sessionName}</p>
                    </td>
                    <td className="table-cell text-text-secondary">{p.predictedPoleDriverName ?? p.predictedPoleDriverNumber ?? '—'}</td>
                    <td className="table-cell text-text-secondary">{p.predictedP1DriverName ?? p.predictedP1DriverNumber ?? '—'}</td>
                    <td className="table-cell text-text-secondary">{p.predictedP2DriverName ?? p.predictedP2DriverNumber ?? '—'}</td>
                    <td className="table-cell text-text-secondary">{p.predictedP3DriverName ?? p.predictedP3DriverNumber ?? '—'}</td>
                    <td className="table-cell text-right font-mono text-text-secondary">{p.pointsEarned}</td>
                    <td className="table-cell text-right"><span className={`badge ${statusBadge(p.status)}`}>{p.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}
