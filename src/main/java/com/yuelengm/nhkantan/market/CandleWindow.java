package com.yuelengm.nhkantan.market;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * The visible part of a candle history: at most {@link #capacity()} candles, oldest first. The newest candle may still
 * be forming; {@link #upsert(Candle)} replaces it in place until a candle with a higher index arrives.
 */
public class CandleWindow {

    private final Candle[] ring;
    private int start;
    private int size;

    public CandleWindow(int capacity) {
        this.ring = new Candle[Math.max(1, capacity)];
    }

    public int capacity() {
        return ring.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @param index 0 is the oldest visible candle
     */
    public Candle get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of " + size);
        }
        return ring[(start + index) % ring.length];
    }

    @Nullable
    public Candle oldest() {
        return size == 0 ? null : get(0);
    }

    @Nullable
    public Candle latest() {
        return size == 0 ? null : get(size - 1);
    }

    /**
     * Replaces the newest candle if it has the same index, appends the candle if it is newer, and ignores it if it is
     * older. Appending drops the oldest candle once the window is full.
     */
    public void upsert(Candle candle) {
        Candle latest = latest();
        if (latest != null) {
            if (candle.index() == latest.index()) {
                ring[(start + size - 1) % ring.length] = candle;
                return;
            }
            if (candle.index() < latest.index()) return;
        }
        if (size < ring.length) {
            ring[(start + size) % ring.length] = candle;
            size++;
        } else {
            ring[start] = candle;
            start = (start + 1) % ring.length;
        }
    }

    public void upsertAll(Collection<Candle> candles) {
        for (Candle candle : candles) upsert(candle);
    }

    public Candle[] toArray() {
        Candle[] result = new Candle[size];
        for (int i = 0; i < size; i++) result[i] = get(i);
        return result;
    }
}
