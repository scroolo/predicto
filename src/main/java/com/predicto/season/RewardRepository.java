package com.predicto.season;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardRepository extends JpaRepository<Reward, UUID> {

    List<Reward> findBySeasonId(UUID seasonId);

    List<Reward> findByUserId(UUID userId);

    List<Reward> findBySeasonIdAndUserId(UUID seasonId, UUID userId);
}
