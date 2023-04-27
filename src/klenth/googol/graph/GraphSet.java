package klenth.googol.graph;

import javax.swing.*;
import java.util.*;
import java.util.stream.IntStream;

public class GraphSet {

    public interface Listener {
        default void graphAdded(GraphSet source, Graph newGraph, int number) {}
        default void graphRemoved(GraphSet source, Graph removedGraph, int number) {}
        default void graphReplaced(GraphSet source, Graph oldGraph, Graph newGraph, int number) {}
        default void graphAbled(GraphSet source, Graph abledGraph, int number, boolean enabled) {}
        default void graphsCleared(GraphSet source, int graphCount) {}
    }

    private List<Graph> graphs = new ArrayList<>();
    private SortedSet<Integer> enabledGraphNumbers = new TreeSet<>();
    private List<Listener> listeners = new LinkedList<>();

    public int getGraphCount() {
        return graphs.size();
    }

    public boolean isGraphEnabled(int number) {
        return enabledGraphNumbers.contains(number);
    }

    public Graph getGraph(int number) {
        return graphs.get(number);
    }

    public Iterable<Graph> graphs() {
        return Collections.unmodifiableList(graphs);
    }

    public Iterable<Graph> enabledGraphs() {
        return IntStream.range(0, getGraphCount())
                .filter(this::isGraphEnabled)
                .mapToObj(this::getGraph)
                .toList();
    }

    public int addGraph(Graph graph) {
        if (graph == null)
            throw new IllegalArgumentException("Null graph");
        int number = graphs.size();
        enabledGraphNumbers.add(number);
        graphs.add(graph);

        fireGraphAdded(graph, number);
        return number;
    }

    public void removeGraph(int number) {
        Graph removed = graphs.remove(number);
        var newEnabledGraphNumbers = new TreeSet<Integer>();
        for (var enabledNumber : enabledGraphNumbers) {
            if (enabledNumber < number)
                newEnabledGraphNumbers.add(enabledNumber);
            else if (enabledNumber > number)
                newEnabledGraphNumbers.add(enabledNumber - 1);
        }
        enabledGraphNumbers = newEnabledGraphNumbers;

        fireGraphRemoved(removed, number);
    }

    public void setGraph(int number, Graph newGraph) {
        Graph oldGraph = graphs.get(number);
        graphs.set(number, newGraph);

        fireGraphReplaced(oldGraph, newGraph, number);
    }

    public void setGraphEnabled(int number, boolean enabled) {
        var graph = graphs.get(number);
        if (enabled) {
            if (enabledGraphNumbers.add(number))
                fireGraphAbled(graph, number, true);
        } else {
            if (enabledGraphNumbers.remove(number))
                fireGraphAbled(graph, number, false);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void fireGraphAdded(Graph newGraph, int number) {
        for (var listener : listeners)
            onEventThread(() -> listener.graphAdded(this, newGraph, number));
    }

    private void fireGraphRemoved(Graph removedGraph, int number) {
        for (var listener : listeners)
            onEventThread(() -> listener.graphRemoved(this, removedGraph, number));
    }

    private void fireGraphReplaced(Graph oldGraph, Graph newGraph, int number) {
        for (var listener : listeners)
            onEventThread(() -> listener.graphReplaced(this, oldGraph, newGraph, number));
    }

    private void fireGraphAbled(Graph abledGraph, int number, boolean enabled) {
        for (var listener : listeners)
            onEventThread(() -> listener.graphAbled(this, abledGraph, number, enabled));
    }

    private void fireGraphsCleared(int graphCount) {
        for (var listener : listeners)
            onEventThread(() -> listener.graphsCleared(this, graphCount));
    }

    private void onEventThread(Runnable r) {
        if (SwingUtilities.isEventDispatchThread())
            r.run();
        else
            SwingUtilities.invokeLater(r);
    }
}
