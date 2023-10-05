package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class BooleanExpression extends LiteralExpression<Boolean> {
    public BooleanExpression(boolean value, SourceLocation location) {
        super(value, location);
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
