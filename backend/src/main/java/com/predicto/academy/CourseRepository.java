package com.predicto.academy;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByPublishedTrueOrderByCategoryAscLevelAsc();
    List<Course> findByCategoryAndPublishedTrueOrderByLevelAsc(AcademyCategory category);
    List<Course> findByCategoryAndLevelAndPublishedTrue(AcademyCategory category, AcademyLevel level);
}
