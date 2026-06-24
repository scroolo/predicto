package com.predicto.betting;

import com.predicto.common.enums.BetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BetRepository extends JpaRepository<Bet, UUID> {

    List<Bet> findByUserId(UUID userId);

    List<Bet> findByMatchId(UUID matchId);

    List<Bet> findBySeasonId(UUID seasonId);

    List<Bet> findByUserIdAndSeasonId(UUID userId, UUID seasonId);

    List<Bet> findByStatus(BetStatus status);

    Optional<Bet> findByUserIdAndMatchId(UUID userId, UUID matchId);

    List<Bet> findByMatchIdAndStatus(UUID matchId, BetStatus status);
}
