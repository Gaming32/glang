package glang.runtime.lookup;

import glang.runtime.OptionalParameter;
import glang.runtime.RuntimeUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.*;

public class SimpleMethodLookup<E extends Executable> extends MethodLookup {
    public static final int MAX_ARGS = 255; // Maybe increase this?

    private final Class<?> clazz;
    private final Unreflector<E> unreflector;
    private final List<ApplicableMethod<E>> applicableMethods;

    public SimpleMethodLookup(Class<?> clazz, Unreflector<E> unreflector, int minArgs, int maxArgs) throws NoSuchMethodException {
        this.clazz = clazz;
        this.unreflector = unreflector;
        final List<ApplicableMethod<E>> applicableMethods = new ArrayList<>();
        for (final E method : unreflector.getDeclared(clazz)) {
            if (!Modifier.isPublic(method.getModifiers()) || !unreflector.filter(method)) continue;
            final int methodMaxArgs = getMaxArgs(method);
            if (maxArgs < methodMaxArgs) continue;
            final int methodMinArgs = getMinArgs(method);
            if (minArgs > methodMinArgs) continue;
            applicableMethods.add(new ApplicableMethod<>(method, methodMinArgs, methodMaxArgs, method.getParameterTypes()));
        }
        if (applicableMethods.isEmpty()) {
            throw new NoSuchMethodException(toString());
        }
        this.applicableMethods = List.copyOf(applicableMethods);
    }

    public SimpleMethodLookup(Class<?> clazz, Unreflector<E> unreflector, int minArgs) throws NoSuchMethodException {
        this(clazz, unreflector, minArgs, MAX_ARGS);
    }

    public SimpleMethodLookup(Class<?> clazz, Unreflector<E> unreflector) throws NoSuchMethodException {
        this(clazz, unreflector, 0, MAX_ARGS);
    }

    @Override
    public String toString() {
        return "public fn " + unreflector.getName(clazz);
    }

    @Override
    protected MethodHandle lookup(List<Class<?>> args) throws NoSuchMethodException {
        final int argOffset = unreflector.getArgOffset();
        final int argCount = args.size() - argOffset;
        final List<ApplicableMethod<E>> matches = new ArrayList<>();
        methodSearch:
        for (final ApplicableMethod<E> method : applicableMethods) {
            if (method.minimumArgs() > argCount || method.maximumArgs() < argCount) continue;
            final Class<?>[] argTypes = method.argTypes();
            for (int i = 0, l = method.minimumArgs(); i < l; i++) {
                if (!RuntimeUtil.isAssignableFrom(argTypes[i], args.get(i + argOffset))) {
                    continue methodSearch;
                }
            }
            if (method.method().isVarArgs()) {
                final Class<?> argType = argTypes[argTypes.length - 1].componentType();
                for (int i = argTypes.length - 1; i < argCount; i++) {
                    if (!RuntimeUtil.isAssignableFrom(argType, args.get(i + argOffset))) {
                        continue methodSearch;
                    }
                }
            }
            matches.add(method);
        }

        if (matches.isEmpty()) {
            throw new NoSuchMethodException(this + " with args " + RuntimeUtil.prettyPrint(args));
        }
        return adapt(findBestMatch(matches), args);
    }

    private MethodHandle adapt(ApplicableMethod<E> method, List<Class<?>> args) throws NoSuchMethodException {
        final E rMethod = method.method();

        MethodHandle handle;
        try {
            handle = unreflector.unreflect(LOOKUP, rMethod);
        } catch (IllegalAccessException e) {
            handle = findAccessibleSlow(unreflector, rMethod);
            if (handle == null) {
                final NoSuchMethodException e2 = new NoSuchMethodException("Cannot access " + rMethod);
                e2.initCause(e);
                throw e2;
            }
        }

        final int argOffset = unreflector.getArgOffset();

        int optionalArgs = rMethod.getParameterCount() - method.minimumArgs();
        if (rMethod.isVarArgs()) {
            optionalArgs--;
        }
        if (optionalArgs > 0) {
            if (args.size() < method.minimumArgs() + optionalArgs) {
                final Object[] insertedArgs = new Object[method.minimumArgs() + optionalArgs - args.size()];
                for (int i = 0; i < insertedArgs.length; i++) {
                    if (i < optionalArgs) {
                        insertedArgs[i] = OptionalParameter.absent();
                    } else {
                        insertedArgs[i] = new Object[0];
                    }
                }
                handle = MethodHandles.insertArguments(handle, args.size() + argOffset, insertedArgs);
            }
            for (int i = Math.min(args.size(), method.minimumArgs() + optionalArgs) - 1, e = method.minimumArgs(); i >= e; i--) {
                handle = MethodHandles.collectArguments(handle, i + argOffset, OptionalParameter.PRESENT_MH);
            }
        }

        if (rMethod.isVarArgs()) {
            handle = handle.asVarargsCollector(method.argTypes()[rMethod.getParameterCount() - 1]);
        }

        return handle;
    }

    private static <E extends Executable> ApplicableMethod<E> findBestMatch(List<ApplicableMethod<E>> matches) {
        ApplicableMethod<E> singleMatch = matches.get(0);
        if (matches.size() > 1) {
            final Comparator<ApplicableMethod<E>> comparator = ApplicableMethod.createComparator();
            for (int i = 1, l = matches.size(); i < l; i++) {
                final ApplicableMethod<E> match = matches.get(i);
                if (comparator.compare(match, singleMatch) < 0) {
                    singleMatch = match;
                }
            }
        }
        return singleMatch;
    }

    private static int getMinArgs(Executable method) {
        int count = method.getParameterCount();
        if (method.isVarArgs()) {
            count--;
        }
        if (count > 0) {
            final Class<?>[] argTypes = method.getParameterTypes();
            while (count > 0) {
                if (argTypes[count - 1] == OptionalParameter.class) {
                    count--;
                } else {
                    break;
                }
            }
        }
        return count;
    }

    private static int getMaxArgs(Executable method) {
        return method.isVarArgs() ? MAX_ARGS : method.getParameterCount();
    }

    @Nullable
    private static <E extends Executable> MethodHandle findAccessibleSlow(Unreflector<E> unreflector, E method) {
        final Set<Class<?>> searched = new HashSet<>();
        final Queue<Class<?>> toSearch = new ArrayDeque<>();
        addParents(searched, toSearch, method.getDeclaringClass());
        while (!toSearch.isEmpty()) {
            final Class<?> next = toSearch.remove();
            final E searchMethod = unreflector.findEquivalentIn(next, method);
            if (searchMethod != null) {
                try {
                    return unreflector.unreflect(LOOKUP, searchMethod);
                } catch (IllegalAccessException ignored) {
                }
            }
            addParents(searched, toSearch, next);
        }
        return null;
    }

    private static void addParents(Set<Class<?>> searched, Collection<Class<?>> toSearch, Class<?> add) {
        if (add.getSuperclass() != null && searched.add(add.getSuperclass())) {
            toSearch.add(add.getSuperclass());
        }
        for (final Class<?> intf : add.getInterfaces()) {
            if (searched.add(intf)) {
                toSearch.add(intf);
            }
        }
    }
}
