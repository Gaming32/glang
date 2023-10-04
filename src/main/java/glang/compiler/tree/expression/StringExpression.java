package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;

public class StringExpression extends LiteralExpression<String> {
    public StringExpression(String content, SourceLocation location) {
        super(content, location);
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(Token.Str.prettyPrint(getValue()));
    }
}
