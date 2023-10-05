package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class NumberExpression extends LiteralExpression<Number> {
    public NumberExpression(Number value, SourceLocation location) {
        super(value, location);
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
