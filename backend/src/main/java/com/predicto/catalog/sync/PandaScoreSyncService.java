package com.predicto.catalog.sync;

import com.predicto.betting.OddsCalculationService;
import com.predicto.betting.SettlementService;
import com.predicto.catalog.*;
import com.predicto.catalog.pandascore.*;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchFormat;
import com.predicto.common.enums.MatchStatus;
import com.predicto.common.enums.Source;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PandaScoreSyncService {

    private final PandaScoreApiClient client;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final SyncRunnerService syncRunner;
    private final OddsCalculationService oddsCalculationService;
    private final SettlementService settlementService;

    // ========= Scheduled jobs =========

    @Scheduled(cron = "0 0 6 * * *")
    public void scheduledCatalogSync() {
        SyncRun run = syncRunner.startRun("catalog-sync");
        try {
            int total = syncCatalog();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Catalog sync failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 */2 * * *")
    public void scheduledMatchSync() {
        SyncRun run = syncRunner.startRun("match-sync");
        try {
            int total = syncMatches();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Match sync failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 */2 * * *")
    public void scheduledResultSync() {
        SyncRun run = syncRunner.startRun("result-sync");
        try {
            int total = syncResults();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Result sync failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    // ========= Public trigger methods =========

    public synchronized int syncCatalog() {
        int count = 0;
        count += syncLeagues(client.fetchLolLeagues(), Game.LOL);
        count += syncTeams(client.fetchLolTeams(), Game.LOL);
        count += syncLeagues(client.fetchCs2Leagues(), Game.CS2);
        count += syncTeams(client.fetchCs2Teams(), Game.CS2);
        log.info("Catalog sync complete: {} items", count);
        return count;
    }

    public synchronized int syncMatches() {
        int count = 0;
        List<PandaScoreMatch> lolUpcoming = client.fetchLolUpcoming();
        List<PandaScoreMatch> cs2Upcoming = client.fetchCs2Upcoming();
        count += processMatches(lolUpcoming, Game.LOL);
        count += processMatches(client.fetchLolRunning(), Game.LOL);
        count += processMatches(cs2Upcoming, Game.CS2);
        count += processMatches(client.fetchCs2Running(), Game.CS2);
        if (lolUpcoming.isEmpty() && cs2Upcoming.isEmpty()) {
            log.warn("No upcoming matches found from PandaScore API — both LoL and CS2 returned empty lists");
        }
        log.info("Match sync complete: {} matches", count);
        return count;
    }

    public synchronized int syncResults() {
        int count = 0;
        count += processResults(client.fetchLolPast(), Game.LOL);
        count += processResults(client.fetchCs2Past(), Game.CS2);
        log.info("Result sync complete: {} results", count);
        return count;
    }

    // ========= Internal helpers =========

    private <T> T safeSingleResult(List<T> results, String externalId, String entityName) {
        if (results.isEmpty()) return null;
        if (results.size() > 1) {
            log.warn("Found {} {} with external_id '{}', keeping first", results.size(), entityName, externalId);
        }
        return results.getFirst();
    }

    private int syncLeagues(List<PandaScoreLeague> leagues, Game game) {
        int count = 0;
        for (PandaScoreLeague pl : leagues) {
            String extId = String.valueOf(pl.id());
            League league = safeSingleResult(leagueRepository.findAllByExternalId(extId), extId, "leagues");
            if (league == null) {
                league = League.builder().build();
            }
            league.setExternalId(extId);
            league.setSource(Source.API);
            league.setGame(game);
            league.setName(pl.name());
            league.setLogoUrl(pl.imageUrl());
            leagueRepository.save(league);
            count++;
        }
        return count;
    }

    private int syncTeams(List<PandaScoreTeam> teams, Game game) {
        int count = 0;
        for (PandaScoreTeam pt : teams) {
            String extId = String.valueOf(pt.id());
            Team team = safeSingleResult(teamRepository.findAllByExternalId(extId), extId, "teams");
            if (team == null) {
                team = Team.builder().build();
            }
            team.setExternalId(extId);
            team.setSource(Source.API);
            team.setGame(game);
            team.setName(pt.name());
            team.setLogoUrl(pt.imageUrl());
            teamRepository.save(team);

            if (pt.currentRoster() != null) {
                for (PandaScorePlayer pp : pt.currentRoster()) {
                    String playerExtId = String.valueOf(pp.id());
                    Player player = safeSingleResult(playerRepository.findAllByExternalId(playerExtId), playerExtId, "players");
                    if (player == null) {
                        player = Player.builder().build();
                    }
                    player.setExternalId(playerExtId);
                    player.setSource(Source.API);
                    player.setTeam(team);
                    player.setNickname(pp.name());
                    player.setRole(pp.role());
                    player.setPhotoUrl(pp.imageUrl());
                    playerRepository.save(player);
                }
            }
            count++;
        }
        return count;
    }

    @Transactional
    public int processMatches(List<PandaScoreMatch> matches, Game game) {
        int count = 0;
        for (PandaScoreMatch pm : matches) {
            String extId = String.valueOf(pm.id());
            Match existing = safeSingleResult(matchRepository.findAllByExternalId(extId), extId, "matches");
            if (existing != null && existing.getStatus() != MatchStatus.SCHEDULED && existing.getStatus() != MatchStatus.LIVE) {
                log.debug("Match {} skipped: existing status={}", extId, existing.getStatus());
                continue;
            }

            if (pm.opponents() == null || pm.opponents().size() < 2) {
                log.warn("Match {} skipped: opponents={}", extId, pm.opponents());
                continue;
            }
            String teamAExtId = String.valueOf(pm.opponents().get(0).opponent().id());
            String teamBExtId = String.valueOf(pm.opponents().get(1).opponent().id());
            Team teamA = safeSingleResult(teamRepository.findAllByExternalId(teamAExtId), teamAExtId, "teams");
            Team teamB = safeSingleResult(teamRepository.findAllByExternalId(teamBExtId), teamBExtId, "teams");
            if (teamA == null || teamB == null) {
                log.warn("Match {} skipped: teamA={} (found={}), teamB={} (found={})", extId, teamAExtId, teamA != null, teamBExtId, teamB != null);
                continue;
            }

            if (pm.league() == null) {
                log.warn("Match {} skipped: league is null", extId);
                continue;
            }
            String leagueExtId = String.valueOf(pm.league().id());
            League league = safeSingleResult(leagueRepository.findAllByExternalId(leagueExtId), leagueExtId, "leagues");
            if (league == null) {
                log.warn("Match {} skipped: league {} not found in DB", extId, leagueExtId);
                continue;
            }

            Match match = existing != null ? existing : Match.builder().build();
            match.setExternalId(extId);
            match.setSource(Source.API);
            match.setGame(game);
            match.setLeague(league);
            match.setTeamA(teamA);
            match.setTeamB(teamB);

            int numGames = pm.numberOfGames();
            MatchFormat format = numGames <= 1 ? MatchFormat.BO1 : numGames >= 5 ? MatchFormat.BO5 : MatchFormat.BO3;
            match.setFormat(format);

            if (pm.scheduledAt() != null) {
                match.setStartsAt(OffsetDateTime.parse(pm.scheduledAt()));
            }

            MatchStatus status = switch (pm.status() != null ? pm.status() : "") {
                case "not_started" -> MatchStatus.SCHEDULED;
                case "running" -> MatchStatus.LIVE;
                case "finished" -> MatchStatus.FINISHED;
                case "canceled" -> MatchStatus.CANCELLED;
                default -> MatchStatus.SCHEDULED;
            };
            if (existing == null || existing.getStatus() == MatchStatus.SCHEDULED || existing.getStatus() == MatchStatus.LIVE) {
                match.setStatus(status);
            }

            matchRepository.save(match);

            if (existing == null) {
                try {
                    oddsCalculationService.calculateAndSaveOdds(match);
                } catch (Exception e) {
                    log.warn("Failed to calculate odds for new match {}: {}", extId, e.getMessage());
                }
            }

            count++;
        }
        return count;
    }

    @Transactional
    public int processResults(List<PandaScoreMatch> matches, Game game) {
        int count = 0;
        for (PandaScoreMatch pm : matches) {
            if (!"finished".equals(pm.status())) continue;

            String extId = String.valueOf(pm.id());
            Match match = safeSingleResult(matchRepository.findAllByExternalId(extId), extId, "matches");
            if (match == null || match.getStatus() == MatchStatus.FINISHED || match.getStatus() == MatchStatus.CANCELLED) {
                continue;
            }

            if (pm.results() != null && pm.results().size() >= 2) {
                match.setResultScore(pm.results().get(0).score() + ":" + pm.results().get(1).score());
            }

            if (pm.winnerId() != null) {
                String winnerExtId = String.valueOf(pm.winnerId());
                Team winner = safeSingleResult(teamRepository.findAllByExternalId(winnerExtId), winnerExtId, "teams");
                if (winner != null) {
                    match.setResultWinnerTeam(winner);
                }
            }

            match.setStatus(MatchStatus.FINISHED);
            match.setFinishedAt(OffsetDateTime.now());
            matchRepository.save(match);

            if (match.getResultWinnerTeam() != null) {
                try {
                    settlementService.settleMatch(match);
                } catch (Exception e) {
                    log.warn("Failed to auto-settle match {}: {}", match.getId(), e.getMessage());
                }
            }

            count++;
        }
        return count;
    }
}
