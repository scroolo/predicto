package com.predicto.betting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchOddsRepository extends JpaRepository<MatchOdds, UUID> {

    List<MatchOdds> findByMatchId(UUID matchId);

    List<MatchOdds> findByTeamId(UUID teamId);
}
