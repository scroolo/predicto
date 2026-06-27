package com.predicto.betting;

import com.predicto.common.enums.BetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT b FROM Bet b WHERE b.match.id = :matchId AND b.winnerTeam.id = :teamId AND b.status = 'PENDING'")
    List<Bet> findPendingByMatchAndTeam(@Param("matchId") UUID matchId, @Param("teamId") UUID teamId);
}
