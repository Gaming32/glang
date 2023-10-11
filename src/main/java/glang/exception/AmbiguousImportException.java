package glang.exception;

import java.util.List;

public class AmbiguousImportException extends ImportNotFoundException {
    public AmbiguousImportException(List<String> path, String target) {
        super(path, target, "Ambiguous import");
    }
}
