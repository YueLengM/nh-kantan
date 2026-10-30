package com.yuelengm.nhkantan.market;

/**
 * A single new price of a stock, as broadcast to clients after each market update.
 */
public class PriceTick {

    private final String stockId;
    private final long sequence;
    private final double price;

    public PriceTick(String stockId, long sequence, double price) {
        this.stockId = stockId;
        this.sequence = sequence;
        this.price = price;
    }

    public String stockId() {
        return stockId;
    }

    /** Update counter of the stock; consecutive ticks of one stock differ by exactly 1. */
    public long sequence() {
        return sequence;
    }

    public double price() {
        return price;
    }
}
