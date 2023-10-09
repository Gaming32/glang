package glang.compiler.error;

import java.util.List;

public class CompileFailedException extends Exception {
    private final List<CompileError> errors;

    public CompileFailedException(List<CompileError> errors) {
        super(ErrorCollector.errorsToString(errors));
        this.errors = List.copyOf(errors);
    }

    public CompileFailedException(CompileError... errors) {
        this(List.of(errors));
    }

    public List<CompileError> getErrors() {
        return errors;
    }
}
