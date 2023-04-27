package klenth.googol.math;

import java.io.PrintWriter;

public sealed interface EnvFunction {

    boolean acceptsParameters(int count);
    String getName();

    enum Builtin implements EnvFunction {

        Floor, Ceil, Round,
        Abs, Signum,
        Sqrt, Exp,
        Sin, Cos, Tan,
        Asin, Acos, Atan;

        @Override
        public boolean acceptsParameters(int count) {
            return switch (this) {
                default -> count == 1;
            };
        }

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }

        public void generateCode(PrintWriter out) {
            switch (this) {
                case Round -> {
                    out.println("invokestatic java/lang/Math/round (D)J");
                    out.println("l2d");
                }
                default -> out.printf("invokestatic java/lang/Math/%s (%s)D\n",
                        getName(), String.join("", "D".repeat(1)));
            }
        }
    }
}
