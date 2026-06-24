import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'
import OddsButton from '../components/OddsButton'
import BetSlip, { MobileBetBar } from '../components/BetSlip'
import { SkeletonCard } from '../components/Skeleton'

interface WinnerOddsItem { teamId: string; oddsValue: number }
interface ScoreOddsItem { scoreValue: string; oddsValue: number }
interface OddsData { matchId: string; winnerOdds: WinnerOddsItem[]; scoreOdds: ScoreOddsItem[] }
interface TeamRef { id: string; name: string; logoUrl: string | null }
interface MatchDetail {
  id: string; game: string
  league: { id: string; name: string; logoUrl: string | null }
  teamA: TeamRef; teamB: TeamRef; format: string; stage: string
  startsAt: string; status: string
  result: { winnerTeamId: string; score: string } | null
}
interface PlayerBrief { id: string; nickname: string }
interface BetData {
  id: string; winnerTeamId: string; stake: number; mvpPlayerId: string | null
  exactScore: string | null; scoreStake: number | null; status: string
  match: { id: string; game: string; leagueName: string; teamAName: string; teamBName: string; format: string; stage: string; startsAt: string; status: string }
}

export default function MatchDetailPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { user, refreshUser } = useAuth()

  const [match, setMatch] = useState<MatchDetail | null>(null)
  const [odds, setOdds] = useState<OddsData | null>(null)
  const [existingBet, setExistingBet] = useState<BetData | null>(null)
  const [players, setPlayers] = useState<PlayerBrief[]>([])
  const [loading, setLoading] = useState(true)
  const [placing, setPlacing] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [winnerTeamId, setWinnerTeamId] = useState('')
  const [stake, setStake] = useState('')
  const [mvpPlayerId, setMvpPlayerId] = useState('')
  const [exactScore, setExactScore] = useState('')
  const [scoreStake, setScoreStake] = useState('')
  const [lockCountdown, setLockCountdown] = useState('')

  const teamNameById = useCallback((teamId: string) => {
    if (!match) return ''
    if (match.teamA.id === teamId) return match.teamA.name
    if (match.teamB.id === teamId) return match.teamB.name
    return ''
  }, [match])

  const fetchData = useCallback(async () => {
    if (!id) return
    setLoading(true)
    try {
      const [matchRes, oddsRes] = await Promise.all([
        api.get(`/api/matches/${id}`),
        api.get(`/api/matches/${id}/odds`),
      ])
      const m = matchRes.data as MatchDetail
      console.log('MATCH:', m)
      setMatch(m)
      console.log('ODDS:', oddsRes.data)
      setOdds(oddsRes.data)
      const [playersARes, playersBRes] = await Promise.all([
        api.get(`/api/teams/${m.teamA.id}/players`),
        api.get(`/api/teams/${m.teamB.id}/players`),
      ])
      setPlayers([...(playersARes.data ?? []), ...(playersBRes.data ?? [])])
      try {
        const betsRes = await api.get('/api/users/me/bets')
        const userBet = betsRes.data.find((b: BetData) => b.match.id === id)
        if (userBet) {
          setExistingBet(userBet)
          setWinnerTeamId(userBet.winnerTeamId)
          setStake(String(userBet.stake))
          setMvpPlayerId(userBet.mvpPlayerId ?? '')
          setExactScore(userBet.exactScore ?? '')
          setScoreStake(userBet.scoreStake != null ? String(userBet.scoreStake) : '')
        }
      } catch { /* no bet yet */ }
      } catch { setError(t('matches.chyba_nacitanie')) }
    finally { setLoading(false) }
  }, [id])

  useEffect(() => { fetchData() }, [fetchData])

  // Pre-select pick from URL param
  useEffect(() => {
    const pick = searchParams.get('pick')
    if (pick && odds) {
      const valid = odds.winnerOdds.some((wo) => wo.teamId === pick)
      if (valid) setWinnerTeamId(pick)
    }
  }, [searchParams, odds])

  useEffect(() => {
    if (!match) return
    const lockTime = new Date(match.startsAt).getTime() - 15 * 60 * 1000
    const tick = () => {
      const now = Date.now()
      const diff = lockTime - now
      if (diff <= 0) { setLockCountdown('LOCKED'); return }
      const totalSec = Math.floor(diff / 1000)
      const m = Math.floor(totalSec / 60)
      const s = totalSec % 60
      setLockCountdown(`${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`)
    }
    tick()
    const interval = setInterval(tick, 1000)
    return () => clearInterval(interval)
  }, [match])

  const selectedWinnerOdds = odds?.winnerOdds.find((o) => o.teamId === winnerTeamId)
  const selectedScoreOdds = odds?.scoreOdds.find((o) => o.scoreValue === exactScore)

  const handlePlaceBet = async () => {
    setError(''); setSuccess('')
    if (!winnerTeamId) { setError(t('matches.vyber_vitaza_chyba')); return }
    const parsedStake = parseInt(stake) || 0
    if (parsedStake <= 0) { setError(t('matches.platny_vklad')); return }
    setPlacing(true)
    try {
      const body: Record<string, unknown> = { winnerTeamId, stake: parsedStake }
      if (mvpPlayerId) body.mvpPlayerId = mvpPlayerId
      const parsedScoreStake = parseInt(scoreStake) || 0
      if (exactScore && parsedScoreStake > 0) { body.exactScore = exactScore; body.scoreStake = parsedScoreStake }
      await api.post(`/api/matches/${id}/bets`, body)
      setSuccess(existingBet ? t('matches.predikcia_aktualizovana') : t('matches.predikcia_umiestnena'))
      await refreshUser(); await fetchData()
    } catch (err: any) { setError(err.response?.data?.message || t('matches.chyba_vklad')) }
    finally { setPlacing(false) }
  }

  const handleCancelBet = async () => {
    setError(''); setSuccess(''); setPlacing(true)
    try {
      await api.delete(`/api/matches/${id}/bets`)
      setSuccess(t('matches.predikcia_zrusena')); setExistingBet(null); setWinnerTeamId(''); setStake('')
      setMvpPlayerId(''); setExactScore(''); setScoreStake('')
      await refreshUser()
    } catch (err: any) { setError(err.response?.data?.message || t('matches.chyba_zrusenie')) }
    finally { setPlacing(false) }
  }

  const formatTime = (iso: string) => new Date(iso).toLocaleString()

  if (loading) return <PageLayout><div className="max-w-6xl mx-auto px-4 py-5"><SkeletonCard /><div className="mt-4"><SkeletonCard /></div></div></PageLayout>
  if (!match) return <PageLayout><div className="max-w-6xl mx-auto px-4 py-5"><div className="text-center py-16 text-text-secondary">{t('matches.nenajdeny')}</div></div></PageLayout>

  const isLocallyLocked = lockCountdown === 'LOCKED'
  const canBet = (match.status === 'SCHEDULED' || match.status === 'LIVE') && !isLocallyLocked
  const statusColor = match.status === 'LIVE' ? 'bg-status-live/20 text-status-live' : match.status === 'SCHEDULED' ? 'bg-status-scheduled/20 text-status-scheduled' : 'bg-status-finished/20 text-status-finished'

  const scoreOptions = (fmt: string) => {
    if (fmt === 'BO1') return ['1:0', '0:1']
    if (fmt === 'BO5') return ['3:0', '3:1', '3:2', '0:3', '1:3', '2:3']
    return ['2:0', '2:1', '0:2', '1:2']
  }

  const betForm = (
    <div className="space-y-5">
      {error && <div className="bg-red-900/30 border border-red-800 rounded-lg p-3 text-sm text-red-400">{error}</div>}
      {success && <div className="bg-green-900/30 border border-green-800 rounded-lg p-3 text-sm text-green-400">{success}</div>}

      {!canBet && isLocallyLocked && <p className="text-text-secondary text-sm">{t('matches.predikcie_uzamknute')}</p>}

      {canBet && (
        <>
          {!isLocallyLocked && (
            <div className="text-xs text-accent-primary font-mono">{t('matches.zamyka_sa', { time: lockCountdown })}</div>
          )}

          {/* Winner pick */}
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-2">{t('matches.vyber_vitaza')}</label>
            <div className="grid grid-cols-2 gap-2">
              {((odds?.winnerOdds?.length ?? 0) > 0
                ? odds!.winnerOdds
                : match?.teamA && match?.teamB
                  ? [{ teamId: match.teamA.id, oddsValue: 1.90 }, { teamId: match.teamB.id, oddsValue: 1.90 }]
                  : []
              )?.map((wo) => (
                <OddsButton
                  key={wo.teamId}
                  label={teamNameById(wo.teamId)}
                  odds={wo.oddsValue}
                  selected={winnerTeamId === wo.teamId}
                  onClick={() => setWinnerTeamId(wo.teamId)}
                />
              ))}
            </div>
          </div>

          {/* Winner stake */}
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-2">{t('matches.vklad')}</label>
            <input type="number" min="1" value={stake} onChange={(e) => setStake(e.target.value)} className="input-field" placeholder={t('matches.zadaj_vklad')} />
          </div>

          {/* Score pick */}
          {odds && odds.scoreOdds.length > 0 && (
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-2">{t('matches.exact_score')}</label>
              <div className="grid grid-cols-3 gap-2 mb-2">
                {odds.scoreOdds.map((so) => (
                  <OddsButton
                    key={so.scoreValue}
                    label={so.scoreValue}
                    odds={so.oddsValue}
                    size="sm"
                    selected={exactScore === so.scoreValue}
                    onClick={() => setExactScore(so.scoreValue)}
                  />
                ))}
              </div>
              {exactScore && (
                <input type="number" min="1" value={scoreStake} onChange={(e) => setScoreStake(e.target.value)} className="input-field" placeholder={t('matches.vklad_score')} />
              )}
            </div>
          )}

          {/* MVP pick */}
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-2">{t('matches.mvp_hrac')}</label>
            <select value={mvpPlayerId} onChange={(e) => setMvpPlayerId(e.target.value)} className="select-field">
              <option value="">{t('matches.vyber_mvp')}</option>
              {players.map((p) => <option key={p.id} value={p.id}>{p.nickname}</option>)}
            </select>
          </div>
        </>
      )}

      {/* Existing bet display for finished matches */}
      {!canBet && match.status === 'FINISHED' && existingBet && (
        <div className="bg-surface-elevated rounded-lg p-4 space-y-2 text-sm">
          <p className="font-semibold">{t('matches.tvoja_predikcia')}</p>
          <p><span className="text-text-secondary">{t('matches.status')}</span> {existingBet.status}</p>
          <p><span className="text-text-secondary">{t('matches.vklad_dvojbodka')}</span> {existingBet.stake} pts</p>
          {existingBet.exactScore && <p><span className="text-text-secondary">{t('matches.score_predikcia')}</span> {existingBet.exactScore}</p>}
        </div>
      )}
    </div>
  )

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5">
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">
        {/* Left: match info + bet form */}
        <div className="lg:col-span-3 space-y-5">
          {/* Match header */}
          <div className="card p-5">
            <div className="flex items-center justify-center gap-4 mb-3">
              <div className="text-right flex-1">
                <p className="text-lg font-bold">{match.teamA.name}</p>
              </div>
              <div className="text-center">
                <p className="text-xs text-text-secondary uppercase">{match.league.name}</p>
                <p className="text-3xl font-bold text-accent-primary">{match.result?.score ?? 'VS'}</p>
                <p className="text-xs text-text-secondary">{match.stage}</p>
              </div>
              <div className="flex-1">
                <p className="text-lg font-bold">{match.teamB.name}</p>
              </div>
            </div>
            <div className="flex items-center justify-center gap-3 text-xs text-text-secondary">
              <span>{formatTime(match.startsAt)}</span>
              <span className={`badge ${statusColor}`}>{match.status}</span>
              <span>{match.format}</span>
            </div>
          </div>

          {/* Bet form */}
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-text-primary mb-4">
              {existingBet ? t('matches.aktualizovat') : t('matches.umiestnit')}
            </h3>
            {!user ? (
              <div className="bg-surface-elevated border border-accent-primary/20 rounded-xl p-6 text-center">
                <p className="text-text-secondary mb-4">{t('auth.loginToPredict')}</p>
                <Link to="/login" className="btn-primary inline-block">{t('auth.login')}</Link>
              </div>
            ) : (
              betForm
            )}
          </div>
        </div>

        {/* Right: Bet slip (desktop) */}
        <div className="hidden lg:block lg:col-span-2">
          <BetSlip
            winnerTeam={winnerTeamId ? teamNameById(winnerTeamId) : undefined}
            exactScore={exactScore || undefined}
            winnerStake={stake}
            scoreStake={scoreStake}
            winnerOdds={selectedWinnerOdds?.oddsValue}
            scoreOdds={selectedScoreOdds?.oddsValue}
            onPlaceBet={handlePlaceBet}
            onCancelBet={handleCancelBet}
            placing={placing}
            existingBet={!!existingBet}
          />
        </div>
      </div>

      {/* Mobile bottom bar */}
      <MobileBetBar
        winnerStake={stake}
        scoreStake={scoreStake}
        winnerOdds={selectedWinnerOdds?.oddsValue}
        scoreOdds={selectedScoreOdds?.oddsValue}
        placing={placing}
        hasBet={!!winnerTeamId && canBet}
        onPlaceBet={handlePlaceBet}
      />
      </div>
    </PageLayout>
  )
}
