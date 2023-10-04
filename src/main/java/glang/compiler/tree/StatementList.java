package glang.compiler.tree;

import glang.compiler.SourceLocation;
import glang.compiler.tree.statement.StatementNode;

import java.util.List;

public class StatementList extends ASTNode {
    private final List<StatementNode> statements;

    public StatementList(
        List<StatementNode> statements,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.statements = List.copyOf(statements);
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        for (final StatementNode node : statements) {
            result.append(" ".repeat(currentIndent));
            node.print(result, currentIndent, indent);
            result.append('\n');
        }
        return result;
    }
}
