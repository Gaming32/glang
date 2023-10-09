package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.TokenType;

import java.util.Map;

public class UnaryExpression extends ExpressionNode {
    private final Operator operator;
    private final ExpressionNode operand;

    public UnaryExpression(
        Operator operator, ExpressionNode operand,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.operator = operator;
        this.operand = operand;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append(operator);
        operand.print(result, currentIndent, indent);
        return result;
    }

    public enum Operator {
        NOT("!"),
        PLUS("+"),
        NEGATE("-"),
        INVERT("~");

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
