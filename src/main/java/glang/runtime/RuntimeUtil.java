package glang.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RuntimeUtil {
    public static final Map<Class<?>, Class<?>> TO_WRAPPER_MAP = new HashMap<>(12);
    public static final Map<Class<?>, Class<?>> TO_PRIMITVE_MAP = new HashMap<>(12);

    static {
        addPrimitive(void.class, Void.class);
        addPrimitive(boolean.class, Boolean.class);
        addPrimitive(byte.class, Byte.class);
        addPrimitive(short.class, Short.class);
        addPrimitive(char.class, Character.class);
        addPrimitive(int.class, Integer.class);
        addPrimitive(float.class, Float.class);
        addPrimitive(long.class, Long.class);
        addPrimitive(double.class, Double.class);
    }

    private static <T> void addPrimitive(Class<T> primitive, Class<T> wrapper) {
        if (!primitive.isPrimitive()) {
            throw new IllegalArgumentException("primitive isn't primitive");
        }
        if (wrapper.isPrimitive()) {
            throw new IllegalArgumentException("wrapper is primitive");
        }
        TO_WRAPPER_MAP.put(primitive, wrapper);
        TO_PRIMITVE_MAP.put(wrapper, primitive);
    }

    private RuntimeUtil() {
    }

    public static boolean isAssignableFrom(Class<?> target, Class<?> value) {
        if (target.isAssignableFrom(value)) {
            return true;
        }
        if (value == Void.class) {
            return !target.isPrimitive();
        }
        if (target.isPrimitive()) {
            target = TO_WRAPPER_MAP.get(target);
        }
        if (value.isPrimitive()) {
            value = TO_WRAPPER_MAP.get(value);
        }
        return target.isAssignableFrom(value);
    }

    public static String prettyPrint(List<Class<?>> args) {
        return args.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
