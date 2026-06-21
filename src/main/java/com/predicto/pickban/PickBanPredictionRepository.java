package com.predicto.pickban;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PickBanPredictionRepository extends JpaRepository<PickBanPrediction, UUID> {

    List<PickBanPrediction> findByUserId(UUID userId);

    List<PickBanPrediction> findByLeagueId(UUID leagueId);

    List<PickBanPrediction> findBySeasonId(UUID seasonId);

    Optional<PickBanPrediction> findByUserIdAndLeagueId(UUID userId, UUID leagueId);
}
