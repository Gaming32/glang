package glang.runtime;

public final class OptionalParameter<T> {
    private static final OptionalParameter<?> ABSENT = new OptionalParameter<>(null);

    private T value;

    private OptionalParameter(T value) {
        this.value = value;
    }

    public static <T> OptionalParameter<T> present(T value) {
        return new OptionalParameter<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> OptionalParameter<T> absent() {
        return (OptionalParameter<T>)ABSENT;
    }

    public boolean isPresent() {
        return this != ABSENT;
    }

    public boolean isAbsent() {
        return this == ABSENT;
    }

    public T get() {
        if (this == ABSENT) {
            throw new IllegalStateException("Cannot get() an absent OptionalParameter");
        }
        return value;
    }
}
