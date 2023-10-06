package glang.runtime.lookup;

import glang.runtime.OptionalParameter;
import glang.runtime.RuntimeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StaticMethodLookup extends MethodLookup {
    public static final int MAX_ARGS = 255; // Maybe increase this?

    private final Class<?> clazz;
    private final String name;
    private final List<ApplicableMethod> applicableMethods;

    public StaticMethodLookup(Class<?> clazz, String name, int minArgs, int maxArgs) {
        this.clazz = clazz;
        this.name = name;
        final List<ApplicableMethod> applicableMethods = new ArrayList<>();
        for (final Method method : clazz.getDeclaredMethods()) {
            if (
                !Modifier.isStatic(method.getModifiers()) ||
                    !method.getName().equals(name) ||
                    !method.canAccess(null)
            ) continue;
            final int methodMaxArgs = getMaxArgs(method);
            if (maxArgs < methodMaxArgs) continue;
            final int methodMinArgs = getMinArgs(method);
            if (minArgs > methodMinArgs) continue;
            applicableMethods.add(new ApplicableMethod(method, methodMinArgs, methodMaxArgs, method.getParameterTypes()));
        }
        this.applicableMethods = List.copyOf(applicableMethods);
    }

    public StaticMethodLookup(Class<?> clazz, String name, int minArgs) {
        this(clazz, name, minArgs, MAX_ARGS);
    }

    public StaticMethodLookup(Class<?> clazz, String name) {
        this(clazz, name, 0, MAX_ARGS);
    }

    @Override
    public String toString() {
        return "public fn " + clazz.getCanonicalName() + "." + name;
    }

    @Override
    protected MethodHandle lookup(List<Class<?>> args) throws NoSuchMethodException {
        final int argCount = args.size();
        final List<ApplicableMethod> matches = new ArrayList<>();
        methodSearch:
        for (final ApplicableMethod method : applicableMethods) {
            if (method.minimumArgs() > argCount || method.maximumArgs() < argCount) continue;
            final Class<?>[] argTypes = method.argTypes();
            for (int i = 0, l = method.minimumArgs(); i < l; i++) {
                if (!RuntimeUtil.isAssignableFrom(argTypes[i], args.get(i))) {
                    continue methodSearch;
                }
            }
            if (method.method().isVarArgs()) {
                final Class<?> argType = argTypes[argTypes.length - 1].componentType();
                for (int i = argTypes.length - 1, l = args.size(); i < l; i++) {
                    if (!RuntimeUtil.isAssignableFrom(argType, args.get(i))) {
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

    private MethodHandle adapt(ApplicableMethod method, List<Class<?>> args) throws NoSuchMethodException {
        final Method rMethod = method.method();

        MethodHandle handle;
        try {
            handle = LOOKUP.unreflect(rMethod);
        } catch (IllegalAccessException e) {
            final NoSuchMethodException e2 = new NoSuchMethodException("Cannot access " + rMethod);
            e2.initCause(e);
            throw e2;
        }

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
                handle = MethodHandles.insertArguments(handle, args.size(), insertedArgs);
            }
            for (int i = Math.min(args.size(), method.minimumArgs() + optionalArgs) - 1, e = method.minimumArgs(); i >= e; i--) {
                handle = MethodHandles.collectArguments(handle, i, OptionalParameter.PRESENT_MH);
            }
        }

        if (rMethod.isVarArgs()) {
            handle = handle.asVarargsCollector(method.argTypes()[rMethod.getParameterCount() - 1]);
        }

        return handle;
    }

    private static ApplicableMethod findBestMatch(List<ApplicableMethod> matches) {
        ApplicableMethod singleMatch = matches.get(0);
        if (matches.size() > 1) {
            final Comparator<ApplicableMethod> comparator = ApplicableMethod.createComparator();
            for (int i = 1, l = matches.size(); i < l; i++) {
                final ApplicableMethod match = matches.get(i);
                if (comparator.compare(match, singleMatch) < 0) {
                    singleMatch = match;
                }
            }
        }
        return singleMatch;
    }

    private static int getMinArgs(Method method) {
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

    private static int getMaxArgs(Method method) {
        return method.isVarArgs() ? MAX_ARGS : method.getParameterCount();
    }
}
