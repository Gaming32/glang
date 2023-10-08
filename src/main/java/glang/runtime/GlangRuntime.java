package glang.runtime;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import glang.exception.UninvokableObjectException;
import glang.exception.UnknownGlobalException;
import glang.runtime.lookup.InstanceMethodLookup;
import glang.runtime.lookup.MethodLookup;
import glang.runtime.lookup.SimpleMethodLookup;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.*;

public final class GlangRuntime {
    private static final LoadingCache<Class<?>, SimpleMethodLookup<Constructor<?>>> CONSTRUCTOR_CACHE =
        Caffeine.from(CaffeineSpec.parse(System.getProperty(
            "glang.constructorLookup.cacheSpec", "softValues"
        ))).build(clazz -> new SimpleMethodLookup<>(clazz, MethodLookup.Unreflector.CONSTRUCTOR));

    private GlangRuntime() {
    }

    public static Object getGlobal(Map<String, Object> globals, String name) {
        Object value = globals.get(name);
        if (value == null && !globals.containsKey(name)) {
            final Map<String, Object> defaults = DefaultImports.getDefaultImports();
            value = defaults.get(name);
            if (value == null && !defaults.containsKey(name)) {
                throw new UnknownGlobalException(name);
            }
        }
        return value;
    }

    public static Object putGlobal(Map<String, Object> globals, String name, Object value) {
        globals.put(name, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> SimpleMethodLookup<Constructor<T>> findConstructors(Class<T> clazz) {
        return (SimpleMethodLookup<Constructor<T>>)(SimpleMethodLookup<?>)CONSTRUCTOR_CACHE.get(clazz);
    }

    public static Object invokeObject(Object target, List<Object> args) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.invoke(args);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object... args) throws Throwable {
        return invokeObject(target, List.of(args));
    }

    public static Map<String, Object> collectStarImport(Class<?> clazz) {
        final Map<String, Object> result = new LinkedHashMap<>();
        final int publicStatic = Modifier.PUBLIC | Modifier.STATIC;

        final Set<String> fieldNames = new HashSet<>();
        for (final Field field : clazz.getDeclaredFields()) {
            if ((field.getModifiers() & publicStatic) != publicStatic) continue;
            fieldNames.add(field.getName());
            try {
                result.put(field.getName(), field.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access public field " + field.getName(), e);
            }
        }

        final Set<String> methodNames = new HashSet<>();
        for (final Method method : clazz.getDeclaredMethods()) {
            if ((method.getModifiers() & publicStatic) != publicStatic) continue;
            if (fieldNames.contains(method.getName())) {
                result.remove(method.getName()); // Duplicate names aren't exported at all, to resolve ambiguous imports
                continue;
            }
            if (methodNames.add(method.getName())) {
                try {
                    result.put(method.getName(), new SimpleMethodLookup<>(
                        clazz, MethodLookup.Unreflector.method(method.getName(), true, false)
                    ));
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(clazz + " doesn't contain public method " + method.getName());
                }
            }
        }

        return result;
    }

    public static Class<?> getClass(Object obj) {
        return obj != null ? obj.getClass() : Void.class;
    }

    public static MethodLookup getInstanceMethod(Object obj, String name, boolean requireDirect) throws NoSuchMethodException {
        if (obj == null) {
            throw new NullPointerException("Cannot invoke method " + name + " on null");
        }
        final boolean isClass = obj instanceof Class<?>;
        return InstanceMethodLookup.get(isClass ? (Class<?>)obj : obj.getClass(), isClass)
            .getLookup(name, requireDirect);
    }

    public static MethodLookup getInstanceMethod(Object obj, String name) throws NoSuchMethodException {
        return getInstanceMethod(obj, name, false);
    }

    public static MethodLookup getDirectMethod(Object obj, String name) throws NoSuchMethodException {
        return getInstanceMethod(obj, name, true);
    }

    public static boolean isTruthy(Object obj) throws Throwable {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean b) {
            return b;
        }
        if (obj instanceof Integer i) {
            return i != 0;
        }
        if (obj instanceof Long l) {
            return l != 0L;
        }
        if (obj instanceof BigInteger i) {
            return !i.equals(BigInteger.ZERO);
        }
        if (obj instanceof Number n) {
            final double dValue = n.doubleValue();
            return dValue != 0.0 && !Double.isNaN(dValue);
        }
        if (obj instanceof String s) {
            return !s.isEmpty();
        }
        if (obj instanceof Collection<?> c) {
            return !c.isEmpty();
        }
        if (obj instanceof Map<?, ?> m) {
            return !m.isEmpty();
        }
        final MethodLookup booleanValue;
        try {
            booleanValue = getInstanceMethod(obj, "booleanValue");
        } catch (NoSuchMethodException e) {
            return true;
        }
        return (boolean)booleanValue.invoke();
    }
}
