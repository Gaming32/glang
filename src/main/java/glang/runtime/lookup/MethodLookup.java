package glang.runtime.lookup;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
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

    protected record ApplicableMethod(Method method, int minimumArgs, int maximumArgs, Class<?>[] argTypes) {
        public static Comparator<ApplicableMethod> createComparator() {
            return Comparator.comparingInt(m -> m.maximumArgs - m.minimumArgs);
        }
    }
}
