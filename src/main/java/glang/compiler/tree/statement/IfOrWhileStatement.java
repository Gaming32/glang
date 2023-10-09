package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import glang.compiler.tree.expression.ExpressionNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IfOrWhileStatement extends StatementNode {
    private static final List<Class<? extends StatementNode>> BLOCKED_BODY_STATEMENTS = List.of(
        ImportStatement.class, VariableDeclaration.class
    );

    private final ExpressionNode condition;
    private final boolean isWhile;
    private final StatementNode body;
    @Nullable
    private final StatementNode elseBody;

    public IfOrWhileStatement(
        ExpressionNode condition, boolean isWhile, StatementNode body, @Nullable StatementNode elseBody,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.condition = condition;
        this.isWhile = isWhile;
        this.body = body;
        this.elseBody = elseBody;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public boolean isWhile() {
        return isWhile;
    }

    public StatementNode getBody() {
        return body;
    }

    @Nullable
    public StatementNode getElseBody() {
        return elseBody;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append(isWhile ? "while" : "if").append(" (");
        condition.print(result, currentIndent, indent);
        result.append(")");
        if (elseBody != null) {
            if (!(body instanceof BlockStatement block) || block.getStatements().getStatements().isEmpty()) {
                result.append("\n").append(" ".repeat(currentIndent));
            } else {
                result.append(" ");
            }
            result.append("else");
            printBody(elseBody, result, currentIndent, indent);
        }
        return result;
    }

    public static void printBody(StatementNode body, StringBuilder result, int currentIndent, int indent) {
        final int newIndent = currentIndent + indent;
        if (body instanceof BlockStatement) {
            result.append(" ");
            body.print(result, currentIndent, indent);
        } else {
            result.append("\n").append(" ".repeat(newIndent));
            body.print(result, newIndent, indent);
        }
    }

    public static boolean isBlockedBody(StatementNode body) {
        for (final var blocked : IfOrWhileStatement.BLOCKED_BODY_STATEMENTS) {
            if (blocked.isInstance(body)) {
                return true;
            }
        }
        return false;
    }
}
