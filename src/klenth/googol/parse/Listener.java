package klenth.googol.parse;

import klenth.googol.ast.node.Node;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Listener extends ExpressionBaseListener {

    private Deque<Node.Expr> stack = new LinkedList<>();

    public Node.Expr getExpression() {
        if (stack.size() != 1)
            throw new IllegalStateException("Stack size = " + stack.size());
        return stack.peek();
    }

    @Override
    public void exitVariable(ExpressionParser.VariableContext ctx) {
        stack.push(new Node.Variable(ctx.ID().getText()));
    }

    @Override
    public void exitVariable2(ExpressionParser.Variable2Context ctx) {
        stack.push(new Node.Variable(ctx.ID().getText()));
    }

    @Override
    public void exitLiteral(ExpressionParser.LiteralContext ctx) {
        stack.push(new Node.Number(Double.parseDouble(ctx.NUMBER().getText())));
    }

    @Override
    public void exitLiteral2(ExpressionParser.Literal2Context ctx) {
        stack.push(new Node.Number(Double.parseDouble(ctx.NUMBER().getText())));
    }

    @Override
    public void exitProduct(ExpressionParser.ProductContext ctx) {
        var r = stack.pop();
        var l = stack.pop();

        if (ctx.op.getText().equals("/"))
            stack.push(new Node.Divide(l, r));
        else
            stack.push(new Node.Multiply(l, r));
    }

    @Override
    public void exitJuxtaposition(ExpressionParser.JuxtapositionContext ctx) {
        var r = stack.pop();
        var l = stack.pop();
        stack.push(new Node.Multiply(l, r));
    }

    @Override
    public void exitProduct2(ExpressionParser.Product2Context ctx) {
        var r = stack.pop();
        var l = stack.pop();

        if (ctx.op.getText().equals("/"))
            stack.push(new Node.Divide(l, r));
        else
            stack.push(new Node.Multiply(l, r));
    }

    @Override
    public void exitSum(ExpressionParser.SumContext ctx) {
        var r = stack.pop();
        var l = stack.pop();

        if (ctx.op.getText().equals("-"))
            stack.push(new Node.Subtract(l, r));
        else
            stack.push(new Node.Add(l, r));
    }

    @Override
    public void exitFunctionCall(ExpressionParser.FunctionCallContext ctx) {
        exitFunctionCall(ctx.ID().getText());
    }

    @Override
    public void exitFunctionCall2(ExpressionParser.FunctionCall2Context ctx) {
        exitFunctionCall(ctx.ID().getText());
    }

    private void exitFunctionCall(String functionName) {
        List<Node.Expr> args = new ArrayList<>(stack.size());
        while (true) {
            var arg = stack.pop();
            if (arg == null)
                break;
            args.add(0, arg);
        }

        stack.push(new Node.FunctionCall(functionName, args));
    }

    @Override
    public void exitNegate(ExpressionParser.NegateContext ctx) {
        if (ctx.op.getText().equals("-"))
            stack.push(new Node.Negate(stack.pop()));
    }

    @Override
    public void exitSuperscript(ExpressionParser.SuperscriptContext ctx) {
        int power = parseSuperscript(ctx.SUPERSCRIPT().getText());
        stack.push(new Node.Power(stack.pop(), new Node.Number(power)));
    }

    @Override
    public void exitSuperscript2(ExpressionParser.Superscript2Context ctx) {
        int power = parseSuperscript(ctx.SUPERSCRIPT().getText());
        stack.push(new Node.Power(stack.pop(), new Node.Number(power)));
    }

    @Override
    public void exitPower(ExpressionParser.PowerContext ctx) {
        Node.Expr r = stack.pop(), l = stack.pop();
        stack.push(new Node.Power(l, r));
    }

    @Override
    public void exitPower2(ExpressionParser.Power2Context ctx) {
        Node.Expr r = stack.pop(), l = stack.pop();
        stack.push(new Node.Power(l, r));
    }

    @Override
    public void enterArguments(ExpressionParser.ArgumentsContext ctx) {
        stack.push(null); // marker for end of arguments
    }

    private int parseSuperscript(String chars) {
        int total = 0;
        boolean negative = false;

        if (chars.startsWith("⁻")) {
            negative = true;
            chars = chars.substring(1);
        }

        for (int i = 0; i < chars.length(); ++i) {
            char c = chars.charAt(i);
            int charValue = switch (c) {
                case '⁰' -> 0;
                case '¹' -> 1;
                case '²' -> 2;
                case '³' -> 3;
                case '⁴' -> 4;
                case '⁵' -> 5;
                case '⁶' -> 6;
                case '⁷' -> 7;
                case '⁸' -> 8;
                case '⁹' -> 9;
                default -> throw new RuntimeException("Internal parser error");
            };
            total = 10 * total + charValue;
        }

        return negative ? -total : total;
    }
}
