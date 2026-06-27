package com.predicto.betting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchOddsRepository extends JpaRepository<MatchOdds, UUID> {

    List<MatchOdds> findByMatchId(UUID matchId);

    List<MatchOdds> findByTeamId(UUID teamId);

    Optional<MatchOdds> findByMatchIdAndTeamId(UUID matchId, UUID teamId);

    @Query("SELECT COUNT(b) FROM Bet b WHERE b.winnerTeam.id = :teamId AND b.match.id = :matchId AND b.status = 'PENDING'")
    long countBetsByTeamAndMatch(@Param("teamId") UUID teamId, @Param("matchId") UUID matchId);
}
