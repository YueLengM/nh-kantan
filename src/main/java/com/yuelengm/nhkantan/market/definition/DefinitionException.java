package com.yuelengm.nhkantan.market.definition;

/**
 * Thrown when a market definition file is missing, malformed or fails validation.
 */
public class DefinitionException extends RuntimeException {

    public DefinitionException(String message) {
        super(message);
    }

    public DefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
