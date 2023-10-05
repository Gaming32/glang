package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class NullExpression extends LiteralExpression<Void> {
    public NullExpression(SourceLocation location) {
        super(null, location);
    }

    @Override
    public String toString() {
        return "null";
    }
}
