package klenth.googol.gui;

import klenth.googol.graph.CompiledGraph;
import klenth.googol.graph.Graph;
import klenth.googol.graph.GraphSet;

import javax.swing.table.AbstractTableModel;

public class GraphSetTableModel extends AbstractTableModel {

    private GraphSet graphSet;

    private GraphSet.Listener graphSetListener = new GraphSet.Listener() {
        @Override
        public void graphAdded(GraphSet source, Graph newGraph, int number) {
            fireTableRowsInserted(number, number);
        }

        @Override
        public void graphRemoved(GraphSet source, Graph removedGraph, int number) {
            fireTableRowsDeleted(number, number);
        }

        @Override
        public void graphAbled(GraphSet source, Graph abledGraph, int number, boolean enabled) {
            fireTableRowsUpdated(number, number);
        }

        @Override
        public void graphsCleared(GraphSet source, int graphCount) {
            fireTableRowsDeleted(0, graphCount - 1);
        }
    };

    public GraphSetTableModel(GraphSet graphSet) {
        this.graphSet = graphSet;
        graphSet.addListener(graphSetListener);
    }

    @Override
    public int getRowCount() {
        return graphSet.getGraphCount();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> graphSet.isGraphEnabled(rowIndex);
            case 1 -> graphSet.getGraph(rowIndex);
            default -> throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "";
            case 1 -> "Equation";
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Boolean.class;
            case 1 -> Graph.class;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (!(aValue instanceof Boolean b))
                throw new IllegalArgumentException("Value not instanceof Boolean");
            graphSet.setGraphEnabled(rowIndex, b);
        } else if (columnIndex == 1) {
            if (!(aValue instanceof Graph graph))
                throw new IllegalArgumentException("Value not instanceof Graph");
            graphSet.setGraph(rowIndex, graph);
        }
    }
}
