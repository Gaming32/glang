import glang.compiler.bytecode.GlangCompiler;
import glang.compiler.error.CompileFailedException;
import glang.runtime.cl.GlangClassLoader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestMain {
    public static final String SOURCE = """
        import java.lang.invoke.MethodHandles
        println(MethodHandles.lookup().findClass("TestMain").SOURCE)
        """;

    public static void main(String[] args) {
//        testCL(args);
        testCode(args);
//        testInvoke();
    }

    private static void testCL(String[] args) {
        try (var cl = new GlangClassLoader()) {
            cl.loadClass("a.b.Test2Ns")
                .getDeclaredMethod("main", String[].class)
                .invoke(null, (Object)args);
        } catch (InvocationTargetException e) {
            System.err.println("Failed to run main class");
            e.getCause().printStackTrace();
        } catch (Throwable t) {
            System.err.println("Failed to load main class");
            t.printStackTrace();
        }
    }

    private static void testCode(String[] args) {
        final Map<String, ClassWriter> result = new HashMap<>();
        final GlangCompiler compiler;
        try {
            compiler = new GlangCompiler(
                "a.b.test", SOURCE,
                c -> result.computeIfAbsent(c, k ->
                    new ClassWriter(ClassWriter.COMPUTE_FRAMES)
                )
            );
//            compiler.insertDebugPrints(true);
            compiler.compile("test.glang");
            compiler.getErrorCollector().throwIfFailed();
        } catch (CompileFailedException e) {
            System.err.println(e.getMessage());
            return;
        }

        result.forEach((name, writer) -> {
            try {
                final Path path = Path.of("dump", name.replace('.', '/').concat(".class"));
                Files.createDirectories(path.getParent());
                Files.write(path, writer.toByteArray());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        final ClassLoader cl = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                final ClassWriter writer = result.get(name);
                if (writer == null) {
                    throw new ClassNotFoundException(name);
                }
                final byte[] bytes = writer.toByteArray();
                return defineClass(name, bytes, 0, bytes.length);
            }
        };
        try {
            cl.loadClass(compiler.getClassName())
                .getDeclaredMethod("main", String[].class)
                .invoke(null, (Object)args);
        } catch (InvocationTargetException e) {
            System.err.println("Failed to run main class");
            e.getCause().printStackTrace();
        } catch (Throwable t) {
            System.err.println("Failed to load main class");
            t.printStackTrace();
        }
    }
}
