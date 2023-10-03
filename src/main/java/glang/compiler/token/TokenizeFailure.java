package glang.compiler.token;

import glang.compiler.SourceLocation;
import glang.compiler.error.CompileError;
import glang.compiler.error.CompileFailedException;

public class TokenizeFailure extends CompileFailedException {
    private final CompileError error;

    public TokenizeFailure(CompileError error) {
        super(error);
        this.error = error;
    }

    public TokenizeFailure(String reason, SourceLocation location, String line) {
        this(new CompileError(reason, location, line));
    }

    public CompileError getError() {
        return error;
    }
}
