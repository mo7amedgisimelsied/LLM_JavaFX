package com.agecalculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Main extends Application {

    private DatePicker dobPicker;
    private DatePicker currentDatePicker;
    private Label yearsValueLabel;
    private Label monthsValueLabel;
    private Label daysValueLabel;

    @Override
    public void start(Stage primaryStage) {
        // Main container
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header Section
        StackPane header = createHeader();

        // Form Section
        VBox formSection = createFormSection();

        // Calculate Button
        Button calculateButton = createCalculateButton();

        // Results Section
        HBox resultsSection = createResultsSection();

        // Add all sections to root
        root.getChildren().addAll(header, formSection, calculateButton, resultsSection);
        VBox.setMargin(formSection, new Insets(80, 0, 40, 0));
        VBox.setMargin(calculateButton, new Insets(0, 0, 60, 0));
        VBox.setMargin(resultsSection, new Insets(0, 0, 40, 0));

        // Scene
        Scene scene = new Scene(root, 1440, 1024);

        primaryStage.setTitle("Age Calculator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private StackPane createHeader() {
        StackPane header = new StackPane();
        header.setStyle("-fx-background-color: #1abc9c;");
        header.setPrefHeight(96);
        header.setMaxHeight(96);
        header.setMinHeight(96);

        Label titleLabel = new Label("Age Calculator");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: black;");

        header.getChildren().add(titleLabel);
        StackPane.setAlignment(titleLabel, Pos.CENTER);

        return header;
    }

    private VBox createFormSection() {
        VBox formSection = new VBox(40);
        formSection.setAlignment(Pos.CENTER);
        formSection.setPadding(new Insets(0, 200, 0, 200));

        // Date of Birth Row
        HBox dobRow = createDateInputRow("Date of Birth", true);

        // Current Date Row
        HBox currentDateRow = createDateInputRow("Current Date", false);

        formSection.getChildren().addAll(dobRow, currentDateRow);

        return formSection;
    }

    private HBox createDateInputRow(String labelText, boolean isDOB) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(60);

        // Label
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        label.setPrefWidth(250);
        label.setMinWidth(250);
        label.setAlignment(Pos.CENTER_LEFT);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // DatePicker Container
        HBox datePickerContainer = new HBox(10);
        datePickerContainer.setAlignment(Pos.CENTER_LEFT);
        datePickerContainer.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; " +
                "-fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
        datePickerContainer.setPrefWidth(250);
        datePickerContainer.setPrefHeight(45);
        datePickerContainer.setPadding(new Insets(0, 10, 0, 10));

        DatePicker datePicker = new DatePicker();
        datePicker.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        datePicker.setPrefWidth(190);
        datePicker.setMaxWidth(190);

        // Set default values as shown in the image
        if (isDOB) {
            datePicker.setValue(LocalDate.of(2003, 6, 22));
            dobPicker = datePicker;
        } else {
            datePicker.setValue(LocalDate.of(2025, 11, 25));
            currentDatePicker = datePicker;
        }

        // Format the date picker
        datePicker.setPromptText("MM/DD/YYYY");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        // Calendar Icon (SVG)
        SVGPath calendarIcon = new SVGPath();
        calendarIcon.setContent("M30.25 1V10M12.25 1V10M1 19H41.5M5.5 5.5H37C39.4853 5.5 41.5 7.51472 " +
                "41.5 10V41.5C41.5 43.9853 39.4853 46 37 46H5.5C3.01472 46 1 43.9853 1 41.5V10C1 " +
                "7.51472 3.01472 5.5 5.5 5.5Z");
        calendarIcon.setStroke(Color.web("#1E1E1E"));
        calendarIcon.setStrokeWidth(2);
        calendarIcon.setFill(Color.TRANSPARENT);
        calendarIcon.setScaleX(0.5);
        calendarIcon.setScaleY(0.5);

        datePickerContainer.getChildren().addAll(datePicker, calendarIcon);

        row.getChildren().addAll(label, spacer, datePickerContainer);

        return row;
    }

    private Button createCalculateButton() {
        Button calculateButton = new Button("Calculate");
        calculateButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: black; " +
                "-fx-font-size: 22px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-padding: 18 60 18 60;");

        calculateButton.setOnMouseEntered(e ->
                calculateButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: black; " +
                        "-fx-font-size: 22px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-padding: 18 60 18 60;")
        );

        calculateButton.setOnMouseExited(e ->
                calculateButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: black; " +
                        "-fx-font-size: 22px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-padding: 18 60 18 60;")
        );

        calculateButton.setOnAction(e -> calculateAge());

        VBox buttonContainer = new VBox(calculateButton);
        buttonContainer.setAlignment(Pos.CENTER);

        return calculateButton;
    }

    private HBox createResultsSection() {
        HBox resultsSection = new HBox(40);
        resultsSection.setAlignment(Pos.CENTER);
        resultsSection.setPadding(new Insets(0, 200, 0, 200));

        // Years Box
        VBox yearsBox = createResultBox("Years", "22");
        yearsValueLabel = (Label) yearsBox.getChildren().get(1);

        // Months Box
        VBox monthsBox = createResultBox("Months", "5");
        monthsValueLabel = (Label) monthsBox.getChildren().get(1);

        // Days Box
        VBox daysBox = createResultBox("Days", "3");
        daysValueLabel = (Label) daysBox.getChildren().get(1);

        resultsSection.getChildren().addAll(yearsBox, monthsBox, daysBox);

        return resultsSection;
    }

    private VBox createResultBox(String labelText, String valueText) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1abc9c; -fx-background-radius: 8;");
        box.setPrefWidth(160);
        box.setPrefHeight(120);
        box.setPadding(new Insets(20));

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: black;");

        box.getChildren().addAll(label, value);

        return box;
    }

    private void calculateAge() {
        // FR2.4: Validate that both fields are filled
        if (dobPicker.getValue() == null) {
            showError("Validation Error", "Please select a Date of Birth.");
            return;
        }

        if (currentDatePicker.getValue() == null) {
            showError("Validation Error", "Please select a Current Date.");
            return;
        }

        LocalDate dob = dobPicker.getValue();
        LocalDate currentDate = currentDatePicker.getValue();

        // FR1.5: Prevent calculation if DOB is after Current Date
        if (dob.isAfter(currentDate)) {
            showError("Validation Error", "Date of Birth cannot be later than the Current Date.");
            return;
        }

        // FR2.2 & FR2.3: Calculate the period between DOB and Current Date
        Period period = Period.between(dob, currentDate);

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        // FR3.1, FR3.2, FR3.3: Display the results
        yearsValueLabel.setText(String.valueOf(years));
        monthsValueLabel.setText(String.valueOf(months));
        daysValueLabel.setText(String.valueOf(days));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}