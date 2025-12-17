package com.quizapp.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.quizapp.Main;

public class ResultView extends VBox {

    public ResultView(int correctAnswers, int totalQuestions) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);
        this.getStyleClass().add("root-pane");

        Label scoreLabel = new Label("Score: " + correctAnswers + "/" + totalQuestions);
        scoreLabel.getStyleClass().add("score-label");

        Button selectTopicButton = new Button("Select Different Topic");
        selectTopicButton.getStyleClass().add("topic-button");
        selectTopicButton.setPrefWidth(300);

        selectTopicButton.setOnAction(e -> {
            Main.showTopicSelection();
        });

        this.getChildren().addAll(scoreLabel, selectTopicButton);
    }
}