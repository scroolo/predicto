import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'

interface TeamRef {
  id: string
  name: string
  logoUrl: string | null
}

interface MatchItem {
  id: string
  game: string
  league: { id: string; name: string; logoUrl: string | null }
  teamA: TeamRef
  teamB: TeamRef
  format: string
  stage: string
  startsAt: string
  status: string
  result: { winnerTeamId: string; score: string } | null
  resultConfirmedAt: string | null
}

interface WinnerOddsItem { teamId: string; oddsValue: number }
interface ScoreOddsItem { scoreValue: string; oddsValue: number }
interface OddsData { matchId: string; winnerOdds: WinnerOddsItem[]; scoreOdds: ScoreOddsItem[] }

interface PlayerBrief { id: string; nickname: string }

export default function AdminMatchesPage() {
  const { t } = useTranslation()
  const [matches, setMatches] = useState<MatchItem[]>([])
  const [loading, setLoading] = useState(true)
  const [expandedId, setExpandedId] = useState<string | null>(null)
  const [toast, setToast] = useState<{ type: 'success' | 'error'; msg: string } | null>(null)

  const fetchMatches = async () => {
    try {
      const res = await api.get('/api/matches')
      const data: MatchItem[] = res.data ?? []
      data.sort((a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime())
      setMatches(data)
    } catch { /* ignore */ }
    setLoading(false)
  }

  useEffect(() => { fetchMatches() }, [])

  const showToast = (type: 'success' | 'error', msg: string) => {
    setToast({ type, msg })
    setTimeout(() => setToast(null), 4000)
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

  const formatTime = (iso: string) => {
    const d = new Date(iso)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const tomorrow = new Date(today.getTime() + 86400000)
    const diff = d.getTime() - now.getTime()

    if (diff < 86400000 && d >= today) return `Today ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
    if (diff < 172800000 && d >= tomorrow) return `Tomorrow ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
  }

  const scoreOptions = (fmt: string) => {
    if (fmt === 'BO1') return ['1:0', '0:1']
    if (fmt === 'BO5') return ['3:0', '3:1', '3:2', '0:3', '1:3', '2:3']
    return ['2:0', '2:1', '0:2', '1:2']
  }

  const handleCancel = async (m: MatchItem) => {
    if (!window.confirm(t('match.cancelConfirm'))) return
    try {
      await api.post(`/api/admin/matches/${m.id}/cancel`)
      showToast('success', t('match.matchCancelled'))
      fetchMatches()
    } catch (err: any) {
      showToast('error', err.response?.data?.message || t('match.failedCancel'))
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Matches</h1>
      </div>

      {toast && (
        <div className={`mb-4 px-4 py-2 rounded-lg text-sm ${
          toast.type === 'success' ? 'bg-green-900/30 text-green-400 border border-green-800' : 'bg-red-900/30 text-red-400 border border-red-800'
        }`}>
          {toast.msg}
        </div>
      )}

      {loading ? (
        <p className="text-text-secondary text-center py-12">Loading...</p>
      ) : (
        <div className="space-y-2">
          {matches.map((m) => (
            <div key={m.id} className="bg-surface border border-border rounded-xl overflow-hidden">
              <div
                onClick={() => setExpandedId(expandedId === m.id ? null : m.id)}
                className="flex items-center gap-4 px-5 py-3.5 cursor-pointer hover:bg-surface-elevated/50 transition"
              >
                <div className="flex items-center gap-2 w-48 shrink-0">
                  {m.league.logoUrl && <img src={m.league.logoUrl} alt="" className="w-5 h-5 rounded-full object-contain" />}
                  <span className="text-xs text-text-secondary">{m.league.name}</span>
                </div>
                <div className="flex-1 flex items-center justify-center gap-3">
                  <span className="font-medium text-right w-32 truncate">{m.teamA.name}</span>
                  <div className="text-center">
                    <span className="text-lg font-bold text-accent-primary">
                      {m.result?.score ?? 'VS'}
                    </span>
                  </div>
                  <span className="font-medium w-32 truncate">{m.teamB.name}</span>
                </div>
                <div className="flex items-center gap-3 shrink-0">
                  <span className="text-xs text-text-secondary">{m.format}</span>
                  <span className="text-xs text-text-secondary">{formatTime(m.startsAt)}</span>
                  <span className={`badge ${statusColor(m.status)}`}>{m.status}</span>
                  <svg className={`w-4 h-4 text-text-secondary transition ${expandedId === m.id ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
              </div>

              {expandedId === m.id && (
                <div className="border-t border-border p-5 space-y-6">
                  <OddsPanel match={m} showToast={showToast} scoreOptions={scoreOptions} />
                  <ResultPanel match={m} showToast={showToast} fetchMatches={fetchMatches} scoreOptions={scoreOptions} />
                  {(m.status === 'SCHEDULED' || m.status === 'LOCKED') && (
                    <div>
                      <button onClick={() => handleCancel(m)} className="btn-danger text-sm">{t('match.cancelMatch')}</button>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function OddsPanel({ match, showToast, scoreOptions: _so }: { match: MatchItem; showToast: (t: 'success' | 'error', m: string) => void; scoreOptions: (f: string) => string[] }) {
  const { t } = useTranslation()
  const [odds, setOdds] = useState<OddsData | null>(null)
  const [w1, setW1] = useState('')
  const [w2, setW2] = useState('')
  const [scores, setScores] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get(`/api/matches/${match.id}/odds`)
        const data: OddsData = res.data
        setOdds(data)
        if (data.winnerOdds.length >= 2) {
          setW1(String(data.winnerOdds[0].oddsValue))
          setW2(String(data.winnerOdds[1].oddsValue))
        }
        const s: Record<string, string> = {}
        data.scoreOdds.forEach((so) => { s[so.scoreValue] = String(so.oddsValue) })
        setScores(s)
      } catch { setOdds(null) }
    })()
  }, [match.id])

  if (match.status !== 'SCHEDULED') {
    if (!odds) return null
    return (
      <div>
        <h3 className="text-sm font-semibold text-text-secondary uppercase mb-3">{t('match.oddsReadOnly')}</h3>
        <div className="grid grid-cols-2 gap-3 max-w-md">
          {odds.winnerOdds.map((wo) => (
            <div key={wo.teamId} className="bg-surface-elevated rounded-lg p-3 text-center">
              <p className="text-xs text-text-secondary">{wo.teamId === match.teamA.id ? match.teamA.name : match.teamB.name}</p>
              <p className="text-accent-primary font-mono text-lg">{wo.oddsValue.toFixed(2)}</p>
            </div>
          ))}
        </div>
        {odds.scoreOdds.length > 0 && (
          <div className="mt-3 grid grid-cols-4 gap-2 max-w-md">
            {odds.scoreOdds.map((so) => (
              <div key={so.scoreValue} className="bg-surface-elevated rounded p-2 text-center">
                <p className="text-xs">{so.scoreValue}</p>
                <p className="text-accent-primary font-mono text-sm">{so.oddsValue.toFixed(2)}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    )
  }

  const sc = _so(match.format)

  const parseOdds = (val: string) => parseFloat(val.toString().replace(',', '.'))

  const handleSave = async () => {
    setSaving(true)
    try {
      const body: Record<string, unknown> = {
        winnerOdds: [
          { teamId: match.teamA.id, oddsValue: parseOdds(w1) || 0 },
          { teamId: match.teamB.id, oddsValue: parseOdds(w2) || 0 },
        ],
        scoreOdds: sc.map((sv) => ({ scoreValue: sv, oddsValue: parseOdds(scores[sv]) || 0 })),
      }
      await api.put(`/api/admin/matches/${match.id}/odds`, body)
      showToast('success', t('match.oddsSaved'))
    } catch (err: any) {
      showToast('error', err.response?.data?.message || t('match.failedSaveOdds'))
    } finally { setSaving(false) }
  }

  return (
    <div>
      <h3 className="text-sm font-semibold text-text-secondary uppercase mb-3">{t('match.editOdds')}</h3>
      <div className="grid grid-cols-2 gap-4 max-w-md mb-4">
        <div>
          <label className="block text-xs text-text-secondary mb-1">{match.teamA.name}</label>
          <input value={w1} onChange={(e) => setW1(e.target.value)} className="input-field" placeholder={t('match.odds')} />
        </div>
        <div>
          <label className="block text-xs text-text-secondary mb-1">{match.teamB.name}</label>
          <input value={w2} onChange={(e) => setW2(e.target.value)} className="input-field" placeholder={t('match.odds')} />
        </div>
      </div>
      <h4 className="text-xs font-semibold text-text-secondary uppercase mb-2">{t('match.scoreOdds')}</h4>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 max-w-lg mb-4">
        {sc.map((sv) => (
          <div key={sv}>
            <label className="block text-xs text-text-secondary mb-1">{sv}</label>
            <input value={scores[sv] ?? ''} onChange={(e) => setScores({ ...scores, [sv]: e.target.value })} className="input-field" placeholder={t('match.odds')} />
          </div>
        ))}
      </div>
      <button onClick={handleSave} disabled={saving} className="btn-primary text-sm">{saving ? t('match.saving') : t('match.saveOdds')}</button>
    </div>
  )
}

function ResultPanel({ match, showToast, fetchMatches, scoreOptions: _so }: { match: MatchItem; showToast: (t: 'success' | 'error', m: string) => void; fetchMatches: () => void; scoreOptions: (f: string) => string[] }) {
  const { t } = useTranslation()
  const [winnerId, setWinnerId] = useState('')
  const [score, setScore] = useState('')
  const [mvpId, setMvpId] = useState('')
  const [players, setPlayers] = useState<PlayerBrief[]>([])
  const [confirming, setConfirming] = useState(false)

  useEffect(() => {
    if (match.status === 'LOCKED' || match.status === 'LIVE' || match.status === 'FINISHED') {
      (async () => {
        try {
          const [pa, pb] = await Promise.all([
            api.get(`/api/teams/${match.teamA.id}/players`),
            api.get(`/api/teams/${match.teamB.id}/players`),
          ])
          setPlayers([...(pa.data ?? []), ...(pb.data ?? [])])
        } catch { /* ignore */ }
      })()
    }
  }, [match.id, match.teamA.id, match.teamB.id, match.status])

  if (match.resultConfirmedAt) {
    return (
      <div>
        <h3 className="text-sm font-semibold text-text-secondary uppercase mb-3">{t('match.resultConfirmed')}</h3>
        <p className="text-sm">{t('match.winnerLabel')}: {match.result?.winnerTeamId === match.teamA.id ? match.teamA.name : match.teamB.name}</p>
        <p className="text-sm">{t('match.score')}: {match.result?.score}</p>
        <p className="text-xs text-text-secondary mt-1">{t('match.settlementRun')}</p>
      </div>
    )
  }

  if (match.status !== 'LOCKED' && match.status !== 'LIVE' && match.status !== 'FINISHED') return null

  const sc = _so(match.format)

  const handleConfirm = async () => {
    if (!winnerId || !score) { showToast('error', t('match.selectWinnerScore')); return }
    setConfirming(true)
    try {
      const body: Record<string, unknown> = { winnerTeamId: winnerId, score }
      if (mvpId) body.mvpPlayerId = mvpId
      await api.post(`/api/admin/matches/${match.id}/confirm-result`, body)
      showToast('success', t('match.resultConfirmedMsg'))
      fetchMatches()
    } catch (err: any) {
      showToast('error', err.response?.data?.message || t('match.failedConfirmResult'))
    } finally { setConfirming(false) }
  }

  return (
    <div>
      <h3 className="text-sm font-semibold text-text-secondary uppercase mb-3">{t('match.confirmResultTitle')}</h3>
      <div className="space-y-4 max-w-md">
        <div>
          <label className="block text-xs text-text-secondary mb-1">{t('match.winnerLabel')}</label>
          <div className="flex gap-3">
            <label className={`flex-1 flex items-center justify-center gap-2 p-3 rounded-lg border cursor-pointer transition ${winnerId === match.teamA.id ? 'border-accent-primary bg-accent-primary/10' : 'border-border bg-surface-elevated hover:border-accent-primary'}`}>
              <input type="radio" name="winner" value={match.teamA.id} checked={winnerId === match.teamA.id} onChange={() => setWinnerId(match.teamA.id)} className="sr-only" />
              <span className="text-sm font-medium">{match.teamA.name}</span>
            </label>
            <label className={`flex-1 flex items-center justify-center gap-2 p-3 rounded-lg border cursor-pointer transition ${winnerId === match.teamB.id ? 'border-accent-primary bg-accent-primary/10' : 'border-border bg-surface-elevated hover:border-accent-primary'}`}>
              <input type="radio" name="winner" value={match.teamB.id} checked={winnerId === match.teamB.id} onChange={() => setWinnerId(match.teamB.id)} className="sr-only" />
              <span className="text-sm font-medium">{match.teamB.name}</span>
            </label>
          </div>
        </div>
        <div>
          <label className="block text-xs text-text-secondary mb-1">{t('match.score')}</label>
          <select value={score} onChange={(e) => setScore(e.target.value)} className="select-field">
            <option value="">{t('match.selectScore')}</option>
            {sc.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs text-text-secondary mb-1">{t('match.mvp')}</label>
          <select value={mvpId} onChange={(e) => setMvpId(e.target.value)} className="select-field">
            <option value="">{t('match.selectMvp')}</option>
            {players.map((p) => <option key={p.id} value={p.id}>{p.nickname}</option>)}
          </select>
        </div>
        <button onClick={handleConfirm} disabled={confirming} className="btn-primary text-sm">{confirming ? t('match.confirming') : t('match.confirmResult')}</button>
      </div>
    </div>
  )
}
