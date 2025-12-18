package com.example.agecalculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main extends Application {

    // SVG Path Data from specifications
    private static final String CALENDAR_ICON_PATH = "M30.25 1V10M12.25 1V10M1 19H41.5M5.5 5.5H37C39.4853 5.5 41.5 7.51472 41.5 10V41.5C41.5 43.9853 39.4853 46 37 46H5.5C3.01472 46 1 43.9853 1 41.5V10C1 7.51472 3.01472 5.5 5.5 5.5Z";

    private DatePicker dobPicker;
    private DatePicker currentDatePicker;
    private Label yearsValueLabel;
    private Label monthsValueLabel;
    private Label daysValueLabel;
    private Label errorLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // --- Header ---
        VBox header = new VBox();
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Age Calculator");
        titleLabel.getStyleClass().add("header-title");
        header.getChildren().add(titleLabel);
        root.setTop(header);

        // --- Center Content ---
        VBox contentBox = new VBox(40); // Spacing between sections
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(60, 20, 20, 20));
        contentBox.setMaxWidth(800); // Constraint for better visuals on large screen

        // --- Form Section ---
        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(40);
        formGrid.setVgap(30);

        // Date of Birth Row
        Label dobLabel = new Label("Date of Birth");
        dobLabel.getStyleClass().add("input-label");
        dobPicker = createStyledDatePicker("Select Date of Birth");

        formGrid.add(dobLabel, 0, 0);
        formGrid.add(dobPicker, 1, 0);

        // Current Date Row
        Label currentLabel = new Label("Current Date");
        currentLabel.getStyleClass().add("input-label");
        currentDatePicker = createStyledDatePicker("Select Current Date");
        // Initialize current date to today as a convenience
        currentDatePicker.setValue(LocalDate.now());

        formGrid.add(currentLabel, 0, 1);
        formGrid.add(currentDatePicker, 1, 1);

        // --- Action Button ---
        Button calculateBtn = new Button("Calculate");
        calculateBtn.getStyleClass().add("action-button");
        calculateBtn.setOnAction(e -> calculateAge());

        // Error Message Area
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        VBox actionBox = new VBox(20, calculateBtn, errorLabel);
        actionBox.setAlignment(Pos.CENTER);

        // --- Results Section ---
        HBox resultsBox = new HBox(30);
        resultsBox.setAlignment(Pos.CENTER);

        yearsValueLabel = new Label("--");
        monthsValueLabel = new Label("--");
        daysValueLabel = new Label("--");

        resultsBox.getChildren().addAll(
                createResultCard("Years", yearsValueLabel),
                createResultCard("Months", monthsValueLabel),
                createResultCard("Days", daysValueLabel)
        );

        // Assemble Content
        contentBox.getChildren().addAll(formGrid, actionBox, resultsBox);

        // Wrap content in a ScrollPane just in case height is small, though 1024 is plenty
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        // Center the contentBox inside the scroll pane visually
        StackPane centerStack = new StackPane(contentBox);
        centerStack.setAlignment(Pos.TOP_CENTER);

        root.setCenter(centerStack);

        // --- Scene Setup ---
        Scene scene = new Scene(root, 1440, 1024);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Age Calculator");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates a styled DatePicker with custom icon and format.
     */
    private DatePicker createStyledDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(promptText);
        datePicker.getStyleClass().add("styled-date-picker");

        // Enforce MM/dd/yyyy format
        String pattern = "MM/dd/yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (DateTimeParseException e) {
                        return null; // Handle invalid format gracefully
                    }
                }
                return null;
            }
        });

        return datePicker;
    }

    /**
     * Creates a single result card (Years, Months, Days).
     */
    private VBox createResultCard(String title, Label valueLabel) {
        VBox card = new VBox(10);
        card.getStyleClass().add("result-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 120);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("result-title");

        valueLabel.getStyleClass().add("result-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Logic to calculate age.
     */
    private void calculateAge() {
        LocalDate dob = dobPicker.getValue();
        LocalDate current = currentDatePicker.getValue();

        // FR2.4: Validation
        if (dob == null || current == null) {
            showError("Please enter valid dates for both fields.");
            return;
        }

        // FR1.5: Validation - DOB later than current
        if (dob.isAfter(current)) {
            showError("Date of Birth cannot be later than Current Date.");
            return;
        }

        // Clear previous errors
        errorLabel.setVisible(false);

        // FR2.2: Calculate difference
        Period period = Period.between(dob, current);

        // FR3.1, 3.2, 3.3: Display results
        yearsValueLabel.setText(String.valueOf(period.getYears()));
        monthsValueLabel.setText(String.valueOf(period.getMonths()));
        daysValueLabel.setText(String.valueOf(period.getDays()));
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        // Clear results on error
        yearsValueLabel.setText("--");
        monthsValueLabel.setText("--");
        daysValueLabel.setText("--");
    }

    public static void main(String[] args) {
        launch(args);
    }
}