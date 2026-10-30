package com.yuelengm.nhkantan.market.engine;

import java.util.List;

import com.yuelengm.nhkantan.market.Candle;

/**
 * Persistent, append-only storage of the full candle history of each stock. Server side only.
 */
public interface CandleStore {

    /** Appends a completed candle; candles of one stock are appended in ascending index order. */
    void append(String stockId, Candle candle);

    /**
     * Drops every stored candle with an index of {@code firstInvalidIndex} or higher. Used on startup to discard
     * candles written after the last world save, e.g. after a crash.
     */
    void truncateFrom(String stockId, long firstInvalidIndex);

    /**
     * @return up to {@code count} newest stored candles, oldest first
     */
    List<Candle> readTail(String stockId, int count);
}
