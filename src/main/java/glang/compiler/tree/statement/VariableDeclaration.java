package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import glang.compiler.token.Token;
import glang.compiler.tree.expression.ExpressionNode;
import org.jetbrains.annotations.Nullable;

public class VariableDeclaration extends StatementNode {
    private final String name;
    @Nullable
    private final ExpressionNode initializer;

    public VariableDeclaration(
        String name, @Nullable ExpressionNode initializer,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.name = name;
        this.initializer = initializer;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ExpressionNode getInitializer() {
        return initializer;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append("var ").append(Token.Identifier.prettyPrint(name));
        if (initializer != null) {
            result.append(" = ");
            initializer.print(result, currentIndent, indent);
        }
        return result;
    }
}
