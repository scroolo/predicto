package com.predicto.betting;

import com.predicto.catalog.Match;
import com.predicto.common.enums.BetStatus;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.SeasonStatus;
import com.predicto.season.LeaderboardService;
import com.predicto.season.Season;
import com.predicto.season.SeasonRepository;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BetRepository betRepository;
    private final WalletRepository walletRepository;
    private final LeaderboardService leaderboardService;
    private final SeasonRepository seasonRepository;

    @Transactional
    public void settleMatch(Match match) {
        List<Bet> pendingBets = betRepository.findByMatchIdAndStatus(match.getId(), BetStatus.PENDING);

        for (Bet bet : pendingBets) {
            int pointsAwarded = 0;
            int payout = 0;

            if (bet.getWinnerTeam() != null
                    && bet.getWinnerTeam().getId().equals(match.getResultWinnerTeam().getId())) {
                pointsAwarded += 1;
                payout += (int) Math.floor(bet.getStake() * bet.getWinnerOddsSnapshot().doubleValue());
            }

            if (bet.getExactScore() != null
                    && bet.getExactScore().equals(match.getResultScore())) {
                pointsAwarded += 2;
                payout += (int) Math.floor(bet.getScoreStake() * bet.getScoreOddsSnapshot().doubleValue());
            }

            if (bet.getMvpPlayer() != null
                    && match.getResultMvpPlayer() != null
                    && bet.getMvpPlayer().getId().equals(match.getResultMvpPlayer().getId())) {
                pointsAwarded += 2;
            }

            if (payout > 0) {
                Wallet wallet = walletRepository.findByUserId(bet.getUser().getId())
                        .orElse(null);
                if (wallet != null) {
                    wallet.setBalance(wallet.getBalance() + payout);
                    walletRepository.save(wallet);
                }
            }

            bet.setPointsAwarded(pointsAwarded);
            bet.setActualReturn(payout);
            bet.setStatus(pointsAwarded > 0 ? BetStatus.WON : BetStatus.LOST);
            bet.setSettledAt(OffsetDateTime.now());
            betRepository.save(bet);

            boolean winnerCorrect = bet.getWinnerTeam() != null
                    && match.getResultWinnerTeam() != null
                    && bet.getWinnerTeam().getId().equals(match.getResultWinnerTeam().getId());
            boolean mvpCorrect = bet.getMvpPlayer() != null
                    && match.getResultMvpPlayer() != null
                    && bet.getMvpPlayer().getId().equals(match.getResultMvpPlayer().getId());
            boolean scoreCorrect = bet.getExactScore() != null
                    && match.getResultScore() != null
                    && bet.getExactScore().equals(match.getResultScore());

            int eloGain = 0;
            if (winnerCorrect && mvpCorrect && scoreCorrect) {
                eloGain = 40;
            } else if (winnerCorrect && mvpCorrect) {
                eloGain = 30;
            } else if (winnerCorrect && scoreCorrect) {
                eloGain = 30;
            } else if (winnerCorrect) {
                eloGain = 15;
            } else if (mvpCorrect) {
                eloGain = 10;
            } else if (scoreCorrect) {
                eloGain = 10;
            }

            if (eloGain > 0) {
                Wallet eloWallet = walletRepository.findByUserId(bet.getUser().getId())
                        .orElseThrow();
                if (match.getGame() == Game.LOL) {
                    eloWallet.setLolElo(eloWallet.getLolElo() + eloGain);
                } else if (match.getGame() == Game.CS2) {
                    eloWallet.setCs2Elo(eloWallet.getCs2Elo() + eloGain);
                }
                walletRepository.save(eloWallet);
            }
        }

        log.info("Settled {} bets for match {}", pendingBets.size(), match.getId());

        List<Season> activeSeasons = seasonRepository.findByGameAndStatus(match.getGame(), SeasonStatus.ACTIVE);
        if (!activeSeasons.isEmpty()) {
            leaderboardService.recompute(activeSeasons.get(0).getId());
        }
    }

    @Transactional
    public void voidMatchBets(Match match) {
        List<Bet> pendingBets = betRepository.findByMatchIdAndStatus(match.getId(), BetStatus.PENDING);

        for (Bet bet : pendingBets) {
            int refund = bet.getStake() + (bet.getScoreStake() != null ? bet.getScoreStake() : 0);

            Wallet wallet = walletRepository.findByUserId(bet.getUser().getId())
                    .orElse(null);
            if (wallet != null) {
                wallet.setBalance(wallet.getBalance() + refund);
                int wageredRefund = -(bet.getStake() + (bet.getScoreStake() != null ? bet.getScoreStake() : 0));
                if (match.getGame() == Game.LOL) {
                    wallet.setLifetimeWageredLol(
                            Math.max(0, wallet.getLifetimeWageredLol() + wageredRefund)
                    );
                } else if (match.getGame() == Game.CS2) {
                    wallet.setLifetimeWageredCs2(
                            Math.max(0, wallet.getLifetimeWageredCs2() + wageredRefund)
                    );
                }
                walletRepository.save(wallet);
            }

            bet.setPointsAwarded(0);
            bet.setActualReturn(0);
            bet.setStatus(BetStatus.VOID);
            bet.setSettledAt(OffsetDateTime.now());
            betRepository.save(bet);
        }

        log.info("Voided {} bets for cancelled match {}", pendingBets.size(), match.getId());
    }
}
