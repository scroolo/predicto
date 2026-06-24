package com.predicto.f1;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface F1MeetingRepository extends JpaRepository<F1Meeting, UUID> {

    Optional<F1Meeting> findByMeetingKey(Integer meetingKey);

    List<F1Meeting> findByYearOrderByDateStartAsc(int year);

    @Query("SELECT m FROM F1Meeting m WHERE (m.dateEnd IS NOT NULL AND m.dateEnd > :now OR m.dateStart > :now) AND m.isCancelled = false ORDER BY m.dateStart ASC")
    List<F1Meeting> findUpcomingMeetings(OffsetDateTime now, Pageable pageable);
}
