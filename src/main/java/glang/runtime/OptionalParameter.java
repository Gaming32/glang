package glang.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public final class OptionalParameter {
    private static final OptionalParameter ABSENT = new OptionalParameter(null);

    public static final MethodHandle PRESENT_MH;

    static {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            PRESENT_MH = lookup.findConstructor(OptionalParameter.class, MethodType.methodType(void.class, Object.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Object value;

    private OptionalParameter(Object value) {
        this.value = value;
    }

    public static OptionalParameter present(Object value) {
        return new OptionalParameter(value);
    }

    public static OptionalParameter absent() {
        return ABSENT;
    }

    public boolean isPresent() {
        return this != ABSENT;
    }

    public boolean isAbsent() {
        return this == ABSENT;
    }

    public Object get() {
        if (this == ABSENT) {
            throw new IllegalStateException("Cannot get() an absent OptionalParameter");
        }
        return value;
    }

    @Override
    public String toString() {
        return this == ABSENT ? "Absent" : "Present[" + value + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (this == ABSENT || obj == ABSENT) {
            return false;
        }
        return obj instanceof OptionalParameter opt && value.equals(opt.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
