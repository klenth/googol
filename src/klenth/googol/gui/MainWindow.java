package klenth.googol.gui;

import edu.westminstercollege.cs.jade.SyntaxException;
import klenth.googol.Equations;
import klenth.googol.Expressions;
import klenth.googol.ast.Node;
import klenth.googol.graph.*;
import klenth.googol.math.EnvFunction;
import klenth.googol.math.MathContext;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;

public class MainWindow extends JFrame {

    private MathContext mathContext = new MathContext(
            Map.of(
                    "pi", Math.PI,
                    "π", Math.PI,
                    "e", Math.E
            ),
            Arrays.stream(EnvFunction.Builtin.values())
                    .collect(Collectors.toMap(EnvFunction.Builtin::getName, f -> f))
    );

    private GraphCanvas canvas = new GraphCanvas();
    private GraphSetDialog dialog = new GraphSetDialog(this, canvas.getGraphSet(), mathContext);

    private Graph graph = null;


    public MainWindow() {
        super("Googol");

        var cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.add(BorderLayout.CENTER, canvas);

        pack();

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        canvas.getGraphSet().addGraph(new CompiledGraph("", new EmptyGraph()));

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_OPENED) {
            dialog.setLocation(getX() + getWidth(), getY());
            dialog.setVisible(true);
        }

        super.processWindowEvent(e);
    }

    public static void main(String... args) {
        for (var lafInfo : UIManager.getInstalledLookAndFeels()) {
            System.out.printf("%s / %s\n", lafInfo.getName(), lafInfo.getClassName());
        }

        try {
            var clazz = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            clazz.getMethod("setup").invoke(null);
        } catch (ClassNotFoundException ex) {
            System.out.println("FlatLaf not in classpath. Using default look and feel.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        var window = new MainWindow();
        window.setVisible(true);
    }
}
