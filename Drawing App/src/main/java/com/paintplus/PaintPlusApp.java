package com.paintplus;

import com.paintplus.components.CustomColorPicker;
import com.paintplus.ui.Icons;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Stack;

public class PaintPlusApp extends Application {

    private enum Tool {
        PENCIL, RECTANGLE, CIRCLE, TRIANGLE, ERASER, TEXT
    }

    private Tool currentTool = Tool.PENCIL;

    // Canvas & State
    private Canvas mainCanvas;
    private Canvas tempCanvas; // For previews (drag shapes)
    private GraphicsContext gc;
    private GraphicsContext tempGc;

    // Undo/Redo Stacks
    private final Stack<WritableImage> undoStack = new Stack<>();
    private final Stack<WritableImage> redoStack = new Stack<>();

    // UI Components
    private CustomColorPicker colorPicker;

    // Drawing State variables
    private double startX, startY;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // 1. Top Navigation
        HBox topNav = createTopNav(primaryStage);
        root.setTop(topNav);

        // 2. Left Toolbar
        VBox leftToolbar = createLeftToolbar();
        root.setLeft(leftToolbar);

        // 3. Right Sidebar (Color & Properties)
        colorPicker = new CustomColorPicker();
        root.setRight(colorPicker);

        // 4. Center Canvas
        StackPane canvasContainer = new StackPane();
        canvasContainer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");
        canvasContainer.setAlignment(Pos.CENTER);

        double canvasWidth = 800;
        double canvasHeight = 600;

        mainCanvas = new Canvas(canvasWidth, canvasHeight);
        tempCanvas = new Canvas(canvasWidth, canvasHeight);
        tempCanvas.setMouseTransparent(true);

        gc = mainCanvas.getGraphicsContext2D();
        tempGc = tempCanvas.getGraphicsContext2D();

        // Initialize white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        saveState();

        // Canvas Border
        Pane canvasBorder = new Pane();
        canvasBorder.setMaxSize(canvasWidth, canvasHeight);
        canvasBorder.getStyleClass().add("canvas-border");
        canvasBorder.setMouseTransparent(true);

        StackPane canvasStack = new StackPane(mainCanvas, tempCanvas, canvasBorder);
        canvasStack.setMaxSize(canvasWidth, canvasHeight);
        canvasStack.setMinSize(canvasWidth, canvasHeight);

        canvasContainer.getChildren().add(canvasStack);
        root.setCenter(canvasContainer);

        setupEventHandlers();

