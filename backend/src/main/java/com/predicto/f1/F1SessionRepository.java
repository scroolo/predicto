package com.predicto.f1;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface F1SessionRepository extends JpaRepository<F1Session, UUID> {

    Optional<F1Session> findBySessionKey(Integer sessionKey);

    List<F1Session> findByStatusOrderByDateStartAsc(F1SessionStatus status);

    List<F1Session> findByMeetingIdOrderByDateStartAsc(UUID meetingId);

    @Query("SELECT s FROM F1Session s WHERE s.dateStart > :now AND s.isCancelled = false ORDER BY s.dateStart ASC")
    List<F1Session> findUpcomingSessions(OffsetDateTime now, Pageable pageable);

    @Query("SELECT s FROM F1Session s WHERE s.meeting.id = :meetingId ORDER BY s.dateStart ASC")
    List<F1Session> findByMeetingId(UUID meetingId);
}
