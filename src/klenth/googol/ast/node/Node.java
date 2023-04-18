package klenth.googol.ast.node;

import java.io.PrintWriter;
import java.util.List;

public sealed interface Node {

    sealed interface Expr extends Node {}

    record Variable(String name) implements Expr {
        @Override
        public String getDescription() {
            return String.format(name);
        }
    }
    record Number(double value) implements Expr {
        @Override
        public String getDescription() {
            return String.valueOf(value);
        }
    }

    sealed interface BinaryOp extends Expr {
        Expr left();
        Expr right();
    }

    record Power(Expr left, Expr right) implements BinaryOp {}
    record Multiply(Expr left, Expr right) implements BinaryOp {}
    record Divide(Expr left, Expr right) implements BinaryOp {}
    record Add(Expr left, Expr right) implements BinaryOp {}
    record Subtract(Expr left, Expr right) implements BinaryOp {}

    record Negate(Expr e) implements Expr {}

    record FunctionCall(String functionName, List<Expr> arguments) implements Expr {
        @Override
        public String getDescription() {
            return String.format("%s[]", functionName);
        }
    }

    default List<? extends Node> children() {
        return switch (this) {
            case Variable v -> List.of();
            case Number n -> List.of();
            case BinaryOp op -> List.of(op.left(), op.right());
            case Negate n -> List.of(n.e);
            case FunctionCall fc -> fc.arguments;
        };
    }

    default String getDescription() {
        return getClass().getSimpleName();
    }
}
