package glang.compiler.error;

import glang.compiler.SourceLocation;

public record CompileError(String reason, SourceLocation location, String line) {
    public String createMessage() {
        final StringBuilder result = new StringBuilder(reason);
        if (!reason.isEmpty()) {
            result.append(' ');
        }
        result.append("[line ").append(location.line()).append(", column ").append(location.column()).append(']');
        if (!line.isEmpty()) {
            result.append('\n').append(line);
            if (location.column() > 0) {
                result.append('\n')
                    .append(" ".repeat(location.column() - 1))
                    .append("^".repeat(location.length()));
            }
        }
        return result.toString();
    }
}
