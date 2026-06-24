package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PandaScorePlayer(
    long id,
    String name,
    String slug,
    String role,
    @JsonProperty("image_url") String imageUrl,
    @JsonProperty("current_team") PandaScoreTeamRef currentTeam
) {}
