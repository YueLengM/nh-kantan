package com.yuelengm.nhkantan.market.definition;

import java.util.Collections;
import java.util.List;

import com.yuelengm.nhkantan.market.expression.Expression;

/**
 * A reusable market mode from {@code modes.json}, with its formula and variable updates already compiled.
 */
public class ModeDefinition {

    private final String id;
    private final String name;
    private final OutputType outputType;
    private final Expression formula;
    private final List<ModeVariable> variables;
    private final int slotCount;

    /**
     * @param slotCount total slots of the mode scope: system variables followed by the mode variables
     */
    public ModeDefinition(String id, String name, OutputType outputType, Expression formula,
        List<ModeVariable> variables, int slotCount) {
        this.id = id;
        this.name = name;
        this.outputType = outputType;
        this.formula = formula;
        this.variables = Collections.unmodifiableList(variables);
        this.slotCount = slotCount;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public OutputType outputType() {
        return outputType;
    }

    public Expression formula() {
        return formula;
    }

    public List<ModeVariable> variables() {
        return variables;
    }

    public int slotCount() {
        return slotCount;
    }
}
