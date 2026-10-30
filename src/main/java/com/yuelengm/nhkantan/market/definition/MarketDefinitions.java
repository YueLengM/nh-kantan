package com.yuelengm.nhkantan.market.definition;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The validated content of {@code modes.json} and {@code stocks.json}. Server side only.
 */
public class MarketDefinitions {

    private final Map<String, ModeDefinition> modes;
    private final List<StockDefinition> stocks;

    public MarketDefinitions(Map<String, ModeDefinition> modes, List<StockDefinition> stocks) {
        this.modes = Collections.unmodifiableMap(modes);
        this.stocks = Collections.unmodifiableList(stocks);
    }

    public static MarketDefinitions empty() {
        return new MarketDefinitions(Collections.emptyMap(), Collections.emptyList());
    }

    public Map<String, ModeDefinition> modes() {
        return modes;
    }

    /** All stocks in file order, including disabled ones. */
    public List<StockDefinition> stocks() {
        return stocks;
    }
}
