package com.yuelengm.nhkantan.market.view;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.yuelengm.nhkantan.market.CandleUpdate;
import com.yuelengm.nhkantan.market.StockSnapshot;

/**
 * The single client-side copy of market data, shared by every display renderer and terminal GUI.
 * <p>
 * Free of client-only classes so it can be referenced from common code. The stock map is replaced as a whole on
 * snapshots (copy-on-write), so readers never observe a partially rebuilt map.
 */
public class ClientMarketData {

    public static final ClientMarketData INSTANCE = new ClientMarketData();

    private volatile Map<String, StockView> stocks = Collections.emptyMap();
    private volatile int revision;

    private ClientMarketData() {}

    /** Replaces all data with a full snapshot from the server. */
    public void applySnapshot(int visibleCandles, List<StockSnapshot> snapshots) {
        Map<String, StockView> next = new LinkedHashMap<>();
        for (StockSnapshot snapshot : snapshots) {
            next.put(snapshot.id(), new StockView(snapshot, visibleCandles));
        }
        stocks = Collections.unmodifiableMap(next);
        revision++;
    }

    /** Applies the latest candles; updates for unknown stocks are ignored. */
    public void applyUpdates(List<CandleUpdate> updates) {
        Map<String, StockView> current = stocks;
        for (CandleUpdate update : updates) {
            StockView view = current.get(update.stockId());
            if (view != null) view.applyCandle(update.candle());
        }
        revision++;
    }

    public void clear() {
        stocks = Collections.emptyMap();
        revision++;
    }

    /** All stocks in server definition order. */
    public Collection<StockView> stocks() {
        return stocks.values();
    }

    @Nullable
    public StockView get(String stockId) {
        return stocks.get(stockId);
    }

    @Nullable
    public StockView first() {
        Collection<StockView> values = stocks.values();
        return values.isEmpty() ? null
            : values.iterator()
                .next();
    }

    /** Incremented on every change; lets renderers cache derived data. */
    public int revision() {
        return revision;
    }
}
