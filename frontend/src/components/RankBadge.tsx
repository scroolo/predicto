const LOL_RANKS: Record<string, { color: string; accent: string }> = {
  Rookie:     { color: '#6b7280', accent: '#9ca3af' },
  Silver:     { color: '#94a3b8', accent: '#cbd5e1' },
  Gold:       { color: '#f59e0b', accent: '#fcd34d' },
  Diamond:    { color: '#3b82f6', accent: '#93c5fd' },
  Master:     { color: '#8b5cf6', accent: '#c4b5fd' },
  Challenger: { color: '#f97316', accent: '#fdba74' },
}

const FACEIT_COLORS: Record<number, string> = {
  1: '#808080', 2: '#808080', 3: '#FFCC00', 4: '#FFCC00',
  5: '#FF8C00', 6: '#FF8C00', 7: '#FF4500', 8: '#FF4500',
  9: '#FF0000', 10: '#AA00FF',
}

interface RankBadgeProps {
  rank: string | null
  game: 'LOL' | 'CS2'
  size?: number
}

export default function RankBadge({ rank, game, size = 32 }: RankBadgeProps) {
  if (!rank) return null

  if (game === 'LOL') {
    const config = LOL_RANKS[rank]
    if (!config) return null
    const shieldH = Math.round(size * 1.125)
    const fontSize = Math.round(size * 0.34)
    return (
      <svg width={size} height={shieldH} viewBox="0 0 32 36" style={{ display: 'block' }}>
        <path d="M16 2 L30 8 L30 20 Q30 30 16 34 Q2 30 2 20 L2 8 Z" fill={config.color} stroke={config.accent} strokeWidth="1.5" />
        <text x="16" y="22" textAnchor="middle" fontSize={fontSize} fontWeight="bold" fill="white">
          {rank.substring(0, 2).toUpperCase()}
        </text>
      </svg>
    )
  }

  const level = parseInt((rank ?? '').replace('FACEIT ', ''), 10)
  if (isNaN(level)) return null
  const bg = FACEIT_COLORS[level] || '#808080'
  const innerSize = Math.round(size * 0.75)
  const fontSize = Math.round(size * 0.44)
  return (
    <div
      style={{
        width: innerSize, height: innerSize, borderRadius: '50%',
        background: bg, display: 'flex', alignItems: 'center',
        justifyContent: 'center', color: 'white', fontWeight: 'bold',
        fontSize, border: '2px solid rgba(255,255,255,0.2)',
      }}
    >
      {level}
    </div>
  )
}
