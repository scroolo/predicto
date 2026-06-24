package com.predicto.f1;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface F1PredictionRepository extends JpaRepository<F1Prediction, UUID> {

    Optional<F1Prediction> findByUserIdAndSessionId(UUID userId, UUID sessionId);

    List<F1Prediction> findBySessionId(UUID sessionId);

    List<F1Prediction> findByUserId(UUID userId);

    boolean existsByUserIdAndSessionId(UUID userId, UUID sessionId);
}
