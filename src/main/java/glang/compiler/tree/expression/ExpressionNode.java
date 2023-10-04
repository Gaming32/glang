package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.tree.ASTNode;

public abstract class ExpressionNode extends ASTNode {
    protected ExpressionNode(SourceLocation startLocation, SourceLocation endLocation) {
        super(startLocation, endLocation);
    }
}
