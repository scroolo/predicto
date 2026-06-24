package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PandaScoreMatch(
    long id,
    String name,
    String slug,
    String status,
    @JsonProperty("scheduled_at") String scheduledAt,
    @JsonProperty("begin_at") String beginAt,
    @JsonProperty("end_at") String endAt,
    @JsonProperty("number_of_games") int numberOfGames,
    @JsonProperty("match_type") String matchType,
    List<PandaScoreOpponent> opponents,
    List<PandaScoreResult> results,
    Object winner,
    @JsonProperty("winner_id") Long winnerId,
    PandaScoreLeagueRef league,
    PandaScoreTournamentRef tournament
) {}
