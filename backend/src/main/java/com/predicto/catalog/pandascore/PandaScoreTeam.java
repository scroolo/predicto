package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PandaScoreTeam(
    long id,
    String name,
    String slug,
    @JsonProperty("image_url") String imageUrl,
    @JsonProperty("current_roster") List<PandaScorePlayer> currentRoster
) {}
