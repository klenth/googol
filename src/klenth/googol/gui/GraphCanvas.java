package klenth.googol.gui;

import klenth.googol.graph.*;
import klenth.util.Zipper;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GraphCanvas extends JComponent {

    private GraphSet graphs = new GraphSet();
    private List<Object> graphRenders = new ArrayList<>();
    private ViewWindow window = new ViewWindow(-4, 4, 0.5, -4, 4, 0.5);

    private GraphSet.Listener graphListener = new GraphSet.Listener() {
        @Override
        public void graphAdded(GraphSet source, Graph newGraph, int number) {
            graphRenders.add(null);
            repaint();
        }

        @Override
        public void graphRemoved(GraphSet source, Graph removedGraph, int number) {
            graphRenders.remove(number);
            repaint();
        }

        @Override
        public void graphReplaced(GraphSet source, Graph oldGraph, Graph newGraph, int number) {
            graphRenders.set(number, null);
            repaint();
        }

        @Override
        public void graphAbled(GraphSet source, Graph abledGraph, int number, boolean enabled) {
            repaint();
        }

        @Override
        public void graphsCleared(GraphSet source, int graphCount) {
            graphRenders.clear();
            repaint();
        }
    };

    {
        enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
        graphs.addListener(graphListener);
    }

    public GraphSet getGraphSet() {
        return graphs;
    }

    @Override
    protected void paintComponent(Graphics _g) {
        super.paintComponent(_g);

        Graphics2D g = (Graphics2D)_g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintGrid(g);
        paintAxes(g);

        for (int i = 0; i < graphs.getGraphCount(); ++i) {
            if (graphs.isGraphEnabled(i)) {
                var graph = graphs.getGraph(i);
                paintGraph(i, graph, g);
            }
        }
    }

    private void paintGrid(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        g.setPaint(Color.lightGray);
        Line2D line = new Line2D.Double();
        double bx = window.xMin() - window.xMin() % window.xStep();

        Dimension size = getSize();
        Point2D sMin = window.mathToScreen(size, new Point2D.Double(window.xMin(), window.yMin()));
        Point2D sMax = window.mathToScreen(size, new Point2D.Double(window.xMax(), window.yMax()));
        for (double x = bx; x <= window.xMax(); x += window.xStep()) {
            double sx = (x - window.xMin()) / (window.xMax() - window.xMin()) * size.width;
            line.setLine(sx, sMin.getY(), sx, sMax.getY());
            g.draw(line);
        }

        double by = window.yMin() - window.yMin() % window.yStep();
        for (double y = by; y <= window.yMax(); y += window.yStep()) {
            double sy = size.height - (y - window.yMin()) / (window.yMax() - window.yMin()) * size.height;
            line.setLine(sMin.getX(), sy, sMax.getX(), sy);
            g.draw(line);
        }
    }

    private void paintAxes(Graphics2D g) {
        g.setPaint(new Color(0x102040));
        g.setStroke(new BasicStroke(3f));

        Dimension size = getSize();
        Line2D line = new Line2D.Double();
        Point2D sMin = window.mathToScreen(size, new Point2D.Double(window.xMin(), window.yMin()));
        Point2D sMax = window.mathToScreen(size, new Point2D.Double(window.xMax(), window.yMax()));
        Point2D sOrigin = window.mathToScreen(size, new Point2D.Double(0, 0));

        line.setLine(sMin.getX(), sOrigin.getY(), sMax.getX(), sOrigin.getY());
        g.draw(line);

        line.setLine(sOrigin.getX(), sMin.getY(), sOrigin.getX(), sMax.getY());
        g.draw(line);
    }

    @SuppressWarnings("unchecked")
    private <R> R findRender(int number, Supplier<R> renderer) {
        R render = (R)graphRenders.get(number);
        if (render == null) {
            render = renderer.get();
            graphRenders.set(number, render);
        }

        return render;
    }

    private void paintGraph(int number, Graph graph, Graphics2D g) {
        switch (graph) {
            case CompiledGraph(var eqn, Graph actualGraph) -> paintGraph(number, actualGraph, g);
            case XFunction xf -> paintGraph(number, xf, g);
            case TruthPlot tp -> paintGraph(number, tp, g);
            case EmptyGraph eg -> { /* do nothing */ }
            default -> throw new RuntimeException("No rendering implemented for graph " + graph);
        }
    }

    private void paintGraph(int number, XFunction graph, Graphics2D g) {
        g.setPaint(Color.red.darker());
        g.setStroke(new BasicStroke(2f));

        Shape render = findRender(number, () -> {
            double yMax = window.screenToMath(getSize(), new Point2D.Float(0, 0)).getY(),
                    yMin = window.screenToMath(getSize(), new Point2D.Float(0, getHeight() - 1)).getY();

            Dimension size = getSize();
            var path = new GeneralPath();
            var lastY = Double.NaN;
            for (int i = 0; i < size.width; ++i) {
                double x = ((double) i) / size.width * (window.xMax() - window.xMin()) + window.xMin();
                double y = graph.evaluate(x);
                Point2D screen = window.mathToScreen(size, new Point2D.Double(x, y));

                if (i == 0)
                    path.moveTo(screen.getX(), screen.getY());
                else {
                    if (lastY > yMax && y < yMin
                            || lastY < yMin && y > yMax)
                        path.moveTo(screen.getX(), screen.getY());
                    else
                        path.lineTo(screen.getX(), screen.getY());
                }

                lastY = y;
            }

            return path;
        });

        g.draw(render);
    }

    private void paintGraph(int number, TruthPlot graph, Graphics2D g) {
        Image render = findRender(number, () -> {
            var image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

            int yesColor = 0x40ff0000;
            int noColor = 0x00000000;

            for (int j = 0; j < getHeight(); ++j) {
                for (int i = 0; i < getWidth(); ++i) {
                    Point2D math = window.screenToMath(getSize(), new Point2D.Float(i, j));
                    if (graph.satisfies(math.getX(), math.getY()))
                        image.setRGB(i, j, yesColor);
                    else
                        image.setRGB(i, j, noColor);
                }
            }

            return image;
        });

        g.drawImage(render, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 800);
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        Point2D c = window.screenToMath(getSize(), new Point2D.Float(e.getX(), e.getY()));
        zoom(c.getX(), c.getY(), (e.getWheelRotation() < 0) ? 0.8 : 1.25);
    }

    @Override
    protected void processComponentEvent(ComponentEvent e) {
        if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
            graphRenders.replaceAll(o -> null);
            repaint();
        }
    }

    public void zoom(double cx, double cy, double factor) {
        throw new RuntimeException("Unimplemented");
    }
}
