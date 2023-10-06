package glang.runtime;

import glang.exception.UninvokableObjectException;
import glang.exception.UnknownGlobalException;
import glang.runtime.lookup.MethodLookup;
import glang.runtime.lookup.StaticMethodLookup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public final class GlangRuntime {
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

    public static Object invokeObject(Object target, List<Object> args) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.invoke(args);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Map<String, Object> collectStarImport(Class<?> clazz) {
        final Map<String, Object> result = new LinkedHashMap<>();

        final Set<String> fieldNames = new HashSet<>();
        for (final Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !field.canAccess(null)) continue;
            fieldNames.add(field.getName());
            try {
                result.put(field.getName(), field.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access public field " + field.getName(), e);
            }
        }

        final Set<String> methodNames = new HashSet<>();
        for (final Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.canAccess(null)) continue;
            if (fieldNames.contains(method.getName())) {
                result.remove(method.getName()); // Duplicate names aren't exported at all, to resolve ambiguous imports
                continue;
            }
            if (methodNames.add(method.getName())) {
                result.put(method.getName(), new StaticMethodLookup(clazz, method.getName()));
            }
        }

        return result;
    }
}
