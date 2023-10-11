package glang.runtime;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import glang.exception.AmbiguousImportException;
import glang.exception.ImportNotFoundException;
import glang.exception.UninvokableObjectException;
import glang.exception.UnknownGlobalException;
import glang.runtime.lookup.FieldLookup;
import glang.runtime.lookup.InstanceMethodLookup;
import glang.runtime.lookup.MethodLookup;
import glang.runtime.lookup.SimpleMethodLookup;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.*;

public final class GlangRuntime {
    private static final MethodType IMPORT_STAR_MT = MethodType.methodType(void.class, Map.class);
    private static final MethodType IMPORT_STAR_0_MT = MethodType.methodType(
        void.class, MethodHandles.Lookup.class, List.class, Map.class
    );
    private static final MethodType DO_IMPORT_MT = MethodType.methodType(Object.class);
    private static final MethodType DO_IMPORT_0_MT = MethodType.methodType(
        Object.class, MethodHandles.Lookup.class, List.class, String.class
    );

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

        for (final Class<?> nestMember : clazz.getNestMembers()) {
            if (!Modifier.isPublic(nestMember.getModifiers()) || nestMember.getDeclaringClass() != clazz) continue;
            final String simpleName = nestMember.getSimpleName();
            if (fieldNames.contains(simpleName) || methodNames.contains(simpleName)) {
                result.remove(simpleName);
                continue;
            }
            result.put(simpleName, nestMember);
        }

        return result;
    }

    public static Class<?> getClass(Object obj) {
        return obj != null ? obj.getClass() : Void.class;
    }

    public static MethodLookup getInstanceMethod(Object obj, String name, boolean requireDirect) throws NoSuchMethodException {
        if (obj == null) {
            throw new NullPointerException("Cannot invoke method '" + name + "' on null");
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

    public static FieldLookup getFieldLookup(Object obj) {
        final boolean isClass = obj instanceof Class<?>;
        return FieldLookup.get(isClass ? (Class<?>)obj : obj.getClass(), isClass);
    }

    public static Object getField(Object obj, String name) throws Throwable {
        if (obj == null) {
            throw new NullPointerException("Cannot get field '" + name + "' on null");
        }
        return getFieldLookup(obj).get(obj, name);
    }

    public static Object setField(Object obj, String name, Object value) throws Throwable {
        if (obj == null) {
            throw new NullPointerException("Cannot set field '" + name + "' on null");
        }
        return getFieldLookup(obj).set(obj, name, value);
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

    public static Boolean isTruthyW(Object obj) throws Throwable {
        return isTruthy(obj);
    }

    public static Boolean isFalseyW(Object obj) throws Throwable {
        return !isTruthy(obj);
    }

    public static CallSite importStar(
        MethodHandles.Lookup lookup, String name, MethodType type, String... path
    ) throws NoSuchMethodException, IllegalAccessException {
        if (!type.equals(IMPORT_STAR_MT)) {
            throw new IllegalArgumentException("importStar type != " + IMPORT_STAR_MT);
        }
        return new ConstantCallSite(MethodHandles.insertArguments(
            lookup.findStatic(GlangRuntime.class, "importStar0", IMPORT_STAR_0_MT),
            0, lookup, List.of(path)
        ));
    }

    public static void importStar0(
        MethodHandles.Lookup lookup, List<String> path, Map<String, Object> destination
    ) throws ImportNotFoundException {
        destination.putAll(collectStarImport(findImportStarClass(lookup, path)));
    }

    private static Class<?> findImportStarClass(MethodHandles.Lookup lookup, List<String> path) throws ImportNotFoundException {
        try {
            return lookup.findClass(String.join(".", path));
        } catch (Exception ignored) {
        }
        for (int i = path.size() - 2; i >= 0; i--) {
            try {
                return lookup.findClass(
                    String.join(".", path.subList(0, i)) + "." +
                        String.join("$", path.subList(i, path.size()))
                );
            } catch (Exception ignored) {
            }
        }
        throw new ImportNotFoundException(path, null, null);
    }

    public static CallSite doImport(
        MethodHandles.Lookup lookup, String name, MethodType type, String... path
    ) throws NoSuchMethodException, IllegalAccessException {
        if (!type.equals(DO_IMPORT_MT)) {
            throw new IllegalArgumentException("doImport type != " + DO_IMPORT_MT);
        }
        return new ConstantCallSite(MethodHandles.insertArguments(
            lookup.findStatic(GlangRuntime.class, "doImport0", DO_IMPORT_0_MT),
            0, lookup, List.of(path), name
        ));
    }

    public static Object doImport0(MethodHandles.Lookup lookup, List<String> path, String target) throws ImportNotFoundException {
        if (path.isEmpty()) {
            try {
                return lookup.findClass(target);
            } catch (Exception e) {
                throw new ImportNotFoundException(path, target, null).initCause(e);
            }
        }
        String joined = String.join(".", path);
        try {
            return lookup.findClass(joined + "$" + target);
        } catch (Exception ignored) {
        }
        //noinspection CatchMayIgnoreException
        try {
            return findImport(lookup.findClass(joined), target, path);
        } catch (Exception e) {
            if (e instanceof AmbiguousImportException aie) {
                throw aie;
            }
        }
        for (int i = path.size() - 2; i >= 0; i--) {
            joined = String.join(".", path.subList(0, i)) + "." +
                String.join("$", path.subList(i, path.size()));
            try {
                return lookup.findClass(joined + "$" + target);
            } catch (Exception ignored) {
            }
            //noinspection CatchMayIgnoreException
            try {
                return findImport(lookup.findClass(joined), target, path);
            } catch (Exception e) {
                if (e instanceof AmbiguousImportException aie) {
                    throw aie;
                }
            }
        }
        throw new ImportNotFoundException(path, target, null);
    }

    private static Object findImport(Class<?> clazz, String name, List<String> path) throws ImportNotFoundException {
        boolean found = false;
        Object result = null;
        final int publicStatic = Modifier.PUBLIC | Modifier.STATIC;

        for (final Field field : clazz.getDeclaredFields()) {
            if ((field.getModifiers() & publicStatic) != publicStatic || !field.getName().equals(name)) continue;
            try {
                result = field.get(null);
                found = true;
                break;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access public field " + field.getName(), e);
            }
        }

        for (final Method method : clazz.getDeclaredMethods()) {
            if ((method.getModifiers() & publicStatic) != publicStatic || !method.getName().equals(name)) continue;
            if (found) {
                throw new AmbiguousImportException(path, name);
            }
            try {
                result = new SimpleMethodLookup<>(
                    clazz, MethodLookup.Unreflector.method(method.getName(), true, false)
                );
                found = true;
                break;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(clazz + " doesn't contain public method " + method.getName());
            }
        }

        for (final Class<?> nestMember : clazz.getNestMembers()) {
            if (
                !Modifier.isPublic(nestMember.getModifiers()) ||
                    nestMember.getDeclaringClass() != clazz ||
                    !nestMember.getSimpleName().equals(name)
            ) continue;
            if (found) {
                throw new AmbiguousImportException(path, name);
            }
            result = nestMember;
            found = true;
            break;
        }

        if (!found) {
            throw new ImportNotFoundException(path, name, null);
        }

        return result;
    }
}
