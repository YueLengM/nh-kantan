package com.yuelengm.nhkantan.market.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps variable names to dense slot indices so compiled expressions read variables from a plain array.
 */
public class VariableScope {

    private final Map<String, Integer> slots = new HashMap<>();

    /**
     * Declares a new variable and returns its slot.
     *
     * @throws IllegalArgumentException if the name is already declared
     */
    public int declare(String name) {
        if (slots.containsKey(name)) {
            throw new IllegalArgumentException("Variable '" + name + "' is already declared");
        }
        int slot = slots.size();
        slots.put(name, slot);
        return slot;
    }

    /**
     * @return the slot of the variable, or {@code -1} if it is not declared
     */
    public int slotOf(String name) {
        Integer slot = slots.get(name);
        return slot == null ? -1 : slot;
    }

    public boolean contains(String name) {
        return slots.containsKey(name);
    }

    public int size() {
        return slots.size();
    }
}
