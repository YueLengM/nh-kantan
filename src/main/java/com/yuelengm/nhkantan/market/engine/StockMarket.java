package com.yuelengm.nhkantan.market.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.market.Candle;
import com.yuelengm.nhkantan.market.CandleUpdate;
import com.yuelengm.nhkantan.market.CandleWindow;
import com.yuelengm.nhkantan.market.StockSnapshot;
import com.yuelengm.nhkantan.market.definition.MarketDefinitions;
import com.yuelengm.nhkantan.market.definition.StockDefinition;

/**
 * The running market: binds stock definitions to their persisted states, advances all enabled stocks and aggregates
 * prices into candles.
 * <p>
 * Completed candles go to the {@link CandleStore}; only the visible window is kept in memory. States of stocks that
 * are disabled or no longer defined are kept untouched, so re-enabling a stock resumes it.
 */
public class StockMarket {

    private final Map<String, StockState> states;
    private final CandleStore candleStore;
    private final List<StockDefinition> activeStocks = new ArrayList<>();
    private final Map<String, CandleWindow> windows = new HashMap<>();
    private final Set<String> failingStocks = new HashSet<>();

    /**
     * @param states         persisted states keyed by stock id; missing entries are created and added
     * @param visibleCandles number of candles kept in memory and sent to clients per stock
     * @param seeds          seed source for newly created stocks
     */
    public StockMarket(MarketDefinitions definitions, Map<String, StockState> states, CandleStore candleStore,
        int visibleCandles, LongSupplier seeds) {
        this.states = states;
        this.candleStore = candleStore;
        for (StockDefinition stock : definitions.stocks()) {
            if (!stock.enabled()) continue;
            StockState state = states.get(stock.id());
            if (state == null) {
                state = createState(stock, seeds.getAsLong());
                states.put(stock.id(), state);
            } else {
                reconcile(stock, state);
            }
            windows.put(stock.id(), loadWindow(state, visibleCandles));
            activeStocks.add(stock);
        }
    }

    private static StockState createState(StockDefinition stock, long seed) {
        StockState state = new StockState(stock.id(), seed);
        state.setPrice(stock.initialPrice());
        PriceEngine.enterRandomMode(stock, state);
        return state;
    }

    /** Adapts a persisted state to a possibly edited definition without resetting its price. */
    private static void reconcile(StockDefinition stock, StockState state) {
        double price = state.price();
        boolean priceValid = !Double.isNaN(price) && !Double.isInfinite(price) && price > 0.0D;
        double adjusted = priceValid ? stock.clamp(price) : stock.initialPrice();
        if (adjusted != price) {
            state.setPrice(adjusted);
            PriceEngine.enterRandomMode(stock, state);
        } else if (!PriceEngine.isModeCompatible(stock, state)) {
            PriceEngine.enterRandomMode(stock, state);
        }
    }

    /** Restores the visible window from storage, discarding candles written after the last world save. */
    private CandleWindow loadWindow(StockState state, int visibleCandles) {
        candleStore.truncateFrom(state.stockId(), state.nextCandleIndex());
        CandleWindow window = new CandleWindow(visibleCandles);
        window.upsertAll(candleStore.readTail(state.stockId(), visibleCandles));
        Candle forming = state.formingCandle();
        if (forming != null) window.upsert(forming);
        return window;
    }

    public boolean isEmpty() {
        return activeStocks.isEmpty();
    }

    /**
     * Advances every enabled stock by one step.
     *
     * @param updatesPerCandle price updates aggregated into one candle
     * @return the latest candle of every stock that was updated
     */
    public List<CandleUpdate> update(int updatesPerCandle) {
        List<CandleUpdate> updates = new ArrayList<>(activeStocks.size());
        for (StockDefinition stock : activeStocks) {
            StockState state = states.get(stock.id());
            double previousPrice = state.price();
            try {
                PriceEngine.step(stock, state);
                failingStocks.remove(stock.id());
            } catch (PriceEvaluationException e) {
                // Keep the price unchanged; log once per failure streak to avoid spamming every update
                if (failingStocks.add(stock.id())) {
                    NHKantan.LOG.warn("Stock '{}' skipped a price update: {}", stock.id(), e.getMessage());
                }
                continue;
            }
            Candle candle = state.recordCandlePrice(previousPrice, state.price());
            windows.get(stock.id())
                .upsert(candle);
            if (state.formingCandleUpdates() >= updatesPerCandle) {
                candleStore.append(stock.id(), candle);
                state.completeCandle();
            }
            updates.add(new CandleUpdate(stock.id(), candle));
        }
        return updates;
    }

    /** Public snapshots of all enabled stocks, in definition order. */
    public List<StockSnapshot> snapshots() {
        if (activeStocks.isEmpty()) return Collections.emptyList();
        List<StockSnapshot> snapshots = new ArrayList<>(activeStocks.size());
        for (StockDefinition stock : activeStocks) {
            snapshots.add(
                new StockSnapshot(
                    stock.id(),
                    stock.name(),
                    stock.lowerBound(),
                    stock.upperBound(),
                    states.get(stock.id())
                        .price(),
                    windows.get(stock.id())
                        .toArray()));
        }
        return snapshots;
    }
}
