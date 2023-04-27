package klenth.googol;

import edu.westminstercollege.cs.jade.assembler.Assembler;
import klenth.googol.ast.Node;
import klenth.googol.math.BinaryFunction;
import klenth.googol.parse.ExpressionLexer;
import klenth.googol.parse.ExpressionParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static Random random = new Random();

    private static class Timer {
        private long time;

        {
            mark();
        }

        void mark() {
            time = System.currentTimeMillis();
        }

        double elapsed() {
            return (System.currentTimeMillis() - time) / 1000.0;
        }
    }

    public static void main(String... args) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.printf("> ");
            String line = in.nextLine();
            if (line.isBlank())
                break;

            var lexer = new ExpressionLexer(CharStreams.fromString(line));
            var parser = new ExpressionParser(new CommonTokenStream(lexer));

            var expr = parser.totalExpr().n;
            print(expr);

            final int NUM_TRIALS = 100_000_000;
            double[] data = new double[NUM_TRIALS],
                    data2 = new double[NUM_TRIALS];

            Timer t = new Timer();
            for (int i = 0; i < NUM_TRIALS; ++i) {
                double x = ((double)i) / NUM_TRIALS;
                double y = (NUM_TRIALS - ((double)i)) / NUM_TRIALS;
                data[i] = evaluate(x, y, expr);
            }
            double evaluateTime = t.elapsed();

            System.out.printf("Using evaluate(): %.3gs\n", evaluateTime);

            t.mark();
            var className = String.format("ExprClass_%16x", random.nextLong());
            var asmCode = generateCode(expr, className);
            System.out.printf("--\n%s\n--\n", asmCode);
            try {
                Assembler assembler = new Assembler();
                var buffer = ByteBuffer.allocate(4096);
                assembler.assemble(new StringReader(asmCode), buffer);
                buffer.limit(buffer.position());
                buffer.position(0);

                Class<?> exprClass = loadClass(buffer);
                BinaryFunction f = (BinaryFunction)exprClass.getConstructor().newInstance();
                //var method = exprClass.getMethod("evaluate", Double.TYPE, Double.TYPE);

                t.mark();
                for (int i = 0; i < NUM_TRIALS; ++i) {
                    double x = ((double)i) / NUM_TRIALS;
                    double y = (NUM_TRIALS - ((double)i)) / NUM_TRIALS;
                    //double z = (Double)method.invoke(null, (Double)x, (Double)y);
                    data2[i] = f.evaluate(x, y);
                }

                double nativeTime = t.elapsed();
                System.out.printf("Using native compilation: %.3gs\n", nativeTime);

                int differentCount = 0;
                for (int i = 0; i < NUM_TRIALS; ++i)
                    if (data[i] != data2[i])
                        ++differentCount;
                System.out.printf("%d results differ.\n", differentCount);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static Class<?> loadClass(ByteBuffer bytecode) {
        return new ClassLoader() {
            Class<?> load() {
                return super.defineClass(null, bytecode, null);
            }
        }.load();
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

    private static String generateCode(Node.Expr expr, String className) {
        var sb = new StringBuilder(1024);
        sb.append(String.format(".class public %s\n", className));
        sb.append(".super klenth/googol/Function\n");
        sb.append("""
                .method public <init> ()V
                .code
                    .limit locals 1
                    .limit stack 1
                    aload_0
                    invokespecial klenth/googol/Function/<init> ()V
                    return
                .end code
                """);
        sb.append(".method public evaluate (DD)D\n");
        sb.append(".code\n");
        sb.append(".limit locals 5\n");
        sb.append(".limit stack 100\n");

        generateCode(expr, sb);
        sb.append("dreturn\n");

        sb.append(".end code\n");

        return sb.toString();
    }

    private static void generateCode(Node.Expr expr, StringBuilder sb) {
        switch (expr) {
            case Node.Number(double x) -> sb.append(String.format("ldc2_w %f\n", x));

            case Node.Variable(String name) -> {
                if (name.equals("x"))
                    sb.append("dload_1\n");
                else if (name.equals("y"))
                    sb.append("dload_3\n");
                else if (name.equals("pi") || name.equals("π"))
                    sb.append("getstatic java/lang/Math/PI D\n");
                else if (name.equals("e"))
                    sb.append("getstatic java/lang/Math/E D\n");
                else
                    throw new RuntimeException("Unknown variable: " + name);
            }

            case Node.BinaryOp bop -> {
                generateCode(bop.left(), sb);
                generateCode(bop.right(), sb);
                sb.append(switch (bop) {
                    case Node.Add a -> "dadd\n";
                    case Node.Subtract s -> "dsub\n";
                    case Node.Multiply m -> "dmul\n";
                    case Node.Divide d -> "ddiv\n";
                    case Node.Power p -> "invokestatic java/lang/Math/pow (DD)D\n";
                });
            }

            case Node.Negate(var e) -> {
                generateCode(e, sb);
                sb.append("dneg\n");
            }

            case Node.AbsoluteValue(var e) -> {
                generateCode(e, sb);
                sb.append("invokestatic java/lang/Math/abs (D)D\n");
            }

            case Node.FunctionCall(String name, var args) -> {
                checkArgs(name, args);
                for (var arg : args)
                    generateCode(arg, sb);
                sb.append(switch (name) {
                    case "sqrt" -> "invokestatic java/lang/Math/sqrt (D)D\n";
                    case "exp" -> "invokestatic java/lang/Math/exp (D)D\n";
                    default -> throw new RuntimeException("Unknown method: " + name);
                });
            }
        }
    }

    private static void checkArgs(String functionName, List<Node.Expr> arguments) {
        if (!List.of("sqrt", "exp").contains(functionName))
            throw new RuntimeException("Unknown function: " + functionName);
        if (arguments.size() != 1)
            throw new RuntimeException("Function " + functionName + " should have one argument");
    }

    private static double evaluate(double x, double y, Node.Expr expr) {
        return switch (expr) {
            case Node.Number(double d) -> d;
            case Node.Variable(String name) -> switch (name) {
                case "x" -> x;
                case "y" -> y;
                case "pi", "π" -> Math.PI;
                case "e" -> Math.E;
                default -> throw new RuntimeException();
            };
            case Node.Negate(Node.Expr e) -> -evaluate(x, y, e);
            case Node.AbsoluteValue(Node.Expr e) -> Math.abs(evaluate(x, y, e));
            case Node.BinaryOp bop -> {
                double l = evaluate(x, y, bop.left()),
                        r = evaluate(x, y, bop.right());
                yield switch (bop) {
                    case Node.Add a -> l + r;
                    case Node.Subtract s -> l - r;
                    case Node.Multiply m -> l * r;
                    case Node.Divide m -> l / r;
                    case Node.Power p -> Math.pow(l, r);
                };
            }
            case Node.FunctionCall(String name, List<Node.Expr> args) -> {
                checkArgs(name, args);
                double a = evaluate(x, y, args.get(0));
                yield switch (name) {
                    case "sqrt" -> Math.sqrt(a);
                    case "exp" -> Math.exp(a);
                    default -> throw new RuntimeException();
                };
            }
        };
    }
}
