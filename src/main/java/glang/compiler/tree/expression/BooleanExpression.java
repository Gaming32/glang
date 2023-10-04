package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class BooleanExpression extends ExpressionNode {
    private final boolean value;

    public BooleanExpression(
        boolean value,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(value);
    }
}
