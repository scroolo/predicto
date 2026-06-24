import { useEffect, useState } from 'react'

export type ToastType = 'success' | 'error' | 'info' | 'warning'

export interface ToastMessage {
  id: string
  message: string
  type: ToastType
  duration?: number
}

interface ToastProps {
  toasts: ToastMessage[]
  removeToast: (id: string) => void
}

const ICONS: Record<ToastType, string> = {
  success: '✅',
  error: '❌',
  info: 'ℹ️',
  warning: '⚠️',
}

const COLORS: Record<ToastType, { bg: string; border: string; color: string }> = {
  success: { bg: '#0d2818', border: '#4CAF50', color: '#4CAF50' },
  error: { bg: '#2d0a0a', border: '#e10600', color: '#ff6666' },
  info: { bg: '#0a1628', border: '#4781D7', color: '#7ab3f5' },
  warning: { bg: '#2d1a00', border: '#FF9800', color: '#FFB74D' },
}

function ToastItem({ toast, onRemove }: { toast: ToastMessage; onRemove: () => void }) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const t1 = setTimeout(() => setVisible(true), 10)
    const duration = toast.duration ?? 3500
    const t2 = setTimeout(() => setVisible(false), duration - 400)
    const t3 = setTimeout(() => onRemove(), duration)
    return () => { clearTimeout(t1); clearTimeout(t2); clearTimeout(t3) }
  }, [])

  const c = COLORS[toast.type]

  return (
    <div
      onClick={onRemove}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        padding: '12px 16px',
        background: c.bg,
        border: `1px solid ${c.border}`,
        borderRadius: '10px',
        cursor: 'pointer',
        minWidth: '280px',
        maxWidth: '400px',
        boxShadow: `0 4px 20px ${c.border}30`,
        opacity: visible ? 1 : 0,
        transform: visible ? 'translateX(0)' : 'translateX(40px)',
        transition: 'opacity 0.3s ease, transform 0.3s ease',
      }}
    >
      <span style={{ fontSize: '18px' }}>{ICONS[toast.type]}</span>
      <span style={{ color: 'white', fontSize: '14px', flex: 1 }}>{toast.message}</span>
      <span style={{ color: '#555', fontSize: '18px', lineHeight: 1 }}>×</span>
    </div>
  )
}

export default function ToastContainer({ toasts, removeToast }: ToastProps) {
  return (
    <div style={{
      position: 'fixed',
      bottom: '24px',
      right: '24px',
      display: 'flex',
      flexDirection: 'column',
      gap: '10px',
      zIndex: 9999,
    }}>
      {toasts.map(t => (
        <ToastItem key={t.id} toast={t} onRemove={() => removeToast(t.id)} />
      ))}
    </div>
  )
}
