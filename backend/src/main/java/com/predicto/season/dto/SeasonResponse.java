package com.predicto.season.dto;

import com.predicto.common.enums.Game;
import com.predicto.common.enums.SeasonStatus;
import com.predicto.common.enums.SeasonType;
import com.predicto.season.Season;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SeasonResponse(
        UUID id,
        String name,
        Game game,
        SeasonType type,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        SeasonStatus status
) {
    public static SeasonResponse from(Season s) {
        return new SeasonResponse(s.getId(), s.getName(), s.getGame(), s.getType(), s.getStartsAt(), s.getEndsAt(), s.getStatus());
    }
}
