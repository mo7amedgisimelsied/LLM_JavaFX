package com.quizapp;

import com.quizapp.db.DatabaseManager;
import com.quizapp.model.Question;
import com.quizapp.model.Topic;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizApp extends Application {

    private Stage primaryStage;
    private DatabaseManager dbManager;
    private Scene mainScene;

    // Quiz State
    private List<Question> currentQuestions;
    private int currentQuestionIndex;
    private Map<Integer, String> userAnswers; // Question Index -> Selected Option (A,B,C,D)

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.dbManager = new DatabaseManager();

        stage.setTitle("Quiz App");
        stage.setWidth(1440);
        stage.setHeight(1024);
        stage.setResizable(false);

        showTopicSelection();
        stage.show();
    }

    // ==========================================
    // VIEW 1: Topic Selection
    // ==========================================
    private void showTopicSelection() {
        VBox root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label titleLabel = new Label("Select Topic");
        titleLabel.getStyleClass().add("page-title");

        VBox topicsContainer = new VBox(20);
        topicsContainer.setAlignment(Pos.CENTER);

        // Loading indicator
        ProgressIndicator spinner = new ProgressIndicator();
        topicsContainer.getChildren().add(spinner);

        root.getChildren().addAll(titleLabel, topicsContainer);
        setupScene(root);

        // Fetch topics in background
        Task<List<Topic>> task = new Task<>() {
            @Override
            protected List<Topic> call() {
                return dbManager.getAllTopics();
            }
        };

        task.setOnSucceeded(e -> {
            topicsContainer.getChildren().clear();
            List<Topic> topics = task.getValue();
            if (topics.isEmpty()) {
                Label noData = new Label("No topics found in database.");
                noData.getStyleClass().add("question-text"); // Recycle style for visibility
                topicsContainer.getChildren().add(noData);
            } else {
                for (Topic topic : topics) {
                    Button topicBtn = new Button(topic.getName());
                    topicBtn.getStyleClass().add("topic-button");
                    topicBtn.setOnAction(event -> startQuiz(topic));
                    topicsContainer.getChildren().add(topicBtn);
                }
            }
        });

        new Thread(task).start();
    }

    // ==========================================
    // LOGIC: Quiz Initialization
    // ==========================================
    private void startQuiz(Topic topic) {
        Task<List<Question>> task = new Task<>() {
            @Override
            protected List<Question> call() {
                return dbManager.getQuestionsByTopicId(topic.getId());
            }
        };

        task.setOnSucceeded(e -> {
            this.currentQuestions = task.getValue();
            this.currentQuestionIndex = 0;
            this.userAnswers = new HashMap<>();

            if (currentQuestions.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No questions available for this topic.");
                alert.show();
                return;
            }
            showQuizView();
        });

        new Thread(task).start();
    }

    // ==========================================
    // VIEW 2: Quiz Question View
    // ==========================================
    private void showQuizView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(50));

        // Top: Question Number
        Label qNumLabel = new Label("Question " + (currentQuestionIndex + 1));
        qNumLabel.getStyleClass().add("page-title");

        // Center: Question Content
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.CENTER_LEFT);
        centerBox.setMaxWidth(800);

        // Question Text Box
        Label questionText = new Label(currentQuestions.get(currentQuestionIndex).getText());
        questionText.setWrapText(true);
        questionText.getStyleClass().add("question-text");

        HBox questionBoxContainer = new HBox(questionText);
        questionBoxContainer.getStyleClass().add("question-box");
        questionBoxContainer.setAlignment(Pos.CENTER_LEFT);

        // Options
        VBox optionsBox = new VBox(20);
        optionsBox.setPadding(new Insets(20, 0, 0, 0));
        ToggleGroup group = new ToggleGroup();

        Question q = currentQuestions.get(currentQuestionIndex);
        RadioButton optA = createOption(q.getOptionA(), "A", group);
        RadioButton optB = createOption(q.getOptionB(), "B", group);
        RadioButton optC = createOption(q.getOptionC(), "C", group);
        RadioButton optD = createOption(q.getOptionD(), "D", group);

        // Restore previous selection if any
        String savedAnswer = userAnswers.get(currentQuestionIndex);
        if (savedAnswer != null) {
            switch (savedAnswer) {
                case "A" -> optA.setSelected(true);
                case "B" -> optB.setSelected(true);
                case "C" -> optC.setSelected(true);
                case "D" -> optD.setSelected(true);
            }
        }

        // Listener to save selection
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                RadioButton selected = (RadioButton) newVal;
                userAnswers.put(currentQuestionIndex, (String) selected.getUserData());
            }
        });

        optionsBox.getChildren().addAll(optA, optB, optC, optD);
        centerBox.getChildren().addAll(qNumLabel, questionBoxContainer, optionsBox);

        // Bottom: Navigation
        HBox navBox = new HBox(300); // Space between buttons
        navBox.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("< Previous");
        prevBtn.getStyleClass().add("nav-button");
        prevBtn.setDisable(currentQuestionIndex == 0); // FR2.11
        prevBtn.setOnAction(e -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuizView();
            }
        });

        boolean isLast = currentQuestionIndex == currentQuestions.size() - 1;
        Button nextBtn = new Button(isLast ? "Submit" : "Next >"); // FR2.12
        nextBtn.getStyleClass().add("nav-button");
        nextBtn.setOnAction(e -> {
            if (isLast) {
                processResults();
            } else {
                currentQuestionIndex++;
                showQuizView();
            }
        });

        navBox.getChildren().addAll(prevBtn, nextBtn);

        // Layout Assembly
        // Using StackPane to center the content vertically
        VBox mainContent = new VBox(50, centerBox, navBox);
        mainContent.setAlignment(Pos.CENTER);

        root.setCenter(mainContent);

        setupScene(root);
    }

    private RadioButton createOption(String text, String value, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setUserData(value);
        rb.setToggleGroup(group);
        rb.getStyleClass().add("option-radio");
        return rb;
    }

    // ==========================================
    // VIEW 3: Results
    // ==========================================
    private void processResults() {
        int score = 0;
        int total = currentQuestions.size();

        for (int i = 0; i < total; i++) {
            String correct = currentQuestions.get(i).getCorrectAnswer();
            String userSel = userAnswers.get(i);
            if (correct != null && correct.equals(userSel)) {
                score++;
            }
        }
        showResultView(score, total);
    }

    private void showResultView(int score, int total) {
        VBox root = new VBox(50);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label scoreLabel = new Label("Score: " + score + "/" + total);
        scoreLabel.getStyleClass().add("page-title");

        Button homeBtn = new Button("Select Different Topic");
        homeBtn.getStyleClass().add("action-button"); // distinct style for result page button
        homeBtn.setOnAction(e -> showTopicSelection());

        root.getChildren().addAll(scoreLabel, homeBtn);
        setupScene(root);
    }

    // ==========================================
    // Helpers
    // ==========================================
    private void setupScene(javafx.scene.Parent root) {
        mainScene = new Scene(root);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(mainScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}