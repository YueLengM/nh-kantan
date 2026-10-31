package com.yuelengm.nhkantan.market.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.yuelengm.nhkantan.market.definition.StockModeEntry;

/**
 * Weighted mode selection, identical to the web configurator.
 */
public class ModeSelector {

    private ModeSelector() {}

    /**
     * Picks a mode by weight. When {@code excludeModeId} is given, that mode is left out unless it is the only
     * candidate.
     *
     * @param candidates non-empty list of entries with positive weight
     */
    public static StockModeEntry choose(List<StockModeEntry> candidates, Random random,
        @Nullable String excludeModeId) {
        List<StockModeEntry> pool = candidates;
        if (excludeModeId != null) {
            List<StockModeEntry> filtered = new ArrayList<>(candidates.size());
            for (StockModeEntry entry : candidates) {
                if (!entry.modeId()
                    .equals(excludeModeId)) filtered.add(entry);
            }
            if (!filtered.isEmpty()) pool = filtered;
        }
        double total = 0.0D;
        for (StockModeEntry entry : pool) total += entry.weight();
        double roll = random.nextDouble() * total;
        for (StockModeEntry entry : pool) {
            roll -= entry.weight();
            if (roll < 0.0D) return entry;
        }
        return pool.get(pool.size() - 1);
    }
}
