package klenth.googol;

import klenth.googol.ast.node.Node;
import klenth.googol.parse.ExpressionLexer;
import klenth.googol.parse.ExpressionParser;
import klenth.googol.parse.Listener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String... args) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.printf("> ");
            String line = in.nextLine();
            if (line.isBlank())
                break;

            var lexer = new ExpressionLexer(CharStreams.fromString(line));
            var parser = new ExpressionParser(new CommonTokenStream(lexer));
            var listener = new Listener();
            parser.addParseListener(listener);

            parser.goal();
            var expr = listener.getExpression();
            print(expr);

            System.out.println();
        }
    }

    public static void print(Node root) {
        print(root, new PrintWriter(System.out));
    }

    public static void print(Node root, PrintWriter out) {
        out.println(root.getDescription());
        var it = root.children().iterator();
        while (it.hasNext())
            print(it.next(), out, List.of(it.hasNext()));
        out.flush();
    }

    private static void print(Node root, PrintWriter out, List<Boolean> levels) {
        for (boolean b : levels.subList(0, levels.size() - 1))
            out.printf("%3s", b ? "│ " : "");
        out.printf("%3s ", levels.get(levels.size() - 1) ? "├─" : "└─");

        String[] lines = root.getDescription().split("\n");
        out.println(lines[0]);
        for (int i = 1; i < lines.length; ++i) {
            for (boolean b: levels)
                out.printf("%3s", b ? "│ " : "");
            out.printf(" %s\n", lines[i]);
        }

        var it = root.children().iterator();
        while (it.hasNext()) {
            var childLevels = new ArrayList<>(levels);
            var child = it.next();
            childLevels.add(it.hasNext());
            print(child, out, childLevels);
        }
    }

}
