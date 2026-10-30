package com.yuelengm.nhkantan.market.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

import com.yuelengm.nhkantan.NHKantan;
import com.yuelengm.nhkantan.market.PriceTick;
import com.yuelengm.nhkantan.market.StockSnapshot;
import com.yuelengm.nhkantan.market.definition.MarketDefinitions;
import com.yuelengm.nhkantan.market.definition.StockDefinition;

/**
 * The running market: binds stock definitions to their persisted states and advances all enabled stocks.
 * <p>
 * States of stocks that are disabled or no longer defined are kept untouched, so re-enabling a stock resumes it.
 */
public class StockMarket {

    private final Map<String, StockState> states;
    private final List<StockDefinition> activeStocks = new ArrayList<>();
    private final Set<String> failingStocks = new HashSet<>();

    /**
     * @param states          persisted states keyed by stock id; missing entries are created and added
     * @param historyCapacity number of recent prices kept per stock
     * @param seeds           seed source for newly created stocks
     */
    public StockMarket(MarketDefinitions definitions, Map<String, StockState> states, int historyCapacity,
        LongSupplier seeds) {
        this.states = states;
        for (StockDefinition stock : definitions.stocks()) {
            if (!stock.enabled()) continue;
            StockState state = states.get(stock.id());
            if (state == null) {
                state = createState(stock, historyCapacity, seeds.getAsLong());
                states.put(stock.id(), state);
            } else {
                reconcile(stock, state, historyCapacity);
            }
            activeStocks.add(stock);
        }
    }

    private static StockState createState(StockDefinition stock, int historyCapacity, long seed) {
        StockState state = new StockState(stock.id(), seed, historyCapacity);
        state.setPrice(stock.initialPrice());
        state.history()
            .add(stock.initialPrice());
        PriceEngine.enterRandomMode(stock, state);
        return state;
    }

    /** Adapts a persisted state to a possibly edited definition without resetting its price. */
    private static void reconcile(StockDefinition stock, StockState state, int historyCapacity) {
        state.history()
            .resize(historyCapacity);
        double price = state.price();
        boolean priceValid = !Double.isNaN(price) && !Double.isInfinite(price) && price > 0.0D;
        double adjusted = priceValid ? stock.clamp(price) : stock.initialPrice();
        if (adjusted != price) {
            state.setPrice(adjusted);
            state.history()
                .add(adjusted);
            PriceEngine.enterRandomMode(stock, state);
        } else if (!PriceEngine.isModeCompatible(stock, state)) {
            PriceEngine.enterRandomMode(stock, state);
        }
        if (state.history()
            .isEmpty()) {
            state.history()
                .add(state.price());
        }
    }

    public boolean isEmpty() {
        return activeStocks.isEmpty();
    }

    /**
     * Advances every enabled stock by one step.
     *
     * @return the new prices of all stocks that were updated
     */
    public List<PriceTick> update() {
        List<PriceTick> ticks = new ArrayList<>(activeStocks.size());
        for (StockDefinition stock : activeStocks) {
            StockState state = states.get(stock.id());
            try {
                PriceEngine.step(stock, state);
                failingStocks.remove(stock.id());
                ticks.add(new PriceTick(stock.id(), state.sequence(), state.price()));
            } catch (PriceEvaluationException e) {
                // Keep the price unchanged; log once per failure streak to avoid spamming every update
                if (failingStocks.add(stock.id())) {
                    NHKantan.LOG.warn("Stock '{}' skipped a price update: {}", stock.id(), e.getMessage());
                }
            }
        }
        return ticks;
    }

    /** Public snapshots of all enabled stocks, in definition order. */
    public List<StockSnapshot> snapshots() {
        if (activeStocks.isEmpty()) return Collections.emptyList();
        List<StockSnapshot> snapshots = new ArrayList<>(activeStocks.size());
        for (StockDefinition stock : activeStocks) {
            StockState state = states.get(stock.id());
            snapshots.add(
                new StockSnapshot(
                    stock.id(),
                    stock.name(),
                    stock.lowerBound(),
                    stock.upperBound(),
                    state.sequence(),
                    state.history()
                        .toArray()));
        }
        return snapshots;
    }
}
