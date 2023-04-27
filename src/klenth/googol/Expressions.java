package klenth.googol;

import edu.westminstercollege.cs.jade.SyntaxException;
import edu.westminstercollege.cs.jade.assembler.Assembler;
import klenth.googol.ast.AST;
import klenth.googol.ast.Node;
import klenth.googol.math.BinaryFunction;
import klenth.googol.math.EnvFunction;
import klenth.googol.math.UnaryFunction;
import klenth.googol.parse.ExpressionLexer;
import klenth.googol.parse.ExpressionParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import static klenth.googol.ast.Node.*;
import static klenth.googol.ast.Node.Number;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.*;

public final class Expressions {

    private static Random random = new Random();

    private Expressions() {
        throw new IllegalStateException();
    }

    public static double evaluate(Node.Expr expression, Map<String, Double> variableValues) {
        return switch (expression) {
            case Node.Number(double d) -> d;
            case Node.Variable(String name) -> Optional.ofNullable(variableValues.get(name)).orElseThrow();
            case Node.Negate(Node.Expr e) -> -evaluate(e, variableValues);
            case Node.AbsoluteValue(var e) -> Math.abs(evaluate(e, variableValues));
            case Node.BinaryOp bop -> {
                double l = evaluate(bop.left(), variableValues),
                        r = evaluate(bop.right(), variableValues);
                yield switch (bop) {
                    case Node.Add a -> l + r;
                    case Node.Subtract s -> l - r;
                    case Node.Multiply m -> l * r;
                    case Node.Divide m -> l / r;
                    case Node.Power p -> Math.pow(l, r);
                };
            }
            case Node.FunctionCall(String name, List<Node.Expr> args) -> {
                if (args.size() != 1)
                    throw new RuntimeException("Wrong number of arguments to function " + name + ": " + args.size());
                double a = evaluate(args.get(0), variableValues);
                yield switch (name) {
                    case "sqrt", "√" -> Math.sqrt(a);
                    case "exp" -> Math.exp(a);
                    case "sin" -> Math.sin(a);
                    case "sin²" -> Math.pow(Math.sin(a), 2);
                    case "cos" -> Math.cos(a);
                    case "tan" -> Math.tan(a);
                    case "floor" -> Math.floor(a);
                    case "ceil" -> Math.ceil(a);
                    case "round" -> Math.round(a);
                    default -> throw new RuntimeException();
                };
            }
        };
    }

    public static boolean isIndependentOf(Node.Expr expr, String variableName) {
        return AST.traversePostOrder(expr)
                .filter(n -> n instanceof Node.Variable v && v.name().equals(variableName))
                .findAny().isEmpty();
    }

    static ExpressionParser parser(String input, Iterable<String> variableNames, Iterable<String> functionNames) {
        var lexer = new ExpressionLexer(CharStreams.fromString(input));
        variableNames.forEach(lexer::registerVariable);
        functionNames.forEach(lexer::registerFunction);
        return new ExpressionParser(new CommonTokenStream(lexer));
    }

    public static UnaryFunction compileUnaryFunction(String expr, String variableName, Map<String, Double> constantValues, Map<String, EnvFunction> envFunctions) throws SyntaxException {
        try {
            var variableNames = new ArrayList<String>();
            variableNames.add(variableName);
            variableNames.addAll(constantValues.keySet());
            var functionNames = envFunctions.keySet();

            var parser = parser(expr, variableNames, functionNames);
            var e = parser.totalExpr().n;

            return compileUnaryFunction(e, variableName, constantValues, envFunctions);
        } catch (Exception ex) {
            throw new SyntaxException(ex);
        }
    }

    public static BinaryFunction compileBinaryFunction(String expr, String variable1Name, String variable2Name, Map<String, Double> constantValues, Map<String, EnvFunction> envFunctions) throws SyntaxException {
        try {
            var variableNames = new ArrayList<String>();
            variableNames.add(variable1Name);
            variableNames.add(variable2Name);
            variableNames.addAll(constantValues.keySet());
            var functionNames = envFunctions.keySet();

            var parser = parser(expr, variableNames, functionNames);
            var e = parser.totalExpr().n;

            return compileBinaryFunction(e, variable1Name, variable2Name, constantValues, envFunctions);
        } catch (Exception ex) {
            throw new SyntaxException(ex);
        }
    }

