import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { SkeletonProfile } from '../components/Skeleton'

const rarityColors: Record<string, string> = {
  COMMON: 'border-gray-500/50 bg-gray-500/10',
  UNCOMMON: 'border-green-500/50 bg-green-500/10',
  RARE: 'border-blue-500/50 bg-blue-500/10',
  EPIC: 'border-purple-500/50 bg-purple-500/10',
  LEGENDARY: 'border-amber-500/50 bg-amber-500/10',
  MYTHIC: 'border-pink-500/50 bg-pink-500/10',
}

const BADGE_COLORS: Record<string, string> = {
  ROOKIE: '#9E9E9E',
  SILVER: '#C0C0C0',
  GOLD: '#FFD700',
  PLATINUM: '#4fc3f7',
  DIAMOND: '#b388ff',
  MASTER: '#e10600',
}

export default function ProfilePage() {
  const navigate = useNavigate()
  const { refreshUser } = useAuth()
  const [profile, setProfile] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState({ username: '', displayName: '' })
  const [saving, setSaving] = useState(false)
  const [achievements, setAchievements] = useState<any[]>([])
  const [allAchievements, setAllAchievements] = useState<any[]>([])

  useEffect(() => {
    (async () => {
      try {
        const [profileRes, achRes, allAchRes] = await Promise.all([
          api.get('/api/users/me/profile'),
          api.get('/api/achievements/me'),
          api.get('/api/achievements'),
        ])
        const data = profileRes.data
        setProfile(data)
        setForm({ username: data.username, displayName: data.displayName || '' })
        setAchievements(achRes.data ?? [])
        setAllAchievements(allAchRes.data ?? [])
      } catch {}
      setLoading(false)
    })()
  }, [])

  const handleSave = async () => {
    setSaving(true)
    try {
      const res = await api.patch('/api/users/me', form)
      setProfile((prev: any) => ({ ...prev, ...res.data }))
      await refreshUser()
      setEditing(false)
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <SkeletonProfile />

  if (!profile) return null

  const badgeColor = BADGE_COLORS[profile.badge] ?? '#9E9E9E'

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '24px 16px' }}>
      <button onClick={() => navigate(-1)} className="btn-secondary mb-4 flex items-center gap-2">
        ← Späť
      </button>
      {/* HEADER */}
      <div style={{
        background: 'linear-gradient(135deg, #0d1117 0%, #1a1f2e 100%)',
        border: '1px solid #ffffff15',
        borderRadius: '16px',
        padding: '32px',
        display: 'flex',
        alignItems: 'center',
        gap: '24px',
        marginBottom: '24px',
      }}>
        {profile.avatarUrl ? (
          <img
            src={profile.avatarUrl}
            alt={profile.username}
            style={{ width: '96px', height: '96px', borderRadius: '50%', border: `3px solid ${badgeColor}` }}
            onError={e => { (e.target as HTMLImageElement).src = '/avatar-placeholder.svg' }}
          />
        ) : (
          <div style={{
            width: '96px', height: '96px', borderRadius: '50%',
            border: `3px solid ${badgeColor}`,
            background: '#ffffff10',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#aaa" strokeWidth="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
        )}

        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '6px' }}>
            {editing ? (
              <input
                value={form.displayName}
                onChange={e => setForm(f => ({ ...f, displayName: e.target.value }))}
                style={{ fontSize: '24px', fontWeight: 'bold', background: '#ffffff10', border: '1px solid #ffffff30', borderRadius: '8px', padding: '4px 8px', color: 'white', width: '200px' }}
              />
            ) : (
              <h1 style={{ fontSize: '24px', fontWeight: 'bold', color: 'white', margin: 0 }}>
                {profile.displayName ?? profile.username}
              </h1>
            )}
            {profile.badge && (
              <span style={{
                padding: '3px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: 'bold',
                background: `${badgeColor}20`, border: `1px solid ${badgeColor}`, color: badgeColor,
              }}>
                {profile.badge}
              </span>
            )}
            {!editing && (
              <button onClick={() => setEditing(true)} style={{ background: 'none', border: 'none', color: '#aaa', cursor: 'pointer', fontSize: '14px', padding: '4px' }}>
                ✏️
              </button>
            )}
          </div>
          {editing ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ color: '#aaa', fontSize: '12px' }}>@</span>
                <input
                  value={form.username}
                  onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
                  style={{ background: '#ffffff10', border: '1px solid #ffffff30', borderRadius: '8px', padding: '4px 8px', color: 'white', fontSize: '14px', width: '150px' }}
                />
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button onClick={handleSave} disabled={saving} className="btn-primary text-sm">
                  {saving ? 'Saving...' : 'Save'}
                </button>
                <button onClick={() => { setEditing(false); setForm({ username: profile.username, displayName: profile.displayName || '' }) }} className="btn-secondary text-sm">
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <>
              <div style={{ color: '#aaa', fontSize: '14px', marginBottom: '8px' }}>
                @{profile.username}
              </div>
              <div style={{ color: '#555', fontSize: '12px' }}>
                Člen od {profile.createdAt ? new Date(profile.createdAt).toLocaleDateString('sk-SK', { month: 'long', year: 'numeric' }) : ''}
              </div>
            </>
          )}
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#FFD700' }}>
            {profile.balance?.toLocaleString()}
          </div>
          <div style={{ color: '#aaa', fontSize: '12px' }}>bodov</div>
        </div>
      </div>

      {/* STATS */}
      <div style={{
        display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px', marginBottom: '24px'
      }}>
        {[
          { label: 'Predikcie', value: profile.totalPredictions, color: 'white' },
          { label: 'Výhry', value: profile.wonPredictions, color: '#4CAF50' },
          { label: 'Win rate', value: `${profile.winRate}%`, color: profile.winRate >= 50 ? '#4CAF50' : '#FF9800' },
          { label: 'LoL ELO', value: profile.lolElo?.toLocaleString(), color: '#C89B3C' },
        ].map(({ label, value, color }) => (
          <div key={label} style={{
            background: '#ffffff08', border: '1px solid #ffffff10',
            borderRadius: '12px', padding: '16px', textAlign: 'center',
          }}>
            <div style={{ fontSize: '22px', fontWeight: 'bold', color, marginBottom: '4px' }}>{value}</div>
            <div style={{ color: '#aaa', fontSize: '12px' }}>{label}</div>
          </div>
        ))}
      </div>

      {/* ACHIEVEMENTS */}
      <div className="mt-8">
        <h2 className="text-xl font-bold mb-4">Achievementy</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {allAchievements.map(ach => {
            const unlocked = achievements.find((u: any) => u.achievement.id === ach.id)
            return (
              <div key={ach.id} className={`card p-3 border ${rarityColors[ach.rarity] || ''} ${!unlocked ? 'opacity-30 grayscale' : ''} transition-all`}>
                <div className="text-2xl mb-1">{ach.icon}</div>
                <div className="font-semibold text-sm">{ach.name}</div>
                <div className="text-xs text-text-secondary mt-1">{ach.description}</div>
                {unlocked && <div className="text-xs text-green-400 mt-1">✓ Odomknuté</div>}
                <div className="text-xs text-text-secondary mt-1">{ach.points} bodov</div>
              </div>
            )
          })}
        </div>
      </div>

      {/* RECENT PREDICTIONS */}
      <div style={{
        background: '#ffffff08', border: '1px solid #ffffff10',
        borderRadius: '12px', overflow: 'hidden',
      }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #ffffff10' }}>
          <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>Posledné predikcie</h3>
        </div>
        {profile.recentBets?.length === 0 ? (
          <div style={{ padding: '32px', textAlign: 'center', color: '#aaa' }}>
            Zatiaľ žiadne predikcie
          </div>
        ) : (
          profile.recentBets?.map((bet: any) => (
            <div key={bet.id} style={{
              display: 'flex', alignItems: 'center', gap: '12px',
              padding: '14px 20px', borderBottom: '1px solid #ffffff08',
            }}>
              <div style={{
                padding: '4px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold',
                background: bet.game === 'LOL' ? '#C89B3C20' : '#1b9cd820',
                color: bet.game === 'LOL' ? '#C89B3C' : '#1b9cd8',
                whiteSpace: 'nowrap',
              }}>
                {bet.game}
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ color: 'white', fontSize: '14px', fontWeight: '500' }}>{bet.matchTitle}</div>
                <div style={{ color: '#aaa', fontSize: '12px' }}>
                  Tip: {bet.predictedWinner ?? '—'}
                </div>
              </div>

              <div style={{ textAlign: 'right', minWidth: '60px' }}>
                <div style={{ color: '#aaa', fontSize: '12px' }}>{bet.stake} bodov</div>
              </div>

              <div style={{ textAlign: 'right', minWidth: '80px' }}>
                <span style={{
                  padding: '3px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold',
                  background: bet.pointsAwarded > 0 ? '#4CAF5020'
                    : bet.status === 'LOST' ? '#ff000020' : '#FF980020',
                  color: bet.pointsAwarded > 0 ? '#4CAF50'
                    : bet.status === 'LOST' ? '#ff6666' : '#FF9800',
                }}>
                  {bet.pointsAwarded > 0 ? `+${bet.pointsAwarded}`
                    : bet.status === 'LOST' ? 'LOST' : 'PENDING'}
                </span>
              </div>
            </div>
          ))
        )}
        <div style={{ padding: '12px 20px', borderTop: '1px solid #ffffff10', textAlign: 'center' }}>
          <Link to="/predictions" style={{ color: '#aaa', fontSize: '13px', textDecoration: 'none' }}>
            Zobraziť všetky predikcie →
          </Link>
        </div>
      </div>
    </div>
  )
}