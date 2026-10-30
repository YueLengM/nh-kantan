package com.yuelengm.nhkantan.market.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Built-in functions and constants of the formula language. Semantics mirror the web configurator.
 */
public class MathFunctions {

    /** Implementation of a deterministic function; {@code random} is handled by the parser separately. */
    @FunctionalInterface
    public interface Function {

        double apply(double[] args);
    }

    /** A function together with its accepted argument count range. */
    public static class Spec {

        private final int minArgs;
        private final int maxArgs;
        private final Function function;

        public Spec(int minArgs, int maxArgs, Function function) {
            this.minArgs = minArgs;
            this.maxArgs = maxArgs;
            this.function = function;
        }

        public boolean accepts(int argCount) {
            return argCount >= minArgs && argCount <= maxArgs;
        }

        public Function function() {
            return function;
        }

        public String arityDescription() {
            if (minArgs == maxArgs) return String.valueOf(minArgs);
            return maxArgs == Integer.MAX_VALUE ? "at least " + minArgs : minArgs + " to " + maxArgs;
        }
    }

    public static final String RANDOM = "random";

    private static final Map<String, Spec> FUNCTIONS;
    private static final Map<String, Double> CONSTANTS;

    static {
        Map<String, Spec> functions = new HashMap<>();
        unary(functions, "sin", Math::sin);
        unary(functions, "cos", Math::cos);
        unary(functions, "tan", Math::tan);
        unary(functions, "asin", Math::asin);
        unary(functions, "acos", Math::acos);
        unary(functions, "atan", Math::atan);
        unary(functions, "sqrt", Math::sqrt);
        unary(functions, "abs", Math::abs);
        unary(functions, "floor", Math::floor);
        unary(functions, "ceil", Math::ceil);
        // Same as Java Math.round: halves round toward positive infinity
        unary(functions, "round", x -> Math.floor(x + 0.5D));
        unary(functions, "exp", Math::exp);
        functions.put("log", new Spec(1, 2, a -> a.length == 1 ? Math.log(a[0]) : Math.log(a[0]) / Math.log(a[1])));
        functions.put("min", new Spec(1, Integer.MAX_VALUE, MathFunctions::min));
        functions.put("max", new Spec(1, Integer.MAX_VALUE, MathFunctions::max));
        functions.put("clamp", new Spec(3, 3, a -> Math.min(Math.max(a[0], a[1]), a[2])));
        FUNCTIONS = Collections.unmodifiableMap(functions);

        Map<String, Double> constants = new HashMap<>();
        constants.put("pi", Math.PI);
        constants.put("e", Math.E);
        CONSTANTS = Collections.unmodifiableMap(constants);
    }

    private MathFunctions() {}

    /**
     * @return the function spec, or {@code null} if no such deterministic function exists
     */
    public static Spec function(String name) {
        return FUNCTIONS.get(name);
    }

    /**
     * @return the constant value, or {@code null} if the name is not a constant
     */
    public static Double constant(String name) {
        return CONSTANTS.get(name);
    }

    public static boolean isConstant(String name) {
        return CONSTANTS.containsKey(name);
    }

    private static void unary(Map<String, Spec> target, String name, DoubleUnaryOperator op) {
        target.put(name, new Spec(1, 1, a -> op.applyAsDouble(a[0])));
    }

    private static double min(double[] args) {
        double result = args[0];
        for (int i = 1; i < args.length; i++) result = Math.min(result, args[i]);
        return result;
    }

    private static double max(double[] args) {
        double result = args[0];
        for (int i = 1; i < args.length; i++) result = Math.max(result, args[i]);
        return result;
    }
}
