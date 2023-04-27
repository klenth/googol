package klenth.googol.math;

import java.util.Map;

public class MathContext {

    private final Map<String, Double> constantValues;
    private final Map<String, EnvFunction> envFunctions;

    public MathContext() {
        this(Map.of(), Map.of());
    }

    public MathContext(Map<String, Double> constantValues, Map<String, EnvFunction> envFunctions) {
        this.constantValues = constantValues;
        this.envFunctions = envFunctions;
    }

    public Map<String, Double> getConstantValues() {
        return constantValues;
    }

    public Map<String, EnvFunction> getEnvFunctions() {
        return envFunctions;
    }
}
