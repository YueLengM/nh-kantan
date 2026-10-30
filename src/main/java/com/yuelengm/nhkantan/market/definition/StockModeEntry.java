package com.yuelengm.nhkantan.market.definition;

/**
 * A mode referenced by a stock, with the stock-specific selection weight and switch probability.
 */
public class StockModeEntry {

    private final ModeDefinition mode;
    private final double weight;
    private final double switchProbability;

    public StockModeEntry(ModeDefinition mode, double weight, double switchProbability) {
        this.mode = mode;
        this.weight = weight;
        this.switchProbability = switchProbability;
    }

    public ModeDefinition mode() {
        return mode;
    }

    public String modeId() {
        return mode.id();
    }

    /** Relative chance of being picked when a mode is chosen; entries with weight 0 are never picked. */
    public double weight() {
        return weight;
    }

    /** Probability, per price update, of leaving this mode. */
    public double switchProbability() {
        return switchProbability;
    }
}
