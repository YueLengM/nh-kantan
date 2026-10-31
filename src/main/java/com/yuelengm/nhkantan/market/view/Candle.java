package com.yuelengm.nhkantan.market.view;

/**
 * Client-side K-line candle derived from two consecutive prices: it opens at the previous price and closes at the new
 * one. The server never deals with candles.
 */
public class Candle {

    private final float open;
    private final float close;

    public Candle(float open, float close) {
        this.open = open;
        this.close = close;
    }

    public float open() {
        return open;
    }

    public float close() {
        return close;
    }

    public float high() {
        return Math.max(open, close);
    }

    public float low() {
        return Math.min(open, close);
    }

    public boolean isRising() {
        return close >= open;
    }
}
