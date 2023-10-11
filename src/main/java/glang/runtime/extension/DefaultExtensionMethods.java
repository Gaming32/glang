package glang.runtime.extension;

import glang.StringNs;

public class DefaultExtensionMethods implements ExtensionMethodRegistrar {
    @Override
    public void register(ExtensionMethodRegistry registry) {
        registry.registerAll(StringNs.class);
    }
}
