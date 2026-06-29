package com.predicto.academy;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByCourseIdAndPublishedTrueOrderBySortOrderAsc(UUID courseId);
    List<Lesson> findByCourseIdOrderBySortOrderAsc(UUID courseId);
    long countByCourseIdAndPublishedTrue(UUID courseId);
}
