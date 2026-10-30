package com.yuelengm.nhkantan.market;

/**
 * The latest candle of a stock after a price update; its close is the new price. Broadcast to clients once per market
 * update.
 */
public class CandleUpdate {

    private final String stockId;
    private final Candle candle;

    public CandleUpdate(String stockId, Candle candle) {
        this.stockId = stockId;
        this.candle = candle;
    }

    public String stockId() {
        return stockId;
    }

    public Candle candle() {
        return candle;
    }
}
