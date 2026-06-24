import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'

export default function SplashScreen({ onFinish }: { onFinish: () => void }) {
  const { t } = useTranslation()
  const [fadeOut, setFadeOut] = useState(false)
  const finishedRef = useRef(false)
  const onFinishRef = useRef(onFinish)
  onFinishRef.current = onFinish

  useEffect(() => {
    const fadeTimer = setTimeout(() => setFadeOut(true), 1300)
    const finishTimer = setTimeout(() => {
      if (!finishedRef.current) {
        finishedRef.current = true
        onFinishRef.current()
      }
    }, 1800)
    const fallbackTimer = setTimeout(() => {
      if (!finishedRef.current) {
        finishedRef.current = true
        onFinishRef.current()
      }
    }, 2500)

    return () => {
      clearTimeout(fadeTimer)
      clearTimeout(finishTimer)
      clearTimeout(fallbackTimer)
    }
  }, [])

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-bg transition-opacity duration-500" style={{ zIndex: 9999 }} data-opacity={fadeOut ? 0 : 1}>
      <div style={{ textAlign: 'center', opacity: fadeOut ? 0 : 1, transition: 'opacity 500ms' }}>
        <img
          src="/logo.png"
          alt={t('auth.predicto')}
          style={{
            width: 'min(700px, 90vw)',
            height: 'auto',
            objectFit: 'contain'
          }}
        />
            <p style={{
              color: '#3b82f6',
              letterSpacing: '0.3em',
              fontSize: '14px',
              marginTop: '20px',
              fontWeight: 600,
              textAlign: 'center',
              background: 'linear-gradient(90deg, #3b82f6, #8b5cf6)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}>
              PREDICT. COMPETE. WIN.
            </p>
          </div>
    </div>
  )
}
