package glang.compiler.tree;

import glang.compiler.SourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public abstract class ASTNode {
    private final SourceLocation startLocation;
    private final SourceLocation endLocation;

    protected ASTNode(SourceLocation startLocation, SourceLocation endLocation) {
        this.startLocation = startLocation.compressStart();
        this.endLocation = endLocation.compressEnd();
    }

    public SourceLocation getStartLocation() {
        return startLocation;
    }

    public SourceLocation getEndLocation() {
        return endLocation;
    }

    @Contract("_, _, _ -> param1")
    public abstract StringBuilder print(StringBuilder result, int currentIndent, int indent);

    public final String print(int indent) {
        return print(new StringBuilder(), 0, indent).toString();
    }

    @Override
    public final String toString() {
        return print(0);
    }

    @Nullable
    public final SourceLocation singleLineLocation() {
        if (startLocation.line() != endLocation.line()) {
            return null;
        }
        return new SourceLocation(
            startLocation.line(), startLocation.column(),
            endLocation.column() - startLocation.column() + 1
        );
    }
}
