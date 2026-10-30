package com.yuelengm.nhkantan.market.view;

import com.yuelengm.nhkantan.market.PriceHistory;
import com.yuelengm.nhkantan.market.StockSnapshot;

/**
 * Client copy of a stock's public data, updated by snapshots and price ticks.
 */
public class StockView {

    private final String id;
    private final String name;
    private final double lowerBound;
    private final double upperBound;
    private final PriceHistory history;
    private long sequence;

    public StockView(StockSnapshot snapshot, int historyCapacity) {
        this.id = snapshot.id();
        this.name = snapshot.name();
        this.lowerBound = snapshot.lowerBound();
        this.upperBound = snapshot.upperBound();
        this.history = new PriceHistory(historyCapacity);
        this.history.setAll(snapshot.history());
        this.sequence = snapshot.sequence();
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

    /** Recent prices, oldest first. Read only; never empty. */
    public PriceHistory history() {
        return history;
    }

    public long sequence() {
        return sequence;
    }

    public double price() {
        return history.latest();
    }

    /** Relative change over the visible history, e.g. {@code 0.05} for +5%. */
    public double historyChange() {
        return history.latest() / history.oldest() - 1.0D;
    }

    /** Appends a newer price; stale or duplicate ticks are ignored. */
    public void applyTick(long tickSequence, double price) {
        if (tickSequence <= sequence) return;
        sequence = tickSequence;
        history.add(price);
    }
}
