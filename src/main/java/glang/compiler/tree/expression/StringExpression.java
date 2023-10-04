package glang.compiler.tree.expression;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;

public class StringExpression extends ExpressionNode {
    private final String content;

    public StringExpression(
        String content,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        return result.append(Token.Str.prettyPrint(content));
    }
}
