package glang.runtime.lookup;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import glang.util.ConcurrentCache;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public final class InstanceMethodLookup {
    public static final CaffeineSpec CACHE_SPEC = CaffeineSpec.parse(System.getProperty(
        "glang.instanceMethodLookup.cacheSpec", "softValues"
    ));

    private static final ConcurrentCache<Class<?>, InstanceMethodLookup> INSTANCE_CACHE =
        new ConcurrentCache<>(clazz -> new InstanceMethodLookup(clazz, false));
    private static final ConcurrentCache<Class<?>, InstanceMethodLookup> STATIC_CACHE =
        new ConcurrentCache<>(clazz -> new InstanceMethodLookup(clazz, true));

    private static final Set<String> CLASS_METHODS = Arrays.stream(Class.class.getMethods())
        .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
        .map(Method::getName)
        .collect(Collectors.toUnmodifiableSet());
    private static final ConcurrentCache<String, MethodLookup> CLASS_LOOKUP =
        new ConcurrentCache<>(name -> {
            try {
                return new SimpleMethodLookup<>(Class.class, MethodLookup.Unreflector.method(name, false, false));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

    private final ConcurrentCache<String, Optional<MethodLookup>> lookup;
    private final ConcurrentCache<String, Optional<MethodLookup>> extensionLookup;
    private final boolean forClass;

    private InstanceMethodLookup(Class<?> clazz, boolean forClass) {
        lookup = new ConcurrentCache<>(name -> {
            try {
                return Optional.of(new SimpleMethodLookup<>(
                    clazz, MethodLookup.Unreflector.method(name, forClass, forClass)
                ));
            } catch (NoSuchMethodException e) {
                if (forClass && CLASS_METHODS.contains(name)) {
                    return Optional.empty();
                }
                try {
                    throw e;
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        extensionLookup = new ConcurrentCache<>(name -> {
            try {
                return Optional.of(new SimpleMethodLookup<>(clazz, MethodLookup.Unreflector.extensionMethod(name)));
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        });
        this.forClass = forClass;
    }

    public static InstanceMethodLookup get(Class<?> clazz, boolean forClass) {
        return forClass ? STATIC_CACHE.get(clazz) : INSTANCE_CACHE.get(clazz);
    }

    public MethodLookup getLookup(String methodName, boolean requireDirect) throws NoSuchMethodException {
        try {
            Optional<MethodLookup> result;
            if (forClass) {
                if (requireDirect) {
                    return CLASS_LOOKUP.get(methodName);
                }
                result = extensionLookup.get(methodName);
                if (result.isPresent()) {
                    return result.get();
                }
                result = lookup.get(methodName);
                //noinspection OptionalIsPresent
                if (result.isPresent()) {
                    return result.get();
                }
                return CLASS_LOOKUP.get(methodName);
            } else {
                if (!requireDirect) {
                    result = extensionLookup.get(methodName);
                    if (result.isPresent()) {
                        return result.get();
                    }
                }
                return lookup.get(methodName).orElseThrow(AssertionError::new);
            }
        } catch (CompletionException e) {
            if (e.getCause() instanceof NoSuchMethodException e1) {
                throw e1;
            }
            throw e;
        }
    }
}
