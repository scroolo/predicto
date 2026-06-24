package com.predicto.auth.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String username,
    String displayName,
    String email,
    String role,
    Integer balance,
    OffsetDateTime createdAt
) {}
