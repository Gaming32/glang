package glang.runtime.extension;

public interface ExtensionMethodRegistrar {
    void register(ExtensionMethodRegistry registry) throws ReflectiveOperationException;
}
