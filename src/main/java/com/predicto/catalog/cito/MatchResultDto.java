package com.predicto.catalog.cito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchResultDto(
        String winner,
        String score
) {}
