package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;
import glang.compiler.token.TokenType;

public class UnaryExpression extends ExpressionNode {
    private final TokenType operator;
    private final ExpressionNode operand;

    public UnaryExpression(
        TokenType operator, ExpressionNode operand,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        if (operator.getTokenClass() != Token.Basic.class || operator.isKeyword()) {
            throw new IllegalArgumentException("Operator must be simple token, was " + operator);
        }
        this.operator = operator;
        this.operand = operand;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append(operator.getBasicText());
        operand.print(result, currentIndent, indent);
        return result;
    }
}
