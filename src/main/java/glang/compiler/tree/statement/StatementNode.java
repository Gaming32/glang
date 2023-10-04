package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import glang.compiler.tree.ASTNode;

public abstract class StatementNode extends ASTNode {
    protected StatementNode(SourceLocation startLocation, SourceLocation endLocation) {
        super(startLocation, endLocation);
    }
}
