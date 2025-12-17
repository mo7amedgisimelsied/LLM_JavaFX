package com.quizapp.view;

import com.quizapp.model.QuizResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ResultView extends StackPane {

    public ResultView(QuizResult result, Runnable onSelectDifferentTopic) {
        setPadding(new Insets(40));
        getStyleClass().add("screen-background");

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label scoreLabel = new Label("Score: " + result.correctAnswers() + "/" + result.totalQuestions());
        scoreLabel.getStyleClass().add("result-score");

        Button selectTopicButton = new Button("Select Different Topic");
        selectTopicButton.getStyleClass().add("primary-button");
        selectTopicButton.setOnAction(e -> onSelectDifferentTopic.run());

        content.getChildren().addAll(scoreLabel, selectTopicButton);
        getChildren().add(content);
    }
}