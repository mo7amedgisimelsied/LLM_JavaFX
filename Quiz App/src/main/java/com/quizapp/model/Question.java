package com.quizapp.model;

public class Question {
    private final int id;
    private final String text;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final String optionD;
    private final String correctAnswer; // 'A', 'B', 'C', or 'D'

    public Question(int id, String text, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
}