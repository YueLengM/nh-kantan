package com.yuelengm.nhkantan.market.definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yuelengm.nhkantan.market.expression.Expression;
import com.yuelengm.nhkantan.market.expression.ExpressionException;
import com.yuelengm.nhkantan.market.expression.ExpressionParser;
import com.yuelengm.nhkantan.market.expression.MathFunctions;
import com.yuelengm.nhkantan.market.expression.VariableScope;

/**
 * Parses and validates the root object of {@code modes.json}, compiling every formula.
 */
public class ModeDefinitionParser {

    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String UNIFORM = "uniform";

    private ModeDefinitionParser() {}

    /**
     * @return modes keyed by id, in file order
     */
    public static Map<String, ModeDefinition> parse(JsonObject root) {
        JsonArray array = JsonFields.requireArray(root, "modes", "modes.json");
        if (array.size() == 0) {
            throw new DefinitionException("modes.json must define at least one mode");
        }
        Map<String, ModeDefinition> modes = new LinkedHashMap<>();
        for (int i = 0; i < array.size(); i++) {
            ModeDefinition mode = parseMode(JsonFields.asObject(array.get(i), "modes[" + i + "]"), "modes[" + i + "]");
            if (modes.put(mode.id(), mode) != null) {
                throw new DefinitionException("Duplicate mode id '" + mode.id() + "'");
            }
        }
        return modes;
    }

    private static ModeDefinition parseMode(JsonObject json, String location) {
        String id = JsonFields.requireString(json, "id", location);
        String where = "mode '" + id + "'";
        String name = JsonFields.optionalString(json, "name", where, id);
        String outputName = JsonFields.requireString(json, "outputType", where);
        OutputType outputType = OutputType.fromJsonName(outputName);
        if (outputType == null) {
            throw new DefinitionException(where + " has unknown outputType '" + outputName + "'");
        }
        String formulaSource = JsonFields.requireString(json, "formula", where);

        JsonArray variablesJson = JsonFields.optionalArray(json, "variables", where);
        VariableScope scope = SystemVariables.newScope();
        List<JsonObject> variableObjects = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (int i = 0; i < variablesJson.size(); i++) {
            JsonObject variable = JsonFields.asObject(variablesJson.get(i), where + ".variables[" + i + "]");
            String varName = JsonFields.requireString(variable, "name", where + ".variables[" + i + "]");
            validateVariableName(varName, where);
            if (!names.add(varName)) {
                throw new DefinitionException(where + " declares variable '" + varName + "' twice");
            }
            scope.declare(varName);
            variableObjects.add(variable);
        }

        List<ModeVariable> variables = new ArrayList<>();
        for (JsonObject variable : variableObjects) {
            String varName = JsonFields.requireString(variable, "name", where);
            String varWhere = where + " variable '" + varName + "'";
            VariableInitializer initializer = parseInitializer(variable.get("initial"), varWhere);
            String updateSource = JsonFields.optionalString(variable, "update", varWhere, "");
            Expression update = updateSource.trim()
                .isEmpty() ? null : compile(updateSource, scope, varWhere + " update");
            variables.add(new ModeVariable(varName, scope.slotOf(varName), initializer, update));
        }

        Expression formula = compile(formulaSource, scope, where + " formula");
        return new ModeDefinition(id, name, outputType, formula, variables, scope.size());
    }

    private static void validateVariableName(String name, String where) {
        if (!VARIABLE_NAME.matcher(name)
            .matches()) {
            throw new DefinitionException(where + " has invalid variable name '" + name + "'");
        }
        if (SystemVariables.NAMES.contains(name) || MathFunctions.isConstant(name)) {
            throw new DefinitionException(where + " variable '" + name + "' clashes with a built-in name");
        }
    }

    private static VariableInitializer parseInitializer(JsonElement initial, String where) {
        if (initial == null || initial.isJsonNull()) {
            throw new DefinitionException(where + " is missing 'initial'");
        }
        if (JsonFields.isNumber(initial)) {
            double value = initial.getAsDouble();
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new DefinitionException(where + " initial value must be finite");
            }
            return VariableInitializer.constant(value);
        }
        JsonObject object = JsonFields.asObject(initial, where + ".initial");
        String distribution = JsonFields.requireString(object, "distribution", where + ".initial");
        if (!UNIFORM.equals(distribution)) {
            throw new DefinitionException(where + " has unknown distribution '" + distribution + "'");
        }
        double min = JsonFields.requireNumber(object, "min", where + ".initial");
        double max = JsonFields.requireNumber(object, "max", where + ".initial");
        if (min > max) {
            throw new DefinitionException(where + " uniform range requires min <= max");
        }
        return VariableInitializer.uniform(min, max);
    }

    private static Expression compile(String source, VariableScope scope, String where) {
        try {
            return ExpressionParser.compile(source, scope);
        } catch (ExpressionException e) {
            throw new DefinitionException(where + ": " + e.getMessage(), e);
        }
    }
}
