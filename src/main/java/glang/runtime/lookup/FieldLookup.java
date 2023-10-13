package glang.runtime.lookup;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import glang.util.SoftCacheMap;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletionException;

public final class FieldLookup {
    public static final CaffeineSpec CACHE_SPEC = CaffeineSpec.parse(System.getProperty(
        "glang.fieldLookup.cacheSpec", "softValues"
    ));

    private static final SoftCacheMap<Class<?>, FieldLookup> INSTANCE_CACHE =
        new SoftCacheMap<>(clazz -> new FieldLookup(clazz, false));
    private static final SoftCacheMap<Class<?>, FieldLookup> STATIC_CACHE =
        new SoftCacheMap<>(clazz -> new FieldLookup(clazz, true));

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final SoftCacheMap<String, ResolvedField> lookup;
    private final Class<?> clazz;
    private final boolean forClass;

    private FieldLookup(Class<?> clazz, boolean forClass) {
        lookup = new SoftCacheMap<>(name -> {
            try {
                return getField(clazz, forClass, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.clazz = clazz;
        this.forClass = forClass;
    }

    public static FieldLookup get(Class<?> clazz, boolean forClass) {
        return forClass ? STATIC_CACHE.get(clazz) : INSTANCE_CACHE.get(clazz);
    }

    public ResolvedField getField(String name) throws Throwable {
        try {
            return lookup.get(name);
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    public Object get(Object obj, String name) throws Throwable {
        return getField(name).getter.invoke(obj);
    }

    public Object set(Object obj, String name, Object value) throws Throwable {
        final ResolvedField field = getField(name);
        if (field.setter == null) {
            throw new IllegalAccessException("Cannot set final field " + clazz.getCanonicalName() + "." + name);
        }
        field.setter.invoke(obj, value);
        return value;
    }

    private static ResolvedField getField(Class<?> clazz, boolean isStatic, String name) throws Exception {
        Field field;
        try {
            field = clazz.getField(name);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        if (field == null || (!Modifier.isStatic(field.getModifiers()) && isStatic)) {
            throw new NoSuchFieldException((isStatic ? "static " : "") + clazz.getCanonicalName() + "." + name);
        }

        MethodHandle getter = LOOKUP.unreflectGetter(field);
        if (isStatic) {
            getter = MethodHandles.dropArguments(getter, 0, Class.class);
        } else if (Modifier.isStatic(field.getModifiers())) {
            getter = MethodHandles.dropArguments(getter, 0, field.getDeclaringClass());
        }

        MethodHandle setter = null;
        if (!Modifier.isFinal(field.getModifiers())) {
            setter = LOOKUP.unreflectSetter(field);
            if (isStatic) {
                setter = MethodHandles.dropArguments(setter, 0, Class.class);
            } else if (Modifier.isStatic(field.getModifiers())) {
                setter = MethodHandles.dropArguments(setter, 0, field.getDeclaringClass());
            }
        }

        return new ResolvedField(getter, setter);
    }

    public record ResolvedField(MethodHandle getter, @Nullable MethodHandle setter) {
    }
}
