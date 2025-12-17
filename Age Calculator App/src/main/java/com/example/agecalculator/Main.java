package com.example.agecalculator;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class Main extends Application {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private DatePicker dobPicker;
    private DatePicker currentPicker;
    private ResultCard yearsCard;
    private ResultCard monthsCard;
    private ResultCard daysCard;
    private Label messageLabel;

    @Override
    public void start(Stage stage) {
        dobPicker = buildDatePicker();
        currentPicker = buildDatePicker();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        StackPane headerPane = new StackPane();
        headerPane.getStyleClass().add("header-pane");
        Label headerLabel = new Label("Age Calculator");
        headerLabel.getStyleClass().add("header-label");
        headerPane.getChildren().add(headerLabel);
        root.setTop(headerPane);

        VBox mainContent = new VBox();
        mainContent.getStyleClass().add("main-content");
        mainContent.setSpacing(40);
        mainContent.setAlignment(Pos.TOP_CENTER);

        VBox formBox = new VBox();
        formBox.getStyleClass().add("form-box");
        formBox.setSpacing(30);
        formBox.setAlignment(Pos.CENTER);

        formBox.getChildren().addAll(
                createDateRow("Date of Birth", dobPicker),
                createDateRow("Current Date", currentPicker)
        );

        Button calculateButton = new Button("Calculate");
        calculateButton.getStyleClass().add("calculate-button");
        calculateButton.setOnAction(evt -> calculateAge());

        messageLabel = new Label();
        messageLabel.getStyleClass().add("error-label");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(600);

        yearsCard = new ResultCard("Years");
        monthsCard = new ResultCard("Months");
        daysCard = new ResultCard("Days");

        HBox resultsRow = new HBox(yearsCard, monthsCard, daysCard);
        resultsRow.getStyleClass().add("results-row");
        resultsRow.setSpacing(30);
        resultsRow.setAlignment(Pos.CENTER);

        mainContent.getChildren().addAll(formBox, calculateButton, messageLabel, resultsRow);
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(40, 0, 40, 0));

        Scene scene = new Scene(root, 1440, 1024);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/agecalculator/styles.css").toExternalForm()
        );

        stage.setTitle("Age Calculator");
        stage.setScene(scene);
        stage.show();
    }

    private DatePicker buildDatePicker() {
        DatePicker picker = new DatePicker();
        picker.getStyleClass().add("modern-date-picker");
        picker.setPromptText("MM/DD/YYYY");
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE_FORMATTER.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text, DATE_FORMATTER);
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        });
        return picker;
    }

    private HBox createDateRow(String labelText, DatePicker picker) {
        Label label = new Label(labelText);
        label.getStyleClass().add("input-label");
        label.setMinWidth(200);

        StackPane inputContainer = wrapWithIcon(picker);
        HBox.setHgrow(inputContainer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(30, label, inputContainer);
        row.getStyleClass().add("date-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private StackPane wrapWithIcon(DatePicker datePicker) {
        StackPane container = new StackPane();
        container.getStyleClass().add("date-input-container");
        container.setMinWidth(360);

        datePicker.setPrefWidth(360);

        SVGPath icon = createCalendarIcon();
        StackPane.setAlignment(icon, Pos.CENTER_RIGHT);
        StackPane.setMargin(icon, new Insets(0, 8, 0, 0));

        container.getChildren().addAll(datePicker, icon);
        return container;
    }

    private SVGPath createCalendarIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M30.25 1V10M12.25 1V10M1 19H41.5M5.5 5.5H37C39.4853 5.5 41.5 7.51472 41.5 10V41.5C41.5 43.9853 39.4853 46 37 46H5.5C3.01472 46 1 43.9853 1 41.5V10C1 7.51472 3.01472 5.5 5.5 5.5Z");
        icon.setStroke(Color.web("#1E1E1E"));
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.45);
        icon.setScaleY(0.45);
        icon.setMouseTransparent(true);
        return icon;
    }

    private void calculateAge() {
        LocalDate dob = parseDatePickerValue(dobPicker);
        if (dob == null) {
            if (isEditorBlank(dobPicker)) {
                showError("Please enter your Date of Birth (MM/DD/YYYY).");
            } else {
                showError("Date of Birth format is invalid. Use MM/DD/YYYY.");
            }
            resetResults();
            return;
        }

        LocalDate current = parseDatePickerValue(currentPicker);
        if (current == null) {
            if (isEditorBlank(currentPicker)) {
                showError("Please enter the Current Date (MM/DD/YYYY).");
            } else {
                showError("Current Date format is invalid. Use MM/DD/YYYY.");
            }
            resetResults();
            return;
        }

        if (dob.isAfter(current)) {
            showError("Date of Birth cannot be later than the Current Date.");
            resetResults();
            return;
        }

        Period period = Period.between(dob, current);
        yearsCard.setValue(period.getYears());
        monthsCard.setValue(period.getMonths());
        daysCard.setValue(period.getDays());
        messageLabel.setText("");
    }

    private LocalDate parseDatePickerValue(DatePicker picker) {
        LocalDate value = picker.getValue();
        if (value != null) {
            return value;
        }
        String text = picker.getEditor().getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            LocalDate parsed = LocalDate.parse(text, DATE_FORMATTER);
            picker.setValue(parsed);
            return parsed;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean isEditorBlank(DatePicker picker) {
        String text = picker.getEditor().getText();
        return text == null || text.isBlank();
    }

    private void showError(String message) {
        messageLabel.setText(message);
    }

    private void resetResults() {
        yearsCard.setValue(0);
        monthsCard.setValue(0);
        daysCard.setValue(0);
    }

    private static class ResultCard extends VBox {
        private final Label valueLabel;

        ResultCard(String labelText) {
            getStyleClass().add("result-card");
            setAlignment(Pos.CENTER);
            setSpacing(8);
            setPadding(new Insets(18));

            Label label = new Label(labelText);
            label.getStyleClass().add("result-label");

            valueLabel = new Label("0");
            valueLabel.getStyleClass().add("result-value");

            getChildren().addAll(label, valueLabel);
        }

        void setValue(int value) {
            valueLabel.setText(String.valueOf(value));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}