package com.predicto.catalog.cito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RosterEntryDto(
        String playerId,
        String nickname,
        String role,
        String photoUrl
) {}
