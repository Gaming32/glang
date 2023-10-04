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

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        if (statements.isEmpty()) {
            return result;
        }
        statements.get(0).print(result, currentIndent, indent);
        for (int i = 1; i < statements.size(); i++) {
            result.append(" ".repeat(currentIndent));
            statements.get(i).print(result, currentIndent, indent);
            result.append('\n');
        }
        if (statements.size() > 1) {
            result.setLength(result.length() - 1);
        }
        return result;
    }
}
