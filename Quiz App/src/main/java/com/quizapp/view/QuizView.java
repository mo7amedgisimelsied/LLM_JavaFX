package com.quizapp.view;

import com.quizapp.model.Question;
import com.quizapp.model.QuizSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class QuizView extends BorderPane {

    private final QuizSession session;
    private final Consumer<QuizSession> onSubmit;
    private final Label questionNumberLabel = new Label();
    private final Label questionTextLabel = new Label();
    private final RadioButton optionAButton = new RadioButton();
    private final RadioButton optionBButton = new RadioButton();
    private final RadioButton optionCButton = new RadioButton();
    private final RadioButton optionDButton = new RadioButton();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final Button previousButton = new Button("< Previous");
    private final Button nextButton = new Button("Next >");
    private final List<RadioButton> optionButtons;
    private int currentIndex = 0;
    private boolean suppressToggleListener = false;
    private Question currentQuestion;

    public QuizView(QuizSession session, Consumer<QuizSession> onSubmit) {
        this.session = session;
        this.onSubmit = onSubmit;
        this.optionButtons = List.of(optionAButton, optionBButton, optionCButton, optionDButton);

        getStyleClass().add("screen-background");
        setPadding(new Insets(80, 200, 80, 200));

        createLayout();
        bindToggleGroup();
        showCurrentQuestion();
    }

    private void createLayout() {
        questionNumberLabel.getStyleClass().add("page-title");

        questionTextLabel.setWrapText(true);
        questionTextLabel.getStyleClass().add("question-text");

        StackPane questionCard = new StackPane(questionTextLabel);
        questionCard.getStyleClass().add("question-card");

        VBox optionsBox = buildOptionsBox();
        HBox navigationBox = buildNavigationBox();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox content = new VBox(32);
        content.setAlignment(Pos.TOP_LEFT);
        content.getChildren().addAll(questionNumberLabel, questionCard, optionsBox, spacer, navigationBox);

        setCenter(content);
    }

    private VBox buildOptionsBox() {
        configureRadioButton(optionAButton, 'A');
        configureRadioButton(optionBButton, 'B');
        configureRadioButton(optionCButton, 'C');
        configureRadioButton(optionDButton, 'D');

        VBox optionsBox = new VBox(14, optionAButton, optionBButton, optionCButton, optionDButton);
        optionsBox.setAlignment(Pos.TOP_LEFT);
        return optionsBox;
    }

    private void configureRadioButton(RadioButton radioButton, char answerCode) {
        radioButton.setToggleGroup(toggleGroup);
        radioButton.setUserData(answerCode);
        radioButton.getStyleClass().add("option-radio");
    }

    private HBox buildNavigationBox() {
        previousButton.getStyleClass().add("nav-button");
        nextButton.getStyleClass().addAll("nav-button", "nav-primary");

        previousButton.setOnAction(e -> {
            if (currentIndex > 0) {
                currentIndex--;
                showCurrentQuestion();
            }
        });

        nextButton.setOnAction(e -> {
            if (isLastQuestion()) {
                onSubmit.accept(session);
            } else {
                currentIndex++;
                showCurrentQuestion();
            }
        });

        HBox navigation = new HBox(30, previousButton, nextButton);
        navigation.setAlignment(Pos.CENTER_LEFT);
        return navigation;
    }

    private void bindToggleGroup() {
        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (suppressToggleListener || currentQuestion == null) {
                return;
            }

            if (newToggle == null) {
                session.removeAnswer(currentQuestion.questionId());
            } else {
                char answerCode = (char) newToggle.getUserData();
                session.saveAnswer(currentQuestion.questionId(), answerCode);
            }
            updateOptionStyles();
        });
    }

    private void showCurrentQuestion() {
        currentQuestion = session.getQuestions().get(currentIndex);
        questionNumberLabel.setText("Question " + (currentIndex + 1));
        questionTextLabel.setText(currentQuestion.questionText());

        optionAButton.setText(currentQuestion.optionA());
        optionBButton.setText(currentQuestion.optionB());
        optionCButton.setText(currentQuestion.optionC());
        optionDButton.setText(currentQuestion.optionD());

        suppressToggleListener = true;
        toggleGroup.selectToggle(null);
        Character savedAnswer = session.getAnswer(currentQuestion.questionId());
        if (savedAnswer != null) {
            switch (Character.toUpperCase(savedAnswer)) {
                case 'A' -> toggleGroup.selectToggle(optionAButton);
                case 'B' -> toggleGroup.selectToggle(optionBButton);
                case 'C' -> toggleGroup.selectToggle(optionCButton);
                case 'D' -> toggleGroup.selectToggle(optionDButton);
                default -> toggleGroup.selectToggle(null);
            }
        }
        suppressToggleListener = false;

        updateOptionStyles();
        updateNavigationState();
    }

    private void updateNavigationState() {
        previousButton.setDisable(currentIndex == 0);
        nextButton.setText(isLastQuestion() ? "Submit" : "Next >");
    }

    private void updateOptionStyles() {
        optionButtons.forEach(radioButton -> {
            if (radioButton.isSelected()) {
                if (!radioButton.getStyleClass().contains("selected-option")) {
                    radioButton.getStyleClass().add("selected-option");
                }
            } else {
                radioButton.getStyleClass().remove("selected-option");
            }
        });
    }

    private boolean isLastQuestion() {
        return currentIndex == session.getQuestions().size() - 1;
    }
}