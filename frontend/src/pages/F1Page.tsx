import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'
import AdBanner from '../components/AdBanner'

interface F1Meeting {
  id: string
  meetingKey: number
  meetingName: string
  countryName: string | null
  countryFlagUrl: string | null
  circuitShortName: string | null
  circuitImageUrl: string | null
  dateStart: string
  dateEnd: string | null
  isCancelled: boolean
}

export default function F1Page() {
  const { t } = useTranslation()
  const [meetings, setMeetings] = useState<F1Meeting[]>([])
  const [loading, setLoading] = useState(true)
  const [year, setYear] = useState(2026)

  useEffect(() => {
    (async () => {
      setLoading(true)
      try {
        const res = await api.get('/api/f1/meetings', { params: { year } })
        setMeetings(res.data ?? [])
      } catch {
        setMeetings([])
      }
      setLoading(false)
    })()
  }, [year])

  const statusBadge = (m: F1Meeting) => {
    const now = new Date()
    const start = new Date(m.dateStart)
    const end = m.dateEnd ? new Date(m.dateEnd) : null
    if (m.isCancelled) return <span className="badge bg-red-500/20 text-red-400">CANCELLED</span>
    if (end && now > end) return <span className="badge bg-surface-elevated text-text-secondary">FINISHED</span>
    if (now >= start) return <span className="badge bg-green-500/20 text-green-400">LIVE</span>
    return <span className="badge bg-accent-primary/20 text-accent-primary">UPCOMING</span>
  }

  const formatDate = (iso: string) => {
    return new Date(iso).toLocaleDateString('sk-SK', { day: 'numeric', month: 'long', year: 'numeric' })
  }

  return (
    <PageLayout>
      <div className="max-w-6xl mx-auto px-4 py-5">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-4">
            <h1 className="text-2xl font-bold">🏎️ {t('f1.title')}</h1>
            <Link to="/f1/leaderboard" className="text-sm text-accent-primary hover:underline">🏆 Rebríček</Link>
          </div>
          <select
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="bg-surface border border-border rounded-lg px-3 py-2 text-sm text-text-primary"
          >
            <option value={2026}>2026</option>
            <option value={2025}>2025</option>
            <option value={2024}>2024</option>
            <option value={2023}>2023</option>
          </select>
        </div>

        <div style={{ margin: '16px 0' }}>
          <AdBanner size="horizontal" index={1} />
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1,2,3,4,5,6].map(i => (
              <div key={i} className="card p-4 animate-skeleton">
                <div className="h-32 bg-surface-elevated rounded-lg mb-3" />
                <div className="h-4 w-3/4 bg-surface-elevated rounded mb-2" />
                <div className="h-3 w-1/2 bg-surface-elevated rounded" />
              </div>
            ))}
          </div>
        ) : meetings.length === 0 ? (
          <div className="text-center py-16 text-text-secondary">
            <p className="text-4xl mb-3">🏎️</p>
            <p>{t('home.ziadne_zapasy')}</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {meetings.map((m) => (
              <Link
                key={m.id}
                to={`/f1/meetings/${m.id}`}
                className="card card-hover overflow-hidden group"
              >
                <div className="h-32 bg-surface-elevated relative overflow-hidden">
                  {m.circuitImageUrl ? (
                    <img src={m.circuitImageUrl} alt={m.circuitShortName ?? ''} className="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-3xl">🏁</div>
                  )}
                  <div className="absolute top-2 right-2">{statusBadge(m)}</div>
                  {m.countryFlagUrl && (
                    <img src={m.countryFlagUrl} alt="" className="absolute top-2 left-2 w-8 h-5 rounded shadow" />
                  )}
                </div>
                <div className="p-4">
                  <h3 className="font-bold text-sm mb-1">{m.meetingName}</h3>
                  <p className="text-xs text-text-secondary">
                    {m.countryName && `${m.countryName} — `}{m.circuitShortName}
                  </p>
                  <p className="text-xs text-text-secondary mt-1">
                    {formatDate(m.dateStart)}{m.dateEnd ? ` — ${formatDate(m.dateEnd)}` : ''}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </PageLayout>
  )
}
