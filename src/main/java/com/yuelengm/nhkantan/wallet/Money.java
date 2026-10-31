package com.yuelengm.nhkantan.wallet;

/**
 * Rounding of fractional currency amounts to whole units. A small tolerance absorbs floating point noise, so that for
 * example {@code 100 x 1.1} costs 110 and not 111.
 */
public class Money {

    private static final double EPSILON = 1.0E-9D;

    private Money() {}

    /** Rounds down; used for amounts credited to the player. */
    public static long floor(double amount) {
        return (long) Math.floor(amount + EPSILON);
    }

    /** Rounds up; used for amounts charged to the player. */
    public static long ceil(double amount) {
        return (long) Math.ceil(amount - EPSILON);
    }
}
