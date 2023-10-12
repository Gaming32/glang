package glang.runtime.extension;

import glang.runtime.RuntimeUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public final class ExtensionMethodRegistry {
    public static final ExtensionMethodRegistry REGISTRY = new ExtensionMethodRegistry();

    private final Map<Class<?>, Map<String, List<Method>>> lookup = new WeakHashMap<>();
    private volatile boolean loaded = false;

    private ExtensionMethodRegistry() {
    }

    public void register(String name, Method method) {
        if (method.getParameterCount() == 0) {
            throw new IllegalArgumentException("Extension method must have at least one parameter");
        }
        final Class<?> clazz = method.getParameterTypes()[0];
        lookup.computeIfAbsent(RuntimeUtil.TO_WRAPPER_MAP.getOrDefault(clazz, clazz), c -> new HashMap<>())
            .computeIfAbsent(name, m -> new ArrayList<>())
            .add(method);
    }

    public void register(Method method) {
        register(method.getName(), method);
    }

    public void registerAll(Class<?> clazz) {
        for (final Method m : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) || !m.isAnnotationPresent(ExtensionMethod.class)) continue;
            register(m);
        }
    }

    public void registerAllStatic(Class<?> clazz, Predicate<Method> predicate) {
        final int publicStatic = Modifier.PUBLIC | Modifier.STATIC;
        for (final Method m : clazz.getDeclaredMethods()) {
            if ((m.getModifiers() & publicStatic) != publicStatic || m.getParameterCount() == 0 || !predicate.test(m)) continue;
            register(m);
        }
    }

    public void copy(Class<?> clazz, String from, String to) {
        lookup.computeIfAbsent(RuntimeUtil.TO_WRAPPER_MAP.getOrDefault(clazz, clazz), c -> new HashMap<>())
            .computeIfAbsent(to, m -> new ArrayList<>())
            .addAll(getExtensionMethods(clazz, from));
    }

    public List<Method> getExtensionMethods(Class<?> target, String name) {
        return getExtensionMethods(target, name, new ArrayList<>(), new HashSet<>());
    }

    private List<Method> getExtensionMethods(Class<?> target, String name, List<Method> result, Set<Class<?>> interfaces) {
        final var forClass = lookup.get(target);
        if (forClass != null) {
            final List<Method> forName = forClass.get(name);
            if (forName != null) {
                result.addAll(forName);
            }
        }
        if (target.getSuperclass() != null) {
            getExtensionMethods(target.getSuperclass(), name, result, interfaces);
        }
        for (final Class<?> intf : target.getInterfaces()) {
            // Prevent duplicates (e.g. ImmutableList < List < Collection, but also ImmutableList < ImmutableCollection < Collection)
            if (interfaces.add(intf)) {
                getExtensionMethods(intf, name, result, interfaces);
            }
        }
        return result;
    }

    public void load() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    reload();
                }
            }
        }
    }

    public synchronized void reload() {
        loaded = false;
        lookup.clear();
        registerDefaults();
        loaded = true;
    }

    public synchronized void registerDefaults() {
        ServiceLoader.load(ExtensionMethodRegistrar.class).forEach(r -> {
            try {
                r.register(this);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
