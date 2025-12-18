package com.paintplus.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class CustomColorPicker extends VBox {

    public enum ColorTarget {
        FILL, OUTLINE
    }

    private final ObjectProperty<Color> fillColor = new SimpleObjectProperty<>(Color.RED);
    private final ObjectProperty<Color> outlineColor = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty lineThickness = new SimpleDoubleProperty(5.0);
    private final DoubleProperty globalOpacity = new SimpleDoubleProperty(1.0);

    private ColorTarget activeTarget = ColorTarget.FILL;

    private Pane gradientBox;
    private Circle gradientSelector;
    private Slider hueSlider;
    private Slider opacitySlider;
    private Slider thicknessSlider;

    private Rectangle fillPreview;
    private Rectangle outlinePreview;

    private double currentHue = 0.0;
    private double currentSat = 1.0;
    private double currentBri = 1.0;

    public CustomColorPicker() {
        setSpacing(20);
        setPadding(new Insets(20));
        setPrefWidth(300);
        getStyleClass().add("right-sidebar");

        createGradientBox();
        createHueSlider();
        createOpacityControl();
        createColorIndicators();
        createLineThicknessControl();

        updateColorFromHSB();
    }

    private void createGradientBox() {
        gradientBox = new Pane();
        gradientBox.setPrefSize(260, 200);
        gradientBox.getStyleClass().add("gradient-box");

        // The backgrounds will be set programmatically based on Hue
        updateGradientBackgrounds();

        gradientSelector = new Circle(6);
        gradientSelector.setStroke(Color.WHITE);
        gradientSelector.setStrokeWidth(2);
        gradientSelector.setFill(Color.TRANSPARENT);
        gradientSelector.setManaged(false);

        // Initial position (Top Right for Red)
        gradientSelector.setCenterX(260);
        gradientSelector.setCenterY(0);

        gradientBox.getChildren().add(gradientSelector);

        gradientBox.setOnMousePressed(e -> pickColorFromBox(e.getX(), e.getY()));
        gradientBox.setOnMouseDragged(e -> pickColorFromBox(e.getX(), e.getY()));

        getChildren().add(gradientBox);
    }

    private void updateGradientBackgrounds() {
        // Layer 1: Hue Base
        Color hueColor = Color.hsb(currentHue, 1.0, 1.0);
        BackgroundFill hueFill = new BackgroundFill(hueColor, CornerRadii.EMPTY, Insets.EMPTY);

        // Layer 2: White -> Transparent (Horizontal for Saturation)
        Stop[] stopsSat = { new Stop(0, Color.WHITE), new Stop(1, Color.TRANSPARENT) };
        LinearGradient gradSat = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stopsSat);
        BackgroundFill satFill = new BackgroundFill(gradSat, CornerRadii.EMPTY, Insets.EMPTY);

        // Layer 3: Transparent -> Black (Vertical for Brightness)
        Stop[] stopsBri = { new Stop(0, Color.TRANSPARENT), new Stop(1, Color.BLACK) };
        LinearGradient gradBri = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stopsBri);
        BackgroundFill briFill = new BackgroundFill(gradBri, CornerRadii.EMPTY, Insets.EMPTY);

        gradientBox.setBackground(new Background(hueFill, satFill, briFill));
    }

    private void pickColorFromBox(double x, double y) {
        double w = gradientBox.getWidth();
        double h = gradientBox.getHeight();

        x = Math.max(0, Math.min(x, w));
        y = Math.max(0, Math.min(y, h));

        gradientSelector.setCenterX(x);
        gradientSelector.setCenterY(y);

        currentSat = x / w;
        currentBri = 1.0 - (y / h);

        updateColorFromHSB();
    }

    private void createHueSlider() {
        hueSlider = new Slider(0, 360, 0);
        hueSlider.getStyleClass().add("hue-slider");

        // Custom CSS handles the rainbow background, here we just handle logic
        hueSlider.valueProperty().addListener((obs, old, val) -> {
            currentHue = val.doubleValue();
            updateGradientBackgrounds();
            updateColorFromHSB();
        });

        getChildren().add(hueSlider);
    }

    private void createOpacityControl() {
        VBox container = new VBox(5);
        Label label = new Label("Opacity");
        Label valueLabel = new Label("100 %");

        opacitySlider = new Slider(0, 1, 1);
        opacitySlider.valueProperty().addListener((obs, old, val) -> {
            globalOpacity.set(val.doubleValue());
            valueLabel.setText(String.format("%.0f %%", val.doubleValue() * 100));
        });

        HBox header = new HBox(10, label, valueLabel);
        container.getChildren().addAll(header, opacitySlider);
        getChildren().add(container);
    }

    private void createColorIndicators() {
        VBox container = new VBox(10);

        // Fill Row
        HBox fillRow = new HBox(20);
        Label fillLbl = new Label("Fill");
        fillPreview = new Rectangle(40, 20);
        fillPreview.setStroke(Color.BLACK);
        fillPreview.setStrokeWidth(1);
        fillPreview.fillProperty().bind(fillColor);

        // Select Fill logic
        fillPreview.setOnMouseClicked(e -> setActiveTarget(ColorTarget.FILL));
        fillRow.getChildren().addAll(fillLbl, fillPreview);

        // Outline Row
        HBox outlineRow = new HBox(20);
        Label outLbl = new Label("Outline");
        outlinePreview = new Rectangle(40, 20);
        outlinePreview.setStroke(Color.BLACK);
        outlinePreview.setStrokeWidth(1);
        outlinePreview.fillProperty().bind(outlineColor);

        // Select Outline logic
        outlinePreview.setOnMouseClicked(e -> setActiveTarget(ColorTarget.OUTLINE));
        outlineRow.getChildren().addAll(outLbl, outlinePreview);

        container.getChildren().addAll(fillRow, outlineRow);

        // Initial highlight
        setActiveTarget(ColorTarget.FILL);

        getChildren().add(container);
    }

    private void setActiveTarget(ColorTarget target) {
        this.activeTarget = target;
        fillPreview.setStrokeWidth(target == ColorTarget.FILL ? 3 : 1);
        outlinePreview.setStrokeWidth(target == ColorTarget.OUTLINE ? 3 : 1);

        // Sync sliders to current color of target?
        // Requirement FR3.8 says adjustments update target.
        // It doesn't explicitly require the picker to jump to the target's existing color on selection,
        // but it's good UX. We will skip complex reverse-HSB calc for now to stick strictly to requirements
        // and avoid bugs, focusing on "adjustments... update selected target".
    }

    private void createLineThicknessControl() {
        VBox container = new VBox(5);
        Label label = new Label("Line Thickness");
        Label valueLabel = new Label("5 px"); // Starting value

        thicknessSlider = new Slider(1, 100, 5);
        thicknessSlider.valueProperty().addListener((obs, old, val) -> {
            lineThickness.set(val.doubleValue());
            valueLabel.setText(String.format("%.0f %%", val.doubleValue())); // Wireframe says %, usually px
        });

        // Wireframe shows 76%, so we allow range 1-100
        thicknessSlider.setValue(76);

        HBox header = new HBox(10, label, valueLabel);
        container.getChildren().addAll(header, thicknessSlider);
        getChildren().add(container);
    }

    private void updateColorFromHSB() {
        Color c = Color.hsb(currentHue, currentSat, currentBri);

        if (activeTarget == ColorTarget.FILL) {
            fillColor.set(c);
        } else {
            outlineColor.set(c);
        }
    }

    // Getters for properties
    public ObjectProperty<Color> fillColorProperty() { return fillColor; }
    public ObjectProperty<Color> outlineColorProperty() { return outlineColor; }
    public DoubleProperty lineThicknessProperty() { return lineThickness; }
    public DoubleProperty globalOpacityProperty() { return globalOpacity; }
}