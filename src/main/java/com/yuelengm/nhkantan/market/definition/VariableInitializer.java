package com.yuelengm.nhkantan.market.definition;

import java.util.Random;

/**
 * Produces the value of a mode variable each time the mode is entered.
 */
@FunctionalInterface
public interface VariableInitializer {

    double initialValue(Random random);

    static VariableInitializer constant(double value) {
        return random -> value;
    }

    static VariableInitializer uniform(double min, double max) {
        return random -> min + random.nextDouble() * (max - min);
    }
}
