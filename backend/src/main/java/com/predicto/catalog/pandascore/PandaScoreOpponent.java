package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PandaScoreOpponent(
    PandaScoreTeamRef opponent,
    String type
) {}
