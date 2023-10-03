package glang.compiler.token;

import glang.compiler.SourceLocation;

public class TokenizeFailure extends RuntimeException {
    private final String reason;
    private final SourceLocation location;
    private final String line;

    public TokenizeFailure(String reason, SourceLocation location, String line) {
        super(createMessage(reason, location, line));
        this.reason = reason;
        this.location = location;
        this.line = line;
    }

    public String getReason() {
        return reason;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public String getLine() {
        return line;
    }

    private static String createMessage(String reason, SourceLocation location, String line) {
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
