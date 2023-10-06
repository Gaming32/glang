package glang.runtime;

import glang.BuiltinsNs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DefaultImports {
    private static final List<Class<?>> INIT_WITH = List.of(
        BuiltinsNs.class
    );

    private static volatile Map<String, Object> defaultImports;

    private DefaultImports() {
    }

    public static Map<String, Object> getDefaultImports() {
        if (defaultImports == null) {
            synchronized (DefaultImports.class) {
                if (defaultImports == null) {
                    defaultImports = findDefaultImports();
                }
            }
        }
        return defaultImports;
    }

    private static Map<String, Object> findDefaultImports() {
        final Map<String, Object> result = new LinkedHashMap<>();
        for (final String inLang : JdkPackageLister.listJavaLang()) {
            try {
                result.put(
                    inLang.substring(inLang.lastIndexOf('.') + 1),
                    Class.forName(inLang, false, null) // null --> bootstrap CL, which java.lang is on
                );
            } catch (ClassNotFoundException e) {
                throw new AssertionError("Missing java.lang class on the bootstrap CL?", e);
            }
        }
        for (final Class<?> initWith : INIT_WITH) {
            result.putAll(GlangRuntime.collectStarImport(initWith));
        }
        return result;
    }
}
