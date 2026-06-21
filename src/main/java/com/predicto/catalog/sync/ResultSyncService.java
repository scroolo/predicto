package com.predicto.catalog.sync;

import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
import com.predicto.catalog.Team;
import com.predicto.catalog.TeamRepository;
import com.predicto.catalog.cito.CitoApiClient;
import com.predicto.catalog.cito.MatchResultDto;
import com.predicto.catalog.cito.ScheduledMatchDto;
import com.predicto.common.enums.MatchStatus;
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
public class ResultSyncService {

    private final CitoApiClient citoClient;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final SyncRunnerService syncRunner;

    @Scheduled(fixedRateString = "${cito.sync.result-interval-ms:300000}")
    public void syncResults() {
        SyncRun run = syncRunner.startRun("result-sync");
        int total = 0;
        try {
            total += doSync();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Result sync failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    @Transactional
    public int doSync() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Match> pendingMatches = matchRepository
                .findByStartsAtLessThanEqualAndStatusNotIn(now,
                        List.of(MatchStatus.FINISHED, MatchStatus.CANCELLED));
        int count = 0;
        for (Match match : pendingMatches) {
            ScheduledMatchDto detail = citoClient.fetchMatchDetail(match.getExternalId());
            if (detail == null || detail.result() == null) continue;
            MatchResultDto result = detail.result();
            Team winner = teamRepository.findByExternalId(result.winner()).orElse(null);
            if (winner == null) {
                log.warn("Unknown winner team {} for match {}", result.winner(), match.getExternalId());
                continue;
            }
            match.setResultWinnerTeam(winner);
            match.setResultScore(result.score());
            match.setStatus(MatchStatus.FINISHED);
            match.setFinishedAt(OffsetDateTime.now());
            matchRepository.save(match);
            count++;
        }
        log.info("Synced {} match results", count);
        return count;
    }
}
