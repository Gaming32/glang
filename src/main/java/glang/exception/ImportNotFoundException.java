package glang.exception;

import java.util.List;

public class ImportNotFoundException extends RuntimeException {
    private final List<String> path;
    private final String target;

    public ImportNotFoundException(List<String> path, String target, String reason) {
        super(
            (!path.isEmpty() ? String.join(".", path) + "." : "") +
                (target != null ? target : "*") +
                (reason != null ? ": " + reason : "")
        );
        this.path = List.copyOf(path);
        this.target = target;
    }

    @Override
    public ImportNotFoundException initCause(Throwable cause) {
        return (ImportNotFoundException)super.initCause(cause);
    }

    public List<String> getPath() {
        return path;
    }

    public String getTarget() {
        return target;
    }
}
