package com.predicto.season;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, UUID> {

    List<LeaderboardEntry> findBySeasonIdOrderByPointsDesc(UUID seasonId);

    List<LeaderboardEntry> findByUserId(UUID userId);
}
