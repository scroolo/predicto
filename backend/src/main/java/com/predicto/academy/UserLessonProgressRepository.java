package com.predicto.academy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UUID> {

    Optional<UserLessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<UserLessonProgress> findByUserIdAndCompletedTrue(UUID userId);

    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.user.id = :userId AND p.lesson.course.id = :courseId AND p.completed = true")
    long countCompletedLessonsByCourse(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT p.lesson.id FROM UserLessonProgress p WHERE p.user.id = :userId AND p.completed = true")
    List<UUID> findCompletedLessonIdsByUserId(@Param("userId") UUID userId);
}
