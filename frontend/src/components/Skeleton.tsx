export function SkeletonRow() {
  return (
    <div className="flex items-center gap-3 px-4 py-3 border-b border-border/50 animate-skeleton">
      <div className="w-14 h-10 bg-surface-elevated rounded" />
      <div className="flex-1 space-y-2">
        <div className="h-3 w-24 bg-surface-elevated rounded" />
        <div className="h-3 w-48 bg-surface-elevated rounded" />
      </div>
      <div className="flex gap-2">
        <div className="w-16 h-10 bg-surface-elevated rounded-lg" />
        <div className="w-16 h-10 bg-surface-elevated rounded-lg" />
        <div className="w-14 h-10 bg-surface-elevated rounded-lg" />
      </div>
    </div>
  )
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div>
      {Array.from({ length: rows }).map((_, i) => (
        <SkeletonRow key={i} />
      ))}
    </div>
  )
}

export function SkeletonCard() {
  return (
    <div className="card p-5 space-y-4 animate-skeleton">
      <div className="h-4 w-32 bg-surface-elevated rounded" />
      <div className="h-3 w-full bg-surface-elevated rounded" />
      <div className="h-3 w-3/4 bg-surface-elevated rounded" />
    </div>
  )
}

interface SkeletonProps {
  width?: string
  height?: string
  borderRadius?: string
  style?: React.CSSProperties
}

export function SkeletonBox({ width = '100%', height = '16px', borderRadius = '6px', style }: SkeletonProps) {
  return (
    <div style={{
      width, height, borderRadius,
      background: 'linear-gradient(90deg, #ffffff08 25%, #ffffff15 50%, #ffffff08 75%)',
      backgroundSize: '200% 100%',
      animation: 'shimmer 1.5s infinite',
      ...style,
    }} />
  )
}

export function SkeletonStyles() {
  return (
    <style>{`
      @keyframes shimmer {
        0% { background-position: 200% 0; }
        100% { background-position: -200% 0; }
      }
    `}</style>
  )
}

export function SkeletonProfile() {
  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '24px 16px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <SkeletonStyles />
      <div style={{ background: '#ffffff08', borderRadius: '16px', padding: '32px', display: 'flex', gap: '24px', alignItems: 'center' }}>
        <SkeletonBox width="96px" height="96px" borderRadius="50%" />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <SkeletonBox height="24px" width="200px" />
          <SkeletonBox height="14px" width="120px" />
          <SkeletonBox height="12px" width="150px" />
        </div>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px' }}>
        {[1,2,3,4].map(i => <SkeletonCard key={i} />)}
      </div>
    </div>
  )
}
