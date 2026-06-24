package com.predicto.catalog.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncRunnerService {

    private final SyncRunRepository syncRunRepository;

    public SyncRun startRun(String jobName) {
        SyncRun run = SyncRun.builder()
                .jobName(jobName)
                .startedAt(OffsetDateTime.now())
                .status(SyncRunStatus.SUCCESS)
                .itemsProcessed(0)
                .build();
        return syncRunRepository.save(run);
    }

    @Transactional
    public void completeRun(SyncRun run, int itemsProcessed) {
        run.setFinishedAt(OffsetDateTime.now());
        run.setItemsProcessed(itemsProcessed);
        syncRunRepository.save(run);
    }

    @Transactional
    public void failRun(SyncRun run, String errorMessage) {
        run.setFinishedAt(OffsetDateTime.now());
        run.setStatus(SyncRunStatus.FAILED);
        run.setErrorMessage(errorMessage);
        syncRunRepository.save(run);
    }
}
