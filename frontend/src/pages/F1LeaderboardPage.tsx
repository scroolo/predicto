import { useEffect, useState } from 'react'
import api from '../lib/api'

interface LeaderboardEntry {
  userId: number
  username: string
  avatarUrl?: string
  totalPoints: number
  totalPredictions: number
  correctPredictions: number
}

export default function F1LeaderboardPage() {
  const [entries, setEntries] = useState<LeaderboardEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/api/f1/leaderboard')
      .then(r => setEntries(r.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '60px' }}>
      <div style={{ color: '#aaa' }}>Načítavam...</div>
    </div>
  )

  const medals = ['🥇', '🥈', '🥉']

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '24px 16px' }}>
      <h1 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '10px' }}>
        🏎️ <span>F1 Leaderboard 2026</span>
      </h1>
      <p style={{ color: '#aaa', fontSize: '14px', marginBottom: '28px' }}>
        Max. 75 bodov za Grand Prix · Pole position, P1, P2, P3 + podium bonus
      </p>

      {entries.length === 0 ? (
        <div style={{ textAlign: 'center', color: '#aaa', padding: '48px' }}>
          Zatiaľ žiadne predikcie nie sú vyhodnotené.
        </div>
      ) : (
        <div style={{ background: '#ffffff08', borderRadius: '12px', overflow: 'hidden' }}>
          <div style={{
            display: 'grid',
            gridTemplateColumns: '48px 1fr 80px 80px 80px',
            padding: '10px 16px',
            borderBottom: '1px solid #ffffff15',
            color: '#aaa',
            fontSize: '11px',
            fontWeight: '600',
            letterSpacing: '0.05em',
          }}>
            <span>#</span>
            <span>HRÁČ</span>
            <span style={{ textAlign: 'right' }}>BODY</span>
            <span style={{ textAlign: 'right' }}>TIPOV</span>
            <span style={{ textAlign: 'right' }}>SPRÁVNE</span>
          </div>

          {entries.map((entry, idx) => (
            <div key={entry.userId} style={{
              display: 'grid',
              gridTemplateColumns: '48px 1fr 80px 80px 80px',
              alignItems: 'center',
              padding: '14px 16px',
              borderBottom: '1px solid #ffffff08',
              background: idx === 0 ? '#FFD70008' : idx === 1 ? '#C0C0C008' : idx === 2 ? '#CD7F3208' : 'transparent',
            }}>
              <span style={{ fontSize: idx < 3 ? '20px' : '14px', color: idx < 3 ? 'white' : '#aaa', fontWeight: 'bold' }}>
                {idx < 3 ? medals[idx] : `${idx + 1}.`}
              </span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                {entry.avatarUrl ? (
                  <img src={entry.avatarUrl} alt={entry.username} style={{ width: '32px', height: '32px', borderRadius: '50%' }} />
                ) : (
                  <div style={{
                    width: '32px', height: '32px', borderRadius: '50%',
                    background: '#e1060020', border: '1px solid #e10600',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: '#e10600', fontSize: '13px', fontWeight: 'bold'
                  }}>
                    {entry.username.charAt(0).toUpperCase()}
                  </div>
                )}
                <span style={{ color: 'white', fontSize: '14px', fontWeight: idx < 3 ? '600' : '400' }}>
                  {entry.username}
                </span>
              </div>
              <span style={{
                textAlign: 'right', fontWeight: 'bold', fontSize: '15px',
                color: idx === 0 ? '#FFD700' : idx === 1 ? '#C0C0C0' : idx === 2 ? '#CD7F32' : 'white'
              }}>
                {entry.totalPoints}
              </span>
              <span style={{ textAlign: 'right', color: '#aaa', fontSize: '13px' }}>{entry.totalPredictions}</span>
              <span style={{ textAlign: 'right', color: '#4CAF50', fontSize: '13px' }}>{entry.correctPredictions}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
