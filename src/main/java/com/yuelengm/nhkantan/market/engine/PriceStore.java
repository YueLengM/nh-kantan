package com.yuelengm.nhkantan.market.engine;

/**
 * Persistent, append-only storage of the full price history of each stock. It is the only persisted market data: the
 * newest stored price is where a stock resumes after a restart. Server side only.
 */
public interface PriceStore {

    /**
     * Restores a consistent history after an unclean shutdown, e.g. by dropping a partially written trailing record.
     * Must be called before the first {@link #append} of a stock.
     */
    void repair(String stockId);

    void append(String stockId, float price);

    /**
     * @return up to {@code count} newest stored prices, oldest first
     */
    float[] readTail(String stockId, int count);
}
