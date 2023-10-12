package glang.runtime.extension;

import glang.ComparableNs;
import glang.NumberNs;
import glang.StringNs;
import glang.runtime.RuntimeUtil;

import java.util.List;

public class DefaultExtensionMethods implements ExtensionMethodRegistrar {
    @Override
    public void register(ExtensionMethodRegistry registry) {
        registry.registerAll(StringNs.class);
        registry.registerAll(NumberNs.class);
        registry.registerAll(ComparableNs.class);

        for (final Class<?> simpleNumber : List.of(Integer.class, Long.class, Float.class, Double.class)) {
            final Class<?> primitive = RuntimeUtil.TO_PRIMITVE_MAP.get(simpleNumber);
            registry.registerAllStatic(simpleNumber, m -> m.getParameterTypes()[0] == primitive && !m.getName().equals("toString"));
            registry.copy(simpleNumber, "sum", "add");
        }
    }
}
