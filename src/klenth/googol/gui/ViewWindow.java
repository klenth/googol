package klenth.googol.gui;

import java.awt.*;
import java.awt.geom.Point2D;

public record ViewWindow(double xMin, double xMax, double xStep, double yMin, double yMax, double yStep) {

    public static final ViewWindow DEFAULT = new ViewWindow(-10, 10, 1, -10, 10, 1);

    public Point2D mathToScreen(Dimension screenSize, Point2D p) {
        return new Point2D.Double(
            mathToScreenX(screenSize, p.getX()),
            mathToScreenY(screenSize, p.getY())
        );
    }

    public double mathToScreenX(Dimension screenSize, double x) {
        return (x - xMin) / (xMax - xMin) * screenSize.width;
    }

    public double mathToScreenY(Dimension screenSize, double y) {
        return screenSize.height - (y - yMin) / (yMax - yMin) * screenSize.height;
    }

    public Point2D screenToMath(Dimension screenSize, Point2D p) {
        return new Point2D.Double(
            screenToMathX(screenSize, p.getX()),
            screenToMathY(screenSize, p.getY())
        );
    }

    public double screenToMathX(Dimension screenSize, double x) {
        return x / screenSize.width * (xMax - xMin) + xMin;
    }

    public double screenToMathY(Dimension screenSize, double y) {
        return (screenSize.height - y) / screenSize.height * (yMax - yMin) + yMin;
    }

    public double xSpan() {
        return xMax - xMin;
    }

    public double ySpan() {
        return yMax - yMin;
    }

    public ViewWindow translate(double dx, double dy) {
        return new ViewWindow(xMin + dx, xMax + dx, xStep, yMin + dy, yMax + dy, yStep);
    }
}
