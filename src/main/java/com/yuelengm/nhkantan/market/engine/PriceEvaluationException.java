package com.yuelengm.nhkantan.market.engine;

/**
 * Thrown when a mode formula or variable update produces a value the engine cannot use.
 */
public class PriceEvaluationException extends RuntimeException {

    public PriceEvaluationException(String message) {
        super(message);
    }
}
