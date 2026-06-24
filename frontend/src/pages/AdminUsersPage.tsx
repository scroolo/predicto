import { useEffect, useState, useCallback } from 'react'
import api from '../lib/api'
import { useAuth } from '../context/AuthContext'

interface UserItem {
  id: string
  username: string
  displayName: string
  email: string | null
  role: string
  balance: number
  createdAt: string
}

const roleColors: Record<string, string> = {
  USER: 'bg-gray-500/20 text-gray-400',
  EDITOR: 'bg-blue-500/20 text-blue-400',
  ADMIN: 'bg-amber-500/20 text-amber-400',
}

export default function AdminUsersPage() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState<UserItem[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [savingId, setSavingId] = useState<string | null>(null)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  const fetchUsers = useCallback(async (p: number, s: string) => {
    setLoading(true)
    try {
      const params: Record<string, any> = { page: p, size: 20 }
      if (s.trim()) params.search = s.trim()
      const res = await api.get('/api/admin/users', { params })
      setUsers(res.data?.content ?? [])
      setTotalPages(res.data?.totalPages ?? 0)
    } catch {
      setUsers([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchUsers(page, search)
  }, [page, fetchUsers])

  const handleSearch = () => {
    setPage(0)
    fetchUsers(0, search)
  }

  const handleRoleChange = async (userId: string, newRole: string) => {
    setMessage(null)
    setSavingId(userId)
    try {
      const res = await api.patch(`/api/admin/users/${userId}/role`, { role: newRole })
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: res.data.role } : u))
      setMessage({ type: 'success', text: 'Role updated successfully' })
    } catch (err: any) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update role' })
    } finally {
      setSavingId(null)
    }
  }

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">Users</h1>

      {message && (
        <div className={`mb-4 px-4 py-3 rounded-lg text-sm ${
          message.type === 'success'
            ? 'bg-green-900/30 text-green-400 border border-green-800/50'
            : 'bg-red-900/30 text-red-400 border border-red-800/50'
        }`}>
          {message.text}
        </div>
      )}

      <div className="flex gap-2 mb-4">
        <input
          className="input-field max-w-xs"
          placeholder="Search by username or display name..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch} className="btn-primary text-sm">Search</button>
      </div>

      {loading && users.length === 0 ? (
        <p className="text-text-secondary">Loading...</p>
      ) : users.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-text-secondary">
          <span className="text-3xl mb-3">👤</span>
          <p className="text-sm">No users found</p>
        </div>
      ) : (
        <div className="bg-surface border border-border rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-text-secondary text-xs uppercase">
                <th className="table-header w-10"></th>
                <th className="table-header">Username</th>
                <th className="table-header hidden sm:table-cell">Display Name</th>
                <th className="table-header hidden md:table-cell">Email</th>
                <th className="table-header">Role</th>
                <th className="table-header text-right">Balance</th>
                <th className="table-header hidden md:table-cell">Joined</th>
                <th className="table-header">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => {
                const isMe = u.id === currentUser?.id
                return (
                  <tr key={u.id} className={`border-b border-border/30 ${isMe ? 'bg-accent-glow' : 'hover:bg-surface-elevated/30'} transition`}>
                    <td className="table-cell">
                      <div className="w-7 h-7 rounded-full bg-accent-glow flex items-center justify-center text-accent-primary font-bold text-xs">
                        {u.username?.charAt(0)?.toUpperCase() ?? '?'}
                      </div>
                    </td>
                    <td className="table-cell font-medium">{u.username}</td>
                    <td className="table-cell text-text-secondary hidden sm:table-cell">{u.displayName}</td>
                    <td className="table-cell text-text-secondary hidden md:table-cell">{u.email ?? '—'}</td>
                    <td className="table-cell">
                      <span className={`badge text-[10px] ${roleColors[u.role] || 'bg-gray-500/20 text-gray-400'}`}>
                        {u.role}
                      </span>
                    </td>
                    <td className="table-cell text-right font-mono">{(u.balance ?? 0).toLocaleString()}</td>
                    <td className="table-cell text-text-secondary hidden md:table-cell">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                    </td>
                    <td className="table-cell">
                      {isMe ? (
                        <span className="text-xs text-text-secondary italic">(you)</span>
                      ) : u.role === 'ADMIN' ? (
                        <span className="text-xs text-text-secondary">—</span>
                      ) : (
                        <select
                          value={u.role}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          disabled={savingId === u.id}
                          className="select-field text-xs py-1 w-24"
                        >
                          <option value="USER">USER</option>
                          <option value="EDITOR">EDITOR</option>
                        </select>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-4">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="btn-secondary text-sm !px-3 !py-1.5 disabled:opacity-30"
          >
            Previous
          </button>
          <span className="text-xs text-text-secondary">Page {page + 1} of {totalPages}</span>
          <button
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="btn-secondary text-sm !px-3 !py-1.5 disabled:opacity-30"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
