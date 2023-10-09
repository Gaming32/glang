package glang.compiler.error;

import glang.compiler.SourceLocation;

public record CompileError(String reason, SourceLocation location, String line) {
    public String createMessage() {
        final StringBuilder result = new StringBuilder(reason);
        if (!reason.isEmpty()) {
            result.append('\n');
        }
        if (line.isEmpty()) {
            if (!reason.isEmpty()) {
                result.append("   ");
            }
            result.append("Line ").append(location.line()).append(", column ").append(location.column());
        } else {
            final String lineStr = Integer.toString(location.line());
            final String stripped = line.stripLeading();
            result.append(' ');
            if (lineStr.length() < 3) {
                result.append(" ".repeat(3 - lineStr.length()));
            }
            result.append(lineStr).append(" | ").append(stripped)
                .append("\n    ");
            if (lineStr.length() > 3) {
                result.append(" ".repeat(lineStr.length() - 3));
            }
            result.append(" | ")
                .append(" ".repeat(location.column() - 1 - line.length() + stripped.length()))
                .append("^".repeat(location.length()));
        }
//        if (!reason.isEmpty()) {
//            result.append(" at line ");
//        } else {
//            result.append("Line ");
//        }
//        result.append(location.line()).append(", column ").append(location.column());
//        if (!line.isEmpty()) {
//            result.append('\n').append(line);
//            if (location.column() > 0) {
//                result.append('\n')
//                    .append(" ".repeat(location.column() - 1))
//                    .append("^".repeat(location.length()));
//            }
//        }
        return result.toString();
    }
}
