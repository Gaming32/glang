package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import glang.compiler.tree.expression.ExpressionNode;
import org.jetbrains.annotations.Nullable;

public class IfStatement extends StatementNode {
    private final ExpressionNode condition;
    private final StatementNode body;
    @Nullable
    private final StatementNode elseBody;

    public IfStatement(
        ExpressionNode condition, StatementNode body, @Nullable StatementNode elseBody,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.condition = condition;
        this.body = body;
        this.elseBody = elseBody;
    }

    public ExpressionNode getCondition() {
        return condition;
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
        result.append("if (");
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
}
