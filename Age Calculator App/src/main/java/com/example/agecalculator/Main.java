package com.example.agecalculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class Main extends Application {

    private static final String DATE_PATTERN = "MM/dd/yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private Label yearsValueLabel;
    private Label monthsValueLabel;
    private Label daysValueLabel;
    private Label messageLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        Label headerLabel = new Label("Age Calculator");
        headerLabel.getStyleClass().add("app-title");
        StackPane headerPane = new StackPane(headerLabel);
        headerPane.getStyleClass().add("header");
        headerPane.setPadding(new Insets(34, 16, 34, 16));
        root.setTop(headerPane);

        DatePicker dobPicker = createDatePicker();
        DatePicker currentPicker = createDatePicker();
        currentPicker.setValue(LocalDate.now());

        VBox formBox = new VBox(32,
                createInputRow("Date of Birth", dobPicker),
                createInputRow("Current Date", currentPicker));
        formBox.getStyleClass().add("form-section");

        Button calculateButton = new Button("Calculate");
        calculateButton.getStyleClass().add("action-button");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(520);

        yearsValueLabel = new Label("0");
        yearsValueLabel.getStyleClass().add("result-value");
        monthsValueLabel = new Label("0");
        monthsValueLabel.getStyleClass().add("result-value");
        daysValueLabel = new Label("0");
        daysValueLabel.getStyleClass().add("result-value");

        HBox resultsRow = new HBox(30,
                createResultBox("Years", yearsValueLabel),
                createResultBox("Months", monthsValueLabel),
                createResultBox("Days", daysValueLabel));
        resultsRow.setAlignment(Pos.CENTER);
        resultsRow.setPadding(new Insets(25, 0, 0, 0));

        calculateButton.setOnAction(event -> calculateAge(dobPicker, currentPicker));

        VBox centerBox = new VBox(40, formBox, calculateButton, messageLabel, resultsRow);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(100, 0, 0, 0));
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 1440, 1024);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Age Calculator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPromptText(DATE_PATTERN);
        picker.setEditable(true);
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

    private HBox createInputRow(String labelText, DatePicker picker) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        label.setPrefWidth(220);
        label.setAlignment(Pos.CENTER_LEFT);

        StackPane field = createDateField(picker);
        field.setMaxWidth(360);
        HBox.setHgrow(field, Priority.ALWAYS);

        HBox row = new HBox(30, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(700);
        return row;
    }

    private StackPane createDateField(DatePicker picker) {
        StackPane wrapper = new StackPane();
        wrapper.getStyleClass().add("date-field-wrapper");
        wrapper.setPrefWidth(320);
        wrapper.setMinWidth(260);
        wrapper.setMaxWidth(Double.MAX_VALUE);

        picker.setMaxWidth(Double.MAX_VALUE);
        picker.prefWidthProperty().bind(wrapper.widthProperty());

        SVGPath icon = createCalendarIcon();
        StackPane.setAlignment(icon, Pos.CENTER_RIGHT);
        StackPane.setMargin(icon, new Insets(0, 10, 0, 0));
        icon.setMouseTransparent(true);

        wrapper.getChildren().addAll(picker, icon);
        return wrapper;
    }

    private SVGPath createCalendarIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M30.25 1V10M12.25 1V10M1 19H41.5M5.5 5.5H37C39.4853 5.5 41.5 7.51472 41.5 10V41.5C41.5 43.9853 39.4853 46 37 46H5.5C3.01472 46 1 43.9853 1 41.5V10C1 7.51472 3.01472 5.5 5.5 5.5Z");
        icon.setStroke(Color.web("#1E1E1E"));
        icon.setFill(Color.TRANSPARENT);
        icon.setStrokeWidth(2.0);
        icon.setScaleX(0.45);
        icon.setScaleY(0.45);
        return icon;
    }

    private VBox createResultBox(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("result-title");
        VBox box = new VBox(8, titleLabel, valueLabel);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("result-box");
        return box;
    }

    private void calculateAge(DatePicker dobPicker, DatePicker currentPicker) {
        messageLabel.setText("");

        LocalDate dob = resolveDate(dobPicker, "Date of Birth");
        if (dob == null) {
            resetResults();
            return;
        }

        LocalDate current = resolveDate(currentPicker, "Current Date");
        if (current == null) {
            resetResults();
            return;
        }

        if (dob.isAfter(current)) {
            messageLabel.setText("Date of Birth must be earlier than Current Date.");
            resetResults();
            return;
        }

        long years = ChronoUnit.YEARS.between(dob, current);
        LocalDate afterYears = dob.plusYears(years);

        long months = ChronoUnit.MONTHS.between(afterYears, current);
        LocalDate afterMonths = afterYears.plusMonths(months);

        long days = ChronoUnit.DAYS.between(afterMonths, current);

        yearsValueLabel.setText(String.valueOf(years));
        monthsValueLabel.setText(String.valueOf(months));
        daysValueLabel.setText(String.valueOf(days));
    }

    private LocalDate resolveDate(DatePicker picker, String fieldName) {
        LocalDate value = picker.getValue();
        if (value != null) {
            return value;
        }

        String raw = picker.getEditor().getText();
        if (raw == null || raw.isBlank()) {
            messageLabel.setText(fieldName + " cannot be blank. Use MM/DD/YYYY.");
            return null;
        }

        try {
            LocalDate parsed = LocalDate.parse(raw, DATE_FORMATTER);
            picker.setValue(parsed);
            return parsed;
        } catch (DateTimeParseException ex) {
            messageLabel.setText(fieldName + " must follow MM/DD/YYYY.");
            return null;
        }
    }

    private void resetResults() {
        yearsValueLabel.setText("0");
        monthsValueLabel.setText("0");
        daysValueLabel.setText("0");
    }

    public static void main(String[] args) {
        launch(args);
    }
}