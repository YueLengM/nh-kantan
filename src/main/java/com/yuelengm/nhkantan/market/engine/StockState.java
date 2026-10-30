package com.yuelengm.nhkantan.market.engine;

import java.nio.ByteBuffer;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.yuelengm.nhkantan.market.PriceHistory;
import com.yuelengm.nhkantan.market.random.SplitMix64Random;

/**
 * Mutable runtime state of one stock. Everything needed to continue the exact same price path after a restart is
 * persisted: price, mode context, mode variables, random state and history.
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
    private static final String NBT_HISTORY = "History";

    private static final String[] NO_NAMES = new String[0];
    private static final double[] NO_VALUES = new double[0];

    private final String stockId;
    private final SplitMix64Random random;
    private final PriceHistory history;

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

    public StockState(String stockId, long seed, int historyCapacity) {
        this.stockId = stockId;
        this.random = new SplitMix64Random(seed);
        this.history = new PriceHistory(historyCapacity);
    }

    public String stockId() {
        return stockId;
    }

    public SplitMix64Random random() {
        return random;
    }

    public PriceHistory history() {
        return history;
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
        history.add(actualPrice);
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
        tag.setTag(NBT_HISTORY, new NBTTagByteArray(encode(history.toArray())));
        return tag;
    }

    public static StockState readFromNBT(String stockId, NBTTagCompound tag, int historyCapacity) {
        StockState state = new StockState(stockId, tag.getLong(NBT_RANDOM), historyCapacity);
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
        state.history.setAll(decode(tag.getByteArray(NBT_HISTORY)));
        return state;
    }

    private static byte[] encode(double[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * Double.BYTES);
        for (double value : values) buffer.putDouble(value);
        return buffer.array();
    }

    private static double[] decode(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        double[] values = new double[bytes.length / Double.BYTES];
        for (int i = 0; i < values.length; i++) values[i] = buffer.getDouble();
        return values;
    }
}
