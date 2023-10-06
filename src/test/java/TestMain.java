import glang.compiler.bytecode.GlangCompiler;
import glang.compiler.error.CompileFailedException;
import glang.runtime.DefaultImports;
import glang.runtime.lookup.MethodLookup;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestMain {
    private static final String SOURCE = """
        // import a.b.C
        
        // println(5)
        // println(5.5)
        // println(.5)
        // println(5.5e3)
        // println(5.5e-11); println(5.5e+11)
        // println(1234567890)
        // println(123456789012345678901234)
        // println(0xdeadbeef)
        // println(0x123)
        // println(0b101)
        println(010)
        
        // println("Hello, world!"); // Comment!
        // // println("a, b\\n\\f\\U0001f60a".escapeGlang(false));
        // /*
        //     Block comment
        //  */
        // // a == b;
        // // a >>>= b;
        // println(`%*\\\\\\``);
        
        { println("Hello, world!") }
        println(Object, String, Class)
        """;

    public static void main(String[] args) {
        testCode(args);
//        testInvoke();
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
            compiler.compile();
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

    private static void testInvoke() {
        try {
            final var imports = DefaultImports.getDefaultImports();

            final var println = (MethodLookup)imports.get("println");
            println.invoke("Hello, world!");
            println.invoke(5);
            println.invoke(5, 10, 15);
            println.invoke();

            final var optionalArgTest = (MethodLookup)imports.get("optionalArgTest");
            println.invoke(optionalArgTest.invoke("hi"));
            println.invoke(optionalArgTest.invoke("hi", "bye"));
            println.invoke(optionalArgTest.invoke("hi", "bye", "cry"));
            println.invoke(optionalArgTest.invoke("hi", "bye", "cry", 5));
            println.invoke(optionalArgTest.invoke("hi", "bye", "cry", 5, 10));
            println.invoke(optionalArgTest.invoke("hi", "bye", 5, 10));
        } catch (Throwable t) {
            if (t instanceof RuntimeException re) {
                throw re;
            }
            if (t instanceof Error e) {
                throw e;
            }
            throw new RuntimeException(t);
        }
    }
}
