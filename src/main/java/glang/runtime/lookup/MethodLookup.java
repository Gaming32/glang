package glang.runtime.lookup;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class MethodLookup {
    public static final CaffeineSpec CACHE_SPEC = CaffeineSpec.parse(System.getProperty(
        "glang.methodLookup.cacheSpec", "initialCapacity=4,maximumSize=64,softValues"
    ));

    protected static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

    protected final LoadingCache<List<Class<?>>, MethodHandle> cache = Caffeine.from(CACHE_SPEC).build(this::lookup);

    protected abstract MethodHandle lookup(List<Class<?>> args) throws NoSuchMethodException;

    public MethodHandle getInvoker(List<Class<?>> argTypes) {
        return cache.get(argTypes);
    }

    public Object invoke(List<Object> args) throws Throwable {
        final List<Class<?>> argTypes = new ArrayList<>(args.size());
        for (final Object arg : args) {
            argTypes.add(arg.getClass());
        }
        return cache.get(argTypes).invokeWithArguments(args);
    }

    public Object invoke() throws Throwable {
        return cache.get(List.of()).invoke();
    }

    public Object invoke(Object arg1) throws Throwable {
        return cache.get(List.of(arg1.getClass())).invoke(arg1);
    }

    public Object invoke(Object arg1, Object arg2) throws Throwable {
        return cache.get(List.of(arg1.getClass(), arg2.getClass())).invoke(arg1, arg2);
    }

    public Object invoke(Object arg1, Object arg2, Object arg3) throws Throwable {
        return cache.get(List.of(arg1.getClass(), arg2.getClass(), arg3.getClass())).invoke(arg1, arg2, arg3);
    }

    public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        return cache.get(List.of(arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass())).invoke(arg1, arg2, arg3, arg4);
    }

    public Object invoke(
        Object arg1,
        Object arg2,
        Object arg3,
        Object arg4,
        Object arg5
    ) throws Throwable {
        return cache.get(List.of(
            arg1.getClass(),
            arg2.getClass(),
            arg3.getClass(),
            arg4.getClass(),
            arg5.getClass()
        )).invoke(arg1, arg2, arg3, arg4, arg5);
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
        };

        static Unreflector<Method> staticMethod(String name) {
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
                    return Modifier.isStatic(method.getModifiers()) && method.getName().equals(name);
                }

                @Override
                public MethodHandle unreflect(MethodHandles.Lookup lookup, Method method) throws IllegalAccessException {
                    return lookup.unreflect(method);
                }
            };
        }

        E[] getDeclared(Class<?> clazz);

        String getName(Class<?> clazz);

        boolean filter(E method);

        MethodHandle unreflect(MethodHandles.Lookup lookup, E method) throws IllegalAccessException;
    }
}
