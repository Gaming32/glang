package glang.compiler.tree;

import glang.compiler.SourceLocation;

public abstract class ASTNode {
    private final SourceLocation startLocation;
    private final SourceLocation endLocation;

    protected ASTNode(SourceLocation startLocation, SourceLocation endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }
}
