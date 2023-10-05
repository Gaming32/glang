package glang.runtime;

public class UnknownGlobalException extends RuntimeException {
    private final String variableName;

    public UnknownGlobalException(String variableName) {
        super(variableName);
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
