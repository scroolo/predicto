package com.predicto.academy;

public record CompleteLessonResponse(
    int xpEarned,
    int quizScore,
    boolean certificateAwarded
) {}
