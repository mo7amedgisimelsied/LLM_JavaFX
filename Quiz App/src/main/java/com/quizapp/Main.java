package com.quizapp;

import com.quizapp.data.DatabaseManager;
import com.quizapp.data.QuizService;
import com.quizapp.model.Question;
import com.quizapp.model.QuizResult;
import com.quizapp.model.QuizSession;
import com.quizapp.model.Topic;
import com.quizapp.view.QuizView;
import com.quizapp.view.ResultView;
import com.quizapp.view.TopicSelectionView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class Main extends Application {

    private static final double WINDOW_WIDTH = 1440;
    private static final double WINDOW_HEIGHT = 1024;

    private Stage primaryStage;
    private QuizService quizService;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.quizService = new QuizService(
                new DatabaseManager("jdbc:mysql://localhost:3306/quiz_app_db", "root", "1234")
        );

        primaryStage.setTitle("Quiz App");
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(false);

        showTopicSelection();
        primaryStage.show();
    }

    private void showTopicSelection() {
        List<Topic> topics;
        try {
            topics = quizService.fetchTopics();
        } catch (SQLException ex) {
            showError("Unable to load topics from the database.", ex);
            topics = Collections.emptyList();
        }

        TopicSelectionView view = new TopicSelectionView(topics, this::startQuiz);
        setScene(view);
    }

    private void startQuiz(Topic topic) {
        List<Question> questions;
        try {
            questions = quizService.fetchQuestionsByTopic(topic.id());
        } catch (SQLException ex) {
            showError("Unable to load questions for the selected topic.", ex);
            return;
        }

        if (questions.isEmpty()) {
            showInfo("No questions are available for \"" + topic.name() + "\".");
            return;
        }

        QuizSession session = new QuizSession(topic, questions);
        showQuizView(session);
    }

    private void showQuizView(QuizSession session) {
        QuizView view = new QuizView(session, completedSession -> {
            QuizResult result = quizService.evaluate(completedSession);
            showResultView(result);
        });
        setScene(view);
    }

    private void showResultView(QuizResult result) {
        ResultView view = new ResultView(result, this::showTopicSelection);
        setScene(view);
    }

    private void setScene(Parent root) {
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showError(String message, Exception exception) {
        exception.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unexpected Error");
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}