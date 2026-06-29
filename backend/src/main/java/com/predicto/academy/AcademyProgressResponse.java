package com.predicto.academy;

import java.util.List;

public record AcademyProgressResponse(
    int completedLessons,
    int totalXp,
    List<UserCertificate> certificates
) {}
