package glang.compiler.tree;

import glang.compiler.token.TokenType;

import java.util.EnumMap;
import java.util.Map;

public enum Operator {
    OR("||", true),
    AND("&&", true),
    LT("<", true),
    GT(">", true),
    LE("<=", true),
    GE(">=", true),
    EQ("==", true),
    NEQ("!=", true),
    EQ_REF("===", true),
    NEQ_REF("!==", true),
    BITWISE_OR("|", true),
    BITWISE_XOR("^", true),
    BITWISE_AND("&", true),
    SHIFT_LEFT("<<", true),
    SHIFT_RIGHT(">>", true),
    ADD("+", true),
    SUBTRACT("-", true),
    MULTIPLY("*", true),
    DIVIDE("/", true),
    MODULO("%", true),
    NOT("!", false),
    PLUS("+", false),
    NEGATE("-", false),
    INVERT("~", false),
    ;

    private static final Map<TokenType, Operator> BY_TOKEN_BINARY = new EnumMap<>(TokenType.class);
    private static final Map<TokenType, Operator> BY_TOKEN_UNARY = new EnumMap<>(TokenType.class);

    static {
        for (final Operator operator : values()) {
            final TokenType token = TokenType.SIMPLE_TOKENS.get(operator.text);
            if (token == null) {
                throw new AssertionError("Unknown token for operator " + operator);
            }
            final Operator old;
            if (operator.binary) {
                old = BY_TOKEN_BINARY.put(token, operator);
            } else {
                old = BY_TOKEN_UNARY.put(token, operator);
            }
            if (old != null) {
                throw new AssertionError(
                    "Duplicate " + (operator.binary ? "binary" : "unary") + " operators: " + operator.text
                );
            }
        }
    }

    private final String text;
    private final boolean binary;

    Operator(String text, boolean binary) {
        this.text = text;
        this.binary = binary;
    }

    public static Operator binary(TokenType token) {
        return BY_TOKEN_BINARY.get(token);
    }

    public static Operator unary(TokenType token) {
        return BY_TOKEN_UNARY.get(token);
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isBinary() {
        return binary;
    }
}
