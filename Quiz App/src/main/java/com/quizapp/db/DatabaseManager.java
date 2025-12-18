package com.quizapp.db;

import com.quizapp.model.Question;
import com.quizapp.model.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/quiz_app_db";
    private static final String USER = "root";
    private static final String PASS = "1234";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT topic_id, topic_name FROM Topics";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                topics.add(new Topic(rs.getInt("topic_id"), rs.getString("topic_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public List<Question> getQuestionsByTopicId(int topicId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Questions WHERE topic_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, topicId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(new Question(
                            rs.getInt("question_id"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_answer")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }
}