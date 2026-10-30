package com.yuelengm.nhkantan.market.definition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.yuelengm.nhkantan.market.expression.VariableScope;

/**
 * Variables provided by the engine to every mode formula. They always occupy the first slots of a mode scope,
 * followed by the mode's own variables.
 */
public class SystemVariables {

    /** Calculation index within the current mode, starting at 1. */
    public static final int T = 0;
    /** Market price when the current mode was entered; constant while the mode runs. */
    public static final int START_PRICE = 1;
    /** Previous calculated price, before the hard bounds were applied. */
    public static final int PREV_CALC_PRICE = 2;
    /** Previous actual market price, after the hard bounds were applied. */
    public static final int PREV_PRICE = 3;
    /** Previous change rate between two calculated prices. */
    public static final int PREV_CALC_RATE = 4;
    /** Previous change rate between two actual prices. */
    public static final int PREV_RATE = 5;

    public static final List<String> NAMES = Collections
        .unmodifiableList(Arrays.asList("t", "startPrice", "prevCalcPrice", "prevPrice", "prevCalcRate", "prevRate"));

    public static final int COUNT = 6;

    private SystemVariables() {}

    /** Creates a scope with all system variables declared in slot order. */
    public static VariableScope newScope() {
        VariableScope scope = new VariableScope();
        for (String name : NAMES) scope.declare(name);
        return scope;
    }
}
