package com.predicto.betting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SetOddsRequest(
        @NotEmpty List<@Valid WinnerOddsItem> winnerOdds,
        @NotEmpty List<@Valid ScoreOddsItem> scoreOdds
) {
    public record WinnerOddsItem(
            @NotNull UUID teamId,
            @NotNull @DecimalMin("1.01") BigDecimal oddsValue
    ) {}

    public record ScoreOddsItem(
            @NotNull String scoreValue,
            @NotNull @DecimalMin("1.01") BigDecimal oddsValue
    ) {}
}
