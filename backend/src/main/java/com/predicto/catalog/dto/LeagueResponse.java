package com.predicto.catalog.dto;

import java.util.UUID;

public record LeagueResponse(UUID id, String name, String region, String logoUrl) {}
