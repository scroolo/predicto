package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PandaScoreLeagueRef(
    long id,
    String name,
    String slug,
    @JsonProperty("image_url") String imageUrl
) {}
