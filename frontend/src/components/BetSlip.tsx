import { useTranslation } from 'react-i18next'

interface BetSlipProps {
  winnerTeam?: string
  exactScore?: string
  winnerStake: string
  scoreStake: string
  winnerOdds?: number
  scoreOdds?: number
  onPlaceBet: () => void
  onCancelBet?: () => void
  placing: boolean
  existingBet?: boolean
}

export default function BetSlip({
  winnerTeam, exactScore, winnerStake, scoreStake,
  winnerOdds, scoreOdds, onPlaceBet, onCancelBet,
  placing, existingBet,
}: BetSlipProps) {
  const { t } = useTranslation()
  const wStake = parseInt(winnerStake) || 0
  const sStake = parseInt(scoreStake) || 0
  const wReturn = winnerOdds && wStake > 0 ? Math.floor(wStake * winnerOdds) : 0
  const sReturn = scoreOdds && sStake > 0 ? Math.floor(sStake * scoreOdds) : 0
  const totalReturn = wReturn + sReturn
  const totalStake = wStake + sStake

  const hasBet = !!winnerTeam

  const content = (
    <div className="flex flex-col h-full">
      <h3 className="text-sm font-semibold text-text-primary mb-4">{t('match.betSlip')}</h3>
      {!hasBet ? (
        <div className="flex-1 flex flex-col items-center justify-center text-text-secondary text-sm">
          <span className="text-2xl mb-2">🎯</span>
          <p className="text-center">{t('match.selectOutcome')}</p>
        </div>
      ) : (
        <div className="flex-1 space-y-4">
          {winnerTeam && (
            <div className="bg-surface-elevated rounded-lg p-3 text-sm space-y-1">
              <div className="flex justify-between">
                <span className="text-text-secondary">{t('match.winner')}</span>
                <span className="font-medium">{winnerTeam}</span>
              </div>
              {winnerOdds && (
                <div className="flex justify-between">
                  <span className="text-text-secondary">{t('match.odds')}</span>
                  <span className="font-mono">{winnerOdds.toFixed(2)}</span>
                </div>
              )}
              {wReturn > 0 && (
                <div className="flex justify-between">
                  <span className="text-text-secondary">{t('match.return')}</span>
                  <span className="font-mono text-accent-primary">{wReturn.toLocaleString()} pts</span>
                </div>
              )}
            </div>
          )}
          {exactScore && (
            <div className="bg-surface-elevated rounded-lg p-3 text-sm space-y-1">
              <div className="flex justify-between">
                <span className="text-text-secondary">{t('match.score')}</span>
                <span className="font-medium">{exactScore}</span>
              </div>
              {scoreOdds && (
                <div className="flex justify-between">
                  <span className="text-text-secondary">{t('match.odds')}</span>
                  <span className="font-mono">{scoreOdds.toFixed(2)}</span>
                </div>
              )}
              {sReturn > 0 && (
                <div className="flex justify-between">
                  <span className="text-text-secondary">{t('match.return')}</span>
                  <span className="font-mono text-accent-primary">{sReturn.toLocaleString()} pts</span>
                </div>
              )}
            </div>
          )}
          {totalStake > 0 && (
            <div className="border-t border-border pt-3 space-y-1 text-sm">
              <div className="flex justify-between font-medium">
                <span>{t('match.totalStake')}</span>
                <span className="font-mono">{totalStake.toLocaleString()} pts</span>
              </div>
              <div className="flex justify-between font-medium text-accent-primary">
                <span>{t('match.totalReturn')}</span>
                <span className="font-mono">{totalReturn.toLocaleString()} pts</span>
              </div>
            </div>
          )}
        </div>
      )}
      <div className="mt-4 space-y-2">
        <button onClick={onPlaceBet} disabled={!hasBet || placing} className="btn-primary w-full text-sm">
          {placing ? t('match.processing') : existingBet ? t('match.updateBet') : t('match.submitPrediction')}
        </button>
        {existingBet && onCancelBet && (
          <button onClick={onCancelBet} disabled={placing} className="btn-danger w-full text-sm">{t('match.cancelBet')}</button>
        )}
      </div>
    </div>
  )

  // Desktop: sidebar panel; Mobile: we return the content and let the page decide
  return (
    <div className="card p-4 h-full sticky top-20">
      {content}
    </div>
  )
}

export function MobileBetBar({
  winnerStake, scoreStake, winnerOdds, scoreOdds, placing, hasBet, onPlaceBet,
}: {
  winnerStake: string; scoreStake: string; winnerOdds?: number; scoreOdds?: number
  placing: boolean; hasBet: boolean; onPlaceBet: () => void
}) {
  const { t } = useTranslation()
  const wStake = parseInt(winnerStake) || 0
  const sStake = parseInt(scoreStake) || 0
  const wReturn = winnerOdds && wStake > 0 ? Math.floor(wStake * winnerOdds) : 0
  const sReturn = scoreOdds && sStake > 0 ? Math.floor(sStake * scoreOdds) : 0
  const totalReturn = wReturn + sReturn
  const totalStake = wStake + sStake

  if (!hasBet) return null

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-surface border-t border-border p-3 flex items-center justify-between lg:hidden z-40">
      <div className="text-sm">
        <span className="text-text-secondary">{t('match.stakeLabel')}: </span>
        <span className="font-mono">{totalStake.toLocaleString()} pts</span>
        <span className="text-text-secondary mx-2">|</span>
        <span className="text-text-secondary">{t('match.returnLabel')}: </span>
        <span className="font-mono text-accent-primary">{totalReturn.toLocaleString()} pts</span>
      </div>
      <button onClick={onPlaceBet} disabled={placing} className="btn-primary text-sm px-6">
        {placing ? '...' : t('match.submitPrediction')}
      </button>
    </div>
  )
}
