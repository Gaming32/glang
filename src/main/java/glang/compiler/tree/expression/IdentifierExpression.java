package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;

public class IdentifierExpression extends LiteralExpression<String> implements AssignableExpression {
    public IdentifierExpression(String identifier, SourceLocation location) {
        super(identifier, location);
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(Token.Identifier.prettyPrint(getValue()));
    }
}
