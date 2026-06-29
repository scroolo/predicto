package com.predicto.academy;

import java.util.List;

public record AcademyProgressResponse(
    int completedLessons,
    int totalXp,
    List<CertificateDto> certificates
) {
    public record CertificateDto(String courseId, String courseTitle) {}
}
