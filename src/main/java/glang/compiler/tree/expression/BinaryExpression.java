package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.tree.Operator;

public class BinaryExpression extends ExpressionNode {
    private final ExpressionNode left;
    private final Operator operator;
    private final ExpressionNode right;

    public BinaryExpression(
        ExpressionNode left, Operator operator, ExpressionNode right,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        if (!operator.isBinary()) {
            throw new IllegalArgumentException("Operator must be binary, was " + operator);
        }
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
}
