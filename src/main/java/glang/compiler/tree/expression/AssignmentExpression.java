package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.TokenType;

public class AssignmentExpression extends ExpressionNode {
    private final ExpressionNode variable;
    private final TokenType operator;
    private final ExpressionNode value;

    public AssignmentExpression(
        ExpressionNode variable, TokenType operator, ExpressionNode value,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.variable = variable;
        this.operator = operator;
        this.value = value;
    }

    public ExpressionNode getVariable() {
        return variable;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        variable.print(result, currentIndent, indent);
        result.append(' ').append(operator).append(' ');
        value.print(result, currentIndent, indent);
        return result;
    }
}
