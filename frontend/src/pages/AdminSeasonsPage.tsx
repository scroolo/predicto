import { useEffect, useState } from 'react'
import api from '../lib/api'

interface SeasonItem {
  id: string
  name: string
  game: string
  type: string
  startsAt: string
  endsAt: string
  status: string
}

interface RewardItem {
  rankPosition: number
  userId: string
  displayName: string
  claimed: boolean
}

export default function AdminSeasonsPage() {
  const [seasons, setSeasons] = useState<SeasonItem[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ type: 'success' | 'error'; msg: string } | null>(null)
  const [showCreate, setShowCreate] = useState(false)
  const [createForm, setCreateForm] = useState({ name: '', game: 'LOL', type: 'MONTHLY', startsAt: '', endsAt: '' })
  const [creating, setCreating] = useState(false)
  const [rewards, setRewards] = useState<Record<string, RewardItem[]>>({})
  const [expandedRewards, setExpandedRewards] = useState<string | null>(null)

  const fetchSeasons = async () => {
    try {
      const res = await api.get('/api/seasons')
      setSeasons(res.data ?? [])
    } catch { /* ignore */ }
    setLoading(false)
  }

  useEffect(() => { fetchSeasons() }, [])

  const showToast = (type: 'success' | 'error', msg: string) => {
    setToast({ type, msg })
    setTimeout(() => setToast(null), 4000)
  }

  const statusColor = (s: string) => {
    const map: Record<string, string> = {
      UPCOMING: 'bg-status-scheduled/20 text-status-scheduled',
      ACTIVE: 'bg-status-live/20 text-status-live',
      CLOSED: 'bg-status-finished/20 text-status-finished',
    }
    return map[s] || 'bg-gray-700 text-gray-400'
  }

  const handleCreate = async () => {
    if (!createForm.name || !createForm.startsAt || !createForm.endsAt) { showToast('error', 'All fields required'); return }
    if (new Date(createForm.startsAt) >= new Date(createForm.endsAt)) { showToast('error', 'startsAt must be before endsAt'); return }
    setCreating(true)
    try {
      await api.post('/api/admin/seasons', {
        name: createForm.name,
        game: createForm.game,
        type: createForm.type,
        startsAt: new Date(createForm.startsAt).toISOString(),
        endsAt: new Date(createForm.endsAt).toISOString(),
      })
      showToast('success', 'Season created')
      setShowCreate(false)
      setCreateForm({ name: '', game: 'LOL', type: 'MONTHLY', startsAt: '', endsAt: '' })
      fetchSeasons()
    } catch (err: any) {
      showToast('error', err.response?.data?.message || 'Failed to create season')
    } finally { setCreating(false) }
  }

  const handleActivate = async (id: string) => {
    if (!window.confirm('This will set the season as active. Users\' bets placed from now will be linked to it.')) return
    try {
      await api.patch(`/api/admin/seasons/${id}/activate`)
      showToast('success', 'Season activated')
      fetchSeasons()
    } catch (err: any) { showToast('error', err.response?.data?.message || 'Failed to activate') }
  }

  const handleClose = async (id: string) => {
    if (!window.confirm('This will finalise the leaderboard and assign top-5 rewards. This cannot be undone.')) return
    try {
      await api.patch(`/api/admin/seasons/${id}/close`)
      showToast('success', 'Season closed')
      fetchSeasons()
    } catch (err: any) { showToast('error', err.response?.data?.message || 'Failed to close') }
  }

  const loadRewards = async (id: string) => {
    if (expandedRewards === id) { setExpandedRewards(null); return }
    try {
      const res = await api.get(`/api/seasons/${id}/rewards`)
      setRewards({ ...rewards, [id]: res.data ?? [] })
      setExpandedRewards(id)
    } catch { showToast('error', 'Failed to load rewards') }
  }

  const handleMarkClaimed = async (seasonId: string, rank: number) => {
    try {
      await api.post(`/api/admin/seasons/${seasonId}/rewards/${rank}/mark-claimed`)
      showToast('success', 'Reward marked as claimed')
      const res = await api.get(`/api/seasons/${seasonId}/rewards`)
      setRewards({ ...rewards, [seasonId]: res.data ?? [] })
    } catch (err: any) { showToast('error', err.response?.data?.message || 'Failed to mark claimed') }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Seasons</h1>
        <button onClick={() => setShowCreate(!showCreate)} className="btn-primary text-sm">
          {showCreate ? 'Cancel' : 'Create Season'}
        </button>
      </div>

      {toast && (
        <div className={`mb-4 px-4 py-2 rounded-lg text-sm ${
          toast.type === 'success' ? 'bg-green-900/30 text-green-400 border border-green-800' : 'bg-red-900/30 text-red-400 border border-red-800'
        }`}>
          {toast.msg}
        </div>
      )}

      {showCreate && (
        <div className="card mb-6 space-y-4">
          <h2 className="text-lg font-semibold">New Season</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs text-text-secondary mb-1">Name</label>
              <input value={createForm.name} onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })} className="input-field" placeholder="Season name" />
            </div>
            <div>
              <label className="block text-xs text-text-secondary mb-1">Game</label>
              <select value={createForm.game} onChange={(e) => setCreateForm({ ...createForm, game: e.target.value })} className="select-field">
                <option value="LOL">LOL</option>
                <option value="CS2">CS2</option>
              </select>
            </div>
            <div>
              <label className="block text-xs text-text-secondary mb-1">Type</label>
              <select value={createForm.type} onChange={(e) => setCreateForm({ ...createForm, type: e.target.value })} className="select-field">
                <option value="MONTHLY">Monthly</option>
                <option value="SEASONAL">Seasonal</option>
              </select>
            </div>
            <div>
              <label className="block text-xs text-text-secondary mb-1">Starts At</label>
              <input type="datetime-local" value={createForm.startsAt} onChange={(e) => setCreateForm({ ...createForm, startsAt: e.target.value })} className="input-field" />
            </div>
            <div>
              <label className="block text-xs text-text-secondary mb-1">Ends At</label>
              <input type="datetime-local" value={createForm.endsAt} onChange={(e) => setCreateForm({ ...createForm, endsAt: e.target.value })} className="input-field" />
            </div>
          </div>
          <button onClick={handleCreate} disabled={creating} className="btn-primary">{creating ? 'Creating...' : 'Create Season'}</button>
        </div>
      )}

      {loading ? (
        <p className="text-text-secondary text-center py-12">Loading...</p>
      ) : (
        <div className="space-y-2">
          {seasons.map((s) => (
            <div key={s.id}>
              <div className="bg-surface border border-border rounded-xl px-5 py-3.5 flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div>
                    <p className="font-medium">{s.name}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs text-text-secondary">{s.game}</span>
                      <span className="text-xs text-text-secondary">{s.type}</span>
                      <span className={`badge ${statusColor(s.status)}`}>{s.status}</span>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-text-secondary">
                    {new Date(s.startsAt).toLocaleDateString()} - {new Date(s.endsAt).toLocaleDateString()}
                  </span>
                  {s.status === 'UPCOMING' && (
                    <button onClick={() => handleActivate(s.id)} className="btn-primary text-xs px-3 py-1">Activate</button>
                  )}
                  {s.status === 'ACTIVE' && (
                    <button onClick={() => handleClose(s.id)} className="btn-danger text-xs px-3 py-1">Close Season</button>
                  )}
                  {s.status === 'CLOSED' && (
                    <button onClick={() => loadRewards(s.id)} className="btn-secondary text-xs px-3 py-1">
                      {expandedRewards === s.id ? 'Hide Rewards' : 'View Rewards'}
                    </button>
                  )}
                </div>
              </div>
              {expandedRewards === s.id && (
                <div className="bg-surface-elevated/50 border border-t-0 border-border rounded-b-xl p-4">
                  <h4 className="text-sm font-semibold text-text-secondary mb-3">Rewards</h4>
                  {(!rewards[s.id] || rewards[s.id].length === 0) ? (
                    <p className="text-sm text-text-secondary">No rewards for this season.</p>
                  ) : (
                    <div className="space-y-2 max-w-md">
                      {rewards[s.id].map((r) => (
                        <div key={r.rankPosition} className="flex items-center justify-between bg-surface-elevated rounded-lg px-4 py-2.5">
                          <div className="flex items-center gap-3">
                            <span className="text-accent-primary font-bold">#{r.rankPosition}</span>
                            <span className="text-sm">{r.displayName}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className={`badge text-xs ${r.claimed ? 'bg-green-900/30 text-green-400' : 'bg-amber-900/30 text-amber-400'}`}>
                              {r.claimed ? 'Claimed' : 'Pending'}
                            </span>
                            {!r.claimed && (
                              <button onClick={() => handleMarkClaimed(s.id, r.rankPosition)} className="text-xs text-accent-secondary hover:underline">Mark claimed</button>
                            )}
                          </div>
                        </div>
                      ))}
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
