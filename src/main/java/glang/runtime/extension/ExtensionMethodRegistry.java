package glang.runtime.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public final class ExtensionMethodRegistry {
    public static final ExtensionMethodRegistry REGISTRY = new ExtensionMethodRegistry();

    private final Map<Class<?>, Map<String, List<Method>>> lookup = new WeakHashMap<>();
    private volatile boolean loaded = false;

    private ExtensionMethodRegistry() {
    }

    public void register(Method method) {
        if (method.getParameterCount() == 0) {
            throw new IllegalArgumentException("Extension method must have at least one parameter");
        }
        lookup.computeIfAbsent(method.getParameterTypes()[0], c -> new HashMap<>())
            .computeIfAbsent(method.getName(), m -> new ArrayList<>())
            .add(method);
    }

    public void registerAll(Class<?> clazz) {
        for (final Method m : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) || !m.isAnnotationPresent(ExtensionMethod.class)) continue;
            register(m);
        }
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
        ServiceLoader.load(ExtensionMethodRegistrar.class).forEach(r -> r.register(this));
    }
}
