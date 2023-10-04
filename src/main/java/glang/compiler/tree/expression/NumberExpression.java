package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class NumberExpression extends ExpressionNode {
    private final Number value;

    public NumberExpression(
        Number value,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.value = value;
    }

    public Number getValue() {
        return value;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(value);
    }
}
