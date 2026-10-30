package com.yuelengm.nhkantan.market;

/**
 * One immutable K-line candle: open/high/low/close of the actual price over a fixed number of price updates.
 */
public class Candle {

    private final long index;
    private final double open;
    private final double high;
    private final double low;
    private final double close;

    /**
     * @param index position of the candle in the stock's full candle history, starting at 0
     */
    public Candle(long index, double open, double high, double low, double close) {
        this.index = index;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    /** A new candle that has not seen any update yet: all four prices equal the opening price. */
    public static Candle open(long index, double open) {
        return new Candle(index, open, open, open, open);
    }

    /** This candle extended by a new price, which becomes the close. */
    public Candle withPrice(double price) {
        return new Candle(index, open, Math.max(high, price), Math.min(low, price), price);
    }

    public long index() {
        return index;
    }

    public double open() {
        return open;
    }

    public double high() {
        return high;
    }

    public double low() {
        return low;
    }

    public double close() {
        return close;
    }

    public boolean isRising() {
        return close >= open;
    }
}
