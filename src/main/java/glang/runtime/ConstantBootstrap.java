package glang.runtime;

import java.lang.invoke.MethodHandles;
import java.math.BigInteger;

public final class ConstantBootstrap {
    private ConstantBootstrap() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T number(
        MethodHandles.Lookup lookup, String name, Class<T> type, String value
    ) throws IllegalArgumentException {
        if (type == Integer.class) {
            return (T)Integer.valueOf(value);
        }
        if (type == Double.class) {
            return (T)Double.valueOf(value);
        }
        if (type == Long.class) {
            return (T)Long.valueOf(value);
        }
        if (type == BigInteger.class) {
            return (T)new BigInteger(value);
        }
        throw new IllegalArgumentException("Unsupported type for ConstantBootstrap.number: " + type);
    }
}
