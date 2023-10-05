package glang.runtime;

import java.util.Map;

public final class GlangRuntime {
    private GlangRuntime() {
    }

    public static Object getGlobal(Map<String, Object> globals, String name) {
        final Object value = globals.get(name);
        if (value == null && !globals.containsKey(name)) {
            throw new UnknownGlobalException(name);
        }
        return value;
    }
}
