package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public class BooleanExpression extends LiteralExpression<Boolean> {
    public BooleanExpression(boolean value, SourceLocation location) {
        super(value, location);
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(getValue());
    }
}
