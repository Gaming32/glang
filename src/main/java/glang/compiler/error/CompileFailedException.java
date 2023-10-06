package glang.compiler.error;

import java.util.List;
import java.util.stream.Collectors;

public class CompileFailedException extends Exception {
    private final List<CompileError> errors;

    public CompileFailedException(List<CompileError> errors) {
        super(
            "Compilation failed with " + errors.size() + " error(s)\n\n" +
                errors.stream().map(CompileError::createMessage).collect(Collectors.joining("\n\n"))
        );
        this.errors = List.copyOf(errors);
    }

    public CompileFailedException(CompileError... errors) {
        this(List.of(errors));
    }

    public List<CompileError> getErrors() {
        return errors;
    }
}
