package com.quizapp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizSession {

    private final Topic topic;
    private final List<Question> questions;
    private final Map<Integer, Character> answers = new HashMap<>();

    public QuizSession(Topic topic, List<Question> questions) {
        this.topic = topic;
        this.questions = List.copyOf(questions);
    }

    public Topic getTopic() {
        return topic;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void saveAnswer(int questionId, char answer) {
        answers.put(questionId, Character.toUpperCase(answer));
    }

    public Character getAnswer(int questionId) {
        return answers.get(questionId);
    }

    public void removeAnswer(int questionId) {
        answers.remove(questionId);
    }

    public Map<Integer, Character> getAnswers() {
        return Map.copyOf(answers);
    }
}