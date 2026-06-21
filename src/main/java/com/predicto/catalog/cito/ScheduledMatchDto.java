package com.predicto.catalog.cito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScheduledMatchDto(
        String matchId,
        String league,
        MatchTeamDto team1,
        MatchTeamDto team2,
        String startTime,
        String format,
        String stage,
        String status,
        MatchResultDto result
) {}
