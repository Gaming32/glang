package glang.runtime.extension;

import glang.ComparableNs;
import glang.IntegerNs;
import glang.StringNs;

public class DefaultExtensionMethods implements ExtensionMethodRegistrar {
    @Override
    public void register(ExtensionMethodRegistry registry) {
        registry.registerAll(StringNs.class);
        registry.registerAll(IntegerNs.class);
        registry.registerAll(ComparableNs.class);

        registry.registerAllStatic(Integer.class, m -> m.getParameterTypes()[0] == int.class && !m.getName().equals("toString"));
        registry.copy(Integer.class, "sum", "add");
    }
}
