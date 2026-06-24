package com.predicto.season.dto;

import com.predicto.common.enums.Game;
import com.predicto.common.enums.SeasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateSeasonRequest(
        @NotBlank String name,
        @NotNull Game game,
        @NotNull SeasonType type,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt
) {
}
