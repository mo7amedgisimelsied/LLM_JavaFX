package com.quizapp.model;

public record QuizResult(Topic topic, int correctAnswers, int totalQuestions) {

    public double percentage() {
        if (totalQuestions == 0) {
            return 0;
        }
        return (correctAnswers * 100.0) / totalQuestions;
    }
}