package com.yuelengm.nhkantan.market;

/**
 * The new price of a stock after a market update, broadcast to clients.
 */
public class PriceUpdate {

    private final String stockId;
    private final float price;

    public PriceUpdate(String stockId, float price) {
        this.stockId = stockId;
        this.price = price;
    }

    public String stockId() {
        return stockId;
    }

    public float price() {
        return price;
    }
}
