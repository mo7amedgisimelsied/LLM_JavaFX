package com.quizapp.data;

import com.quizapp.model.Question;
import com.quizapp.model.QuizResult;
import com.quizapp.model.QuizSession;
import com.quizapp.model.Topic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    private final DatabaseManager databaseManager;

    public QuizService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Topic> fetchTopics() throws SQLException {
        String sql = "SELECT topic_id, topic_name FROM Topics ORDER BY topic_name";
        List<Topic> topics = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                topics.add(new Topic(
                        resultSet.getInt("topic_id"),
                        resultSet.getString("topic_name")
                ));
            }
        }

        return topics;
    }

    public List<Question> fetchQuestionsByTopic(int topicId) throws SQLException {
        String sql = """
                SELECT question_id, topic_id, question_text,
                       option_a, option_b, option_c, option_d, correct_answer
                FROM Questions
                WHERE topic_id = ?
                ORDER BY question_id
                """;

        List<Question> questions = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, topicId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    questions.add(new Question(
                            resultSet.getInt("question_id"),
                            resultSet.getInt("topic_id"),
                            resultSet.getString("question_text"),
                            resultSet.getString("option_a"),
                            resultSet.getString("option_b"),
                            resultSet.getString("option_c"),
                            resultSet.getString("option_d"),
                            resultSet.getString("correct_answer").charAt(0)
                    ));
                }
            }
        }

        return questions;
    }

    public QuizResult evaluate(QuizSession session) {
        int correct = 0;

        for (Question question : session.getQuestions()) {
            Character selected = session.getAnswer(question.questionId());
            if (selected != null && Character.toUpperCase(selected) == Character.toUpperCase(question.correctAnswer())) {
                correct++;
            }
        }

        return new QuizResult(session.getTopic(), correct, session.getQuestions().size());
    }
}