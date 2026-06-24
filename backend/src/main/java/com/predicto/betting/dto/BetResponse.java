package com.predicto.betting.dto;

import com.predicto.betting.Bet;
import com.predicto.common.enums.BetStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BetResponse(
        UUID id,
        MatchSummary match,
        UUID winnerTeamId,
        String predictedWinnerName,
        BigDecimal winnerOddsSnapshot,
        Integer stake,
        Integer potentialReturn,
        UUID mvpPlayerId,
        String exactScore,
        BigDecimal scoreOddsSnapshot,
        Integer scoreStake,
        Integer scorePotentialReturn,
        BetStatus status,
        Integer pointsAwarded,
        Integer actualReturn,
        OffsetDateTime createdAt,
        OffsetDateTime settledAt
) {
    public record MatchSummary(
            UUID id,
            String game,
            String leagueName,
            String teamAName,
            String teamBName,
            String format,
            String stage,
            OffsetDateTime startsAt,
            String status
    ) {}

    public static BetResponse from(Bet bet) {
        return new BetResponse(
                bet.getId(),
                new MatchSummary(
                        bet.getMatch().getId(),
                        bet.getMatch().getGame().name(),
                        bet.getMatch().getLeague().getName(),
                        bet.getMatch().getTeamA().getName(),
                        bet.getMatch().getTeamB().getName(),
                        bet.getMatch().getFormat().name(),
                        bet.getMatch().getStage(),
                        bet.getMatch().getStartsAt(),
                        bet.getMatch().getStatus().name()
                ),
                bet.getWinnerTeam() != null ? bet.getWinnerTeam().getId() : null,
                bet.getWinnerTeam() != null ? bet.getWinnerTeam().getName() : null,
                bet.getWinnerOddsSnapshot(),
                bet.getStake(),
                bet.getPotentialReturn(),
                bet.getMvpPlayer() != null ? bet.getMvpPlayer().getId() : null,
                bet.getExactScore(),
                bet.getScoreOddsSnapshot(),
                bet.getScoreStake(),
                bet.getScorePotentialReturn(),
                bet.getStatus(),
                bet.getPointsAwarded(),
                bet.getActualReturn(),
                bet.getCreatedAt(),
                bet.getSettledAt()
        );
    }
}
