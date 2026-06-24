package com.predicto.season.dto;

import com.predicto.season.Reward;

import java.util.UUID;

public record RewardResponse(
        UUID id,
        Integer rankPosition,
        UUID userId,
        String displayName,
        String description,
        Boolean claimed
) {
    public static RewardResponse from(Reward r) {
        return new RewardResponse(
                r.getId(),
                r.getRankPosition(),
                r.getUser().getId(),
                r.getUser().getDisplayName(),
                r.getDescription(),
                r.getClaimed()
        );
    }
}
