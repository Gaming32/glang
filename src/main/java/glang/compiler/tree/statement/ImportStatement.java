package glang.compiler.tree.statement;

import glang.compiler.SourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImportStatement extends StatementNode {
    private final List<String> parentPath;
    @Nullable
    private final String target;

    public ImportStatement(
        List<String> parentPath, @Nullable String target,
        SourceLocation startLocation, SourceLocation endLocation
    ) {
        super(startLocation, endLocation);
        this.parentPath = List.copyOf(parentPath);
        this.target = target;
    }

    public List<String> getParentPath() {
        return parentPath;
    }

    @Nullable
    public String getTarget() {
        return target;
    }

    @Override
    public StringBuilder print(StringBuilder result, int currentIndent, int indent) {
        result.append("import ");
        for (final String part : parentPath) {
            result.append(part).append('.');
        }
        return result.append(target != null ? target : "*");
    }
}
