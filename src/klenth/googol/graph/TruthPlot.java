package klenth.googol.graph;

@FunctionalInterface
public interface TruthPlot extends Graph {

    boolean satisfies(double x, double y);
}
