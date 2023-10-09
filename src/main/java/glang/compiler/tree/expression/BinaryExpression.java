package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.TokenType;

import java.util.Map;

public class BinaryExpression extends ExpressionNode {
    private final ExpressionNode left;
    private final Operator operator;
    private final ExpressionNode right;

    public BinaryExpression(
        ExpressionNode left, Operator operator, ExpressionNode right,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append('(');
        left.print(result, currentIndent, indent);
        result.append(") ").append(operator).append(" (");
        right.print(result, currentIndent, indent);
        return result.append(')');
    }

    public enum Operator {
        OR("||"),
        AND("&&"),
        LT("<"),
        GT(">"),
        LE("<="),
        GE(">="),
        EQ("=="),
        NEQ("!="),
        EQ_REF("==="),
        NEQ_REF("!=="),
        BITWISE_OR("|"),
        BITWISE_XOR("^"),
        BITWISE_AND("&"),
        SHIFT_LEFT("<<"),
        SHIFT_RIGHT(">>"),
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%");

        public static final Map<TokenType, Operator> BY_TOKEN = TokenType.byToken(values(), Operator::getText);

        private final String text;

        Operator(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
