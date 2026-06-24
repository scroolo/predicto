package com.predicto.catalog.sync;

import com.predicto.betting.OddsCalculationService;
import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Team;
import com.predicto.catalog.TeamRepository;
import com.predicto.catalog.pandascore.PandaScoreMatch;
import com.predicto.catalog.pandascore.PandaScoreApiClient;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HistoricalSyncService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalSyncService.class);
    private static final int PAGES = 5;
    private static final int PER_PAGE = 100;
    private static final long RATE_LIMIT_MS = 1100;

    private final PandaScoreApiClient pandaScoreClient;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final OddsCalculationService oddsCalculationService;

    public HistoricalSyncService(PandaScoreApiClient pandaScoreClient,
                                  MatchRepository matchRepository,
                                  TeamRepository teamRepository,
                                  OddsCalculationService oddsCalculationService) {
        this.pandaScoreClient = pandaScoreClient;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.oddsCalculationService = oddsCalculationService;
    }

    public int syncHistoricalResults(String gameStr) throws InterruptedException {
        int updated = 0;

        for (int page = 1; page <= PAGES; page++) {
            log.info("Fetching historical {} matches page {}/{}", gameStr, page, PAGES);

            List<PandaScoreMatch> pastMatches = pandaScoreClient.fetchPastMatches(gameStr, page, PER_PAGE);
            if (pastMatches.isEmpty()) break;

            for (PandaScoreMatch pm : pastMatches) {
                try {
                    updated += processHistoricalMatch(pm, gameStr);
                } catch (Exception e) {
                    log.warn("Failed to process historical match {}: {}", pm.id(), e.getMessage());
                }
            }

            if (page < PAGES) {
                Thread.sleep(RATE_LIMIT_MS);
            }
        }

        log.info("Historical sync complete for {}: {} matches updated", gameStr, updated);
        return updated;
    }

    @Transactional
    public int processHistoricalMatch(PandaScoreMatch pm, String gameStr) {
        if (pm.winnerId() == null) return 0;

        Game game = Game.valueOf(gameStr);

        Optional<Match> localMatch = matchRepository.findByExternalId(String.valueOf(pm.id()));
        if (localMatch.isEmpty()) {
            if (pm.opponents() == null || pm.opponents().size() < 2) return 0;
            String teamAName = pm.opponents().get(0).opponent().name();
            String teamBName = pm.opponents().get(1).opponent().name();
            if (teamAName == null || teamBName == null) return 0;
            localMatch = matchRepository.findByTeamNamesAnyOrder(teamAName, teamBName, game);
        }

        if (localMatch.isEmpty()) return 0;

        Match match = localMatch.get();
        if (match.getResultWinnerTeam() != null) return 0;

        String winnerExtId = String.valueOf(pm.winnerId());
        List<Team> winnerCandidates = teamRepository.findAllByExternalId(winnerExtId);
        if (winnerCandidates.isEmpty()) return 0;

        Team winnerTeam = winnerCandidates.get(0);
        if (!winnerTeam.getId().equals(match.getTeamA().getId()) && !winnerTeam.getId().equals(match.getTeamB().getId())) {
            return 0;
        }

        match.setResultWinnerTeam(winnerTeam);
        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);

        log.debug("Updated result for match {}: winner={}", match.getId(), winnerTeam.getName());
        return 1;
    }

    public int syncAllHistoricalResults() throws InterruptedException {
        int lol = syncHistoricalResults("LOL");
        int cs2 = syncHistoricalResults("CS2");
        int total = lol + cs2;
        log.info("Total historical results synced: {} (LOL={}, CS2={})", total, lol, cs2);

        int oddsCount = oddsCalculationService.calculateOddsForAllUpcomingMatches();
        log.info("Recalculated odds for {} upcoming matches", oddsCount);

        return total;
    }
}
