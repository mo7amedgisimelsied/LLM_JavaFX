package com.paintapp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private Canvas canvas;
    private GraphicsContext gc;

    // Tool state
    private ToolType currentTool = ToolType.RECTANGLE;

    // Color state
    private Color fillColor = Color.RED;
    private Color outlineColor = Color.BLACK;
    private ColorTarget selectedColorTarget = null;

    // Drawing properties
    private double opacity = 1.0;
    private double lineThickness = 0.76;

    // Color picker state
    private double currentHue = 0.0;
    private double currentSaturation = 1.0;
    private double currentBrightness = 1.0;

    // Drawing state
    private double startX, startY;
    private boolean isDrawing = false;

    // Undo/Redo
    private final List<WritableImage> undoStack = new ArrayList<>();
    private final List<WritableImage> redoStack = new ArrayList<>();
    private static final int MAX_UNDO = 50;

    // UI Components
    private Button undoButton;
    private Button redoButton;
    private Pane colorPickerGradient;
    private Pane hueSlider;
    private Slider opacitySlider;
    private Slider lineThicknessSlider;
    private Pane fillColorBox;
    private Pane outlineColorBox;

    // Tool buttons
    private final List<Button> toolButtons = new ArrayList<>();

    enum ToolType {
        PENCIL, RECTANGLE, CIRCLE, TRIANGLE, ERASER, TEXT
    }

    enum ColorTarget {
        FILL, OUTLINE
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        // Top bar
        root.setTop(createTopBar());

        // Left toolbar
        root.setLeft(createLeftToolbar());

        // Center canvas
        root.setCenter(createCanvas());

        // Right panel
        root.setRight(createRightPanel());

        Scene scene = new Scene(root, 1440, 1024);
        primaryStage.setTitle("Paint+");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Save initial state
        saveState();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");

        // Logo
        Label logo = new Label("Paint+");
        logo.setFont(Font.font("System", FontWeight.BOLD, 18));
        logo.setStyle("-fx-text-fill: #333333;");

        // Action buttons
        undoButton = createTextButton("Undo");
        undoButton.setOnAction(e -> undo());

        redoButton = createTextButton("Redo");
        redoButton.setOnAction(e -> redo());

        Button exportButton = createTextButton("Export");
        exportButton.setOnAction(e -> exportImage());

        topBar.getChildren().addAll(logo, undoButton, redoButton, exportButton);

        updateUndoRedoButtons();

        return topBar;
    }

    private Button createTextButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-size: 14px; " +
                "-fx-padding: 5 10 5 10; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; " +
                "-fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; " +
                "-fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-cursor: hand;"));
        return button;
    }

    private VBox createLeftToolbar() {
        VBox toolbar = new VBox(0);
        toolbar.setPrefWidth(54);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 0 1 0 0;");

        // Create tool buttons
        Button pencilBtn = createToolButton(ToolType.PENCIL, createPencilIcon());
        Button rectangleBtn = createToolButton(ToolType.RECTANGLE, createRectangleIcon());
        Button circleBtn = createToolButton(ToolType.CIRCLE, createCircleIcon());
        Button triangleBtn = createToolButton(ToolType.TRIANGLE, createTriangleIcon());
        Button eraserBtn = createToolButton(ToolType.ERASER, createEraserIcon());
        Button textBtn = createToolButton(ToolType.TEXT, createTextIcon());

        toolButtons.addAll(List.of(pencilBtn, rectangleBtn, circleBtn, triangleBtn, eraserBtn, textBtn));
        toolbar.getChildren().addAll(pencilBtn, rectangleBtn, circleBtn, triangleBtn, eraserBtn, textBtn);

        // Set initial active tool
        setActiveTool(ToolType.RECTANGLE);

        return toolbar;
    }

    private Button createToolButton(ToolType tool, SVGPath icon) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setPrefSize(54, 54);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;");
        button.setOnAction(e -> setActiveTool(tool));

        button.setOnMouseEntered(e -> {
            if (currentTool != tool) {
                button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: transparent; -fx-cursor: hand;");
            }
        });
        button.setOnMouseExited(e -> {
            if (currentTool != tool) {
                button.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        return button;
    }

    private void setActiveTool(ToolType tool) {
        currentTool = tool;

        // Update button styles
        for (Button btn : toolButtons) {
            btn.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;");
        }

        int index = switch (tool) {
            case PENCIL -> 0;
            case RECTANGLE -> 1;
            case CIRCLE -> 2;
            case TRIANGLE -> 3;
            case ERASER -> 4;
            case TEXT -> 5;
        };

        toolButtons.get(index).setStyle("-fx-background-color: #d0d0d0; -fx-border-color: transparent; -fx-cursor: hand;");
    }

    private StackPane createCanvas() {
        StackPane canvasContainer = new StackPane();
        canvasContainer.setStyle("-fx-background-color: #f9f9f9;");
        canvasContainer.setPadding(new Insets(20));

        canvas = new Canvas(900, 700);
        gc = canvas.getGraphicsContext2D();

        // White background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Add border
        Pane canvasBorder = new Pane(canvas);
        canvasBorder.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: white;");
        canvasBorder.setPrefSize(canvas.getWidth() + 2, canvas.getHeight() + 2);

        // Mouse events
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);

        canvasContainer.getChildren().add(canvasBorder);

        return canvasContainer;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(280);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 0 0 0 1;");

        // Color picker (2D gradient)
        VBox colorPickerBox = new VBox(5);
        colorPickerGradient = create2DColorPicker();
        hueSlider = createHueSlider();
        colorPickerBox.getChildren().addAll(colorPickerGradient, hueSlider);

        // Opacity slider
        VBox opacityBox = new VBox(5);
        Label opacityLabel = new Label("Opacity");
        opacityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

        HBox opacityControl = new HBox(10);
        opacityControl.setAlignment(Pos.CENTER_LEFT);
        opacitySlider = new Slider(0, 1, 1);
        opacitySlider.setPrefWidth(180);
        opacitySlider.valueProperty().addListener((obs, old, val) -> {
            opacity = val.doubleValue();
            opacityValueLabel.setText((int)(opacity * 100) + " %");
        });
        Label opacityValueLabel = new Label("100 %");
        opacityValueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        opacityControl.getChildren().addAll(opacitySlider, opacityValueLabel);
        opacityBox.getChildren().addAll(opacityLabel, opacityControl);

        // Fill color
        VBox fillBox = new VBox(5);
        Label fillLabel = new Label("Fill");
        fillLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        fillColorBox = new Pane();
        fillColorBox.setPrefSize(240, 30);
        fillColorBox.setStyle("-fx-background-color: #FF0000; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-cursor: hand;");
        fillColorBox.setOnMouseClicked(e -> selectColorTarget(ColorTarget.FILL));
        fillBox.getChildren().addAll(fillLabel, fillColorBox);

        // Outline color
        VBox outlineBox = new VBox(5);
        Label outlineLabel = new Label("Outline");
        outlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        outlineColorBox = new Pane();
        outlineColorBox.setPrefSize(240, 30);
        outlineColorBox.setStyle("-fx-background-color: #000000; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-cursor: hand;");
        outlineColorBox.setOnMouseClicked(e -> selectColorTarget(ColorTarget.OUTLINE));
        outlineBox.getChildren().addAll(outlineLabel, outlineColorBox);

        // Line thickness slider
        VBox thicknessBox = new VBox(5);
        Label thicknessLabel = new Label("Line Thickness");
        thicknessLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

        HBox thicknessControl = new HBox(10);
        thicknessControl.setAlignment(Pos.CENTER_LEFT);
        lineThicknessSlider = new Slider(0.01, 1, 0.76);
        lineThicknessSlider.setPrefWidth(180);
        lineThicknessSlider.valueProperty().addListener((obs, old, val) -> {
            lineThickness = val.doubleValue();
            thicknessValueLabel.setText((int)(lineThickness * 100) + " %");
        });
        Label thicknessValueLabel = new Label("76 %");
        thicknessValueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        thicknessControl.getChildren().addAll(lineThicknessSlider, thicknessValueLabel);
        thicknessBox.getChildren().addAll(thicknessLabel, thicknessControl);

        panel.getChildren().addAll(colorPickerBox, opacityBox, fillBox, outlineBox, thicknessBox);

        return panel;
    }

    private Pane create2DColorPicker() {
        Pane picker = new Pane();
        picker.setPrefSize(240, 240);
        picker.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        // Create gradient overlay
        Canvas gradientCanvas = new Canvas(240, 240);
        GraphicsContext gradientGc = gradientCanvas.getGraphicsContext2D();
        updateColorPickerGradient(gradientGc);

        picker.getChildren().add(gradientCanvas);

        picker.setOnMouseClicked(e -> handleColorPickerClick(e, gradientGc));
        picker.setOnMouseDragged(e -> handleColorPickerClick(e, gradientGc));

        return picker;
    }

    private void updateColorPickerGradient(GraphicsContext gradientGc) {
        double width = 240;
        double height = 240;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double saturation = x / width;
                double brightness = 1.0 - (y / height);
                Color color = Color.hsb(currentHue * 360, saturation, brightness);
                gradientGc.setFill(color);
                gradientGc.fillRect(x, y, 1, 1);
            }
        }
    }

    private void handleColorPickerClick(MouseEvent e, GraphicsContext gradientGc) {
        if (selectedColorTarget == null) return;

        double x = Math.max(0, Math.min(e.getX(), 240));
        double y = Math.max(0, Math.min(e.getY(), 240));

        currentSaturation = x / 240;
        currentBrightness = 1.0 - (y / 240);

        Color newColor = Color.hsb(currentHue * 360, currentSaturation, currentBrightness);

        if (selectedColorTarget == ColorTarget.FILL) {
            fillColor = newColor;
            fillColorBox.setStyle("-fx-background-color: " + toRgbString(fillColor) + "; -fx-border-color: #000000; -fx-border-width: 2; -fx-cursor: hand;");
        } else {
            outlineColor = newColor;
            outlineColorBox.setStyle("-fx-background-color: " + toRgbString(outlineColor) + "; -fx-border-color: #000000; -fx-border-width: 2; -fx-cursor: hand;");
        }
    }

    private Pane createHueSlider() {
        Pane sliderPane = new Pane();
        sliderPane.setPrefSize(240, 20);

        // Rainbow gradient
        Canvas rainbowCanvas = new Canvas(240, 20);
        GraphicsContext rainbowGc = rainbowCanvas.getGraphicsContext2D();

        for (int x = 0; x < 240; x++) {
            double hue = (x / 240.0) * 360;
            rainbowGc.setFill(Color.hsb(hue, 1.0, 1.0));
            rainbowGc.fillRect(x, 0, 1, 20);
        }

        sliderPane.getChildren().add(rainbowCanvas);
        sliderPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        sliderPane.setOnMouseClicked(e -> handleHueSliderClick(e));
        sliderPane.setOnMouseDragged(e -> handleHueSliderClick(e));

        return sliderPane;
    }

    private void handleHueSliderClick(MouseEvent e) {
        if (selectedColorTarget == null) return;

        double x = Math.max(0, Math.min(e.getX(), 240));
        currentHue = x / 240.0;

        // Update 2D color picker
        Canvas gradientCanvas = (Canvas) colorPickerGradient.getChildren().get(0);
        updateColorPickerGradient(gradientCanvas.getGraphicsContext2D());

        Color newColor = Color.hsb(currentHue * 360, currentSaturation, currentBrightness);

        if (selectedColorTarget == ColorTarget.FILL) {
            fillColor = newColor;
            fillColorBox.setStyle("-fx-background-color: " + toRgbString(fillColor) + "; -fx-border-color: #000000; -fx-border-width: 2; -fx-cursor: hand;");
        } else {
            outlineColor = newColor;
            outlineColorBox.setStyle("-fx-background-color: " + toRgbString(outlineColor) + "; -fx-border-color: #000000; -fx-border-width: 2; -fx-cursor: hand;");
        }
    }

    private void selectColorTarget(ColorTarget target) {
        selectedColorTarget = target;

        // Update visual feedback
        if (target == ColorTarget.FILL) {
            fillColorBox.setStyle("-fx-background-color: " + toRgbString(fillColor) + "; -fx-border-color: #000000; -fx-border-width: 2; -fx-cursor: hand;");
            outlineColorBox.setStyle("-fx-background-color: " + toRgbString(outlineColor) + "; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-cursor: hand;");

            // Update picker to show fill color
            Color c = fillColor;
            currentHue = c.getHue() / 360.0;
            currentSaturation = c.getSaturation();
            currentBrightness = c.getBrightness();
        } else {
            outlineColorBox.setStyle("-fx-background-color: " + toRgbString(outlineColor) + "; -fx-border-color: #000000; -fx-border-width: 2; -fx-cursor: hand;");
            fillColorBox.setStyle("-fx-background-color: " + toRgbString(fillColor) + "; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-cursor: hand;");

            // Update picker to show outline color
            Color c = outlineColor;
            currentHue = c.getHue() / 360.0;
            currentSaturation = c.getSaturation();
            currentBrightness = c.getBrightness();
        }

        // Update 2D color picker
        Canvas gradientCanvas = (Canvas) colorPickerGradient.getChildren().get(0);
        updateColorPickerGradient(gradientCanvas.getGraphicsContext2D());
    }

    private void handleMousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        isDrawing = true;

        if (currentTool == ToolType.PENCIL) {
            gc.setStroke(outlineColor.deriveColor(0, 1, 1, opacity));
            gc.setLineWidth(lineThickness * 30);
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.stroke();
        } else if (currentTool == ToolType.ERASER) {
            gc.clearRect(startX - 10, startY - 10, 20, 20);
        } else if (currentTool == ToolType.TEXT) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Text");
            dialog.setHeaderText("Enter text to add to canvas:");
            dialog.setContentText("Text:");

            dialog.showAndWait().ifPresent(text -> {
                gc.setFill(fillColor.deriveColor(0, 1, 1, opacity));
                gc.setFont(Font.font("System", FontWeight.NORMAL, 24));
                gc.fillText(text, startX, startY);
                saveState();
            });
            isDrawing = false;
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isDrawing) return;

        double currentX = e.getX();
        double currentY = e.getY();

        if (currentTool == ToolType.PENCIL) {
            gc.lineTo(currentX, currentY);
            gc.stroke();
        } else if (currentTool == ToolType.ERASER) {
            gc.clearRect(currentX - 10, currentY - 10, 20, 20);
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (!isDrawing) return;
        isDrawing = false;

        double endX = e.getX();
        double endY = e.getY();

        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);

        switch (currentTool) {
            case RECTANGLE:
                gc.setFill(fillColor.deriveColor(0, 1, 1, opacity));
                gc.setStroke(outlineColor.deriveColor(0, 1, 1, opacity));
                gc.setLineWidth(lineThickness * 30);
                gc.fillRect(x, y, width, height);
                gc.strokeRect(x, y, width, height);
                saveState();
                break;

            case CIRCLE:
                gc.setFill(fillColor.deriveColor(0, 1, 1, opacity));
                gc.setStroke(outlineColor.deriveColor(0, 1, 1, opacity));
                gc.setLineWidth(lineThickness * 30);
                gc.fillOval(x, y, width, height);
                gc.strokeOval(x, y, width, height);
                saveState();
                break;

            case TRIANGLE:
                double centerX = startX + (endX - startX) / 2;
                gc.setFill(fillColor.deriveColor(0, 1, 1, opacity));
                gc.setStroke(outlineColor.deriveColor(0, 1, 1, opacity));
                gc.setLineWidth(lineThickness * 30);
                gc.beginPath();
                gc.moveTo(centerX, y);
                gc.lineTo(x, endY);
                gc.lineTo(endX, endY);
                gc.closePath();
                gc.fill();
                gc.stroke();
                saveState();
                break;

            case PENCIL:
            case ERASER:
                saveState();
                break;
        }
    }

    private void saveState() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(new SnapshotParameters(), snapshot);

        undoStack.add(snapshot);
        if (undoStack.size() > MAX_UNDO) {
            undoStack.remove(0);
        }

        redoStack.clear();
        updateUndoRedoButtons();
    }

    private void undo() {
        if (undoStack.size() <= 1) return;

        WritableImage currentState = undoStack.remove(undoStack.size() - 1);
        redoStack.add(currentState);

        WritableImage previousState = undoStack.get(undoStack.size() - 1);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(previousState, 0, 0);

        updateUndoRedoButtons();
    }

    private void redo() {
        if (redoStack.isEmpty()) return;

        WritableImage nextState = redoStack.remove(redoStack.size() - 1);
        undoStack.add(nextState);

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(nextState, 0, 0);

        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    private void exportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Drawing");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );
        fileChooser.setInitialFileName("drawing.png");

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(new SnapshotParameters(), snapshot);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                ImageIO.write(bufferedImage, "png", file);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Drawing exported successfully!");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to export drawing: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private String toRgbString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private SVGPath createPencilIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M13.6286 4.37035L19.6294 10.3714L6.59893 23.4024L1.24876 23.993C0.532526 24.0722 -0.072614 23.4666 0.0070714 22.7503L0.602368 17.3962L13.6286 4.37035ZM23.3408 3.47689L20.5233 0.659191C19.6444 -0.21973 18.2189 -0.21973 17.3401 0.659191L14.6893 3.31002L20.6901 9.31105L23.3408 6.66023C24.2197 5.78084 24.2197 4.35581 23.3408 3.47689Z");
        path.setFill(Color.BLACK);
        path.setScaleX(0.8);
        path.setScaleY(0.8);
        return path;
    }

    private SVGPath createRectangleIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M512 112C520.8 112 528 119.2 528 128L528 512C528 520.8 520.8 528 512 528L128 528C119.2 528 112 520.8 112 512L112 128C112 119.2 119.2 112 128 112L512 112zM128 64C92.7 64 64 92.7 64 128L64 512C64 547.3 92.7 576 128 576L512 576C547.3 576 576 547.3 576 512L576 128C576 92.7 547.3 64 512 64L128 64z");
        path.setFill(Color.BLACK);
        path.setScaleX(0.035);
        path.setScaleY(0.035);
        return path;
    }

    private SVGPath createCircleIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M528 320C528 205.1 434.9 112 320 112C205.1 112 112 205.1 112 320C112 434.9 205.1 528 320 528C434.9 528 528 434.9 528 320zM64 320C64 178.6 178.6 64 320 64C461.4 64 576 178.6 576 320C576 461.4 461.4 576 320 576C178.6 576 64 461.4 64 320z");
        path.setFill(Color.BLACK);
        path.setScaleX(0.035);
        path.setScaleY(0.035);
        return path;
    }

    private SVGPath createTriangleIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M7.938 2.016A.13.13 0 0 1 8.002 2a.13.13 0 0 1 .063.016.15.15 0 0 1 .054.057l6.857 11.667c.036.06.035.124.002.183a.2.2 0 0 1-.054.06.1.1 0 0 1-.066.017H1.146a.1.1 0 0 1-.066-.017.2.2 0 0 1-.054-.06.18.18 0 0 1 .002-.183L7.884 2.073a.15.15 0 0 1 .054-.057m1.044-.45a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767z");
        path.setFill(Color.BLACK);
        path.setScaleX(1.5);
        path.setScaleY(1.5);
        return path;
    }

    private SVGPath createEraserIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M23.341 11.341C24.2197 10.4623 24.2197 9.03769 23.341 8.15902L15.841 0.659021C14.9623 -0.21965 13.5378 -0.219697 12.659 0.659021L0.659003 12.659C-0.219668 13.5377 -0.219668 14.9623 0.659003 15.841L5.159 20.341C5.58097 20.7629 6.15328 21 6.75003 21H23.4375C23.7481 21 24 20.7481 24 20.4375V18.5625C24 18.2519 23.7481 18 23.4375 18H16.682L23.341 11.341ZM9.15537 8.40535L15.5947 14.8447L12.4394 18H7.06071L3.31072 14.25L9.15537 8.40535Z");
        path.setFill(Color.BLACK);
        path.setScaleX(0.8);
        path.setScaleY(0.8);
        return path;
    }

    private SVGPath createTextIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12.258 3h-8.51l-.083 2.46h.479c.26-1.544.758-1.783 2.693-1.845l.424-.013v7.827c0 .663-.144.82-1.3.923v.52h4.082v-.52c-1.162-.103-1.306-.26-1.306-.923V3.602l.431.013c1.934.062 2.434.301 2.693 1.846h.479z");
        path.setFill(Color.BLACK);
        path.setScaleX(1.5);
        path.setScaleY(1.5);
        return path;
    }

    public static void main(String[] args) {
        launch(args);
    }
}