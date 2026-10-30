package com.yuelengm.nhkantan.market.server;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

import com.yuelengm.nhkantan.config.MarketConfig;
import com.yuelengm.nhkantan.market.engine.StockState;

/**
 * World save storage of all stock states, kept in the global map storage so it is shared by every dimension.
 */
public class MarketSavedData extends WorldSavedData {

    public static final String DATA_NAME = "nhkantan_market";
    public static final int DATA_VERSION = 1;

    private static final String NBT_VERSION = "Version";
    private static final String NBT_STOCKS = "Stocks";

    private final Map<String, StockState> states = new LinkedHashMap<>();

    /** Constructor required by the map storage, which instantiates saved data reflectively. */
    public MarketSavedData(String name) {
        super(name);
    }

    /** Live map of states; the market mutates it in place, callers must {@link #markDirty()} afterwards. */
    public Map<String, StockState> states() {
        return states;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        states.clear();
        NBTTagCompound stocks = tag.getCompoundTag(NBT_STOCKS);
        for (String stockId : stocks.func_150296_c()) {
            states.put(
                stockId,
                StockState.readFromNBT(stockId, stocks.getCompoundTag(stockId), MarketConfig.historyLength));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger(NBT_VERSION, DATA_VERSION);
        NBTTagCompound stocks = new NBTTagCompound();
        for (Map.Entry<String, StockState> entry : states.entrySet()) {
            stocks.setTag(
                entry.getKey(),
                entry.getValue()
                    .writeToNBT());
        }
        tag.setTag(NBT_STOCKS, stocks);
    }
}
