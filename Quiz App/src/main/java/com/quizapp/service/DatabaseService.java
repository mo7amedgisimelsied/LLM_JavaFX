package com.quizapp.service;

import com.quizapp.model.Topic;
import com.quizapp.model.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static final String URL = "jdbc:mysql://localhost:3306/quiz_app_db";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        String query = "SELECT topic_id, topic_name FROM Topics ORDER BY topic_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int topicId = rs.getInt("topic_id");
                String topicName = rs.getString("topic_name");
                topics.add(new Topic(topicId, topicName));
            }

        } catch (SQLException e) {
            System.err.println("Error loading topics: " + e.getMessage());
            e.printStackTrace();
        }

        return topics;
    }

    public List<Question> getQuestionsByTopic(int topicId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT question_id, topic_id, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer " +
                "FROM Questions WHERE topic_id = ? ORDER BY question_id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, topicId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("question_id");
                    String questionText = rs.getString("question_text");
                    String optionA = rs.getString("option_a");
                    String optionB = rs.getString("option_b");
                    String optionC = rs.getString("option_c");
                    String optionD = rs.getString("option_d");
                    char correctAnswer = rs.getString("correct_answer").charAt(0);

                    questions.add(new Question(questionId, topicId, questionText,
                            optionA, optionB, optionC, optionD,
                            correctAnswer));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading questions: " + e.getMessage());
            e.printStackTrace();
        }

        return questions;
    }
}