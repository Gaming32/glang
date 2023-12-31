package glang.runtime;

import glang.exception.UninvokableObjectException;
import glang.runtime.lookup.MethodLookup;

import java.util.List;

// WARNING: This class is generated by the generateObjectInvokers build task, DO NOT MODIFY.
// WARNING: If changes are needed, modify the task, not this file.
public final class ObjectInvokers {
    private ObjectInvokers() {
    }

    public static Object invokeObject(Object target) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.invoke();
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0)
            )).invoke(arg0);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1)
            )).invoke(arg0, arg1);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2)
            )).invoke(arg0, arg1, arg2);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3)
            )).invoke(arg0, arg1, arg2, arg3);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4)
            )).invoke(arg0, arg1, arg2, arg3, arg4);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9), 
                GlangRuntime.getClass(arg10)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9), 
                GlangRuntime.getClass(arg10), 
                GlangRuntime.getClass(arg11)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9), 
                GlangRuntime.getClass(arg10), 
                GlangRuntime.getClass(arg11), 
                GlangRuntime.getClass(arg12)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9), 
                GlangRuntime.getClass(arg10), 
                GlangRuntime.getClass(arg11), 
                GlangRuntime.getClass(arg12), 
                GlangRuntime.getClass(arg13)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9), 
                GlangRuntime.getClass(arg10), 
                GlangRuntime.getClass(arg11), 
                GlangRuntime.getClass(arg12), 
                GlangRuntime.getClass(arg13), 
                GlangRuntime.getClass(arg14)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }

    public static Object invokeObject(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14, Object arg15) throws Throwable {
        if (target == null) {
            throw new NullPointerException("null is not invokable");
        }
        if (target instanceof Class<?> clazz) {
            target = GlangRuntime.findConstructors(clazz);
        }
        if (target instanceof MethodLookup lookup) {
            return lookup.getInvoker(List.of(
                GlangRuntime.getClass(arg0), 
                GlangRuntime.getClass(arg1), 
                GlangRuntime.getClass(arg2), 
                GlangRuntime.getClass(arg3), 
                GlangRuntime.getClass(arg4), 
                GlangRuntime.getClass(arg5), 
                GlangRuntime.getClass(arg6), 
                GlangRuntime.getClass(arg7), 
                GlangRuntime.getClass(arg8), 
                GlangRuntime.getClass(arg9), 
                GlangRuntime.getClass(arg10), 
                GlangRuntime.getClass(arg11), 
                GlangRuntime.getClass(arg12), 
                GlangRuntime.getClass(arg13), 
                GlangRuntime.getClass(arg14), 
                GlangRuntime.getClass(arg15)
            )).invoke(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
        }
        throw new UninvokableObjectException("Cannot invoke object of type " + target.getClass().getName());
    }
}