    public static UnaryFunction compileUnaryFunction(Node.Expr expr, String variableName, Map<String, Double> constantValues, Map<String, EnvFunction> envFunctions) {
        var stringWriter = new StringWriter(1024);
        var out = new PrintWriter(stringWriter);

        String className = String.format("unary_function_%016x", random.nextLong());

        out.printf(".class public %s\n", className);
        out.println(".super java/lang/Object");
        out.printf(".implements klenth/googol/math/UnaryFunction\n");
        out.println(".method public <init>()V");
        out.println(".code");
        out.println(".limit locals 1");
        out.println(".limit stack 1");
        out.println("aload_0");
        out.println("invokespecial java/lang/Object/<init> ()V");
        out.println("return");
        out.println(".end code");
        out.println();
        out.println(".method public final evaluate (D)D");
        out.println(".code");
        out.printf(".limit locals 3\n");
        out.printf(".limit stack %d\n", maxStackSize(expr));

        generateCode(expr, List.of(variableName), constantValues, envFunctions, out);

        out.println("dreturn");
        out.println(".end code");

        out.flush();
        String asmCode = stringWriter.toString();
        System.out.println(asmCode);

        var asm = new Assembler();
        var bytecode = ByteBuffer.allocate(16_384);
        try {
            asm.assemble(new StringReader(asmCode), bytecode);
            bytecode.limit(bytecode.position());
            bytecode.position(0);

            var clazz = new ClassLoader() {
                @SuppressWarnings("unchecked")
                Class<? extends UnaryFunction> define() {
                    return (Class<? extends UnaryFunction>)super.defineClass(className, bytecode, null);
                }
            }.define();

            return clazz.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static BinaryFunction compileBinaryFunction(Node.Expr expr, String variable1Name, String variable2Name, Map<String, Double> constantValues, Map<String, EnvFunction> envFunctions) {
        var stringWriter = new StringWriter(1024);
        var out = new PrintWriter(stringWriter);

        String className = String.format("binary_function_%016x", random.nextLong());

        out.printf(".class public %s\n", className);
        out.println(".super java/lang/Object");
        out.printf(".implements klenth/googol/math/BinaryFunction\n");
        out.println(".method public <init>()V");
        out.println(".code");
        out.println(".limit locals 1");
        out.println(".limit stack 1");
        out.println("aload_0");
        out.println("invokespecial java/lang/Object/<init> ()V");
        out.println("return");
        out.println(".end code");
        out.println();
        out.println(".method public final evaluate (DD)D");
        out.println(".code");
        out.printf(".limit locals 5\n");
        out.printf(".limit stack %d\n", maxStackSize(expr));

        generateCode(expr, List.of(variable1Name, variable2Name), constantValues, envFunctions, out);

        out.println("dreturn");
        out.println(".end code");

        out.flush();
        String asmCode = stringWriter.toString();
        System.out.println(asmCode);

        var asm = new Assembler();
        var bytecode = ByteBuffer.allocate(16_384);
        try {
            asm.assemble(new StringReader(asmCode), bytecode);
            bytecode.limit(bytecode.position());
            bytecode.position(0);

            var clazz = new ClassLoader() {
                @SuppressWarnings("unchecked")
                Class<? extends BinaryFunction> define() {
                    return (Class<? extends BinaryFunction>)super.defineClass(className, bytecode, null);
                }
            }.define();

            return clazz.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void generateCode(Node.Expr expr, List<String> variableNames, Map<String, Double> constantValues, Map<String, EnvFunction> envFunctions, PrintWriter out) {
        switch (expr) {
            case Number(double value) -> out.printf("ldc2_w %.20g\n", value);

            case Variable(String name) -> {
                int index = variableNames.indexOf(name);
                if (index >= 0)
                    out.printf("dload %d\n", 1 + 2 * index);
                else {
                    Double d = constantValues.get(name);
                    if (d == null)
                        throw new RuntimeException(String.format("Unknown variable: %s", name));
                    out.printf("ldc2_w %.20g\n", d);
                }
            }

            case BinaryOp bop -> {
                generateCode(bop.left(), variableNames, constantValues, envFunctions, out);
                generateCode(bop.right(), variableNames, constantValues, envFunctions, out);

                out.println(switch (bop) {
                    case Add a -> "dadd";
                    case Subtract s -> "dsub";
                    case Multiply m -> "dmul";
                    case Divide d -> "ddiv";
                    case Power p -> "invokestatic java/lang/Math/pow (DD)D";
                });
            }

            case Negate(var e) -> {
                generateCode(e, variableNames, constantValues, envFunctions, out);
                out.println("dneg");
            }

            case AbsoluteValue(var e) -> {
                generateCode(e, variableNames, constantValues, envFunctions, out);
                out.println("invokestatic java/lang/Math/abs (D)D");
            }

            case FunctionCall(String functionName, List<Expr> arguments) -> {
                var envf = envFunctions.get(functionName);
                if (envf == null)
                    throw new RuntimeException(String.format("Unknown environment function: %s", functionName));
                if (!envf.acceptsParameters(arguments.size()))
                    throw new RuntimeException(String.format("Invalid use of %s() with %d arguments", functionName, arguments.size()));

                for (var arg : arguments)
                    generateCode(arg, variableNames, constantValues, envFunctions, out);

                switch (envf) {
                    case EnvFunction.Builtin b -> b.generateCode(out);
                }
            }
        }
    }

    private static int maxStackSize(Node.Expr expr) {
        return switch (expr) {
            case Number n -> 2;
            case Variable v -> 2;
            case BinaryOp bop -> Math.max(maxStackSize(bop.left()), 2 + maxStackSize(bop.right()));
            case Negate n -> 2;
            case AbsoluteValue a -> maxStackSize(a.e());
            case FunctionCall(String functionName, List<Expr> arguments) -> {
                int maxSize = 0;
                for (int i = 0; i < arguments.size(); ++i) {
                    maxSize = Math.max(maxSize, 2 * i + maxStackSize(arguments.get(i)));
                }
                yield maxSize;
            }
        };
    }
}
