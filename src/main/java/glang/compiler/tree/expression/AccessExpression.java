package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;

public class AccessExpression extends ExpressionNode {
    private final ExpressionNode parent;
    private final String member;

    public AccessExpression(
        ExpressionNode parent, String member,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.parent = parent;
        this.member = member;
    }

    public ExpressionNode getParent() {
        return parent;
    }

    public String getMember() {
        return member;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append('(');
        parent.print(result, currentIndent, indent);
        return result.append(").").append(Token.Identifier.prettyPrint(member));
    }
}
