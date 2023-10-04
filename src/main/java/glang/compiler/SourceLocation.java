package glang.compiler;

public record SourceLocation(int line, int column, int length) {
    public static final SourceLocation NULL = new SourceLocation(1, 1);

    public SourceLocation {
        if (length < 1) {
            throw new IllegalArgumentException("length < 1");
        }
    }

    public SourceLocation(int line, int column) {
        this(line, column, 1);
    }

    public SourceLocation compressStart() {
        return length == 1 ? this : new SourceLocation(line, column, 1);
    }

    public SourceLocation compressEnd() {
        return length == 1 ? this : new SourceLocation(line, column + length - 1, 1);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder().append("SourceLocation[").append(line).append(':').append(column);
        if (length > 1) {
            result.append(" (").append(length).append(" chars)");
        }
        return result.append(']').toString();
    }
}
