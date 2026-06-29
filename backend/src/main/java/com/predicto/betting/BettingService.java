package com.predicto.betting;

import com.predicto.achievement.AchievementService;
import com.predicto.auth.User;
import com.predicto.betting.dto.BetResponse;
import com.predicto.betting.dto.PlaceBetRequest;
import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Player;
import com.predicto.catalog.Team;
import com.predicto.catalog.PlayerRepository;
import com.predicto.common.enums.BetStatus;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchStatus;
import com.predicto.common.enums.SeasonStatus;
import com.predicto.season.Season;
import com.predicto.season.SeasonRepository;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BettingService {

    private final MatchRepository matchRepository;
    private final BetRepository betRepository;
    private final WalletRepository walletRepository;
    private final MatchOddsRepository matchOddsRepository;
    private final ScoreOddsRepository scoreOddsRepository;
    private final PlayerRepository playerRepository;
    private final SeasonRepository seasonRepository;
    private final AchievementService achievementService;
    private final OddsCalculationService oddsCalculationService;

    private static final long LOCK_WINDOW_MINUTES = 15;

    @Transactional
    public BetResponse placeBet(UUID matchId, PlaceBetRequest request, User user) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Cannot place bet on a match with status " + match.getStatus()
            );
        }

        if (OffsetDateTime.now().isAfter(match.getStartsAt().minusMinutes(LOCK_WINDOW_MINUTES))) {
            throw new IllegalStateException(
                    "Cannot place bet — match locks at " + match.getStartsAt().minusMinutes(LOCK_WINDOW_MINUTES)
            );
        }

        if (!match.getTeamA().getId().equals(request.winnerTeamId())
                && !match.getTeamB().getId().equals(request.winnerTeamId())) {
            throw new IllegalArgumentException("Team " + request.winnerTeamId() + " is not part of this match");
        }

        MatchOdds winnerOdds = matchOddsRepository.findByMatchIdAndTeamId(matchId, request.winnerTeamId())
                .orElseThrow(() -> new IllegalStateException("Kurzy na tento zápas ešte neboli nastavené"));

        if (request.mvpPlayerId() != null) {
            boolean belongsToTeam = playerRepository.findById(request.mvpPlayerId())
                    .map(p -> p.getTeam().getId().equals(match.getTeamA().getId())
                            || p.getTeam().getId().equals(match.getTeamB().getId()))
                    .orElse(false);
            if (!belongsToTeam) {
                throw new IllegalArgumentException("MVP player does not belong to either team in this match");
            }
        }

        if (request.exactScore() != null || request.scoreStake() != null) {
            if (request.exactScore() == null || request.scoreStake() == null) {
                throw new IllegalArgumentException("Both exactScore and scoreStake must be provided together");
            }
            scoreOddsRepository.findByMatchIdAndScoreValue(matchId, request.exactScore())
                    .orElseThrow(() -> new IllegalStateException("Kurzy na tento zápas ešte neboli nastavené"));
        }

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        Bet existingBet = betRepository.findByUserIdAndMatchId(user.getId(), matchId).orElse(null);

        int previousStake = 0;
        int previousScoreStake = 0;
        if (existingBet != null) {
            previousStake = existingBet.getStake();
            previousScoreStake = existingBet.getScoreStake() != null ? existingBet.getScoreStake() : 0;
        }

        int available = wallet.getBalance() + previousStake + previousScoreStake;
        int newTotalStake = request.stake() + (request.scoreStake() != null ? request.scoreStake() : 0);

        if (newTotalStake > available) {
            throw new IllegalArgumentException(
                    "Insufficient balance. Available: " + available + ", required: " + newTotalStake
            );
        }

        if (existingBet != null) {
            wallet.setBalance(wallet.getBalance() + previousStake + previousScoreStake);
            updateLifetimeWagered(wallet, match.getGame(), -(previousStake + previousScoreStake));
            betRepository.delete(existingBet);
            betRepository.flush();
        }

        wallet.setBalance(wallet.getBalance() - newTotalStake);
        updateLifetimeWagered(wallet, match.getGame(), newTotalStake);
        walletRepository.save(wallet);

        int potentialReturn = (int) Math.floor(request.stake() * winnerOdds.getOddsValue().doubleValue());
        Integer scorePotentialReturn = null;
        if (request.exactScore() != null && request.scoreStake() != null) {
            ScoreOdds scoreOdds = scoreOddsRepository.findByMatchIdAndScoreValue(matchId, request.exactScore())
                    .orElseThrow(() -> new IllegalStateException("Score odds not found"));
            scorePotentialReturn = (int) Math.floor(request.scoreStake() * scoreOdds.getOddsValue().doubleValue());
        }

        Team winnerTeam = match.getTeamA().getId().equals(request.winnerTeamId())
                ? match.getTeamA() : match.getTeamB();

        Player mvpPlayer = request.mvpPlayerId() != null
                ? playerRepository.getReferenceById(request.mvpPlayerId())
                : null;

        List<Season> activeSeasons = seasonRepository.findByGameAndStatus(match.getGame(), SeasonStatus.ACTIVE);
        Season activeSeason = activeSeasons.isEmpty() ? null : activeSeasons.get(0);

        Bet bet = Bet.builder()
                .user(user)
                .match(match)
                .season(activeSeason)
                .winnerTeam(winnerTeam)
                .winnerOddsSnapshot(winnerOdds.getOddsValue())
                .stake(request.stake())
                .potentialReturn(potentialReturn)
                .mvpPlayer(mvpPlayer)
                .exactScore(request.exactScore())
                .scoreOddsSnapshot(request.exactScore() != null
                        ? scoreOddsRepository.findByMatchIdAndScoreValue(matchId, request.exactScore())
                                .orElseThrow().getOddsValue()
                        : null)
                .scoreStake(request.scoreStake())
                .scorePotentialReturn(scorePotentialReturn)
                .status(BetStatus.PENDING)
                .build();

        betRepository.save(bet);
        oddsCalculationService.calculateAndSaveOdds(match);
        achievementService.checkAndAward(user.getId(), "bet_placed");
        return BetResponse.from(bet);
    }

    @Transactional
    public void cancelBet(UUID matchId, User user) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot cancel bet on a match with status " + match.getStatus());
        }

        if (OffsetDateTime.now().isAfter(match.getStartsAt().minusMinutes(LOCK_WINDOW_MINUTES))) {
            throw new IllegalStateException("Cannot cancel bet — match is already in lock window");
        }

        Bet bet = betRepository.findByUserIdAndMatchId(user.getId(), matchId)
                .orElseThrow(() -> new IllegalArgumentException("No bet found for this match"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        int refund = bet.getStake() + (bet.getScoreStake() != null ? bet.getScoreStake() : 0);
        wallet.setBalance(wallet.getBalance() + refund);
        updateLifetimeWagered(wallet, match.getGame(), -refund);
        walletRepository.save(wallet);

        betRepository.delete(bet);
        oddsCalculationService.calculateAndSaveOdds(match);
    }

    @Transactional(readOnly = true)
    public List<BetResponse> getUserBets(UUID userId) {
        return betRepository.findByUserId(userId).stream()
                .map(BetResponse::from)
                .sorted((a, b) -> b.match().startsAt().compareTo(a.match().startsAt()))
                .toList();
    }

    private void updateLifetimeWagered(Wallet wallet, Game game, int amount) {
        if (game == Game.LOL) {
            wallet.setLifetimeWageredLol(Math.max(0, wallet.getLifetimeWageredLol() + amount));
        } else if (game == Game.CS2) {
            wallet.setLifetimeWageredCs2(Math.max(0, wallet.getLifetimeWageredCs2() + amount));
        }
    }
}
