import { useEffect, useState } from 'react'

const ADS = [
  {
    label: 'PARTNER',
    bg: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)',
    border: '#e10600',
    text: '🎮 Hraj. Predikuj. Vyhraj.',
    sub: 'Predicto Premium — čoskoro',
    cta: 'Zistiť viac',
    pulse: '#e10600',
  },
  {
    label: 'REKLAMA',
    bg: 'linear-gradient(135deg, #0d1117 0%, #1a1f2e 100%)',
    border: '#4781D7',
    text: '🏎️ F1 2026 Sezóna',
    sub: 'Predikuj výsledky každého preteku',
    cta: 'Predikovať teraz',
    pulse: '#4781D7',
  },
  {
    label: 'SPONZOR',
    bg: 'linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%)',
    border: '#C89B3C',
    text: '⚔️ LEC Playoffs 2026',
    sub: 'Kto vyhrá európsku ligu?',
    cta: 'Predikuj',
    pulse: '#C89B3C',
  },
]

interface AdBannerProps {
  size?: 'horizontal' | 'vertical' | 'square'
  index?: number
}

export default function AdBanner({ size = 'horizontal', index }: AdBannerProps) {
  const [adIndex, setAdIndex] = useState(index ?? Math.floor(Math.random() * ADS.length))
  const [blink, setBlink] = useState(false)

  useEffect(() => {
    const interval = setInterval(() => {
      setBlink(b => !b)
    }, 1500)
    return () => clearInterval(interval)
  }, [])

  const ad = ADS[adIndex % ADS.length]

  const dimensions = size === 'horizontal'
    ? { width: '100%', height: '90px' }
    : size === 'vertical'
    ? { width: '160px', height: '320px' }
    : { width: '250px', height: '250px' }

  return (
    <div style={{
      ...dimensions,
      background: ad.bg,
      border: `1px solid ${ad.border}`,
      borderRadius: '8px',
      display: 'flex',
      flexDirection: size === 'horizontal' ? 'row' : 'column',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: size === 'horizontal' ? '0 20px' : '20px',
      position: 'relative',
      overflow: 'hidden',
      cursor: 'pointer',
      transition: 'border-color 0.3s',
    }}>
      {/* Pulsing corner dot */}
      <div style={{
        position: 'absolute',
        top: '8px',
        right: '8px',
        width: '8px',
        height: '8px',
        borderRadius: '50%',
        background: blink ? ad.pulse : 'transparent',
        border: `1px solid ${ad.pulse}`,
        transition: 'background 0.3s',
      }} />

      {/* Ad label */}
      <div style={{
        position: 'absolute',
        top: '6px',
        left: '8px',
        fontSize: '9px',
        color: '#555',
        letterSpacing: '0.1em',
        fontWeight: '600',
      }}>
        {ad.label}
      </div>

      {/* Content */}
      <div style={{ marginTop: size === 'horizontal' ? '0' : '16px' }}>
        <div style={{
          color: 'white',
          fontWeight: 'bold',
          fontSize: size === 'horizontal' ? '16px' : '18px',
          marginBottom: '4px',
        }}>
          {ad.text}
        </div>
        <div style={{ color: '#aaa', fontSize: '12px' }}>{ad.sub}</div>
      </div>

      {/* CTA Button */}
      <div style={{
        background: ad.pulse,
        color: size === 'horizontal' && ad.pulse === '#C89B3C' ? '#000' : 'white',
        padding: '8px 16px',
        borderRadius: '6px',
        fontSize: '13px',
        fontWeight: 'bold',
        whiteSpace: 'nowrap',
        marginTop: size === 'horizontal' ? '0' : '16px',
        boxShadow: blink ? `0 0 12px ${ad.pulse}60` : 'none',
        transition: 'box-shadow 0.3s',
      }}>
        {ad.cta}
      </div>
    </div>
  )
}
