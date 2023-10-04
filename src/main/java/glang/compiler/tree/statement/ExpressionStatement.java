package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import glang.compiler.tree.expression.ExpressionNode;

public class ExpressionStatement extends StatementNode {
    private final ExpressionNode expression;

    public ExpressionStatement(
        ExpressionNode expression,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        expression.print(result, currentIndent, indent);
        return result;
    }
}
