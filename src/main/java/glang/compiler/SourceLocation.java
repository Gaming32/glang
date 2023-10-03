package glang.compiler;

public record SourceLocation(int line, int column, int length) {
    public SourceLocation {
        if (length < 1) {
            throw new IllegalArgumentException("length < 1");
        }
    }

    public SourceLocation(int line, int column) {
        this(line, column, 1);
    }
}
