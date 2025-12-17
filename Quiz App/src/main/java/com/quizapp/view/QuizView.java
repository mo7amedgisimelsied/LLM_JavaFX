package com.quizapp.view;

import com.quizapp.model.Topic;
import com.quizapp.model.Question;
import com.quizapp.service.DatabaseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.quizapp.Main;
import java.util.List;

public class QuizView extends BorderPane {

    private Topic topic;
    private List<Question> questions;
    private int currentQuestionIndex;

    private Label questionNumberLabel;
    private Label questionTextLabel;
    private ToggleGroup answerGroup;
    private RadioButton optionARadio;
    private RadioButton optionBRadio;
    private RadioButton optionCRadio;
    private RadioButton optionDRadio;
    private Button previousButton;
    private Button nextButton;

    private DatabaseService databaseService;

    public QuizView(Topic topic) {
        this.topic = topic;
        this.databaseService = new DatabaseService();
        this.questions = databaseService.getQuestionsByTopic(topic.getTopicId());
        this.currentQuestionIndex = 0;

        this.getStyleClass().add("root-pane");

        if (questions.isEmpty()) {
            Label noQuestionsLabel = new Label("No questions available for this topic");
            noQuestionsLabel.getStyleClass().add("title-label");
            VBox centerBox = new VBox(noQuestionsLabel);
            centerBox.setAlignment(Pos.CENTER);
            this.setCenter(centerBox);
            return;
        }

        initializeUI();
        displayQuestion();
    }

    private void initializeUI() {
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(50));
        centerBox.setMaxWidth(800);

        questionNumberLabel = new Label();
        questionNumberLabel.getStyleClass().add("title-label");

        VBox questionBox = new VBox(15);
        questionBox.setAlignment(Pos.CENTER_LEFT);
        questionBox.getStyleClass().add("question-box");
        questionBox.setPadding(new Insets(30));
        questionBox.setMaxWidth(Double.MAX_VALUE);

        questionTextLabel = new Label();
        questionTextLabel.getStyleClass().add("question-text");
        questionTextLabel.setWrapText(true);
        questionTextLabel.setMaxWidth(Double.MAX_VALUE);

        questionBox.getChildren().add(questionTextLabel);

        VBox optionsBox = new VBox(15);
        optionsBox.setPadding(new Insets(20, 0, 0, 0));

        answerGroup = new ToggleGroup();

        optionARadio = new RadioButton();
        optionARadio.setToggleGroup(answerGroup);
        optionARadio.getStyleClass().add("option-radio");
        optionARadio.setWrapText(true);
        optionARadio.setMaxWidth(Double.MAX_VALUE);

        optionBRadio = new RadioButton();
        optionBRadio.setToggleGroup(answerGroup);
        optionBRadio.getStyleClass().add("option-radio");
        optionBRadio.setWrapText(true);
        optionBRadio.setMaxWidth(Double.MAX_VALUE);

        optionCRadio = new RadioButton();
        optionCRadio.setToggleGroup(answerGroup);
        optionCRadio.getStyleClass().add("option-radio");
        optionCRadio.setWrapText(true);
        optionCRadio.setMaxWidth(Double.MAX_VALUE);

        optionDRadio = new RadioButton();
        optionDRadio.setToggleGroup(answerGroup);
        optionDRadio.getStyleClass().add("option-radio");
        optionDRadio.setWrapText(true);
        optionDRadio.setMaxWidth(Double.MAX_VALUE);

        answerGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                saveCurrentAnswer();
            }
        });

        optionsBox.getChildren().addAll(optionARadio, optionBRadio, optionCRadio, optionDRadio);

        HBox navigationBox = new HBox(20);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(30, 0, 0, 0));

        previousButton = new Button("< Previous");
        previousButton.getStyleClass().add("nav-button");
        previousButton.setOnAction(e -> previousQuestion());

        nextButton = new Button("Next >");
        nextButton.getStyleClass().add("nav-button");
        nextButton.setOnAction(e -> nextQuestion());

        navigationBox.getChildren().addAll(previousButton, nextButton);

        centerBox.getChildren().addAll(questionNumberLabel, questionBox, optionsBox, navigationBox);

        VBox centerWrapper = new VBox(centerBox);
        centerWrapper.setAlignment(Pos.CENTER);

        this.setCenter(centerWrapper);
    }

    private void displayQuestion() {
        Question currentQuestion = questions.get(currentQuestionIndex);

        questionNumberLabel.setText("Question " + (currentQuestionIndex + 1));
        questionTextLabel.setText(currentQuestion.getQuestionText());

        optionARadio.setText(currentQuestion.getOptionA());
        optionBRadio.setText(currentQuestion.getOptionB());
        optionCRadio.setText(currentQuestion.getOptionC());
        optionDRadio.setText(currentQuestion.getOptionD());

        answerGroup.selectToggle(null);

        if (currentQuestion.getUserAnswer() != null) {
            switch (currentQuestion.getUserAnswer()) {
                case 'A' -> answerGroup.selectToggle(optionARadio);
                case 'B' -> answerGroup.selectToggle(optionBRadio);
                case 'C' -> answerGroup.selectToggle(optionCRadio);
                case 'D' -> answerGroup.selectToggle(optionDRadio);
            }
        }

        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        previousButton.setDisable(currentQuestionIndex == 0);
        previousButton.setVisible(currentQuestionIndex != 0);

        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next >");
        }
    }

    private void saveCurrentAnswer() {
        Toggle selectedToggle = answerGroup.getSelectedToggle();
        if (selectedToggle != null) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            if (selectedToggle == optionARadio) {
                currentQuestion.setUserAnswer('A');
            } else if (selectedToggle == optionBRadio) {
                currentQuestion.setUserAnswer('B');
            } else if (selectedToggle == optionCRadio) {
                currentQuestion.setUserAnswer('C');
            } else if (selectedToggle == optionDRadio) {
                currentQuestion.setUserAnswer('D');
            }
        }
    }

    private void previousQuestion() {
        if (currentQuestionIndex > 0) {
            saveCurrentAnswer();
            currentQuestionIndex--;
            displayQuestion();
        }
    }

    private void nextQuestion() {
        saveCurrentAnswer();

        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayQuestion();
        } else {
            showResults();
        }
    }

    private void showResults() {
        int correctCount = 0;
        for (Question question : questions) {
            if (question.isAnsweredCorrectly()) {
                correctCount++;
            }
        }

        ResultView resultView = new ResultView(correctCount, questions.size());
        Scene scene = new Scene(resultView, 1440, 1024);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        Main.getPrimaryStage().setScene(scene);
    }
}