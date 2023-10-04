package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;

import java.util.List;

public class ImportStatement extends StatementNode {
    private final List<String> parentPath;
    private final String target;

    public ImportStatement(
        List<String> parentPath, String target,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.parentPath = List.copyOf(parentPath);
        this.target = target;
    }

    public List<String> getParentPath() {
        return parentPath;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append("import ");
        for (final String part : parentPath) {
            result.append(part).append('.');
        }
        return result.append(target).append(';');
    }
}
