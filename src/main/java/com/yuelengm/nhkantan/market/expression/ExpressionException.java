package com.yuelengm.nhkantan.market.expression;

/**
 * Thrown when a formula cannot be tokenized, parsed or resolved.
 */
public class ExpressionException extends RuntimeException {

    public ExpressionException(String message) {
        super(message);
    }
}
