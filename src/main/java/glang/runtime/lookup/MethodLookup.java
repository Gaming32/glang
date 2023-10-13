package glang.runtime.lookup;

import glang.runtime.GlangRuntime;
import glang.runtime.extension.ExtensionMethodRegistry;
import glang.util.ConcurrentCache;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;

public abstract class MethodLookup {
    protected static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    protected final ConcurrentCache<List<Class<?>>, MethodHandle> cache = new ConcurrentCache<>(args -> {
        try {
            return lookup(args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    });

    protected abstract MethodHandle lookup(List<Class<?>> args) throws NoSuchMethodException;

    public MethodHandle getInvoker(List<Class<?>> argTypes) throws Throwable {
        try {
            return cache.get(argTypes);
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    public Object invoke(List<Object> args) throws Throwable {
        final List<Class<?>> argTypes = new ArrayList<>(args.size());
        for (final Object arg : args) {
            argTypes.add(GlangRuntime.getClass(arg));
        }
        return getInvoker(argTypes).invokeWithArguments(args);
    }

    public Object invoke() throws Throwable {
        return getInvoker(List.of()).invoke();
    }

    protected record ApplicableMethod<E extends Executable>(E method, int minimumArgs, int maximumArgs, Class<?>[] argTypes) {
        public static <E extends Executable> Comparator<ApplicableMethod<E>> createComparator() {
            return Comparator.comparingInt(m -> m.maximumArgs - m.minimumArgs);
        }
    }

    public interface Unreflector<E extends Executable> {
        Unreflector<Constructor<?>> CONSTRUCTOR = new Unreflector<>() {
            @Override
            public Constructor<?>[] getDeclared(Class<?> clazz) {
                return clazz.getDeclaredConstructors();
            }

            @Override
            public boolean supportsOverride() {
                return false;
            }

            @Nullable
            @Override
            public Constructor<?> findEquivalentIn(Class<?> clazz, Constructor<?> of) {
                try {
                    return clazz.getDeclaredConstructor(of.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                    return null;
                }
            }

            @Override
            public String getName(Class<?> clazz) {
                return clazz.getCanonicalName();
            }

            @Override
            public MethodHandle unreflect(MethodHandles.Lookup lookup, Constructor<?> method) throws IllegalAccessException {
                return lookup.unreflectConstructor(method);
            }
        };

        static Unreflector<Method> method(String name, boolean isStatic, boolean insertDummyThis) {
            if (!isStatic && insertDummyThis) {
                throw new IllegalArgumentException("Cannot insertDummyThis if isStatic is false");
            }
            return new Unreflector<>() {
                @Override
                public Method[] getDeclared(Class<?> clazz) {
                    return isStatic ? clazz.getDeclaredMethods() : clazz.getMethods();
                }

                @Override
                public boolean supportsOverride() {
                    return !isStatic;
                }

                @Nullable
                @Override
                public Method findEquivalentIn(Class<?> clazz, Method of) {
                    try {
                        return clazz.getDeclaredMethod(of.getName(), of.getParameterTypes());
                    } catch (NoSuchMethodException ignored) {
                        return null;
                    }
                }

                @Override
                public String getName(Class<?> clazz) {
                    return clazz.getCanonicalName() + '.' + name;
                }

                @Override
                public boolean filter(Method method) {
                    return Modifier.isStatic(method.getModifiers()) == isStatic && method.getName().equals(name);
                }

                @Override
                public MethodHandle unreflect(MethodHandles.Lookup lookup, Method method) throws IllegalAccessException {
                    final MethodHandle result = lookup.unreflect(method);
                    return insertDummyThis ? MethodHandles.dropArguments(result, 0, Class.class) : result;
                }

                @Override
                public int getArgOffset() {
                    return !isStatic || insertDummyThis ? 1 : 0;
                }
            };
        }

        static Unreflector<Method> extensionMethod(String name) {
            ExtensionMethodRegistry.REGISTRY.load();
            return new Unreflector<>() {
                @Override
                public Method[] getDeclared(Class<?> clazz) {
                    return ExtensionMethodRegistry.REGISTRY.getExtensionMethods(clazz, name).toArray(Method[]::new);
                }

                @Override
                public boolean supportsOverride() {
                    return false;
                }

                @Nullable
                @Override
                public Method findEquivalentIn(Class<?> clazz, Method of) {
                    return null;
                }

                @Override
                public String getName(Class<?> clazz) {
                    return clazz.getCanonicalName() + '.' + name;
                }

                @Override
                public MethodHandle unreflect(MethodHandles.Lookup lookup, Method method) throws IllegalAccessException {
                    return lookup.unreflect(method);
                }
            };
        }

        E[] getDeclared(Class<?> clazz);

        boolean supportsOverride();

        @Nullable
        E findEquivalentIn(Class<?> clazz, E of);

        String getName(Class<?> clazz);

        default boolean filter(E method) {
            return true;
        }

        MethodHandle unreflect(MethodHandles.Lookup lookup, E method) throws IllegalAccessException;

        default int getArgOffset() {
            return 0;
        }
    }
}
