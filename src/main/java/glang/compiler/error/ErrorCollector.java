package glang.compiler.error;

import glang.compiler.SourceLocation;
import glang.util.GlangStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class ErrorCollector {
    private final List<CompileError> errors = new ArrayList<>();
    private final IntFunction<String> lineGetter;

    public ErrorCollector(IntFunction<String> lineGetter) {
        this.lineGetter = lineGetter;
    }

    public ErrorCollector(String source) {
        this(i -> GlangStringUtils.getLine(source, i));
    }

    public List<CompileError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public IntFunction<String> getLineGetter() {
        return lineGetter;
    }

    public ErrorCollector addError(CompileError error) {
        errors.add(error);
        return this;
    }

    public ErrorCollector addError(String reason, SourceLocation location, String line) {
        return addError(new CompileError(reason, location, line));
    }

    public ErrorCollector addError(String reason, SourceLocation location) {
        return addError(reason, location, lineGetter.apply(location.line() - 1));
    }

    public void throwIfFailed() throws CompileFailedException {
        if (!errors.isEmpty()) {
            throw new CompileFailedException(errors);
        }
    }

    @Override
    public String toString() {
        return errorsToString(errors);
    }

    public static String errorsToString(List<CompileError> errors) {
        final String pluralSuffix = errors.size() != 1 ? "s" : "";
        return "Compilation failed with " + errors.size() + " error" + pluralSuffix + "\n\n" +
            errors.stream().map(CompileError::createMessage).collect(Collectors.joining("\n\n"));
    }
}
