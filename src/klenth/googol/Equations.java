package klenth.googol;

import klenth.googol.ast.Node;

import java.util.Optional;

public final class Equations {

    private Equations() {
        throw new IllegalStateException();
    }

    public static Node.Equation parseEquation(String input, Iterable<String> variableNames, Iterable<String> functionNames) {
        return Expressions.parser(input, variableNames, functionNames).totalEquation().n;
    }

    public static Node.Relation parseRelation(String input, Iterable<String> variableNames, Iterable<String> functionNames) {
        return Expressions.parser(input, variableNames, functionNames).totalRelation().n;
    }

    public static Optional<Node.Expr> solvedFor(Node.Equation eqn, String variableName) {
        if (eqn.lhs() instanceof Node.Variable lv
                && lv.name().equals(variableName)
                && Expressions.isIndependentOf(eqn.rhs(), variableName))
            return Optional.of(eqn.rhs());
        else if (eqn.rhs() instanceof Node.Variable rv
                && rv.name().equals(variableName)
                && Expressions.isIndependentOf(eqn.lhs(), variableName))
            return Optional.of(eqn.lhs());
        else
            return Optional.empty();
    }
}
