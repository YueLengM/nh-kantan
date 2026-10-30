package com.yuelengm.nhkantan.market.definition;

/**
 * How the output of a mode formula is turned into the next calculated (pre-clamp) price.
 */
public enum OutputType {

    /** The formula returns a relative change; {@code 0.01} means +1% on the previous calculated price. */
    CHANGE_RATE("changeRate") {

        @Override
        public double toCalculatedPrice(double prevCalcPrice, double output) {
            return prevCalcPrice * (1.0D + output);
        }
    },

    /** The formula returns the calculated price directly. */
    ABSOLUTE_PRICE("absolutePrice") {

        @Override
        public double toCalculatedPrice(double prevCalcPrice, double output) {
            return output;
        }
    };

    private final String jsonName;

    OutputType(String jsonName) {
        this.jsonName = jsonName;
    }

    public String jsonName() {
        return jsonName;
    }

    public abstract double toCalculatedPrice(double prevCalcPrice, double output);

    /**
     * @return the matching type, or {@code null} if the name is unknown
     */
    public static OutputType fromJsonName(String name) {
        for (OutputType type : values()) {
            if (type.jsonName.equals(name)) return type;
        }
        return null;
    }
}
