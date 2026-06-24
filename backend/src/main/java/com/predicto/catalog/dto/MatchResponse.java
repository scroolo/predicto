package com.predicto.catalog.dto;

import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchFormat;
import com.predicto.common.enums.MatchStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        Game game,
        LeagueRef league,
        TeamRef teamA,
        TeamRef teamB,
        MatchFormat format,
        String stage,
        OffsetDateTime startsAt,
        MatchStatus status,
        MatchResultRef result
) {
    public record LeagueRef(UUID id, String name, String logoUrl) {}
    public record TeamRef(UUID id, String name, String logoUrl) {}
    public record MatchResultRef(UUID winnerTeamId, String score) {}
}
