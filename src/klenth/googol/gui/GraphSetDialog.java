package klenth.googol.gui;

import klenth.googol.graph.CompiledGraph;
import klenth.googol.graph.EmptyGraph;
import klenth.googol.graph.Graph;
import klenth.googol.graph.GraphSet;
import klenth.googol.math.MathContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GraphSetDialog extends JDialog {

    private GraphSet graphSet;
    private MathContext mathContext;
    private GraphSetTableModel tableModel;
    private JTable table;
    private JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);

    public GraphSetDialog(Frame owner, GraphSet graphSet, MathContext mathContext) {
        super(owner, "Graphs", ModalityType.MODELESS);
        this.mathContext = mathContext;
        this.graphSet = graphSet;

        tableModel = new GraphSetTableModel(graphSet);
        table = new JTable(tableModel);

        setupLayout();
        pack();

        if (owner != null) {
            Point ownerPoint = owner.getLocation();
            Dimension ownerSize = owner.getSize();
            setLocation(new Point(ownerPoint.x + ownerSize.width, ownerPoint.y));
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        var equationCellEditor = new EquationCellEditor(mathContext);
        table.setDefaultEditor(Graph.class, equationCellEditor);
        table.setDefaultRenderer(Graph.class, equationCellEditor);
        table.setRowHeight(32);
        table.getColumnModel().getColumn(0).setMaxWidth(32);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void setupLayout() {
        var cPane = getContentPane();
        cPane.setLayout(new BorderLayout(8, 8));
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);
        toolbar.add(new AbstractAction("Add graph") {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphSet.addGraph(new CompiledGraph("", new EmptyGraph()));
            }
        });
    }
}
