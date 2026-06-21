package com.predicto.catalog.cito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamDto(
        String slug,
        String name,
        String region,
        String logoUrl
) {}
