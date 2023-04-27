package klenth.googol.gui;

import edu.westminstercollege.cs.jade.SyntaxException;
import klenth.googol.graph.CompiledGraph;
import klenth.googol.math.MathContext;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class EquationCellEditor extends JTextField implements TableCellRenderer, TableCellEditor {

    private MathContext mathContext;
    private List<CellEditorListener> listeners = new ArrayList<>();
    private Object initialValue = null;
    private CompiledGraph editedValue = null;

    public EquationCellEditor(MathContext context) {
        this.mathContext = context;

        setFont(new Font(getFont().getName(), Font.PLAIN, 24));
        setBorder(BorderFactory.createEmptyBorder());
        setHorizontalAlignment(CENTER);
        addActionListener(e -> stopCellEditing());


        addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {

            }

            @Override
            public void editingCanceled(ChangeEvent e) {

            }
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        /*if (hasFocus || isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }*/

        setForeground(table.getForeground());
        if (value instanceof CompiledGraph graph) {
            var eqn = graph.equation();
            if (eqn.isBlank() && !hasFocus) {
                setText("Example: y = x^2");
                setForeground(Color.gray);
            } else
                setText(graph.equation());
        } else
            setText((value == null) ? "" : value.toString());

        return this;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return editedValue;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        editedValue = null;
        try {
            editedValue = CompiledGraph.compile(getText(), mathContext);
        } catch (SyntaxException ex) {
            System.err.println(ex.getMessage());
            return false;
        }

        var tempListeners = new ArrayList<>(listeners);
        for (var listener : tempListeners)
            onEventThread(() -> listener.editingStopped(new ChangeEvent(this)));

        return true;
    }

    @Override
    public void cancelCellEditing() {
        var tempListeners = new ArrayList<>(listeners);
        for (var listener : tempListeners)
            onEventThread(() -> listener.editingCanceled(new ChangeEvent(this)));
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listeners.add(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(l);
    }

    private void onEventThread(Runnable r) {
        if (SwingUtilities.isEventDispatchThread())
            r.run();
        else
            SwingUtilities.invokeLater(r);
    }
}
