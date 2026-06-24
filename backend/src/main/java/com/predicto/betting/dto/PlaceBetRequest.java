package com.predicto.betting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlaceBetRequest(
        @NotNull UUID winnerTeamId,
        @NotNull @Min(1) Integer stake,
        UUID mvpPlayerId,
        String exactScore,
        @Min(1) Integer scoreStake
) {}
