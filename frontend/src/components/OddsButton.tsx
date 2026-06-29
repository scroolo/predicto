interface OddsButtonProps {
  label: string
  odds: number
  selected?: boolean
  onClick?: () => void
  size?: 'sm' | 'md'
}

export default function OddsButton({ label, odds, selected, onClick, size = 'md' }: OddsButtonProps) {
  const pad = size === 'sm' ? 'px-2 py-1.5' : 'px-3 py-2.5'
  return (
    <button
      onClick={onClick}
      className={`flex flex-col items-center justify-center rounded-lg border transition cursor-pointer min-h-[44px] ${pad} ${
        selected
          ? 'bg-accent-glow border-accent-primary text-accent-primary font-semibold'
          : 'bg-surface border-border text-text-primary hover:border-accent-primary hover:text-accent-primary'
      }`}
    >
      <span className={`leading-tight ${size === 'sm' ? 'text-[10px]' : 'text-xs'}`}>{label}</span>
    </button>
  )
}
