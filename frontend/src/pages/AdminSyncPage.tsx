import { useEffect, useState } from 'react'
import api from '../lib/api'

interface SyncRunItem {
  id: string
  jobName: string
  startedAt: string
  finishedAt: string | null
  status: 'SUCCESS' | 'FAILED' | 'PARTIAL'
  itemsProcessed: number
  errorMessage: string | null
}

const jobs = [
  { key: 'catalog', label: 'Catalog', desc: 'Sync leagues, teams, and players' },
  { key: 'matches', label: 'Matches', desc: 'Sync upcoming matches' },
  { key: 'results', label: 'Results', desc: 'Sync match results' },
  { key: 'lock', label: 'Lock', desc: 'Auto-lock matches that are starting soon' },
]

export default function AdminSyncPage() {
  const [runs, setRuns] = useState<SyncRunItem[]>([])
  const [syncing, setSyncing] = useState<string | null>(null)
  const [toast, setToast] = useState<{ type: 'success' | 'error'; msg: string } | null>(null)

  const fetchRuns = async () => {
    try {
      const res = await api.get('/api/admin/sync/runs?limit=20')
      setRuns(res.data ?? [])
    } catch { /* ignore */ }
  }

  useEffect(() => { fetchRuns() }, [])

  const showToast = (type: 'success' | 'error', msg: string) => {
    setToast({ type, msg })
    setTimeout(() => setToast(null), 4000)
  }

  const handleSync = async (job: string) => {
    setSyncing(job)
    try {
      const res = await api.post('/api/admin/sync/trigger', {}, { params: { job } })
      showToast('success', `${job}: completed (${JSON.stringify(res.data)})`)
      fetchRuns()
    } catch (err: any) {
      showToast('error', `${job}: ${err.response?.data?.message || 'failed'}`)
    } finally { setSyncing(null) }
  }

  const statusColor = (s: string) => {
    const map: Record<string, string> = {
      SUCCESS: 'bg-green-900/30 text-green-400',
      FAILED: 'bg-red-900/30 text-red-400',
      PARTIAL: 'bg-amber-900/30 text-amber-400',
    }
    return map[s] || 'bg-gray-700 text-gray-400'
  }

  const formatTime = (iso: string) => new Date(iso).toLocaleString()

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Sync</h1>

      {toast && (
        <div className={`mb-4 px-4 py-2 rounded-lg text-sm ${
          toast.type === 'success' ? 'bg-green-900/30 text-green-400 border border-green-800' : 'bg-red-900/30 text-red-400 border border-red-800'
        }`}>
          {toast.msg}
        </div>
      )}

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        {jobs.map((j) => (
          <button
            key={j.key}
            onClick={() => handleSync(j.key)}
            disabled={syncing !== null}
            className="card text-left hover:border-accent-primary/50 transition flex flex-col items-center justify-center py-8"
          >
            <p className="text-lg font-semibold mb-1">{j.label}</p>
            <p className="text-xs text-text-secondary text-center">{j.desc}</p>
            {syncing === j.key && <p className="text-xs text-accent-primary mt-2">Syncing...</p>}
          </button>
        ))}
      </div>

      <div className="mb-8">
        <button
          onClick={async () => {
            setSyncing('history')
            try {
              const res = await api.post('/api/admin/sync/history')
              showToast('success', `✅ Hotovo: ${res.data.matchesUpdated} zápasov aktualizovaných`)
            } catch {
              showToast('error', '❌ Chyba pri historickom syncu')
            } finally { setSyncing(null) }
          }}
          disabled={syncing !== null}
          style={{
            background: '#9C27B0', color: 'white', border: 'none',
            borderRadius: '6px', padding: '8px 16px', cursor: 'pointer', fontSize: '14px'
          }}
        >
          📊 Sync historických výsledkov {syncing === 'history' ? '(syncing...)' : ''}
        </button>
      </div>

      <h2 className="text-lg font-semibold mb-4">Recent Sync Runs</h2>
      <div className="card">
        {runs.length === 0 ? (
          <p className="text-text-secondary text-center py-8">No sync runs yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-text-secondary text-xs uppercase">
                  <th className="table-header">Job</th>
                  <th className="table-header">Started</th>
                  <th className="table-header">Finished</th>
                  <th className="table-header">Status</th>
                  <th className="table-header">Items</th>
                  <th className="table-header">Error</th>
                </tr>
              </thead>
              <tbody>
                {runs.map((r) => (
                  <tr key={r.id} className="border-b border-border/50 hover:bg-surface-elevated/30 transition">
                    <td className="table-cell font-medium">{r.jobName}</td>
                    <td className="table-cell text-text-secondary">{formatTime(r.startedAt)}</td>
                    <td className="table-cell text-text-secondary">{r.finishedAt ? formatTime(r.finishedAt) : '\u2014'}</td>
                    <td className="table-cell"><span className={`badge ${statusColor(r.status)}`}>{r.status}</span></td>
                    <td className="table-cell font-mono">{r.itemsProcessed}</td>
                    <td className="table-cell text-text-secondary max-w-xs truncate">{r.errorMessage || '\u2014'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
