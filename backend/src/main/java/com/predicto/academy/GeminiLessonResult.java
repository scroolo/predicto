package com.predicto.academy;

import java.util.List;

public record GeminiLessonResult(
    String title,
    String summary,
    String content,
    List<QuizQuestionData> questions
) {}
