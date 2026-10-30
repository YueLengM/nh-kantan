package com.yuelengm.nhkantan.market;

/**
 * Fixed-capacity ring buffer of prices, oldest first. Used by the server state and the client view alike.
 */
public class PriceHistory {

    private double[] buffer;
    private int start;
    private int size;

    public PriceHistory(int capacity) {
        this.buffer = new double[Math.max(1, capacity)];
    }

    public int capacity() {
        return buffer.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Appends a price, dropping the oldest one when full. */
    public void add(double price) {
        if (size < buffer.length) {
            buffer[(start + size) % buffer.length] = price;
            size++;
        } else {
            buffer[start] = price;
            start = (start + 1) % buffer.length;
        }
    }

    /**
     * @param index 0 is the oldest retained price
     */
    public double get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of " + size);
        }
        return buffer[(start + index) % buffer.length];
    }

    public double latest() {
        return get(size - 1);
    }

    public double oldest() {
        return get(0);
    }

    public double[] toArray() {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) result[i] = get(i);
        return result;
    }

    /** Replaces the content with the given prices; only the newest {@link #capacity()} are kept. */
    public void setAll(double[] prices) {
        clear();
        for (double price : prices) add(price);
    }

    /** Changes the capacity, keeping the newest prices. */
    public void resize(int capacity) {
        int newCapacity = Math.max(1, capacity);
        if (newCapacity == buffer.length) return;
        double[] prices = toArray();
        buffer = new double[newCapacity];
        setAll(prices);
    }

    public void clear() {
        start = 0;
        size = 0;
    }
}
