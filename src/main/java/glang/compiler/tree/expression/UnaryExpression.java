package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.tree.Operator;

public class UnaryExpression extends ExpressionNode {
    private final Operator operator;
    private final ExpressionNode operand;

    public UnaryExpression(
        Operator operator, ExpressionNode operand,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        if (operator.isBinary()) {
            throw new IllegalArgumentException("Operator must be unary, was " + operator);
        }
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
}
