package com.yuelengm.nhkantan.market;

/**
 * Public view of a stock sent to clients: display data, current price and the visible candles only. Older candles,
 * mode, formulas and random state never leave the server.
 */
public class StockSnapshot {

    private final String id;
    private final String name;
    private final double lowerBound;
    private final double upperBound;
    private final double price;
    private final Candle[] candles;

    /**
     * @param candles visible candles, oldest first; may be empty for a stock that has not been updated yet
     */
    public StockSnapshot(String id, String name, double lowerBound, double upperBound, double price, Candle[] candles) {
        this.id = id;
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.price = price;
        this.candles = candles;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public double lowerBound() {
        return lowerBound;
    }

    public double upperBound() {
        return upperBound;
    }

    public double price() {
        return price;
    }

    public Candle[] candles() {
        return candles;
    }
}
