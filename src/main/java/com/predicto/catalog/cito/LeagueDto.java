package com.predicto.catalog.cito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueDto(
        String id,
        String name,
        String region,
        String logoUrl
) {}
