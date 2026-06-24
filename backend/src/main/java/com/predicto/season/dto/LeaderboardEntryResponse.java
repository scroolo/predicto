package com.predicto.season.dto;

import com.predicto.season.LeaderboardEntry;

import java.util.UUID;

public record LeaderboardEntryResponse(
        Integer rankPosition,
        UUID userId,
        String displayName,
        String avatarUrl,
        Integer points,
        Integer correctPicks,
        Integer mvpCorrect,
        Integer scoreCorrect
) {
    public static LeaderboardEntryResponse from(LeaderboardEntry e) {
        return new LeaderboardEntryResponse(
                e.getRankPosition(),
                e.getUser().getId(),
                e.getUser().getDisplayName(),
                e.getUser().getAvatarUrl(),
                e.getPoints(),
                e.getCorrectPicks(),
                e.getMvpCorrect(),
                e.getScoreCorrect()
        );
    }
}
