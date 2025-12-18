package com.paintplus.ui;

import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

public class Icons {

    // Target size for all icons
    private static final double TARGET_SIZE = 24.0;

    public static SVGPath getPencilIcon() {
        // Native 24x24
        return createPath("M13.6286 4.37035L19.6294 10.3714L6.59893 23.4024L1.24876 23.993C0.532526 24.0722 -0.072614 23.4666 0.0070714 22.7503L0.602368 17.3962L13.6286 4.37035ZM23.3408 3.47689L20.5233 0.659191C19.6444 -0.21973 18.2189 -0.21973 17.3401 0.659191L14.6893 3.31002L20.6901 9.31105L23.3408 6.66023C24.2197 5.78084 24.2197 4.35581 23.3408 3.47689Z",
                1.0, 1.0);
    }

    public static SVGPath getRectangleIcon() {
        // Native ~640 -> Scale to 24
        double scale = TARGET_SIZE / 640.0;
        return createPath("M512 112C520.8 112 528 119.2 528 128L528 512C528 520.8 520.8 528 512 528L128 528C119.2 528 112 520.8 112 512L112 128C112 119.2 119.2 112 128 112L512 112zM128 64C92.7 64 64 92.7 64 128L64 512C64 547.3 92.7 576 128 576L512 576C547.3 576 576 547.3 576 512L576 128C576 92.7 547.3 64 512 64L128 64z",
                scale, scale);
    }

    public static SVGPath getCircleIcon() {
        // Native ~640 -> Scale to 24
        double scale = TARGET_SIZE / 640.0;
        return createPath("M528 320C528 205.1 434.9 112 320 112C205.1 112 112 205.1 112 320C112 434.9 205.1 528 320 528C434.9 528 528 434.9 528 320zM64 320C64 178.6 178.6 64 320 64C461.4 64 576 178.6 576 320C576 461.4 461.4 576 320 576C178.6 576 64 461.4 64 320z",
                scale, scale);
    }

    public static SVGPath getTriangleIcon() {
        // Native 16 -> Scale to 24
        double scale = TARGET_SIZE / 16.0;
        return createPath("M7.938 2.016A.13.13 0 0 1 8.002 2a.13.13 0 0 1 .063.016.15.15 0 0 1 .054.057l6.857 11.667c.036.06.035.124.002.183a.2.2 0 0 1-.054.06.1.1 0 0 1-.066.017H1.146a.1.1 0 0 1-.066-.017.2.2 0 0 1-.054-.06.18.18 0 0 1 .002-.183L7.884 2.073a.15.15 0 0 1 .054-.057m1.044-.45a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767z",
                scale, scale);
    }

    public static SVGPath getEraserIcon() {
        // Native ~24 -> Scale 1.0
        return createPath("M23.341 11.341C24.2197 10.4623 24.2197 9.03769 23.341 8.15902L15.841 0.659021C14.9623 -0.21965 13.5378 -0.219697 12.659 0.659021L0.659003 12.659C-0.219668 13.5377 -0.219668 14.9623 0.659003 15.841L5.159 20.341C5.58097 20.7629 6.15328 21 6.75003 21H23.4375C23.7481 21 24 20.7481 24 20.4375V18.5625C24 18.2519 23.7481 18 23.4375 18H16.682L23.341 11.341ZM9.15537 8.40535L15.5947 14.8447L12.4394 18H7.06071L3.31072 14.25L9.15537 8.40535Z",
                1.0, 1.0);
    }

    public static SVGPath getTextIcon() {
        // Native 16 -> Scale to 24
        double scale = TARGET_SIZE / 16.0;
        return createPath("M12.258 3h-8.51l-.083 2.46h.479c.26-1.544.758-1.783 2.693-1.845l.424-.013v7.827c0 .663-.144.82-1.3.923v.52h4.082v-.52c-1.162-.103-1.306-.26-1.306-.923V3.602l.431.013c1.934.062 2.434.301 2.693 1.846h.479z",
                scale, scale);
    }

    private static SVGPath createPath(String content, double scaleX, double scaleY) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        // Apply scaling transform to normalization
        path.getTransforms().add(new Scale(scaleX, scaleY, 0, 0));
        return path;
    }
}