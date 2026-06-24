package com.predicto.betting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmResultRequest(
        @NotNull UUID winnerTeamId,
        @NotBlank String score,
        UUID mvpPlayerId
) {}
