package com.yuelengm.nhkantan.market.engine;

/**
 * In-memory runtime state of one stock. Nothing here is persisted: after a restart the stock continues from its last
 * stored price and enters a freshly chosen mode.
 */
public class StockState {

    private static final String[] NO_NAMES = new String[0];
    private static final double[] NO_VALUES = new double[0];

    private final String stockId;

    private double price;

    private String modeId = "";
    private long t;
    private double startPrice;
    private double prevCalcPrice;
    private double prevPrice;
    private double prevCalcRate;
    private double prevRate;
    private String[] variableNames = NO_NAMES;
    private double[] variableValues = NO_VALUES;

    public StockState(String stockId, double price) {
        this.stockId = stockId;
        this.price = price;
    }

    public String stockId() {
        return stockId;
    }

    public double price() {
        return price;
    }

    public String modeId() {
        return modeId;
    }

    public long t() {
        return t;
    }

    public double startPrice() {
        return startPrice;
    }

    public double prevCalcPrice() {
        return prevCalcPrice;
    }

    public double prevPrice() {
        return prevPrice;
    }

    public double prevCalcRate() {
        return prevCalcRate;
    }

    public double prevRate() {
        return prevRate;
    }

    public String[] variableNames() {
        return variableNames;
    }

    public double[] variableValues() {
        return variableValues;
    }

    /** Resets the mode context as required when a mode is entered. */
    public void enterMode(String modeId, String[] variableNames, double[] variableValues) {
        this.modeId = modeId;
        this.t = 1;
        this.startPrice = price;
        this.prevCalcPrice = price;
        this.prevPrice = price;
        this.prevCalcRate = 0.0D;
        this.prevRate = 0.0D;
        this.variableNames = variableNames;
        this.variableValues = variableValues;
    }

    /** Commits the result of one price step. */
    public void applyStep(double calculatedPrice, double actualPrice, double[] newVariableValues) {
        this.prevCalcRate = calculatedPrice / prevCalcPrice - 1.0D;
        this.prevRate = actualPrice / prevPrice - 1.0D;
        this.prevCalcPrice = calculatedPrice;
        this.prevPrice = actualPrice;
        this.price = actualPrice;
        this.variableValues = newVariableValues;
        this.t++;
    }
}
