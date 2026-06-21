package com.predicto.catalog.sync;

import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sync_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRun extends BaseEntity {

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncRunStatus status;

    @Column(name = "items_processed", nullable = false)
    @Builder.Default
    private Integer itemsProcessed = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
