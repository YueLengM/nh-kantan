package com.yuelengm.nhkantan.market.view;

import java.util.Arrays;

import com.yuelengm.nhkantan.market.Candle;
import com.yuelengm.nhkantan.market.CandleWindow;
import com.yuelengm.nhkantan.market.StockSnapshot;

/**
 * Client copy of a stock's public data: current price and the visible candles only. Candles that scroll out of the
 * window are dropped; the client never keeps older history.
 */
public class StockView {

    private final String id;
    private final String name;
    private final double lowerBound;
    private final double upperBound;
    private final CandleWindow candles;
    private double price;

    public StockView(StockSnapshot snapshot, int visibleCandles) {
        this.id = snapshot.id();
        this.name = snapshot.name();
        this.lowerBound = snapshot.lowerBound();
        this.upperBound = snapshot.upperBound();
        this.candles = new CandleWindow(visibleCandles);
        this.candles.upsertAll(Arrays.asList(snapshot.candles()));
        this.price = snapshot.price();
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

    /** Visible candles, oldest first; the newest may still be forming. Read only. */
    public CandleWindow candles() {
        return candles;
    }

    public double price() {
        return price;
    }

    /** Relative change since the open of the oldest visible candle, e.g. {@code 0.05} for +5%. */
    public double windowChange() {
        Candle oldest = candles.oldest();
        return oldest == null ? 0.0D : price / oldest.open() - 1.0D;
    }

    public void applyCandle(Candle candle) {
        Candle latest = candles.latest();
        if (latest != null && candle.index() < latest.index()) return;
        candles.upsert(candle);
        price = candle.close();
    }
}
