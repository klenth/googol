package klenth.googol.ast;

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

    record AbsoluteValue(Expr e) implements Expr {}

    record FunctionCall(String functionName, List<Expr> arguments) implements Expr {
        @Override
        public String getDescription() {
            return String.format("%s()", functionName);
        }
    }

    sealed interface Relation extends Node {
        Expr lhs();
        Expr rhs();
    }
    record Equation(Expr lhs, Expr rhs) implements Relation { }

    record Inequality(Expr lhs, Expr rhs, Type type) implements Relation {
        public enum Type {
            LessThan, GreaterThanEqual, GreaterThan, LessThanEqual, NotEqual;

            public static Type of(String op) {
                return switch (op) {
                    case "<" -> LessThan;
                    case ">=", "≥" -> GreaterThanEqual;
                    case ">" -> GreaterThan;
                    case "<=", "≤" -> LessThanEqual;
                    case "!=", "≠" -> NotEqual;
                    default -> throw new IllegalArgumentException(String.format("Illegal inequality operator: %s", op));
                };
            }
        }
    }

    default List<? extends Node> children() {
        return switch (this) {
            case Variable v -> List.of();
            case Number n -> List.of();
            case BinaryOp op -> List.of(op.left(), op.right());
            case Negate n -> List.of(n.e);
            case AbsoluteValue av -> List.of(av.e);
            case FunctionCall fc -> fc.arguments;
            case Relation r -> List.of(r.lhs(), r.rhs());
        };
    }

    default String getDescription() {
        return getClass().getSimpleName();
    }
}
