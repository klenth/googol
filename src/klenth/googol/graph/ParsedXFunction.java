package klenth.googol.graph;

import edu.westminstercollege.cs.jade.SyntaxException;
import klenth.googol.Expressions;
import klenth.googol.ast.Node;
import klenth.googol.parse.ExpressionLexer;
import klenth.googol.parse.ExpressionParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

import java.util.Map;

public class ParsedXFunction implements XFunction {

    private Node.Expr expr;

    public ParsedXFunction(Node.Expr expr) {
        this.expr = expr;
    }

    public static ParsedXFunction from(String text) throws SyntaxException {
        try {
            var lexer = new ExpressionLexer(CharStreams.fromString(text));
            var parser = new ExpressionParser(new CommonTokenStream(lexer));
            var expr = parser.totalExpr().n;

            return new ParsedXFunction(expr);
        } catch (RecognitionException ex) {
            throw new SyntaxException(ex);
        }
    }

    @Override
    public double evaluate(double x) {
        return Expressions.evaluate(expr, Map.of("x", x));
    }
}
