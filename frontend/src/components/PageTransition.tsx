import { useEffect, useState, ReactNode } from 'react'
import { useLocation } from 'react-router-dom'

export default function PageTransition({ children }: { children: ReactNode }) {
  const location = useLocation()
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    setVisible(false)
    const t = setTimeout(() => setVisible(true), 50)
    return () => clearTimeout(t)
  }, [location.pathname])

  return (
    <div style={{
      opacity: visible ? 1 : 0,
      transform: visible ? 'translateY(0)' : 'translateY(8px)',
      transition: 'opacity 0.25s ease, transform 0.25s ease',
    }}>
      {children}
    </div>
  )
}
