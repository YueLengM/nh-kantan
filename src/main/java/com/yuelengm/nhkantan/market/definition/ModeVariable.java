package com.yuelengm.nhkantan.market.definition;

import javax.annotation.Nullable;

import com.yuelengm.nhkantan.market.expression.Expression;

/**
 * A custom state variable declared by a mode.
 */
public class ModeVariable {

    private final String name;
    private final int slot;
    private final VariableInitializer initializer;
    @Nullable
    private final Expression update;

    /**
     * @param slot   slot of the variable in the mode's variable scope
     * @param update update expression evaluated after every price step, or {@code null} to keep the value constant
     */
    public ModeVariable(String name, int slot, VariableInitializer initializer, @Nullable Expression update) {
        this.name = name;
        this.slot = slot;
        this.initializer = initializer;
        this.update = update;
    }

    public String name() {
        return name;
    }

    public int slot() {
        return slot;
    }

    public VariableInitializer initializer() {
        return initializer;
    }

    @Nullable
    public Expression update() {
        return update;
    }
}
