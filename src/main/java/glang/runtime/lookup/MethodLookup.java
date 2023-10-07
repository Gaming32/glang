package glang.runtime.lookup;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import glang.runtime.GlangRuntime;

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
    public static final CaffeineSpec CACHE_SPEC = CaffeineSpec.parse(System.getProperty(
        "glang.methodLookup.cacheSpec", "initialCapacity=4,maximumSize=64,softValues"
    ));

    protected static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

    protected final LoadingCache<List<Class<?>>, MethodHandle> cache = Caffeine.from(CACHE_SPEC).build(this::lookup);

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
            public String getName(Class<?> clazz) {
                return clazz.getCanonicalName();
            }

            @Override
            public boolean filter(Constructor<?> method) {
                return true;
            }

            @Override
            public MethodHandle unreflect(MethodHandles.Lookup lookup, Constructor<?> method) throws IllegalAccessException {
                return lookup.unreflectConstructor(method);
            }

            @Override
            public int getArgOffset() {
                return 0;
            }
        };

        static Unreflector<Method> method(String name, boolean isStatic) {
            return new Unreflector<>() {
                @Override
                public Method[] getDeclared(Class<?> clazz) {
                    return clazz.getDeclaredMethods();
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
                    return lookup.unreflect(method);
                }

                @Override
                public int getArgOffset() {
                    return isStatic ? 1 : 0;
                }
            };
        }

        E[] getDeclared(Class<?> clazz);

        String getName(Class<?> clazz);

        boolean filter(E method);

        MethodHandle unreflect(MethodHandles.Lookup lookup, E method) throws IllegalAccessException;

        int getArgOffset();
    }
}
