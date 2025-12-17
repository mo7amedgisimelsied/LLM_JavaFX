package com.example.drawingapp;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Main extends Application {

    private enum Tool {PENCIL, RECTANGLE, CIRCLE, TRIANGLE, ERASER, TEXT}

    private enum ColorTarget {NONE, FILL, OUTLINE}

    private static final int GRADIENT_SIZE = 220;
    private static final int HUE_WIDTH = 220;
    private static final int HUE_HEIGHT = 24;

    private static final String SVG_PENCIL = "M13.6286 4.37035L19.6294 10.3714L6.59893 23.4024L1.24876 23.993C0.532526 24.0722 -0.072614 23.4666 0.0070714 22.7503L0.602368 17.3962L13.6286 4.37035ZM23.3408 3.47689L20.5233 0.659191C19.6444 -0.21973 18.2189 -0.21973 17.3401 0.659191L14.6893 3.31002L20.6901 9.31105L23.3408 6.66023C24.2197 5.78084 24.2197 4.35581 23.3408 3.47689Z";
    private static final String SVG_RECTANGLE = "M512 112C520.8 112 528 119.2 528 128L528 512C528 520.8 520.8 528 512 528L128 528C119.2 528 112 520.8 112 512L112 128C112 119.2 119.2 112 128 112L512 112zM128 64C92.7 64 64 92.7 64 128L64 512C64 547.3 92.7 576 128 576L512 576C547.3 576 576 547.3 576 512L576 128C576 92.7 547.3 64 512 64L128 64z";
    private static final String SVG_CIRCLE = "M528 320C528 205.1 434.9 112 320 112C205.1 112 112 205.1 112 320C112 434.9 205.1 528 320 528C434.9 528 528 434.9 528 320zM64 320C64 178.6 178.6 64 320 64C461.4 64 576 178.6 576 320C576 461.4 461.4 576 320 576C178.6 576 64 461.4 64 320z";
    private static final String SVG_TRIANGLE = "M7.938 2.016A.13.13 0 0 1 8.002 2a.13.13 0 0 1 .063.016.15.15 0 0 1 .054.057l6.857 11.667c.036.06.035.124.002.183a.2.2 0 0 1-.054.06.1.1 0 0 1-.066.017H1.146a.1.1 0 0 1-.066-.017.2.2 0 0 1-.054-.06.18.18 0 0 1 .002-.183L7.884 2.073a.15.15 0 0 1 .054-.057m1.044-.45a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767z";
    private static final String SVG_ERASER = "M23.341 11.341C24.2197 10.4623 24.2197 9.03769 23.341 8.15902L15.841 0.659021C14.9623 -0.21965 13.5378 -0.219697 12.659 0.659021L0.659003 12.659C-0.219668 13.5377 -0.219668 14.9623 0.659003 15.841L5.159 20.341C5.58097 20.7629 6.15328 21 6.75003 21H23.4375C23.7481 21 24 20.7481 24 20.4375V18.5625C24 18.2519 23.7481 18 23.4375 18H16.682L23.341 11.341ZM9.15537 8.40535L15.5947 14.8447L12.4394 18H7.06071L3.31072 14.25L9.15537 8.40535Z";
    private static final String SVG_TEXT = "M12.258 3h-8.51l-.083 2.46h.479c.26-1.544.758-1.783 2.693-1.845l.424-.013v7.827c0 .663-.144.82-1.3.923v.52h4.082v-.52c-1.162-.103-1.306-.26-1.306-.923V3.602l.431.013c1.934.062 2.434.301 2.693 1.846h.479z";

    private final ToggleGroup toolGroup = new ToggleGroup();
    private final Deque<Node> undoStack = new ArrayDeque<>();
    private final Deque<Node> redoStack = new ArrayDeque<>();

    private Map<ColorTarget, ColorState> colorStates;

    private Pane drawingPane;
    private Stage primaryStage;

    private Tool activeTool = Tool.RECTANGLE;

    private Path currentPath;
    private Rectangle currentRectangle;
    private Ellipse currentEllipse;
    private Polygon currentTriangle;

    private double startX;
    private double startY;

    private TextField activeTextField;

    private Slider opacitySlider;
    private Slider lineThicknessSlider;

    private Button undoButton;
    private Button redoButton;

    private Canvas gradientCanvas;
    private Canvas hueCanvas;
    private Circle gradientSelector;
    private Circle hueSelector;

    private Region fillPreview;
    private Region outlinePreview;

    private ColorTarget activeColorTarget = ColorTarget.NONE;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        initColorStates();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createTopBar());

        VBox toolbar = createToolBar();
        StackPane canvasArea = createCanvasArea();
        VBox propertiesPanel = createPropertiesPanel();
        canvasArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        HBox mainContent = new HBox();
        mainContent.getStyleClass().add("main-content");
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.setSpacing(0);
        mainContent.setFillHeight(true);
        mainContent.getChildren().addAll(toolbar, canvasArea, propertiesPanel);
        HBox.setHgrow(canvasArea, Priority.ALWAYS);

        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1440, 1024);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                undo();
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                redo();
                event.consume();
            }
        });

        stage.setTitle("2D Drawing App");
        stage.setScene(scene);
        stage.show();

        updateUndoRedoButtons();
    }

    private void initColorStates() {
        colorStates = new EnumMap<>(ColorTarget.class);
        colorStates.put(ColorTarget.FILL, new ColorState(Color.RED));
        colorStates.put(ColorTarget.OUTLINE, new ColorState(Color.BLACK));
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(24);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        Label brand = new Label("Paint+");
        brand.getStyleClass().add("brand-label");

        undoButton = new Button("Undo");
        undoButton.setId("undoButton");
        redoButton = new Button("Redo");
        redoButton.setId("redoButton");
        Button exportButton = new Button("Export");
        exportButton.setId("exportButton");

        Stream.of(undoButton, redoButton, exportButton).forEach(btn -> {
            btn.getStyleClass().add("top-action");
            btn.setFocusTraversable(false);
        });

        undoButton.setOnAction(e -> undo());
        redoButton.setOnAction(e -> redo());
        exportButton.setOnAction(e -> exportSnapshot());

        topBar.getChildren().addAll(brand, undoButton, redoButton, exportButton);
        return topBar;
    }

    private VBox createToolBar() {
        VBox toolbar = new VBox(12);
        toolbar.getStyleClass().add("tool-bar");
        toolbar.setAlignment(Pos.TOP_CENTER);
        toolbar.setPadding(new Insets(24, 12, 24, 12));
        toolbar.setPrefWidth(96);
        toolbar.setMinWidth(96);
        toolbar.setMaxWidth(96);
        toolbar.setFillWidth(false);

        ToggleButton pencil = createToolButton(SVG_PENCIL, Tool.PENCIL);
        ToggleButton rectangle = createToolButton(SVG_RECTANGLE, Tool.RECTANGLE);
        ToggleButton circle = createToolButton(SVG_CIRCLE, Tool.CIRCLE);
        ToggleButton triangle = createToolButton(SVG_TRIANGLE, Tool.TRIANGLE);
        ToggleButton eraser = createToolButton(SVG_ERASER, Tool.ERASER);
        ToggleButton text = createToolButton(SVG_TEXT, Tool.TEXT);

        rectangle.setSelected(true);

        toolGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
                return;
            }
            if (newToggle != null) {
                activeTool = (Tool) newToggle.getUserData();
            }
            finalizeTextField();
        });

        toolbar.getChildren().addAll(pencil, rectangle, circle, triangle, eraser, text);
        return toolbar;
    }

    private ToggleButton createToolButton(String svgPath, Tool tool) {
        ToggleButton button = new ToggleButton();
        button.setGraphic(buildSvgIcon(svgPath));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.getStyleClass().add("tool-button");
        button.setToggleGroup(toolGroup);
        button.setUserData(tool);
        button.setFocusTraversable(false);
        button.setMnemonicParsing(false);
        button.setPrefSize(56, 56);
        button.setMinSize(56, 56);
        String label = toolLabel(tool);
        button.setTooltip(new Tooltip(label));
        button.setAccessibleText(label);
        button.setId(label.toLowerCase(Locale.ROOT) + "Tool");
        return button;
    }

    private Node buildSvgIcon(String pathContent) {
        SVGPath path = new SVGPath();
        path.setContent(pathContent);
        path.setFill(Color.BLACK);
        path.setStroke(Color.BLACK);

        Bounds bounds = path.getBoundsInLocal();
        double max = Math.max(bounds.getWidth(), bounds.getHeight());
        double scale = max == 0 ? 1 : 18d / max;

        path.getTransforms().add(new Translate(-bounds.getMinX(), -bounds.getMinY()));
        path.getTransforms().add(new Scale(scale, scale));

        StackPane wrapper = new StackPane(path);
        wrapper.setPrefSize(24, 24);
        wrapper.setMinSize(24, 24);
        wrapper.setMaxSize(24, 24);
        return wrapper;
    }

    private String toolLabel(Tool tool) {
        return switch (tool) {
            case PENCIL -> "Pencil";
            case RECTANGLE -> "Rectangle";
            case CIRCLE -> "Circle";
            case TRIANGLE -> "Triangle";
            case ERASER -> "Eraser";
            case TEXT -> "Text";
        };
    }

    private StackPane createCanvasArea() {
        drawingPane = new Pane();
        drawingPane.getStyleClass().add("drawing-pane");
        drawingPane.setPrefSize(900, 720);
        drawingPane.setMinSize(320, 320);
        drawingPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        drawingPane.setPickOnBounds(true);

        configureDrawingHandlers();

        StackPane canvasWrapper = new StackPane(drawingPane);
        canvasWrapper.getStyleClass().add("canvas-wrapper");
        canvasWrapper.setPadding(new Insets(32));
        canvasWrapper.setMinSize(360, 360);
        canvasWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Insets padding = canvasWrapper.getPadding();
        final double horizontalPadding = padding.getLeft() + padding.getRight();
        final double verticalPadding = padding.getTop() + padding.getBottom();

        canvasWrapper.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = Math.max(320, newVal.doubleValue() - horizontalPadding);
            drawingPane.setPrefWidth(width);
        });

        canvasWrapper.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = Math.max(320, newVal.doubleValue() - verticalPadding);
            drawingPane.setPrefHeight(height);
        });

        return canvasWrapper;
    }

    private VBox createPropertiesPanel() {
        gradientCanvas = new Canvas(GRADIENT_SIZE, GRADIENT_SIZE);
        gradientCanvas.setOnMousePressed(e -> handleGradientSelection(e.getX(), e.getY()));
        gradientCanvas.setOnMouseDragged(e -> handleGradientSelection(e.getX(), e.getY()));

        gradientSelector = new Circle(7);
        gradientSelector.setStroke(Color.WHITE);
        gradientSelector.setStrokeWidth(2);
        gradientSelector.setFill(Color.TRANSPARENT);
        gradientSelector.setMouseTransparent(true);
        gradientSelector.setVisible(false);

        StackPane gradientStack = new StackPane(gradientCanvas, gradientSelector);
        gradientStack.getStyleClass().add("gradient-stack");
        gradientStack.setAlignment(Pos.CENTER);

        hueCanvas = new Canvas(HUE_WIDTH, HUE_HEIGHT);
        renderHueCanvas();
        hueCanvas.setOnMousePressed(e -> handleHueSelection(e.getX()));
        hueCanvas.setOnMouseDragged(e -> handleHueSelection(e.getX()));

        hueSelector = new Circle(6);
        hueSelector.setStroke(Color.WHITE);
        hueSelector.setStrokeWidth(2);
        hueSelector.setFill(Color.TRANSPARENT);
        hueSelector.setMouseTransparent(true);
        hueSelector.setVisible(false);

        StackPane hueStack = new StackPane(hueCanvas, hueSelector);
        hueStack.getStyleClass().add("hue-stack");
        hueStack.setAlignment(Pos.CENTER_LEFT);

        opacitySlider = new Slider(0, 100, 100);
        opacitySlider.getStyleClass().add("property-slider");
        opacitySlider.setMaxWidth(Double.MAX_VALUE);
        Label opacityLabel = new Label("Opacity");
        Label opacityValue = new Label("100 %");
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                opacityValue.setText(String.format("%d %%", Math.round(newVal.doubleValue()))));
        HBox opacityHeader = createHeader(opacityLabel, opacityValue);

        lineThicknessSlider = new Slider(1, 100, 76);
        lineThicknessSlider.getStyleClass().add("property-slider");
        lineThicknessSlider.setMaxWidth(Double.MAX_VALUE);
        Label thicknessLabel = new Label("Line Thickness");
        Label thicknessValue = new Label("76 %");
        lineThicknessSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                thicknessValue.setText(String.format("%d %%", Math.round(newVal.doubleValue()))));
        HBox thicknessHeader = createHeader(thicknessLabel, thicknessValue);

        fillPreview = new Region();
        outlinePreview = new Region();

        HBox fillRow = createColorRow("Fill", fillPreview, ColorTarget.FILL);
        HBox outlineRow = createColorRow("Outline", outlinePreview, ColorTarget.OUTLINE);

        updateSwatch(ColorTarget.FILL);
        updateSwatch(ColorTarget.OUTLINE);
        updatePreviewHighlights();
        updateGradientCanvas(colorStates.get(ColorTarget.FILL).hue);

        VBox panel = new VBox(24);
        panel.getStyleClass().add("properties-panel");
        panel.getChildren().addAll(gradientStack, hueStack,
                opacityHeader, opacitySlider,
                fillRow, outlineRow,
                thicknessHeader, lineThicknessSlider);
        panel.setPrefWidth(320);
        panel.setMinWidth(320);
        panel.setMaxWidth(320);
        panel.setFillWidth(true);
        return panel;
    }

    private HBox createHeader(Label label, Label valueLabel) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(label, spacer, valueLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createColorRow(String title, Region preview, ColorTarget target) {
        preview.getStyleClass().add("color-preview");
        preview.setPrefSize(96, 34);
        preview.setCursor(Cursor.HAND);
        preview.setOnMouseClicked(e -> setActiveColorTarget(target));

        Label label = new Label(title);
        HBox row = new HBox(16, label, preview);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void configureDrawingHandlers() {
        drawingPane.setOnMousePressed(this::handleMousePressed);
        drawingPane.setOnMouseDragged(this::handleMouseDragged);
        drawingPane.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        if (activeTool == Tool.TEXT) {
            if (isInsideActiveTextField(event)) {
                return;
            }
            finalizeTextField();
            placeTextField(event.getX(), event.getY());
            return;
        }

        finalizeTextField();
        startX = event.getX();
        startY = event.getY();

        switch (activeTool) {
            case PENCIL -> startPath(false, startX, startY);
            case ERASER -> startPath(true, startX, startY);
            case RECTANGLE -> startRectangle(startX, startY);
            case CIRCLE -> startEllipse(startX, startY);
            case TRIANGLE -> startTriangle(startX, startY);
            default -> {
            }
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        double x = event.getX();
        double y = event.getY();

        switch (activeTool) {
            case PENCIL, ERASER -> continuePath(x, y);
            case RECTANGLE -> updateRectangle(x, y);
            case CIRCLE -> updateEllipse(x, y);
            case TRIANGLE -> updateTriangle(x, y);
            default -> {
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        switch (activeTool) {
            case PENCIL, ERASER -> finalizePath();
            case RECTANGLE -> finalizeRectangle();
            case CIRCLE -> finalizeEllipse();
            case TRIANGLE -> finalizeTriangle();
            default -> {
            }
        }
    }

    private boolean isInsideActiveTextField(MouseEvent event) {
        if (activeTextField == null) {
            return false;
        }
        Node node = event.getPickResult().getIntersectedNode();
        while (node != null) {
            if (node == activeTextField) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private void startPath(boolean eraser, double x, double y) {
        currentPath = new Path();
        currentPath.setManaged(false);
        currentPath.setMouseTransparent(true);
        currentPath.setStroke(eraser ? Color.WHITE : getOutlinePaint());
        currentPath.setStrokeWidth(getStrokeWidth());
        currentPath.setStrokeLineCap(StrokeLineCap.ROUND);
        currentPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        currentPath.setFill(Color.TRANSPARENT);
        currentPath.getElements().add(new MoveTo(x, y));
        drawingPane.getChildren().add(currentPath);
    }

    private void continuePath(double x, double y) {
        if (currentPath != null) {
            currentPath.getElements().add(new LineTo(x, y));
        }
    }

    private void finalizePath() {
        if (currentPath == null) {
            return;
        }
        if (currentPath.getElements().size() < 2) {
            drawingPane.getChildren().remove(currentPath);
        } else {
            pushAction(currentPath);
        }
        currentPath = null;
    }

    private void startRectangle(double x, double y) {
        currentRectangle = new Rectangle();
        currentRectangle.setManaged(false);
        currentRectangle.setMouseTransparent(true);
        currentRectangle.setX(x);
        currentRectangle.setY(y);
        currentRectangle.setFill(getFillPaint());
        currentRectangle.setStroke(getOutlinePaint());
        currentRectangle.setStrokeWidth(getStrokeWidth());
        drawingPane.getChildren().add(currentRectangle);
    }

    private void updateRectangle(double x, double y) {
        if (currentRectangle == null) {
            return;
        }
        double minX = Math.min(x, startX);
        double minY = Math.min(y, startY);
        double width = Math.abs(x - startX);
        double height = Math.abs(y - startY);
        currentRectangle.setX(minX);
        currentRectangle.setY(minY);
        currentRectangle.setWidth(width);
        currentRectangle.setHeight(height);
    }

    private void finalizeRectangle() {
        if (currentRectangle == null) {
            return;
        }
        if (currentRectangle.getWidth() < 1 || currentRectangle.getHeight() < 1) {
            drawingPane.getChildren().remove(currentRectangle);
        } else {
            pushAction(currentRectangle);
        }
        currentRectangle = null;
    }

    private void startEllipse(double x, double y) {
        currentEllipse = new Ellipse();
        currentEllipse.setManaged(false);
        currentEllipse.setMouseTransparent(true);
        currentEllipse.setCenterX(x);
        currentEllipse.setCenterY(y);
        currentEllipse.setFill(getFillPaint());
        currentEllipse.setStroke(getOutlinePaint());
        currentEllipse.setStrokeWidth(getStrokeWidth());
        drawingPane.getChildren().add(currentEllipse);
    }

    private void updateEllipse(double x, double y) {
        if (currentEllipse == null) {
            return;
        }
        double centerX = (startX + x) / 2;
        double centerY = (startY + y) / 2;
        currentEllipse.setCenterX(centerX);
        currentEllipse.setCenterY(centerY);
        currentEllipse.setRadiusX(Math.abs(x - startX) / 2);
        currentEllipse.setRadiusY(Math.abs(y - startY) / 2);
    }

    private void finalizeEllipse() {
        if (currentEllipse == null) {
            return;
        }
        if (currentEllipse.getRadiusX() < 0.5 || currentEllipse.getRadiusY() < 0.5) {
            drawingPane.getChildren().remove(currentEllipse);
        } else {
            pushAction(currentEllipse);
        }
        currentEllipse = null;
    }

    private void startTriangle(double x, double y) {
        currentTriangle = new Polygon();
        currentTriangle.setManaged(false);
        currentTriangle.setMouseTransparent(true);
        currentTriangle.setFill(getFillPaint());
        currentTriangle.setStroke(getOutlinePaint());
        currentTriangle.setStrokeWidth(getStrokeWidth());
        drawingPane.getChildren().add(currentTriangle);
    }

    private void updateTriangle(double x, double y) {
        if (currentTriangle == null) {
            return;
        }
        double minX = Math.min(startX, x);
        double maxX = Math.max(startX, x);
        double minY = Math.min(startY, y);
        double maxY = Math.max(startY, y);
        double midX = (minX + maxX) / 2;
        ObservableList<Double> points = currentTriangle.getPoints();
        points.setAll(
                midX, minY,
                minX, maxY,
                maxX, maxY
        );
    }

    private void finalizeTriangle() {
        if (currentTriangle == null) {
            return;
        }
        ObservableList<Double> pts = currentTriangle.getPoints();
        if (pts.size() >= 6) {
            double width = Math.abs(pts.get(4) - pts.get(2));
            double height = Math.abs(pts.get(5) - pts.get(1));
            if (width < 1 || height < 1) {
                drawingPane.getChildren().remove(currentTriangle);
                currentTriangle = null;
                return;
            }
        }
        pushAction(currentTriangle);
        currentTriangle = null;
    }

    private void placeTextField(double x, double y) {
        activeTextField = new TextField();
        activeTextField.getStyleClass().add("canvas-text-field");
        activeTextField.setPrefColumnCount(12);
        activeTextField.relocate(x, y);
        drawingPane.getChildren().add(activeTextField);
        activeTextField.requestFocus();

        activeTextField.setOnAction(e -> finalizeTextField());
        activeTextField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                finalizeTextField();
            }
        });
        activeTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelTextField();
            }
        });
    }

    private void finalizeTextField() {
        if (activeTextField == null) {
            return;
        }
        String value = activeTextField.getText();
        double x = activeTextField.getLayoutX();
        double y = activeTextField.getLayoutY();
        drawingPane.getChildren().remove(activeTextField);
        if (value != null && !value.trim().isEmpty()) {
            Text text = new Text(value.trim());
            text.setFont(Font.font("Arial", 18));
            text.setX(x);
            text.setY(y + 18);
            text.setFill(getFillPaint());
            text.setMouseTransparent(true);
            drawingPane.getChildren().add(text);
            pushAction(text);
        }
        activeTextField = null;
    }

    private void cancelTextField() {
        if (activeTextField != null) {
            drawingPane.getChildren().remove(activeTextField);
            activeTextField = null;
        }
    }

    private void pushAction(Node node) {
        undoStack.push(node);
        redoStack.clear();
        updateUndoRedoButtons();
    }

    private void undo() {
        finalizeTextField();
        if (undoStack.isEmpty()) {
            return;
        }
        Node node = undoStack.pop();
        drawingPane.getChildren().remove(node);
        redoStack.push(node);
        updateUndoRedoButtons();
    }

    private void redo() {
        finalizeTextField();
        if (redoStack.isEmpty()) {
            return;
        }
        Node node = redoStack.pop();
        drawingPane.getChildren().add(node);
        undoStack.push(node);
        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        if (undoButton != null) {
            undoButton.setDisable(undoStack.isEmpty());
        }
        if (redoButton != null) {
            redoButton.setDisable(redoStack.isEmpty());
        }
    }

    private void exportSnapshot() {
        finalizeTextField();
        WritableImage snapshot = drawingPane.snapshot(new SnapshotParameters(), null);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Drawing");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        chooser.setInitialFileName("painting.png");
        File selected = chooser.showSaveDialog(primaryStage);
        if (selected == null) {
            return;
        }
        if (!selected.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".png");
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", selected);
            Alert success = new Alert(Alert.AlertType.INFORMATION,
                    "Image saved to:\n" + selected.getAbsolutePath(), ButtonType.OK);
            success.setHeaderText("Export Successful");
            success.initOwner(primaryStage);
            success.showAndWait();
        } catch (IOException e) {
            showError("Unable to export image:\n" + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private Color getOutlinePaint() {
        Color base = colorStates.get(ColorTarget.OUTLINE).toColor();
        double alpha = getOpacityValue();
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private Color getFillPaint() {
        Color base = colorStates.get(ColorTarget.FILL).toColor();
        double alpha = getOpacityValue();
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private double getOpacityValue() {
        return opacitySlider == null ? 1.0 : opacitySlider.getValue() / 100.0;
    }

    private double getStrokeWidth() {
        return lineThicknessSlider == null ? 5 : 1 + (lineThicknessSlider.getValue() / 100.0) * 29.0;
    }

    private void setActiveColorTarget(ColorTarget target) {
        if (target == null) {
            target = ColorTarget.NONE;
        }
        activeColorTarget = target;
        updatePreviewHighlights();
        if (target == ColorTarget.NONE) {
            gradientSelector.setVisible(false);
            hueSelector.setVisible(false);
            return;
        }
        ColorState state = colorStates.get(target);
        updateGradientCanvas(state.hue);
        updateGradientSelector(state.saturation, state.brightness);
        updateHueIndicator(state.hue);
        gradientSelector.setVisible(true);
        hueSelector.setVisible(true);
    }

    private void updatePreviewHighlights() {
        setPreviewActive(fillPreview, activeColorTarget == ColorTarget.FILL);
        setPreviewActive(outlinePreview, activeColorTarget == ColorTarget.OUTLINE);
    }

    private void setPreviewActive(Region region, boolean active) {
        if (region == null) {
            return;
        }
        if (active) {
            if (!region.getStyleClass().contains("color-preview-active")) {
                region.getStyleClass().add("color-preview-active");
            }
        } else {
            region.getStyleClass().remove("color-preview-active");
        }
    }

    private void handleGradientSelection(double x, double y) {
        if (activeColorTarget == ColorTarget.NONE) {
            return;
        }
        double saturation = clamp(x / GRADIENT_SIZE, 0, 1);
        double brightness = 1 - clamp(y / GRADIENT_SIZE, 0, 1);
        ColorState state = colorStates.get(activeColorTarget);
        state.saturation = saturation;
        state.brightness = brightness;
        updateGradientSelector(saturation, brightness);
        notifyColorChange(activeColorTarget);
    }

    private void handleHueSelection(double x) {
        if (activeColorTarget == ColorTarget.NONE) {
            return;
        }
        double hueRatio = clamp(x / HUE_WIDTH, 0, 1);
        ColorState state = colorStates.get(activeColorTarget);
        state.hue = hueRatio * 360.0;
        updateHueIndicator(state.hue);
        updateGradientCanvas(state.hue);
        notifyColorChange(activeColorTarget);
    }

    private void updateGradientCanvas(double hue) {
        if (gradientCanvas == null) {
            return;
        }
        GraphicsContext gc = gradientCanvas.getGraphicsContext2D();
        PixelWriter writer = gc.getPixelWriter();
        for (int y = 0; y < GRADIENT_SIZE; y++) {
            double brightness = 1 - (double) y / (GRADIENT_SIZE - 1);
            for (int x = 0; x < GRADIENT_SIZE; x++) {
                double saturation = (double) x / (GRADIENT_SIZE - 1);
                writer.setColor(x, y, Color.hsb(hue, saturation, brightness));
            }
        }
    }

    private void renderHueCanvas() {
        GraphicsContext gc = hueCanvas.getGraphicsContext2D();
        for (int x = 0; x < HUE_WIDTH; x++) {
            double hue = (x / (double) (HUE_WIDTH - 1)) * 360.0;
            gc.setStroke(Color.hsb(hue, 1, 1));
            gc.strokeLine(x + 0.5, 0, x + 0.5, HUE_HEIGHT);
        }
    }

    private void updateGradientSelector(double saturation, double brightness) {
        gradientSelector.setCenterX(clamp(saturation, 0, 1) * GRADIENT_SIZE);
        gradientSelector.setCenterY((1 - clamp(brightness, 0, 1)) * GRADIENT_SIZE);
    }

    private void updateHueIndicator(double hue) {
        hueSelector.setCenterX(clamp(hue / 360.0, 0, 1) * HUE_WIDTH);
        hueSelector.setCenterY(HUE_HEIGHT / 2.0);
    }

    private void notifyColorChange(ColorTarget target) {
        if (target == ColorTarget.NONE) {
            return;
        }
        updateSwatch(target);
    }

    private void updateSwatch(ColorTarget target) {
        Region region = target == ColorTarget.FILL ? fillPreview : outlinePreview;
        Color color = colorStates.get(target).toColor();
        region.setStyle("-fx-background-color: " + toCssColor(color) + ";");
    }

    private String toCssColor(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("rgba(%d,%d,%d,1.0)", r, g, b);
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static class ColorState {
        double hue;
        double saturation;
        double brightness;

        ColorState(Color color) {
            setFromColor(color);
        }

        void setFromColor(Color color) {
            this.hue = color.getHue();
            this.saturation = color.getSaturation();
            this.brightness = color.getBrightness();
        }

        Color toColor() {
            return Color.hsb(hue, saturation, brightness);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}