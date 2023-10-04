package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class NullExpression extends ExpressionNode {
    public NullExpression(SourceLocation startLocation, SourceLocation endLocation) {
        super(startLocation, endLocation);
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append("null");
    }
}
