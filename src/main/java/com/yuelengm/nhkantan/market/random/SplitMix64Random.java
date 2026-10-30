package com.yuelengm.nhkantan.market.random;

/**
 * SplitMix64 generator. Its whole state is a single {@code long}, which makes it trivial to persist so a stock
 * continues the exact same random sequence after a server restart.
 */
public class SplitMix64Random implements RandomSource {

    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;
    private static final double DOUBLE_UNIT = 0x1.0p-53;

    private long state;

    public SplitMix64Random(long state) {
        this.state = state;
    }

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    public long nextLong() {
        state += GOLDEN_GAMMA;
        long z = state;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    @Override
    public double nextDouble() {
        return (nextLong() >>> 11) * DOUBLE_UNIT;
    }
}
