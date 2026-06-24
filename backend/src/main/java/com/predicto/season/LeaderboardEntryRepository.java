package com.predicto.season;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, UUID> {

    List<LeaderboardEntry> findBySeasonIdOrderByPointsDesc(UUID seasonId);

    List<LeaderboardEntry> findBySeasonIdOrderByRankPositionAsc(UUID seasonId);

    @Transactional
    void deleteBySeasonId(UUID seasonId);

    List<LeaderboardEntry> findByUserId(UUID userId);
}
