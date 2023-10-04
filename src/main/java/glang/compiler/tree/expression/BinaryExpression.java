package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;
import glang.compiler.token.TokenType;

public class BinaryExpression extends ExpressionNode {
    private final ExpressionNode left;
    private final TokenType operator;
    private final ExpressionNode right;

    public BinaryExpression(
        ExpressionNode left, TokenType operator, ExpressionNode right,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        if (operator.getTokenClass() != Token.Basic.class || operator.isKeyword()) {
            throw new IllegalArgumentException("Operator must be simple token, was " + operator);
        }
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append('(');
        left.print(result, currentIndent, indent);
        result.append(") ").append(operator.getBasicText()).append(" (");
        right.print(result, currentIndent, indent);
        return result.append(')');
    }
}
