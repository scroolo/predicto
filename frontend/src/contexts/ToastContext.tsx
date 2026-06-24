import { createContext, useContext, ReactNode } from 'react'
import { useToast } from '../hooks/useToast'
import ToastContainer from '../components/Toast'

interface ToastContextValue {
  toast: {
    success: (msg: string, duration?: number) => void
    error: (msg: string, duration?: number) => void
    info: (msg: string, duration?: number) => void
    warning: (msg: string, duration?: number) => void
  }
}

const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const { toasts, removeToast, toast } = useToast()
  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </ToastContext.Provider>
  )
}

export function useToastContext() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToastContext must be used within ToastProvider')
  return ctx
}
