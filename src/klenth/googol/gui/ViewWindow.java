package klenth.googol.gui;

import java.awt.*;
import java.awt.geom.Point2D;

public record ViewWindow(double xMin, double xMax, double xStep, double yMin, double yMax, double yStep) {

    public static final ViewWindow DEFAULT = new ViewWindow(-10, 10, 1, -10, 10, 1);

    public Point2D mathToScreen(Dimension screenSize, Point2D p) {
        return new Point2D.Double(
                (p.getX() - xMin) / (xMax - xMin) * screenSize.width,
                screenSize.height - (p.getY() - yMin) / (yMax - yMin) * screenSize.height
        );
    }

    public Point2D screenToMath(Dimension screenSize, Point2D p) {
        return new Point2D.Double(
                p.getX() / screenSize.width * (xMax - xMin) + xMin,
                (screenSize.height - p.getY()) / screenSize.height * (yMax - yMin) + yMin
        );
    }

    public double xSpan() {
        return xMax - xMin;
    }

    public double ySpan() {
        return yMax - yMin;
    }
}
