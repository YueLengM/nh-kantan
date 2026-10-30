package com.yuelengm.nhkantan.market.view;

/**
 * Formatting of prices and changes for displays and GUIs.
 */
public class PriceFormat {

    private PriceFormat() {}

    /** Decimal places that keep roughly five significant digits for prices of the given magnitude. */
    public static int decimalsFor(double magnitude) {
        double value = Math.abs(magnitude);
        if (value >= 10000.0D) return 0;
        if (value >= 1000.0D) return 1;
        if (value >= 100.0D) return 2;
        if (value >= 10.0D) return 3;
        if (value >= 1.0D) return 4;
        return 5;
    }

    public static String price(double price, int decimals) {
        return String.format("%." + decimals + "f", price);
    }

    public static String price(double price) {
        return price(price, decimalsFor(price));
    }

    /** Signed percentage, e.g. {@code +1.25%} for {@code 0.0125}. */
    public static String change(double ratio) {
        return String.format("%+.2f%%", ratio * 100.0D);
    }
}
