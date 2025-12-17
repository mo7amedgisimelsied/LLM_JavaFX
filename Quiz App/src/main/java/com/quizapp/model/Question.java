package com.quizapp.model;

public record Question(
        int questionId,
        int topicId,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        char correctAnswer
) { }