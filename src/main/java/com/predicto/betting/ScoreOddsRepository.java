package com.predicto.betting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScoreOddsRepository extends JpaRepository<ScoreOdds, UUID> {

    List<ScoreOdds> findByMatchId(UUID matchId);
}
