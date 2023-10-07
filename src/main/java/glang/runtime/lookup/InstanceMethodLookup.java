package glang.runtime.lookup;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class InstanceMethodLookup {
    public static final CaffeineSpec CACHE_SPEC = CaffeineSpec.parse(System.getProperty(
        "glang.instanceMethodLookup.cacheSpec", "softValues"
    ));

    private static final LoadingCache<Class<?>, InstanceMethodLookup> INSTANCE_CACHE =
        Caffeine.from(CaffeineSpec.parse(System.getProperty(
            "glang.instanceMethodLookup.instanceCacheSpec", "softValues"
        ))).build(clazz -> new InstanceMethodLookup(clazz, false));
    private static final LoadingCache<Class<?>, InstanceMethodLookup> STATIC_CACHE =
        Caffeine.from(CaffeineSpec.parse(System.getProperty(
            "glang.instanceMethodLookup.staticCacheSpec", "softValues"
        ))).build(clazz -> new InstanceMethodLookup(clazz, true));

    private static final Set<String> CLASS_METHODS = Arrays.stream(Class.class.getDeclaredMethods())
        .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
        .map(Method::getName)
        .collect(Collectors.toUnmodifiableSet());
    private static final LoadingCache<String, MethodLookup> CLASS_LOOKUP = Caffeine.from(CACHE_SPEC)
        .initialCapacity(CLASS_METHODS.size())
        .maximumSize(CLASS_METHODS.size())
        .build(name -> new SimpleMethodLookup<>(Class.class, MethodLookup.Unreflector.method(name, false, false)));

    private final LoadingCache<String, MethodLookup> lookup;
    private final boolean forClass;

    private InstanceMethodLookup(Class<?> clazz, boolean forClass) {
        lookup = Caffeine.from(CACHE_SPEC).build(name -> {
            try {
                return new SimpleMethodLookup<>(clazz, MethodLookup.Unreflector.method(name, forClass, forClass));
            } catch (NoSuchMethodException e) {
                if (forClass && CLASS_METHODS.contains(name)) {
                    return null;
                }
                throw e;
            }
        });
        this.forClass = forClass;
    }

    public static InstanceMethodLookup get(Class<?> clazz, boolean forClass) {
        return forClass ? STATIC_CACHE.get(clazz) : INSTANCE_CACHE.get(clazz);
    }

    public MethodLookup getLookup(String methodName, boolean requireDirect) {
        if (forClass) {
            if (requireDirect) {
                return CLASS_LOOKUP.get(methodName);
            }
            MethodLookup result = lookup.get(methodName);
            if (result == null) {
                return CLASS_LOOKUP.get(methodName);
            }
        }
        return lookup.get(methodName);
    }
}
