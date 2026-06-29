import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import { useToastContext } from '../contexts/ToastContext'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'

interface Driver {
  driverNumber: number
  fullName: string
  nameAcronym: string | null
  headshotUrl: string | null
  teamName: string | null
  teamColour: string | null
}

interface Session {
  id: string
  sessionName: string
  sessionType: string
  dateStart: string
  status: string
  locked: boolean
  predictionsLocked: boolean
  meetingName: string
  meetingId: string
  resultP1DriverNumber: number | null
  resultP2DriverNumber: number | null
  resultP3DriverNumber: number | null
  resultPoleDriverNumber: number | null
}

interface Prediction {
  id: string
  predictedPoleDriverNumber: number | null
  predictedP1DriverNumber: number | null
  predictedP2DriverNumber: number | null
  predictedP3DriverNumber: number | null
  pointsEarned: number
  status: string
  settledAt: string | null
}

const PREDICTION_FIELDS = [
  'predictedPoleDriverNumber',
  'predictedP1DriverNumber',
  'predictedP2DriverNumber',
  'predictedP3DriverNumber',
] as const

const FIELD_LABELS_SK: Record<string, { label: string; points: string }> = {
  predictedPoleDriverNumber: { label: 'Pole Position (víťaz kvalifikácie)', points: '+15 bodov' },
  predictedP1DriverNumber: { label: 'Víťaz preteku (P1)', points: '+20 bodov' },
  predictedP2DriverNumber: { label: 'P2', points: '+10 bodov' },
  predictedP3DriverNumber: { label: 'P3', points: '+10 bodov' },
}

