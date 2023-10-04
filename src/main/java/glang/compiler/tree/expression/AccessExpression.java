package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;

public class AccessExpression extends ExpressionNode implements AssignableExpression {
    private final ExpressionNode target;
    private final String member;

    public AccessExpression(
        ExpressionNode target, String member,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.target = target;
        this.member = member;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public String getMember() {
        return member;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append('(');
        target.print(result, currentIndent, indent);
        return result.append(").").append(Token.Identifier.prettyPrint(member));
    }
}
