package com.predicto.admin;

import com.predicto.catalog.sync.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/sync")
public class SyncTriggerController {

    private final CatalogSyncService catalogSyncService;
    private final ScheduleSyncService scheduleSyncService;
    private final ResultSyncService resultSyncService;
    private final LockJobService lockJobService;

    public SyncTriggerController(CatalogSyncService catalogSyncService,
                                  ScheduleSyncService scheduleSyncService,
                                  ResultSyncService resultSyncService,
                                  LockJobService lockJobService) {
        this.catalogSyncService = catalogSyncService;
        this.scheduleSyncService = scheduleSyncService;
        this.resultSyncService = resultSyncService;
        this.lockJobService = lockJobService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerSync(@RequestParam(defaultValue = "catalog") String job) {
        return switch (job) {
            case "catalog" -> {
                int leagues = catalogSyncService.syncLeagues();
                int teams = catalogSyncService.syncTeams();
                int players = catalogSyncService.syncPlayers();
                yield ResponseEntity.ok(Map.of(
                        "job", "catalog",
                        "status", "completed",
                        "leagues", leagues,
                        "teams", teams,
                        "players", players
                ));
            }
            case "schedule" -> {
                int matches = scheduleSyncService.doSync();
                yield ResponseEntity.ok(Map.of(
                        "job", "schedule",
                        "status", "completed",
                        "matches", matches
                ));
            }
            case "results" -> {
                int results = resultSyncService.doSync();
                yield ResponseEntity.ok(Map.of(
                        "job", "results",
                        "status", "completed",
                        "matches", results
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
            default -> ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown job: " + job + ". Valid: catalog, schedule, results, lock"
            ));
        };
    }
}
