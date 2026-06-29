package com.predicto.academy;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, UUID> {
    List<UserCertificate> findByUserId(UUID userId);
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
}
