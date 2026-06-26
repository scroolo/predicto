package com.predicto.admin;

import com.predicto.betting.OddsCalculationService;
import com.predicto.catalog.LeagueRepository;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.sync.*;
import com.predicto.common.enums.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sync")
public class SyncTriggerController {

    private final PandaScoreSyncService pandaScoreSyncService;
    private final LockJobService lockJobService;
    private final SyncRunRepository syncRunRepository;
    private final OddsCalculationService oddsCalculationService;
    private final HistoricalSyncService historicalSyncService;
    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;

    public SyncTriggerController(PandaScoreSyncService pandaScoreSyncService,
                                  LockJobService lockJobService,
                                  SyncRunRepository syncRunRepository,
                                  OddsCalculationService oddsCalculationService,
                                  HistoricalSyncService historicalSyncService,
                                  MatchRepository matchRepository,
                                  LeagueRepository leagueRepository) {
        this.pandaScoreSyncService = pandaScoreSyncService;
        this.lockJobService = lockJobService;
        this.syncRunRepository = syncRunRepository;
        this.oddsCalculationService = oddsCalculationService;
        this.historicalSyncService = historicalSyncService;
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerSync(@RequestParam(defaultValue = "catalog") String job) {
        return switch (job) {
            case "catalog" -> {
                int total = pandaScoreSyncService.syncCatalog();
                yield ResponseEntity.ok(Map.of(
                        "job", "catalog",
                        "status", "completed",
                        "items", total
                ));
            }
            case "matches" -> {
                int total = pandaScoreSyncService.syncMatches();
                yield ResponseEntity.ok(Map.of(
                        "job", "matches",
                        "status", "completed",
                        "matches", total
                ));
            }
            case "results" -> {
                int total = pandaScoreSyncService.syncResults();
                yield ResponseEntity.ok(Map.of(
                        "job", "results",
                        "status", "completed",
                        "results", total
                ));
            }
            case "lock" -> {
                int locked = lockJobService.doLock();
                yield ResponseEntity.ok(Map.of(
                        "job", "lock",
                        "status", "completed",
                        "matches_locked", locked
                ));
            }
            case "odds" -> {
                int count = oddsCalculationService.calculateOddsForAllUpcomingMatches();
                yield ResponseEntity.ok(Map.of(
                        "job", "odds",
                        "status", "completed",
                        "matches", count
                ));
            }
            default -> ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown job: " + job + ". Valid: catalog, matches, results, lock, odds"
            ));
        };
    }

    @PostMapping("/history")
    public ResponseEntity<Map<String, Object>> triggerHistoricalSync() throws InterruptedException {
        int count = historicalSyncService.syncAllHistoricalResults();
        return ResponseEntity.ok(Map.of(
                "matchesUpdated", count
        ));
    }

    @GetMapping("/debug/games")
    @ResponseBody
    public List<String> getGames() {
        return matchRepository.findDistinctGames();
    }

    @GetMapping("/debug/cs2leagues")
    @ResponseBody
    public List<String> getCs2Leagues() {
        return leagueRepository.findByGame(Game.CS2).stream()
            .map(l -> l.getName())
            .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/runs")
    public ResponseEntity<List<SyncRun>> getRecentRuns(@RequestParam(defaultValue = "20") int limit) {
        var runs = syncRunRepository.findTop20ByOrderByStartedAtDesc();
        if (runs.size() > limit) {
            runs = runs.subList(0, limit);
        }
        return ResponseEntity.ok(runs);
    }
}
