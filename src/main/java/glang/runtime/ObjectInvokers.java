package glang.runtime;

import glang.exception.UninvokableObjectException;
import glang.runtime.lookup.MethodLookup;

import java.util.List;

public final class ObjectInvokers {
    private ObjectInvokers() {
    }

    public static Object invokeObject(Object target) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.invoke();
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass()))
                .invoke(arg0);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass()))
                .invoke(arg0, arg1);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass()))
                .invoke(arg0, arg1, arg2);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass()))
                .invoke(arg0, arg1, arg2, arg3);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass(), arg10.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass(), arg10.getClass(), arg11.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass(), arg10.getClass(), arg11.getClass(), arg12.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass(), arg10.getClass(), arg11.getClass(), arg12.getClass(), arg13.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass(), arg10.getClass(), arg11.getClass(), arg12.getClass(), arg13.getClass(), arg14.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14, Object arg15) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(arg0.getClass(), arg1.getClass(), arg2.getClass(), arg3.getClass(), arg4.getClass(), arg5.getClass(), arg6.getClass(), arg7.getClass(), arg8.getClass(), arg9.getClass(), arg10.getClass(), arg11.getClass(), arg12.getClass(), arg13.getClass(), arg14.getClass(), arg15.getClass()))
                .invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object... args) throws Throwable {
        if (target instanceof MethodLookup lookup) {
            return lookup.invoke(List.of(args));
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }
}
