package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;

public class LoopJumpStatement extends StatementNode {
    private final boolean isContinue;
    private final SourceLocation location;

    public LoopJumpStatement(boolean isContinue, SourceLocation location) {
        super(location.compressStart(), location.compressEnd());
        this.isContinue = isContinue;
        this.location = location;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(isContinue ? "continue" : "break");
    }
}
