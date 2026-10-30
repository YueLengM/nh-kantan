package com.yuelengm.nhkantan.market.expression;

import com.yuelengm.nhkantan.market.random.RandomSource;

/**
 * A compiled formula. Variables are resolved to slots at compile time, see {@link VariableScope}.
 */
@FunctionalInterface
public interface Expression {

    /**
     * @param variables variable values indexed by the slots of the scope the expression was compiled against
     * @param random    random source used by {@code random(min, max)}
     * @return the result, which may be non-finite; callers decide how to treat that
     */
    double evaluate(double[] variables, RandomSource random);
}