export default function F1SessionPage() {
  const { t } = useTranslation()
  const { toast } = useToastContext()
  const { id } = useParams<{ id: string }>()
  console.log('SESSION ID:', id)
  const { user } = useAuth()
  const [session, setSession] = useState<Session | null>(null)
  const [drivers, setDrivers] = useState<Driver[]>([])
  const [prediction, setPrediction] = useState<Partial<Prediction>>({})
  const [existingPrediction, setExistingPrediction] = useState<Prediction | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const isRace = session?.sessionType === 'Race'

  useEffect(() => {
    (async () => {
      try {
        const [sessionRes, driversRes] = await Promise.all([
          api.get(`/api/f1/sessions/${id}`),
          api.get(`/api/f1/sessions/${id}/drivers`),
        ])
        setDrivers(driversRes.data ?? [])

        const sessionData = sessionRes.data
        setSession(sessionData)

        if (user && sessionData.status !== 'FINISHED') {
          try {
            const predRes = await api.get(`/api/f1/sessions/${id}/predictions/me`)
            setExistingPrediction(predRes.data)
            setPrediction(predRes.data)
          } catch {
            setExistingPrediction(null)
            setPrediction({})
          }
        }
      } catch { /* ignore */ }
      setLoading(false)
    })()
  }, [id, user])

  const handleSubmitPrediction = async () => {
    setSaving(true)
    try {
      const body: Record<string, number | null> = {}
      for (const field of PREDICTION_FIELDS) {
        body[field] = (prediction as Record<string, number | null>)[field] ?? null
      }
      await api.post(`/api/f1/sessions/${id}/predictions`, body)
      toast.success(t('predictions.saved'))
      const predRes = await api.get(`/api/f1/sessions/${id}/predictions/me`)
      setExistingPrediction(predRes.data)
      setPrediction(predRes.data)
    } catch (e: any) {
      toast.error(e?.response?.data?.message ?? t('predictions.error'))
    } finally {
      setSaving(false)
    }
  }

  const driverOptions = drivers.map(d => ({
    value: d.driverNumber,
    label: `#${d.driverNumber} ${d.fullName ?? d.nameAcronym} (${d.teamName ?? 'Bez tímu'})`,
  }))

  const formatDate = (iso: string) => {
    return new Date(iso).toLocaleDateString('sk-SK', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })
  }

  const statusBadge = (status: string) => {
    switch (status) {
      case 'UPCOMING': return <span className="badge bg-accent-primary/20 text-accent-primary">UPCOMING</span>
      case 'LIVE': return <span className="badge bg-green-500/20 text-green-400">LIVE</span>
      case 'FINISHED': return <span className="badge bg-surface-elevated text-text-secondary">FINISHED</span>
      case 'CANCELLED': return <span className="badge bg-red-500/20 text-red-400">CANCELLED</span>
      default: return null
    }
  }

  const p1Driver = drivers.find(x => x.driverNumber === session?.resultP1DriverNumber) ?? null
  const p2Driver = drivers.find(x => x.driverNumber === session?.resultP2DriverNumber) ?? null
  const p3Driver = drivers.find(x => x.driverNumber === session?.resultP3DriverNumber) ?? null

  const showPredictionForm = session?.status === 'UPCOMING' && !session.predictionsLocked && user && isRace

  if (loading) {
    return (
      <PageLayout>
        <div className="max-w-4xl mx-auto px-4 py-5">
          <div className="card p-6 animate-skeleton"><div className="h-6 w-1/2 bg-surface-elevated rounded" /></div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-4">
            {[1,2,3,4,5,6,7,8].map(i => <div key={i} className="card p-3 animate-skeleton"><div className="h-12 w-12 rounded-full bg-surface-elevated mx-auto mb-2" /><div className="h-3 w-3/4 bg-surface-elevated rounded mx-auto" /></div>)}
          </div>
        </div>
      </PageLayout>
    )
  }

  if (!session) {
    return <PageLayout><div className="max-w-4xl mx-auto px-4 py-5 text-center text-text-secondary">Relácia neexistuje</div></PageLayout>
  }

  return (
    <PageLayout>
      <div className="max-w-4xl mx-auto px-4 py-5">
        <Link to={`/f1/meetings/${session.meetingId}`} className="text-sm text-accent-primary hover:underline mb-4 inline-block">&larr; {session.meetingName}</Link>

        <div className="card p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold">{session.sessionName}</h1>
              <p className="text-sm text-text-secondary mt-1">{session.meetingName}</p>
              <p className="text-xs text-text-secondary mt-1">{formatDate(session.dateStart)}</p>
            </div>
            {statusBadge(session.status)}
          </div>
        </div>

        {/* RESULTS PODIUM */}
        {session?.status === 'FINISHED' && session?.resultP1DriverNumber != null && (
          <div className="mb-8">
            <h2 className="text-xl font-bold mb-6 flex items-center gap-2">🏆 <span>Výsledky</span></h2>

            <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'center', gap: '12px', marginBottom: '32px' }}>
              {[
                { pos: 2, driver: p2Driver, num: session.resultP2DriverNumber, height: '180px', medal: '🥈', bg: '#C0C0C020', border: '#C0C0C0' },
                { pos: 1, driver: p1Driver, num: session.resultP1DriverNumber, height: '220px', medal: '🥇', bg: '#FFD70020', border: '#FFD700' },
                { pos: 3, driver: p3Driver, num: session.resultP3DriverNumber, height: '150px', medal: '🥉', bg: '#CD7F3220', border: '#CD7F32' },
              ].map(({ pos, driver, num, height, medal, bg, border }) => (
                <div key={pos} style={{
                  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-end',
                  minHeight: height, minWidth: '140px', background: bg,
                  border: `1px solid ${border}`, borderRadius: '12px', padding: '16px 12px', position: 'relative',
                }}>
                  <div style={{ position: 'absolute', top: '10px', left: '12px', fontSize: '20px' }}>{medal}</div>
                  {driver?.headshotUrl ? (
                    <img src={driver.headshotUrl} alt={driver.nameAcronym ?? ''}
                      style={{ width: pos === 1 ? '80px' : '64px', borderRadius: '50%', marginBottom: '8px', border: `2px solid ${border}` }}
                      onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
                  ) : (
                    <div style={{
                      width: pos === 1 ? '80px' : '64px', height: pos === 1 ? '80px' : '64px', borderRadius: '50%',
                      background: driver?.teamColour ? `${driver.teamColour}40` : '#ffffff20',
                      border: `2px solid ${driver?.teamColour ?? border}`,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: pos === 1 ? '24px' : '18px', fontWeight: 'bold',
                      color: driver?.teamColour ?? 'white', marginBottom: '8px',
                    }}>
                      {driver?.nameAcronym ?? `#${num}`}
                    </div>
                  )}
                  {driver?.teamColour && (
                    <div style={{ width: '32px', height: '3px', borderRadius: '2px', background: driver.teamColour, marginBottom: '6px' }} />
                  )}
                  <div style={{ color: 'white', fontWeight: 'bold', fontSize: pos === 1 ? '16px' : '14px', textAlign: 'center' }}>
                    {driver?.nameAcronym ?? `#${num}`}
                  </div>
                  <div style={{ color: '#aaa', fontSize: '11px', textAlign: 'center', marginTop: '2px' }}>
                    {driver?.teamName ?? ''}
                  </div>
                  <div style={{
                    position: 'absolute', bottom: '-1px', left: '0', right: '0',
                    background: border, borderRadius: '0 0 12px 12px', textAlign: 'center',
                    padding: '4px', fontSize: '13px', fontWeight: 'bold',
                    color: pos === 1 ? '#000' : '#fff',
                  }}>
                    P{pos}
                  </div>
                </div>
              ))}
            </div>

            {(() => {
              const podiumNums = [
                session.resultP1DriverNumber,
                session.resultP2DriverNumber,
                session.resultP3DriverNumber,
              ].filter(Boolean)
              const rest = drivers.filter(d => !podiumNums.includes(d.driverNumber))
              if (rest.length === 0) return null
              return (
                <div style={{ background: '#ffffff08', borderRadius: '12px', overflow: 'hidden' }}>
                  <div style={{ padding: '12px 16px', borderBottom: '1px solid #ffffff15', color: '#aaa', fontSize: '12px', fontWeight: '600', letterSpacing: '0.05em' }}>
                    PORADIE
                  </div>
                  {rest.map((d, idx) => (
                    <div key={d.driverNumber} style={{
                      display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px',
                      borderBottom: '1px solid #ffffff08',
                    }}>
                      <span style={{ color: '#aaa', fontSize: '14px', fontWeight: 'bold', minWidth: '28px' }}>P{idx + 4}</span>
                      {d.headshotUrl ? (
                        <img src={d.headshotUrl} alt={d.nameAcronym ?? ''}
                          style={{ width: '32px', height: '32px', borderRadius: '50%', objectFit: 'cover' }}
                          onError={e => { (e.target as HTMLImageElement).style.display = 'none' }} />
                      ) : (
                        <div style={{
                          width: '32px', height: '32px', borderRadius: '50%',
                          background: d.teamColour ? `${d.teamColour}30` : '#ffffff15',
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                          fontSize: '10px', fontWeight: 'bold', color: d.teamColour ?? '#aaa',
                        }}>
                          {d.nameAcronym?.slice(0, 2) ?? d.driverNumber}
                        </div>
                      )}
                      {d.teamColour && (
                        <div style={{ width: '3px', height: '28px', borderRadius: '2px', background: d.teamColour, flexShrink: 0 }} />
                      )}
                      <div style={{ flex: 1 }}>
                        <div style={{ color: 'white', fontSize: '14px', fontWeight: '500' }}>{d.fullName ?? d.nameAcronym}</div>
                        <div style={{ color: '#aaa', fontSize: '11px' }}>{d.teamName}</div>
                      </div>
                      <span style={{ fontSize: '11px', color: '#aaa', background: '#ffffff10', borderRadius: '4px', padding: '2px 6px' }}>#{d.driverNumber}</span>
                    </div>
                  ))}
                </div>
              )
            })()}
          </div>
        )}

        {/* Drivers */}
        <h2 className="text-lg font-bold mb-4">{t('f1.drivers')}</h2>
        {drivers.length === 0 ? (
          <p className="text-text-secondary mb-6">Žiadne dáta o jazdcoch</p>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 mb-8">
            {drivers.map((d) => (
              <div key={d.driverNumber} className="card p-3 flex flex-col items-center text-center">
                {d.headshotUrl ? (
                  <img src={d.headshotUrl} alt={d.fullName} className="w-14 h-14 rounded-full object-cover mb-2" />
                ) : (
                  <div className="w-14 h-14 rounded-full bg-surface-elevated flex items-center justify-center text-lg font-bold mb-2" style={{ color: d.teamColour ?? '#666' }}>
                    {d.nameAcronym ?? d.driverNumber}
                  </div>
                )}
                <p className="text-xs font-medium">{d.nameAcronym ?? d.driverNumber}</p>
                <p className="text-[10px] text-text-secondary truncate w-full">{d.fullName}</p>
                {d.teamColour && (
                  <div className="flex items-center gap-1 mt-1">
                    <span className="w-2 h-2 rounded-full inline-block" style={{ backgroundColor: d.teamColour }} />
                    <span className="text-[10px] text-text-secondary truncate">{d.teamName}</span>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Prediction form — Race, UPCOMING, unlocked */}
        {(session?.sessionType?.toUpperCase() === 'RACE') &&
         !session?.predictionsLocked &&
         session?.status === 'UPCOMING' && (
          <div style={{
            background: '#ffffff08',
            border: '1px solid #e10600',
            borderRadius: '12px',
            padding: '24px',
            margin: '24px 0'
          }}>
            <h3 style={{ color: '#e10600', marginBottom: '4px', fontSize: '18px', fontWeight: 'bold' }}>
              🏁 Tvoje predikcie
            </h3>
            <p style={{ color: '#aaa', fontSize: '13px', marginBottom: '20px' }}>
              Predikcie sa uzamknú pred kvalifikáciou. Maximum: 75 bodov.
            </p>

            {!user ? (
              <div style={{ background: '#ffffff08', border: '1px solid #ffffff20', borderRadius: '8px', padding: '20px', textAlign: 'center' }}>
                <p style={{ color: '#ccc', marginBottom: '12px' }}>{t('auth.loginToPredict')}</p>
                <Link to="/login" style={{
                  display: 'inline-block', background: '#e10600', color: 'white',
                  padding: '10px 24px', borderRadius: '8px', fontWeight: 'bold',
                  fontSize: '14px', textDecoration: 'none'
                }}>{t('auth.login')}</Link>
              </div>
            ) : (
              <>
                {PREDICTION_FIELDS.map(field => {
                  const info = FIELD_LABELS_SK[field]
                  return (
                    <div key={field} style={{ marginBottom: '14px' }}>
                      <label style={{ color: '#ccc', fontSize: '13px', display: 'block', marginBottom: '6px' }}>
                        {info.points === '+15 bodov' && '🏎️ '}
                        {info.points === '+20 bodov' && '🥇 '}
                        {info.points === '+10 bodov' && (field === 'predictedP2DriverNumber' ? '🥈 ' : '🥉 ')}
                        {info.label} — {info.points}
                      </label>
                      <select
                        value={(prediction as Record<string, number | null>)[field] ?? ''}
                        onChange={e => setPrediction(prev => ({
                          ...prev,
                          [field]: e.target.value ? parseInt(e.target.value) : null
                        }))}
                        style={{
                          width: '100%',
                          background: '#1a1a2e',
                          border: '1px solid #ffffff30',
                          borderRadius: '8px',
                          color: 'white',
                          padding: '10px 12px',
                          fontSize: '14px',
                          cursor: 'pointer',
                        }}
                      >
                        <option value="">— Vyber jazdca —</option>
                        {drivers.map(d => (
                          <option key={d.driverNumber} value={d.driverNumber}>
                            #{d.driverNumber} {d.fullName ?? d.nameAcronym} ({d.teamName})
                          </option>
                        ))}
                      </select>
                    </div>
                  )
                })}

                <div style={{ color: '#aaa', fontSize: '12px', marginBottom: '12px', padding: '8px 12px', background: '#ffffff08', borderRadius: '6px' }}>
                  💡 Správne P1+P2+P3 = bonus +20 bodov naviac
                </div>

                <button
                  onClick={handleSubmitPrediction}
                  disabled={saving}
                  style={{
                    width: '100%',
                    background: saving ? '#666' : '#e10600',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    padding: '12px',
                    fontSize: '15px',
                    fontWeight: 'bold',
                    cursor: saving ? 'not-allowed' : 'pointer',
                  }}
                >
                  {saving ? 'Ukladám...' : existingPrediction ? '✏️ Aktualizovať predikciu' : '✅ Odoslať predikciu'}
                </button>
              </>
            )}
          </div>
        )}

        {/* LOCKED */}
        {(session?.sessionType?.toUpperCase() === 'RACE') &&
         session?.predictionsLocked &&
         session?.status === 'UPCOMING' && (
          <div style={{
            background: '#ffffff08',
            border: '1px solid #ffffff20',
            borderRadius: '12px',
            padding: '20px',
            margin: '24px 0',
            textAlign: 'center',
          }}>
            <div style={{ fontSize: '32px', marginBottom: '8px' }}>🔒</div>
            <div style={{ color: '#aaa', fontSize: '14px' }}>Predikcie sú uzamknuté</div>
            {existingPrediction && (
              <div style={{ color: '#4CAF50', fontSize: '13px', marginTop: '8px' }}>
                ✅ Tvoja predikcia bola odoslaná
              </div>
            )}
          </div>
        )}

        {!isRace && session.status === 'UPCOMING' && (
          <p className="text-text-secondary text-sm mb-6">Predikcie sú dostupné len pre hlavné preteky (Race)</p>
        )}

        {/* Settled prediction results */}
        {existingPrediction?.status === 'SETTLED' && (
          <div className="card p-5">
            <h3 className="text-sm font-semibold mb-3">Výsledok predikcie</h3>
            <div className="text-sm space-y-1">
              {existingPrediction.pointsEarned > 0 ? (
                <p className="text-accent-primary font-bold">+{existingPrediction.pointsEarned} bodov</p>
              ) : (
                <p className="text-text-secondary">0 bodov — žiadna správna odpoveď</p>
              )}
            </div>
          </div>
        )}
      </div>
    </PageLayout>
  )
}
