package com.yuelengm.nhkantan.market.engine;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.yuelengm.nhkantan.market.Candle;
import com.yuelengm.nhkantan.market.random.SplitMix64Random;

/**
 * Mutable runtime state of one stock. Everything needed to continue the exact same price path after a restart is
 * persisted: price, mode context, mode variables, random state and the candle still forming. Completed candles live in
 * a {@link CandleStore}.
 */
public class StockState {

    private static final String NBT_PRICE = "Price";
    private static final String NBT_SEQUENCE = "Sequence";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_T = "T";
    private static final String NBT_START_PRICE = "StartPrice";
    private static final String NBT_PREV_CALC_PRICE = "PrevCalcPrice";
    private static final String NBT_PREV_PRICE = "PrevPrice";
    private static final String NBT_PREV_CALC_RATE = "PrevCalcRate";
    private static final String NBT_PREV_RATE = "PrevRate";
    private static final String NBT_VARIABLES = "Variables";
    private static final String NBT_VARIABLE_NAME = "Name";
    private static final String NBT_VARIABLE_VALUE = "Value";
    private static final String NBT_RANDOM = "Random";
    private static final String NBT_NEXT_CANDLE = "NextCandle";
    private static final String NBT_CANDLE = "Candle";
    private static final String NBT_CANDLE_UPDATES = "CandleUpdates";
    private static final String NBT_OPEN = "Open";
    private static final String NBT_HIGH = "High";
    private static final String NBT_LOW = "Low";
    private static final String NBT_CLOSE = "Close";

    private static final String[] NO_NAMES = new String[0];
    private static final double[] NO_VALUES = new double[0];

    private final String stockId;
    private final SplitMix64Random random;

    private double price;
    /** Number of price updates performed for this stock since it was created. */
    private long sequence;

    private String modeId = "";
    private long t;
    private double startPrice;
    private double prevCalcPrice;
    private double prevPrice;
    private double prevCalcRate;
    private double prevRate;
    private String[] variableNames = NO_NAMES;
    private double[] variableValues = NO_VALUES;

    /** Index the next opened candle receives; equals the number of completed candles. */
    private long nextCandleIndex;
    /** The candle still forming, or {@code null} if no update happened since the last candle completed. */
    @Nullable
    private Candle formingCandle;
    private int formingCandleUpdates;

    public StockState(String stockId, long seed) {
        this.stockId = stockId;
        this.random = new SplitMix64Random(seed);
    }

    public String stockId() {
        return stockId;
    }

    public SplitMix64Random random() {
        return random;
    }

    public double price() {
        return price;
    }

    public long sequence() {
        return sequence;
    }

    public String modeId() {
        return modeId;
    }

    public long t() {
        return t;
    }

    public double startPrice() {
        return startPrice;
    }

    public double prevCalcPrice() {
        return prevCalcPrice;
    }

    public double prevPrice() {
        return prevPrice;
    }

    public double prevCalcRate() {
        return prevCalcRate;
    }

    public double prevRate() {
        return prevRate;
    }

    public String[] variableNames() {
        return variableNames;
    }

    public double[] variableValues() {
        return variableValues;
    }

    public long nextCandleIndex() {
        return nextCandleIndex;
    }

    @Nullable
    public Candle formingCandle() {
        return formingCandle;
    }

    /** Sets the price without touching the mode context, e.g. on creation or after a bounds change. */
    public void setPrice(double price) {
        this.price = price;
    }

    /** Resets the mode context as required when a mode is entered. */
    public void enterMode(String modeId, String[] variableNames, double[] variableValues) {
        this.modeId = modeId;
        this.t = 1;
        this.startPrice = price;
        this.prevCalcPrice = price;
        this.prevPrice = price;
        this.prevCalcRate = 0.0D;
        this.prevRate = 0.0D;
        this.variableNames = variableNames;
        this.variableValues = variableValues;
    }

    /** Commits the result of one price step. */
    public void applyStep(double calculatedPrice, double actualPrice, double[] newVariableValues) {
        this.prevCalcRate = calculatedPrice / prevCalcPrice - 1.0D;
        this.prevRate = actualPrice / prevPrice - 1.0D;
        this.prevCalcPrice = calculatedPrice;
        this.prevPrice = actualPrice;
        this.price = actualPrice;
        this.variableValues = newVariableValues;
        this.t++;
        this.sequence++;
    }

