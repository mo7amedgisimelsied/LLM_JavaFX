package com.quizapp.model;

public class Question {
    private int questionId;
    private int topicId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char correctAnswer;
    private Character userAnswer;

    public Question(int questionId, int topicId, String questionText,
                    String optionA, String optionB, String optionC,
                    String optionD, char correctAnswer) {
        this.questionId = questionId;
        this.topicId = topicId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.userAnswer = null;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public char getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(char correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Character getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(Character userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isAnsweredCorrectly() {
        return userAnswer != null && userAnswer == correctAnswer;
    }
}