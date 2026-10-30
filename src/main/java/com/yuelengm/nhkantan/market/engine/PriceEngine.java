package com.yuelengm.nhkantan.market.engine;

import java.util.List;

import com.yuelengm.nhkantan.market.definition.ModeDefinition;
import com.yuelengm.nhkantan.market.definition.ModeVariable;
import com.yuelengm.nhkantan.market.definition.StockDefinition;
import com.yuelengm.nhkantan.market.definition.StockModeEntry;
import com.yuelengm.nhkantan.market.definition.SystemVariables;
import com.yuelengm.nhkantan.market.random.SplitMix64Random;

/**
 * Stateless price stepping logic. Each step follows the web configurator exactly:
 * <ol>
 * <li>with the current mode's switch probability, pick another mode by weight and enter it;</li>
 * <li>evaluate the formula against the pre-step context and derive the calculated price;</li>
 * <li>clamp to the hard bounds to obtain the actual price;</li>
 * <li>evaluate all variable updates, each reading the pre-update values;</li>
 * <li>commit prices, change rates, variables and increment {@code t}.</li>
 * </ol>
 */
public class PriceEngine {

    private PriceEngine() {}

    /** Picks a mode by weight among all selectable modes and enters it. */
    public static void enterRandomMode(StockDefinition stock, StockState state) {
        enterMode(state, ModeSelector.choose(stock.selectableModes(), state.random(), null));
    }

    /** Enters the mode: resets the context to the current price and initializes the mode variables. */
    public static void enterMode(StockState state, StockModeEntry entry) {
        ModeDefinition mode = entry.mode();
        List<ModeVariable> variables = mode.variables();
        String[] names = new String[variables.size()];
        double[] values = new double[variables.size()];
        for (int i = 0; i < names.length; i++) {
            ModeVariable variable = variables.get(i);
            names[i] = variable.name();
            values[i] = requireFinite(
                variable.initializer()
                    .initialValue(state.random()),
                mode,
                "initial value of '" + variable.name() + "'");
        }
        state.enterMode(mode.id(), names, values);
    }

    /**
     * Whether the state's mode is still runnable by the stock with the same variable layout. When false, the state
     * must re-enter a mode before stepping.
     */
    public static boolean isModeCompatible(StockDefinition stock, StockState state) {
        StockModeEntry entry = stock.findSelectableMode(state.modeId());
        if (entry == null) return false;
        List<ModeVariable> variables = entry.mode()
            .variables();
        String[] names = state.variableNames();
        if (names.length != variables.size()) return false;
        for (int i = 0; i < names.length; i++) {
            if (!names[i].equals(
                variables.get(i)
                    .name()))
                return false;
        }
        return true;
    }

    /**
     * Performs one price update. The state must be mode-compatible with the stock.
     *
     * @throws PriceEvaluationException if a formula produces a non-finite value; the price is left unchanged
     */
    public static void step(StockDefinition stock, StockState state) {
        SplitMix64Random random = state.random();
        StockModeEntry entry = stock.findSelectableMode(state.modeId());
        if (entry == null) {
            throw new IllegalStateException("Stock '" + stock.id() + "' is not in a runnable mode");
        }
        if (random.nextDouble() < entry.switchProbability()) {
            StockModeEntry next = ModeSelector.choose(stock.selectableModes(), random, entry.modeId());
            if (next != entry) {
                enterMode(state, next);
                entry = next;
            }
        }

        ModeDefinition mode = entry.mode();
        double[] context = buildContext(mode, state);
        double output = requireFinite(
            mode.formula()
                .evaluate(context, random),
            mode,
            "formula output");
        double calculated = requireFinite(
            mode.outputType()
                .toCalculatedPrice(state.prevCalcPrice(), output),
            mode,
            "calculated price");
        double actual = stock.clamp(calculated);

        List<ModeVariable> variables = mode.variables();
        double[] updated = state.variableValues()
            .clone();
        for (int i = 0; i < updated.length; i++) {
            ModeVariable variable = variables.get(i);
            if (variable.update() == null) continue;
            updated[i] = requireFinite(
                variable.update()
                    .evaluate(context, random),
                mode,
                "update of '" + variable.name() + "'");
        }

        state.applyStep(calculated, actual, updated);
    }

    private static double[] buildContext(ModeDefinition mode, StockState state) {
        double[] context = new double[mode.slotCount()];
        context[SystemVariables.T] = state.t();
        context[SystemVariables.START_PRICE] = state.startPrice();
        context[SystemVariables.PREV_CALC_PRICE] = state.prevCalcPrice();
        context[SystemVariables.PREV_PRICE] = state.prevPrice();
        context[SystemVariables.PREV_CALC_RATE] = state.prevCalcRate();
        context[SystemVariables.PREV_RATE] = state.prevRate();
        double[] values = state.variableValues();
        List<ModeVariable> variables = mode.variables();
        for (int i = 0; i < values.length; i++) {
            context[variables.get(i)
                .slot()] = values[i];
        }
        return context;
    }

    private static double requireFinite(double value, ModeDefinition mode, String what) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new PriceEvaluationException("Mode '" + mode.id() + "' produced a non-finite " + what);
        }
        return value;
    }
}
