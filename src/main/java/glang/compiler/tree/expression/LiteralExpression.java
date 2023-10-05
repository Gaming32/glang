package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;

public abstract class LiteralExpression<T> extends ExpressionNode {
    private final T value;
    private final SourceLocation location;

    protected LiteralExpression(T value, SourceLocation location) {
        super(location, location);
        this.value = value;
        this.location = location;
    }

    public T getValue() {
        return value;
    }

    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(this);
    }

    @Override
    public String toString(int indent) {
        return toString();
    }

    public abstract String toString();

    @Override
    public SourceLocation singleLineLocation() {
        return location;
    }
}
