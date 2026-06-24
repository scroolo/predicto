import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import api from '../lib/api'

export default function AdminF1Page() {
  const { t } = useTranslation()
  const [year, setYear] = useState(2026)
  const [syncing, setSyncing] = useState(false)
  const [syncResult, setSyncResult] = useState<string | null>(null)
  const [sessionId, setSessionId] = useState('')
  const [lockMsg, setLockMsg] = useState<string | null>(null)
  const [settleForm, setSettleForm] = useState({
    sessionId: '',
    resultPoleDriverNumber: '',
    resultP1DriverNumber: '',
    resultP2DriverNumber: '',
    resultP3DriverNumber: '',
  })
  const [settleMsg, setSettleMsg] = useState<string | null>(null)

  const handleSync = async () => {
    setSyncing(true)
    setSyncResult(null)
    try {
      const res = await api.post('/api/admin/f1/sync', null, { params: { year } })
      setSyncResult(`Synced ${res.data.items} items`)
    } catch {
      setSyncResult('Sync failed')
    }
    setSyncing(false)
  }

  const handleLock = async () => {
    if (!sessionId) return
    setLockMsg(null)
    try {
      await api.post(`/api/admin/f1/sessions/${sessionId}/lock`)
      setLockMsg('Session locked')
    } catch {
      setLockMsg('Failed to lock')
    }
  }

  const handleSettle = async () => {
    if (!settleForm.sessionId) return
    setSettleMsg(null)
    try {
      const body: Record<string, number | null> = {}
      if (settleForm.resultPoleDriverNumber) body.resultPoleDriverNumber = Number(settleForm.resultPoleDriverNumber)
      if (settleForm.resultP1DriverNumber) body.resultP1DriverNumber = Number(settleForm.resultP1DriverNumber)
      if (settleForm.resultP2DriverNumber) body.resultP2DriverNumber = Number(settleForm.resultP2DriverNumber)
      if (settleForm.resultP3DriverNumber) body.resultP3DriverNumber = Number(settleForm.resultP3DriverNumber)
      const res = await api.post(`/api/admin/f1/sessions/${settleForm.sessionId}/settle`, body)
      setSettleMsg(`Settled: ${res.data.predictionsSettled} predictions`)
    } catch {
      setSettleMsg('Failed to settle')
    }
  }

  return (
    <div>
      <h1 className="text-xl font-bold mb-6">🏎️ F1 Admin</h1>

      {/* Sync */}
      <div className="card p-5 mb-6">
        <h3 className="text-sm font-semibold mb-3">Sync F1 Data</h3>
        <div className="flex items-center gap-3">
          <input
            type="number"
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="bg-surface border border-border rounded-lg px-3 py-2 text-sm w-24"
          />
          <button onClick={handleSync} disabled={syncing} className="btn-primary text-sm">
            {syncing ? 'Syncing...' : 'Sync'}
          </button>
        </div>
        {syncResult && <p className="text-xs text-text-secondary mt-2">{syncResult}</p>}
      </div>

      {/* Lock */}
      <div className="card p-5 mb-6">
        <h3 className="text-sm font-semibold mb-3">Lock Session</h3>
        <div className="flex items-center gap-3">
          <input
            type="text"
            placeholder="Session UUID"
            value={sessionId}
            onChange={(e) => setSessionId(e.target.value)}
            className="bg-surface border border-border rounded-lg px-3 py-2 text-sm flex-1"
          />
          <button onClick={handleLock} className="btn-primary text-sm">Lock</button>
        </div>
        {lockMsg && <p className="text-xs text-text-secondary mt-2">{lockMsg}</p>}
      </div>

      {/* Settle */}
      <div className="card p-5">
        <h3 className="text-sm font-semibold mb-3">Settle Session</h3>
        <div className="grid grid-cols-2 gap-3 max-w-md">
          <div className="col-span-2">
            <input
              type="text"
              placeholder="Session UUID"
              value={settleForm.sessionId}
              onChange={(e) => setSettleForm({ ...settleForm, sessionId: e.target.value })}
              className="w-full bg-surface border border-border rounded-lg px-3 py-2 text-sm"
            />
          </div>
          <input type="number" placeholder="Pole #" value={settleForm.resultPoleDriverNumber} onChange={(e) => setSettleForm({ ...settleForm, resultPoleDriverNumber: e.target.value })} className="bg-surface border border-border rounded-lg px-3 py-2 text-sm" />
          <input type="number" placeholder="P1 #" value={settleForm.resultP1DriverNumber} onChange={(e) => setSettleForm({ ...settleForm, resultP1DriverNumber: e.target.value })} className="bg-surface border border-border rounded-lg px-3 py-2 text-sm" />
          <input type="number" placeholder="P2 #" value={settleForm.resultP2DriverNumber} onChange={(e) => setSettleForm({ ...settleForm, resultP2DriverNumber: e.target.value })} className="bg-surface border border-border rounded-lg px-3 py-2 text-sm" />
          <input type="number" placeholder="P3 #" value={settleForm.resultP3DriverNumber} onChange={(e) => setSettleForm({ ...settleForm, resultP3DriverNumber: e.target.value })} className="bg-surface border border-border rounded-lg px-3 py-2 text-sm" />
          <div className="col-span-2">
            <button onClick={handleSettle} className="btn-primary text-sm">Settle</button>
          </div>
        </div>
        {settleMsg && <p className="text-xs text-text-secondary mt-2">{settleMsg}</p>}
      </div>
    </div>
  )
}
