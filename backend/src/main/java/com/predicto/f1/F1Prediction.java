package com.predicto.f1;

import com.predicto.auth.User;
import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "f1_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class F1Prediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private F1Session session;

    @Column(name = "predicted_pole_driver_number")
    private Integer predictedPoleDriverNumber;

    @Column(name = "predicted_p1_driver_number")
    private Integer predictedP1DriverNumber;

    @Column(name = "predicted_p2_driver_number")
    private Integer predictedP2DriverNumber;

    @Column(name = "predicted_p3_driver_number")
    private Integer predictedP3DriverNumber;

    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private F1PredictionStatus status = F1PredictionStatus.PENDING;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