    /**
     * Adds a price to the forming candle, opening it at {@code openPrice} if needed.
     *
     * @return the forming candle after the update
     */
    public Candle recordCandlePrice(double openPrice, double price) {
        Candle base = formingCandle != null ? formingCandle : Candle.open(nextCandleIndex, openPrice);
        formingCandle = base.withPrice(price);
        formingCandleUpdates++;
        return formingCandle;
    }

    public int formingCandleUpdates() {
        return formingCandleUpdates;
    }

    /** Marks the forming candle as completed; the next recorded price opens a new candle. */
    public void completeCandle() {
        formingCandle = null;
        formingCandleUpdates = 0;
        nextCandleIndex++;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble(NBT_PRICE, price);
        tag.setLong(NBT_SEQUENCE, sequence);
        tag.setString(NBT_MODE, modeId);
        tag.setLong(NBT_T, t);
        tag.setDouble(NBT_START_PRICE, startPrice);
        tag.setDouble(NBT_PREV_CALC_PRICE, prevCalcPrice);
        tag.setDouble(NBT_PREV_PRICE, prevPrice);
        tag.setDouble(NBT_PREV_CALC_RATE, prevCalcRate);
        tag.setDouble(NBT_PREV_RATE, prevRate);
        NBTTagList variables = new NBTTagList();
        for (int i = 0; i < variableNames.length; i++) {
            NBTTagCompound variable = new NBTTagCompound();
            variable.setString(NBT_VARIABLE_NAME, variableNames[i]);
            variable.setDouble(NBT_VARIABLE_VALUE, variableValues[i]);
            variables.appendTag(variable);
        }
        tag.setTag(NBT_VARIABLES, variables);
        tag.setLong(NBT_RANDOM, random.getState());
        tag.setLong(NBT_NEXT_CANDLE, nextCandleIndex);
        if (formingCandle != null) {
            NBTTagCompound candle = new NBTTagCompound();
            candle.setDouble(NBT_OPEN, formingCandle.open());
            candle.setDouble(NBT_HIGH, formingCandle.high());
            candle.setDouble(NBT_LOW, formingCandle.low());
            candle.setDouble(NBT_CLOSE, formingCandle.close());
            candle.setInteger(NBT_CANDLE_UPDATES, formingCandleUpdates);
            tag.setTag(NBT_CANDLE, candle);
        }
        return tag;
    }

    public static StockState readFromNBT(String stockId, NBTTagCompound tag) {
        StockState state = new StockState(stockId, tag.getLong(NBT_RANDOM));
        state.price = tag.getDouble(NBT_PRICE);
        state.sequence = tag.getLong(NBT_SEQUENCE);
        state.modeId = tag.getString(NBT_MODE);
        state.t = tag.getLong(NBT_T);
        state.startPrice = tag.getDouble(NBT_START_PRICE);
        state.prevCalcPrice = tag.getDouble(NBT_PREV_CALC_PRICE);
        state.prevPrice = tag.getDouble(NBT_PREV_PRICE);
        state.prevCalcRate = tag.getDouble(NBT_PREV_CALC_RATE);
        state.prevRate = tag.getDouble(NBT_PREV_RATE);
        NBTTagList variables = tag.getTagList(NBT_VARIABLES, Constants.NBT.TAG_COMPOUND);
        state.variableNames = new String[variables.tagCount()];
        state.variableValues = new double[variables.tagCount()];
        for (int i = 0; i < variables.tagCount(); i++) {
            NBTTagCompound variable = variables.getCompoundTagAt(i);
            state.variableNames[i] = variable.getString(NBT_VARIABLE_NAME);
            state.variableValues[i] = variable.getDouble(NBT_VARIABLE_VALUE);
        }
        state.nextCandleIndex = tag.getLong(NBT_NEXT_CANDLE);
        if (tag.hasKey(NBT_CANDLE, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound candle = tag.getCompoundTag(NBT_CANDLE);
            state.formingCandle = new Candle(
                state.nextCandleIndex,
                candle.getDouble(NBT_OPEN),
                candle.getDouble(NBT_HIGH),
                candle.getDouble(NBT_LOW),
                candle.getDouble(NBT_CLOSE));
            state.formingCandleUpdates = candle.getInteger(NBT_CANDLE_UPDATES);
        }
        return state;
    }
}
