package com.predicto.catalog;

import com.predicto.common.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByLeagueId(UUID leagueId);

    List<Match> findByStatus(MatchStatus status);

    List<Match> findByStartsAtAfter(OffsetDateTime startsAt);

    List<Match> findByLeagueIdAndStatus(UUID leagueId, MatchStatus status);

    Optional<Match> findByExternalId(String externalId);
}
