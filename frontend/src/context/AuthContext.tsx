import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import api from '../lib/api'

interface WalletData {
  balance: number
  lolElo: number
  cs2Elo: number
  lifetimeWageredLol: number
  lifetimeWageredCs2: number
}

interface UserData {
  id: string
  username: string
  displayName?: string
  avatarUrl?: string
  email?: string
  preferredGame: string
  badge: string | null
  role: string
  wallet: WalletData | null
  lolRank: string | null
  cs2Rank: string | null
}

interface AuthContextType {
  user: UserData | null
  loading: boolean
  showSplash: boolean
  login: (username: string, password: string) => Promise<void>
  register: (username: string, password: string) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
  setShowSplash: (v: boolean) => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserData | null>(null)
  const [loading, setLoading] = useState(true)
  const [showSplash, setShowSplash] = useState(false)

  const fetchUser = useCallback(async () => {
    try {
      const res = await api.get('/api/auth/me')
      setUser(res.data)
    } catch {
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchUser()
  }, [fetchUser])

  const login = async (username: string, password: string) => {
    const res = await api.post('/api/auth/login', { username, password })
    const { userId, username: uname, role } = res.data
    setUser({ id: userId, username: uname, preferredGame: 'LOL', badge: null, role, wallet: null, lolRank: null, cs2Rank: null })
    setShowSplash(true)
    await fetchUser()
  }

  const register = async (username: string, password: string) => {
    const res = await api.post('/api/auth/register', { username, password })
    const { userId, username: uname, role } = res.data
    setUser({ id: userId, username: uname, preferredGame: 'LOL', badge: null, role, wallet: null, lolRank: null, cs2Rank: null })
    setShowSplash(true)
    await fetchUser()
  }

  const logout = async () => {
    try {
      await api.post('/api/auth/logout')
    } catch {
      // ignore
    }
    setUser(null)
  }

  const refreshUser = fetchUser

  return (
    <AuthContext.Provider value={{ user, loading, showSplash, login, register, logout, refreshUser, setShowSplash }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
