package com.yuelengm.nhkantan.market;

/**
 * Public view of a stock sent to clients: display data and recent prices only. Mode, formulas and random state are
 * never part of a snapshot.
 */
public class StockSnapshot {

    private final String id;
    private final String name;
    private final double lowerBound;
    private final double upperBound;
    private final long sequence;
    private final double[] history;

    /**
     * @param sequence update counter of the newest price in {@code history}
     * @param history  recent prices, oldest first; never empty
     */
    public StockSnapshot(String id, String name, double lowerBound, double upperBound, long sequence,
        double[] history) {
        this.id = id;
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.sequence = sequence;
        this.history = history;
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

    public long sequence() {
        return sequence;
    }

    public double[] history() {
        return history;
    }
}
