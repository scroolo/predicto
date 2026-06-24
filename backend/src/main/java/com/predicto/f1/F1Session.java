package com.predicto.f1;

import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "f1_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class F1Session extends BaseEntity {

    @Column(name = "session_key", nullable = false, unique = true)
    private Integer sessionKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private F1Meeting meeting;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "session_type", nullable = false)
    private String sessionType;

    @Column(name = "date_start", nullable = false)
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Column(name = "is_cancelled", nullable = false)
    @Builder.Default
    private Boolean isCancelled = false;

    @Column(name = "result_p1_driver_number")
    private Integer resultP1DriverNumber;

    @Column(name = "result_p2_driver_number")
    private Integer resultP2DriverNumber;

    @Column(name = "result_p3_driver_number")
    private Integer resultP3DriverNumber;

    @Column(name = "result_pole_driver_number")
    private Integer resultPoleDriverNumber;

    @Column(name = "predictions_locked")
    @Builder.Default
    private Boolean predictionsLocked = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private F1SessionStatus status = F1SessionStatus.UPCOMING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
