package glang.runtime.extension;

import glang.StringNs;

public class DefaultExtensionMethods implements ExtensionMethodRegistrar {
    @Override
    public void register(ExtensionMethodRegistry registry) throws ReflectiveOperationException {
        registry.registerAll(StringNs.class);
        registry.registerAllStatic(Integer.class, m -> m.getParameterTypes()[0] == int.class && !m.getName().equals("toString"));
        registry.copy(Integer.class, "sum", "add");
    }
}
