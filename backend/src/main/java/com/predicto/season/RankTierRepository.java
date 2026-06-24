package com.predicto.season;

import com.predicto.common.enums.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RankTierRepository extends JpaRepository<RankTier, UUID> {

    List<RankTier> findByGameOrderBySortOrderAsc(Game game);
}
