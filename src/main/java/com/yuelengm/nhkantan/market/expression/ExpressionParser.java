package com.yuelengm.nhkantan.market.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles formulas of the restricted market expression language into {@link Expression} trees.
 * <p>
 * Grammar (identical precedence to the web configurator):
 *
 * <pre>
 * expr    := mul (('+' | '-') mul)*
 * mul     := unary (('*' | '/' | '%') unary)*
 * unary   := '-' unary | power
 * power   := primary ('^' unary)?
 * primary := number | identifier | identifier '(' args ')' | '(' expr ')' | '-' primary | '+' primary
 * </pre>
 *
 * Unknown variables and functions, and wrong argument counts, are reported at compile time.
 */
public class ExpressionParser {

    private static final Pattern NUMBER = Pattern.compile("(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String OPERATORS = "+-*/%^(),";

    private enum TokenType {
        NUMBER,
        IDENTIFIER,
        SYMBOL,
        END
    }

    private static class Token {

        private final TokenType type;
        private final String text;
        private final double number;

        private Token(TokenType type, String text, double number) {
            this.type = type;
            this.text = text;
            this.number = number;
        }

        private boolean isSymbol(char symbol) {
            return type == TokenType.SYMBOL && text.charAt(0) == symbol;
        }
    }

    private final List<Token> tokens;
    private final VariableScope scope;
    private int position;

    private ExpressionParser(List<Token> tokens, VariableScope scope) {
        this.tokens = tokens;
        this.scope = scope;
    }

    /**
     * Compiles a formula against the given scope.
     *
     * @throws ExpressionException if the formula is invalid
     */
    public static Expression compile(String source, VariableScope scope) {
        ExpressionParser parser = new ExpressionParser(tokenize(source), scope);
        Expression expression = parser.parseAdditive();
        if (parser.peek().type != TokenType.END) {
            throw new ExpressionException("Unexpected trailing content '" + parser.peek().text + "'");
        }
        return expression;
    }

    private static List<Token> tokenize(String source) {
        List<Token> result = new ArrayList<>();
        Matcher number = NUMBER.matcher(source);
        Matcher identifier = IDENTIFIER.matcher(source);
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            number.region(i, source.length());
            if (number.lookingAt()) {
                result.add(new Token(TokenType.NUMBER, number.group(), Double.parseDouble(number.group())));
                i = number.end();
                continue;
            }
            identifier.region(i, source.length());
            if (identifier.lookingAt()) {
                result.add(new Token(TokenType.IDENTIFIER, identifier.group(), 0.0D));
                i = identifier.end();
                continue;
            }
            if (OPERATORS.indexOf(c) < 0) {
                throw new ExpressionException("Unsupported character '" + c + "'");
            }
            result.add(new Token(TokenType.SYMBOL, String.valueOf(c), 0.0D));
            i++;
        }
        result.add(new Token(TokenType.END, "<end>", 0.0D));
        return result;
    }

    private Token peek() {
        return tokens.get(position);
    }

    private Token next() {
        return tokens.get(position++);
    }

    private void expect(char symbol) {
        if (!peek().isSymbol(symbol)) {
            throw new ExpressionException("Expected '" + symbol + "' but found '" + peek().text + "'");
        }
        position++;
    }

    private Expression parseAdditive() {
        Expression left = parseMultiplicative();
        while (peek().isSymbol('+') || peek().isSymbol('-')) {
            char op = next().text.charAt(0);
            Expression l = left;
            Expression r = parseMultiplicative();
            left = op == '+' ? (v, rnd) -> l.evaluate(v, rnd) + r.evaluate(v, rnd)
                : (v, rnd) -> l.evaluate(v, rnd) - r.evaluate(v, rnd);
        }
        return left;
    }

    private Expression parseMultiplicative() {
        Expression left = parseUnary();
        while (peek().isSymbol('*') || peek().isSymbol('/') || peek().isSymbol('%')) {
            char op = next().text.charAt(0);
            Expression l = left;
            Expression r = parseUnary();
            switch (op) {
                case '*' -> left = (v, rnd) -> l.evaluate(v, rnd) * r.evaluate(v, rnd);
                case '/' -> left = (v, rnd) -> l.evaluate(v, rnd) / r.evaluate(v, rnd);
                default -> left = (v, rnd) -> l.evaluate(v, rnd) % r.evaluate(v, rnd);
            }
        }
        return left;
    }

    private Expression parseUnary() {
        if (peek().isSymbol('-')) {
            position++;
            return negate(parseUnary());
        }
        return parsePower();
    }

    private Expression parsePower() {
        Expression base = parsePrimary();
        if (peek().isSymbol('^')) {
            position++;
            Expression exponent = parseUnary();
            return (v, rnd) -> Math.pow(base.evaluate(v, rnd), exponent.evaluate(v, rnd));
        }
        return base;
    }

    private Expression parsePrimary() {
        Token token = peek();
        if (token.type == TokenType.NUMBER) {
            position++;
            double value = token.number;
            return (v, rnd) -> value;
        }
        if (token.type == TokenType.IDENTIFIER) {
            position++;
            if (peek().isSymbol('(')) {
                return parseCall(token.text);
            }
            return resolveVariable(token.text);
        }
        if (token.isSymbol('(')) {
            position++;
            Expression inner = parseAdditive();
            expect(')');
            return inner;
        }
        if (token.isSymbol('-')) {
            position++;
            return negate(parsePrimary());
        }
        if (token.isSymbol('+')) {
            position++;
            return parsePrimary();
        }
        throw new ExpressionException("Expected a number, variable or '(' but found '" + token.text + "'");
    }

    private Expression parseCall(String name) {
        expect('(');
        List<Expression> args = new ArrayList<>();
        if (!peek().isSymbol(')')) {
            args.add(parseAdditive());
            while (peek().isSymbol(',')) {
                position++;
                args.add(parseAdditive());
            }
        }
        expect(')');

        if (MathFunctions.RANDOM.equals(name)) {
            if (args.size() != 2) {
                throw new ExpressionException("random(min, max) requires exactly 2 arguments");
            }
            Expression min = args.get(0);
            Expression max = args.get(1);
            return (v, rnd) -> {
                double lo = min.evaluate(v, rnd);
                double hi = max.evaluate(v, rnd);
                return lo + rnd.nextDouble() * (hi - lo);
            };
        }

        MathFunctions.Spec spec = MathFunctions.function(name);
        if (spec == null) {
            throw new ExpressionException("Unknown function '" + name + "'");
        }
        if (!spec.accepts(args.size())) {
            throw new ExpressionException(
                "Function '" + name + "' expects " + spec.arityDescription() + " argument(s) but got " + args.size());
        }
        Expression[] compiled = args.toArray(new Expression[0]);
        MathFunctions.Function function = spec.function();
        return (v, rnd) -> {
            double[] values = new double[compiled.length];
            for (int i = 0; i < compiled.length; i++) values[i] = compiled[i].evaluate(v, rnd);
            return function.apply(values);
        };
    }

    private Expression resolveVariable(String name) {
        Double constant = MathFunctions.constant(name);
        if (constant != null) {
            double value = constant;
            return (v, rnd) -> value;
        }
        int slot = scope.slotOf(name);
        if (slot < 0) {
            throw new ExpressionException("Undefined variable '" + name + "'");
        }
        return (v, rnd) -> v[slot];
    }

    private static Expression negate(Expression operand) {
        return (v, rnd) -> -operand.evaluate(v, rnd);
    }
}
