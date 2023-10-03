package glang.compiler.error;

import glang.compiler.SourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorCollector {
    private final List<CompileError> errors = new ArrayList<>();

    public List<CompileError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public ErrorCollector addError(CompileError error) {
        errors.add(error);
        return this;
    }

    public ErrorCollector addError(String reason, SourceLocation location, String line) {
        return addError(new CompileError(reason, location, line));
    }

    public void throwIfFailed() throws CompileFailedException {
        if (!errors.isEmpty()) {
            throw new CompileFailedException(errors);
        }
    }
}
