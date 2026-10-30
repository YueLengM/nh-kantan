package com.yuelengm.nhkantan.market.random;

/**
 * Source of uniformly distributed random numbers used by the price engine and mode formulas.
 */
public interface RandomSource {

    /**
     * @return a uniformly distributed value in {@code [0, 1)}
     */
    double nextDouble();
}
