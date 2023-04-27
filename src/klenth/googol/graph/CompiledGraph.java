package klenth.googol.graph;

import edu.westminstercollege.cs.jade.SyntaxException;
import klenth.googol.Equations;
import klenth.googol.Expressions;
import klenth.googol.ast.Node;
import klenth.googol.math.MathContext;

import java.util.ArrayList;
import java.util.List;

public record CompiledGraph(String equation, Graph graph) implements Graph {

    public static CompiledGraph compile(String equation, MathContext mathContext) throws SyntaxException {
        var constantValues = mathContext.getConstantValues();
        var envFunctions = mathContext.getEnvFunctions();

        if (equation.isBlank())
            return new CompiledGraph(equation, new EmptyGraph());

        List<String> varNames = new ArrayList<>();
        varNames.add("x");
        varNames.add("y");
        varNames.addAll(constantValues.keySet());

        var relation = Equations.parseRelation(equation, varNames, envFunctions.keySet());
        Graph graph = null;
        if (relation instanceof Node.Equation eqn) {
            var maybeFunc = Equations.solvedFor(eqn, "y");
            if (maybeFunc.isPresent()) {
                var function = Expressions.compileUnaryFunction(maybeFunc.get(), "x", constantValues, envFunctions);
                graph = (XFunction)function::evaluate;
            }

            else {
                var function = Expressions.compileBinaryFunction(new Node.Subtract(relation.lhs(), relation.rhs()), "x", "y", constantValues, envFunctions);
                graph = (TruthPlot) (x, y) -> {
                    return Math.abs(function.evaluate(x, y)) < 1e-8;
                };
            }
        } else if (relation instanceof Node.Inequality ineq) {
            var function = Expressions.compileBinaryFunction(new Node.Subtract(relation.lhs(), relation.rhs()), "x", "y", constantValues, envFunctions);
            graph = switch (ineq.type()) {
                case LessThan -> (TruthPlot) (x, y) -> function.evaluate(x, y) < 0;
                case GreaterThanEqual -> (TruthPlot) (x, y) -> function.evaluate(x, y) >= 0;
                case LessThanEqual -> (TruthPlot) (x, y) -> function.evaluate(x, y) <= 0;
                case GreaterThan -> (TruthPlot) (x, y) -> function.evaluate(x, y) > 0;
                case NotEqual -> (TruthPlot) (x, y) -> Math.abs(function.evaluate(x, y)) >= 1e-8;
            };
        }

        if (graph == null)
            throw new SyntaxException("Unknown equation type");

        return new CompiledGraph(equation, graph);
    }
}
