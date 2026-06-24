package com.predicto.catalog.pandascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PandaScoreTournamentRef(
    long id,
    String name,
    String slug
) {}
