package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import glang.compiler.tree.StatementList;

public class BlockStatement extends StatementNode {
    private final StatementList statements;

    public BlockStatement(
        StatementList statements,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.statements = statements;
    }

    public StatementList getStatements() {
        return statements;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        if (statements.getStatements().isEmpty()) {
            return result.append("{}");
        }
        final int newIndent = currentIndent + indent;
        result.append("{\n").append(" ".repeat(newIndent));
        statements.print(result, newIndent, indent);
        return result.append('\n').append(" ".repeat(currentIndent)).append('}');
    }
}
