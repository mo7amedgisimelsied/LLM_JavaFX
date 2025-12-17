package com.quizapp.view;

import com.quizapp.model.Topic;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class TopicSelectionView extends StackPane {

    public TopicSelectionView(List<Topic> topics, Consumer<Topic> onTopicSelected) {
        setPadding(new Insets(40));
        getStyleClass().add("screen-background");

        VBox contentBox = new VBox(32);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(420);

        Label titleLabel = new Label("Select Topic");
        titleLabel.getStyleClass().add("page-title");

        VBox topicsBox = new VBox(18);
        topicsBox.setAlignment(Pos.CENTER);

        if (topics.isEmpty()) {
            Label emptyLabel = new Label("No topics were found.\nPlease ensure the database is populated.");
            emptyLabel.getStyleClass().add("empty-text");
            emptyLabel.setWrapText(true);
            emptyLabel.setAlignment(Pos.CENTER);
            topicsBox.getChildren().add(emptyLabel);
        } else {
            topics.forEach(topic -> {
                Button topicButton = new Button(topic.name());
                topicButton.getStyleClass().add("topic-button");
                topicButton.setMaxWidth(Double.MAX_VALUE);
                topicButton.setMinWidth(320);
                topicButton.setOnAction(e -> onTopicSelected.accept(topic));
                topicsBox.getChildren().add(topicButton);
            });
        }

        contentBox.getChildren().addAll(titleLabel, topicsBox);
        getChildren().add(contentBox);
        StackPane.setAlignment(contentBox, Pos.CENTER);
    }
}