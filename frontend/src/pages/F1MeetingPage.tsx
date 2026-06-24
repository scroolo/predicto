import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'
import PageLayout from '../components/PageLayout'

interface F1Session {
  id: string
  sessionKey: number
  sessionName: string
  sessionType: string
  dateStart: string
  dateEnd: string | null
  status: string
}

interface F1Meeting {
  id: string
  meetingName: string
  countryName: string | null
  countryFlagUrl: string | null
  circuitShortName: string | null
  circuitImageUrl: string | null
  dateStart: string
  dateEnd: string | null
}

export default function F1MeetingPage() {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const [meeting, setMeeting] = useState<F1Meeting | null>(null)
  const [sessions, setSessions] = useState<F1Session[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    (async () => {
      try {
        const [meetingsRes, sessionsRes] = await Promise.all([
          api.get('/api/f1/meetings', { params: { year: 2026 } }),
          api.get(`/api/f1/meetings/${id}/sessions`),
        ])
        const m = (meetingsRes.data ?? []).find((m: F1Meeting) => m.id === id)
        setMeeting(m ?? null)
        setSessions(sessionsRes.data ?? [])
      } catch { /* ignore */ }
      setLoading(false)
    })()
  }, [id])

  const statusBadge = (s: F1Session) => {
    switch (s.status) {
      case 'UPCOMING': return <span className="badge bg-accent-primary/20 text-accent-primary">UPCOMING</span>
      case 'LIVE': return <span className="badge bg-green-500/20 text-green-400">LIVE</span>
      case 'FINISHED': return <span className="badge bg-surface-elevated text-text-secondary">FINISHED</span>
      case 'CANCELLED': return <span className="badge bg-red-500/20 text-red-400">CANCELLED</span>
      default: return null
    }
  }

  const formatDate = (iso: string) => {
    return new Date(iso).toLocaleDateString('sk-SK', { day: 'numeric', month: 'long', year: 'numeric' })
  }

  const formatTime = (iso: string) => {
    return new Date(iso).toLocaleTimeString('sk-SK', { hour: '2-digit', minute: '2-digit' })
  }

  if (loading) {
    return (
      <PageLayout>
        <div className="max-w-4xl mx-auto px-4 py-5">
          <div className="card p-6 animate-skeleton">
            <div className="h-8 w-1/2 bg-surface-elevated rounded mb-4" />
            <div className="h-4 w-1/3 bg-surface-elevated rounded" />
          </div>
        </div>
      </PageLayout>
    )
  }

  if (!meeting) {
    return (
      <PageLayout>
        <div className="max-w-4xl mx-auto px-4 py-5 text-center text-text-secondary">
          Meeting not found
        </div>
      </PageLayout>
    )
  }

  return (
    <PageLayout>
      <div className="max-w-4xl mx-auto px-4 py-5">
        <Link to="/f1" className="text-sm text-accent-primary hover:underline mb-4 inline-block">&larr; {t('f1.title')}</Link>

        <div className="card p-6 mb-6">
          <div className="flex items-center gap-4">
            {meeting.countryFlagUrl && (
              <img src={meeting.countryFlagUrl} alt="" className="w-12 h-8 rounded shadow" />
            )}
            <div>
              <h1 className="text-2xl font-bold">{meeting.meetingName}</h1>
              <p className="text-sm text-text-secondary mt-1">
                {meeting.countryName && `${meeting.countryName} — `}{meeting.circuitShortName}
              </p>
              <p className="text-xs text-text-secondary mt-1">
                {formatDate(meeting.dateStart)}{meeting.dateEnd ? ` — ${formatDate(meeting.dateEnd)}` : ''}
              </p>
            </div>
          </div>
        </div>

        <h2 className="text-lg font-bold mb-4">{t('f1.sessions')}</h2>
        {sessions.length === 0 ? (
          <p className="text-text-secondary">No sessions found</p>
        ) : (
          <div className="space-y-2">
            {sessions.map((s) => (
              <Link
                key={s.id}
                to={`/f1/sessions/${s.id}`}
                className="card p-4 flex items-center justify-between hover:border-accent-primary/40 transition group"
              >
                <div>
                  <p className="font-medium text-sm">{s.sessionName}</p>
                  <p className="text-xs text-text-secondary mt-1">
                    {formatDate(s.dateStart)} {formatTime(s.dateStart)}
                  </p>
                </div>
                {statusBadge(s)}
              </Link>
            ))}
          </div>
        )}
      </div>
    </PageLayout>
  )
}