        Scene scene = new Scene(root, 1440, 1024);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Paint+");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopNav(Stage stage) {
        HBox nav = new HBox(30);
        nav.getStyleClass().add("top-nav");
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(10, 20, 10, 20));

        Label brand = new Label("Paint+");
        brand.getStyleClass().add("brand-logo");

        Button btnUndo = new Button("Undo");
        btnUndo.setOnAction(e -> undo());

        Button btnRedo = new Button("Redo");
        btnRedo.setOnAction(e -> redo());

        Button btnExport = new Button("Export");
        btnExport.setOnAction(e -> exportImage(stage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        nav.getChildren().addAll(brand, new Region(), btnUndo, btnRedo, btnExport);
        ((Region)nav.getChildren().get(1)).setPrefWidth(20);

        return nav;
    }

    private VBox createLeftToolbar() {
        VBox toolbar = new VBox(15);
        toolbar.getStyleClass().add("left-toolbar");
        toolbar.setAlignment(Pos.TOP_CENTER);
        toolbar.setPadding(new Insets(20, 10, 20, 10));
        toolbar.setPrefWidth(60);
        toolbar.setMinWidth(60);
        toolbar.setMaxWidth(60);

        ToggleGroup toolsGroup = new ToggleGroup();

        ToggleButton btnPencil = createToolButton(Tool.PENCIL, Icons.getPencilIcon());
        ToggleButton btnRect = createToolButton(Tool.RECTANGLE, Icons.getRectangleIcon());
        ToggleButton btnCircle = createToolButton(Tool.CIRCLE, Icons.getCircleIcon());
        ToggleButton btnTri = createToolButton(Tool.TRIANGLE, Icons.getTriangleIcon());
        ToggleButton btnEraser = createToolButton(Tool.ERASER, Icons.getEraserIcon());
        ToggleButton btnText = createToolButton(Tool.TEXT, Icons.getTextIcon());

        btnPencil.setToggleGroup(toolsGroup);
        btnRect.setToggleGroup(toolsGroup);
        btnCircle.setToggleGroup(toolsGroup);
        btnTri.setToggleGroup(toolsGroup);
        btnEraser.setToggleGroup(toolsGroup);
        btnText.setToggleGroup(toolsGroup);

        btnPencil.setSelected(true);

        toolsGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentTool = (Tool) newVal.getUserData();
            }
        });

        toolbar.getChildren().addAll(btnPencil, btnRect, btnCircle, btnTri, btnEraser, btnText);
        return toolbar;
    }

    private ToggleButton createToolButton(Tool tool, SVGPath icon) {
        ToggleButton btn = new ToggleButton();

        // CRITICAL FIX: Wrap icon in Group.
        // A Group's layout bounds are derived from the transformed bounds of its children.
        // This ensures the Scale transform on the SVGPath is respected by the layout engine.
        Group scaledIcon = new Group(icon);

        StackPane iconContainer = new StackPane(scaledIcon);
        iconContainer.setPrefSize(24, 24);
        iconContainer.setMaxSize(24, 24);
        iconContainer.setMinSize(24, 24);

        icon.getStyleClass().add("tool-icon");

        btn.setGraphic(iconContainer);
        btn.setUserData(tool);
        btn.getStyleClass().add("tool-button");
        return btn;
    }

    private void setupEventHandlers() {
        mainCanvas.setOnMousePressed(e -> {
            saveState();
            startX = e.getX();
            startY = e.getY();

            configureGraphics(gc);
            configureGraphics(tempGc);

            if (currentTool == Tool.PENCIL) {
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                gc.stroke();
            } else if (currentTool == Tool.ERASER) {
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                double size = colorPicker.lineThicknessProperty().get();
                gc.clearRect(e.getX() - size/2, e.getY() - size/2, size, size);
            } else if (currentTool == Tool.TEXT) {
                handleTextTool(e.getX(), e.getY());
            }
        });

        mainCanvas.setOnMouseDragged(e -> {
            if (currentTool == Tool.PENCIL) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (currentTool == Tool.ERASER) {
                double size = colorPicker.lineThicknessProperty().get();
                gc.clearRect(e.getX() - size/2, e.getY() - size/2, size, size);
            } else if (isShapeTool()) {
                tempGc.clearRect(0, 0, tempCanvas.getWidth(), tempCanvas.getHeight());
                drawShape(tempGc, startX, startY, e.getX(), e.getY(), currentTool);
            }
        });

        mainCanvas.setOnMouseReleased(e -> {
            if (isShapeTool()) {
                tempGc.clearRect(0, 0, tempCanvas.getWidth(), tempCanvas.getHeight());
                configureGraphics(gc);
                drawShape(gc, startX, startY, e.getX(), e.getY(), currentTool);
            } else if (currentTool == Tool.PENCIL) {
                gc.closePath();
            }
        });
    }

    private boolean isShapeTool() {
        return currentTool == Tool.RECTANGLE || currentTool == Tool.CIRCLE || currentTool == Tool.TRIANGLE;
    }

    private void configureGraphics(GraphicsContext g) {
        Color fill = colorPicker.fillColorProperty().get();
        Color stroke = colorPicker.outlineColorProperty().get();
        double opacity = colorPicker.globalOpacityProperty().get();
        double width = colorPicker.lineThicknessProperty().get();

        g.setGlobalAlpha(opacity);
        g.setStroke(stroke);
        g.setFill(fill);
        g.setLineWidth(width);
        g.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        g.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
    }

    private void drawShape(GraphicsContext g, double x1, double y1, double x2, double y2, Tool tool) {
        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double w = Math.abs(x1 - x2);
        double h = Math.abs(y1 - y2);

        if (tool == Tool.RECTANGLE) {
            g.fillRect(minX, minY, w, h);
            g.strokeRect(minX, minY, w, h);
        } else if (tool == Tool.CIRCLE) {
            g.fillOval(minX, minY, w, h);
            g.strokeOval(minX, minY, w, h);
        } else if (tool == Tool.TRIANGLE) {
            double[] xPoints = { minX + w / 2, minX, minX + w };
            double[] yPoints = { minY, minY + h, minY + h };

            if (y2 < y1) {
                yPoints[0] = minY + h;
                yPoints[1] = minY;
                yPoints[2] = minY;
            }

            g.fillPolygon(xPoints, yPoints, 3);
            g.strokePolygon(xPoints, yPoints, 3);
        }
    }

    private void handleTextTool(double x, double y) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Text Tool");
        dialog.setHeaderText("Enter text:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(text -> {
            configureGraphics(gc);
            gc.setFont(javafx.scene.text.Font.font("Arial", colorPicker.lineThicknessProperty().get() * 0.5 + 20));
            gc.fillText(text, x, y);
            gc.strokeText(text, x, y);
        });
    }

    private void saveState() {
        WritableImage snap = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
        mainCanvas.snapshot(null, snap);
        undoStack.push(snap);
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            WritableImage current = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
            mainCanvas.snapshot(null, current);
            redoStack.push(current);

            WritableImage prev = undoStack.pop();
            drawImage(prev);
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            WritableImage next = redoStack.pop();
            undoStack.push(currentSnapshot());
            drawImage(next);
        }
    }

    private WritableImage currentSnapshot() {
        WritableImage snap = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
        mainCanvas.snapshot(null, snap);
        return snap;
    }

    private void drawImage(WritableImage img) {
        gc.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        gc.drawImage(img, 0, 0);
    }

    private void exportImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
                mainCanvas.snapshot(null, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}