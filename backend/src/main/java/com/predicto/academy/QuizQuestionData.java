package com.predicto.academy;

public record QuizQuestionData(
    String question,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    String correctOption,
    int sortOrder
) {}
