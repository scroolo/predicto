package com.predicto.catalog;

import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByGame(Game game);

    List<Match> findByGameAndStatus(Game game, MatchStatus status);

    List<Match> findByLeagueId(UUID leagueId);

    List<Match> findByStatus(MatchStatus status);

    List<Match> findByStartsAtAfter(OffsetDateTime startsAt);

    List<Match> findByLeagueIdAndStatus(UUID leagueId, MatchStatus status);

    Optional<Match> findByExternalId(String externalId);

    List<Match> findAllByExternalId(String externalId);

    List<Match> findByStatusAndStartsAtLessThanEqual(MatchStatus status, OffsetDateTime threshold);

    List<Match> findByStartsAtLessThanEqualAndStatusNotIn(OffsetDateTime startsAt, List<MatchStatus> statuses);

    @Query("SELECT m FROM Match m WHERE (m.teamA.id = :teamId OR m.teamB.id = :teamId) AND m.status = 'SCHEDULED' ORDER BY m.startsAt ASC")
    List<Match> findUpcomingByTeamId(@Param("teamId") UUID teamId, Pageable pageable);

    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT m.id FROM matches m
            WHERE (m.team_a_id = :teamId OR m.team_b_id = :teamId)
            AND m.id != :excludeMatchId
            AND m.status = 'FINISHED'
            AND m.result_winner_team_id = :teamId
            ORDER BY m.starts_at DESC
            LIMIT :limit
        ) sub
        """, nativeQuery = true)
    int countWinsByTeamId(@Param("teamId") UUID teamId,
                          @Param("excludeMatchId") UUID excludeMatchId,
                          @Param("limit") int limit);

    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT m.id FROM matches m
            WHERE (m.team_a_id = :teamId OR m.team_b_id = :teamId)
            AND m.id != :excludeMatchId
            AND m.status = 'FINISHED'
            ORDER BY m.starts_at DESC
            LIMIT :limit
        ) sub
        """, nativeQuery = true)
    int countFinishedMatchesByTeamId(@Param("teamId") UUID teamId,
                                     @Param("excludeMatchId") UUID excludeMatchId,
                                     @Param("limit") int limit);

    @Query("SELECT DISTINCT m FROM Match m JOIN FETCH m.teamA JOIN FETCH m.teamB WHERE m.status = 'SCHEDULED'")
    List<Match> findAllUpcoming();

    @Query("SELECT m FROM Match m JOIN FETCH m.teamA JOIN FETCH m.teamB WHERE ((LOWER(m.teamA.name) = LOWER(:teamA) AND LOWER(m.teamB.name) = LOWER(:teamB)) OR (LOWER(m.teamA.name) = LOWER(:teamB) AND LOWER(m.teamB.name) = LOWER(:teamA))) AND m.game = :game")
    Optional<Match> findByTeamNamesAnyOrder(@Param("teamA") String teamA, @Param("teamB") String teamB, @Param("game") Game game);
}
