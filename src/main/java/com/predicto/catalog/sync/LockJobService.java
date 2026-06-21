package com.predicto.catalog.sync;

import com.predicto.catalog.Match;
import com.predicto.catalog.MatchRepository;
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
public class LockJobService {

    private final MatchRepository matchRepository;
    private final SyncRunnerService syncRunner;

    @Scheduled(fixedRateString = "${cito.sync.lock-interval-ms:60000}")
    public void lockMatches() {
        SyncRun run = syncRunner.startRun("lock-job");
        int total = 0;
        try {
            total += doLock();
            syncRunner.completeRun(run, total);
        } catch (Exception e) {
            log.error("Lock job failed", e);
            syncRunner.failRun(run, e.getMessage());
        }
    }

    @Transactional
    public int doLock() {
        OffsetDateTime threshold = OffsetDateTime.now().plusMinutes(15);
        List<Match> matchesToLock = matchRepository
                .findByStatusAndStartsAtLessThanEqual(MatchStatus.SCHEDULED, threshold);
        int count = 0;
        for (Match match : matchesToLock) {
            match.setStatus(MatchStatus.LOCKED);
            match.setLockedAt(OffsetDateTime.now());
            matchRepository.save(match);
            count++;
        }
        log.info("Locked {} matches", count);
        return count;
    }
}
