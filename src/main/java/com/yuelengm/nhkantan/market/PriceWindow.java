package com.yuelengm.nhkantan.market;

/**
 * Fixed-capacity ring of the most recent prices, oldest first. Adding to a full window drops the oldest price.
 */
public class PriceWindow {

    private final float[] ring;
    private int start;
    private int size;

    public PriceWindow(int capacity) {
        this.ring = new float[Math.max(1, capacity)];
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
     * @param index 0 is the oldest retained price
     */
    public float get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of " + size);
        }
        return ring[(start + index) % ring.length];
    }

    public float latest() {
        return get(size - 1);
    }

    public void add(float price) {
        if (size < ring.length) {
            ring[(start + size) % ring.length] = price;
            size++;
        } else {
            ring[start] = price;
            start = (start + 1) % ring.length;
        }
    }

    public void addAll(float[] prices) {
        for (float price : prices) add(price);
    }

    public float[] toArray() {
        float[] result = new float[size];
        for (int i = 0; i < size; i++) result[i] = get(i);
        return result;
    }
}
