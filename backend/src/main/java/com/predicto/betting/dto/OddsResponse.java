package com.predicto.betting.dto;

import com.predicto.betting.MatchOdds;
import com.predicto.betting.ScoreOdds;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OddsResponse(
        List<WinnerOddsItem> winnerOdds,
        List<ScoreOddsItem> scoreOdds
) {
    public record WinnerOddsItem(UUID teamId, BigDecimal oddsValue) {}
    public record ScoreOddsItem(String scoreValue, BigDecimal oddsValue) {}

    public static OddsResponse from(List<MatchOdds> winnerOdds, List<ScoreOdds> scoreOdds) {
        return new OddsResponse(
                winnerOdds.stream().map(o -> new WinnerOddsItem(o.getTeam().getId(), o.getOddsValue())).toList(),
                scoreOdds.stream().map(o -> new ScoreOddsItem(o.getScoreValue(), o.getOddsValue())).toList()
        );
    }
}
