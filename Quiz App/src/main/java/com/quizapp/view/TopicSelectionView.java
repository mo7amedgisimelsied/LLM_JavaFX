package com.quizapp.view;

import com.quizapp.model.Topic;
import com.quizapp.service.DatabaseService;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.quizapp.Main;
import java.util.List;

public class TopicSelectionView extends VBox {

    private DatabaseService databaseService;

    public TopicSelectionView() {
        this.databaseService = new DatabaseService();

        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.getStyleClass().add("root-pane");

        Label titleLabel = new Label("Select Topic");
        titleLabel.getStyleClass().add("title-label");

        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(300);

        List<Topic> topics = databaseService.getAllTopics();

        if (topics.isEmpty()) {
            Label noTopicsLabel = new Label("No topics available");
            noTopicsLabel.getStyleClass().add("subtitle-label");
            buttonContainer.getChildren().add(noTopicsLabel);
        } else {
            for (Topic topic : topics) {
                Button topicButton = new Button(topic.getTopicName());
                topicButton.getStyleClass().add("topic-button");
                topicButton.setMaxWidth(Double.MAX_VALUE);

                topicButton.setOnAction(e -> {
                    QuizView quizView = new QuizView(topic);
                    Scene scene = new Scene(quizView, 1440, 1024);
                    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                    Main.getPrimaryStage().setScene(scene);
                });

                buttonContainer.getChildren().add(topicButton);
            }
        }

        this.getChildren().addAll(titleLabel, buttonContainer);
    }
}