package com.quizapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.quizapp.view.TopicSelectionView;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Quiz App");
        primaryStage.setResizable(false);

        showTopicSelection();

        primaryStage.show();
    }

    public static void showTopicSelection() {
        TopicSelectionView topicView = new TopicSelectionView();
        Scene scene = new Scene(topicView, 1440, 1024);
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}