import glang.compiler.bytecode.GlangCompiler;
import glang.compiler.error.CompileFailedException;
import org.objectweb.asm.ClassWriter;

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
        // println(010)
        
        5
        5.5
        .5
        5.5e3
        5.5e-11
        5.5e+11
        1234567890
        123456789012345678901234
        0xdeadbeef
        0x123
        0b101
        010
        
        // println("Hello, world!"); // Comment!
        // println("a, b\\n\\f\\U0001f60a".escapeGlang(false));
        // /*
        //     Block comment
        //  */
        //     a == b;
        // // a >>>= b;
        // println(`%*\\\\\\``);
        
        // { println("hi") }
        """;

    public static void main(String[] args) {
//        final List<Token> tokens;
//        try {
//            tokens = GlangTokenizer.tokenize(SOURCE);
//        } catch (TokenizeFailure e) {
//            System.err.println(e.getMessage());
//            return;
//        }
//        tokens.forEach(System.out::println);
//        System.out.println();
//        System.out.println();
//        System.out.println(TokenSourcePrinter.print(tokens));

//        try {
//            System.out.println(GlangTreeifier.treeify(SOURCE));
//        } catch (CompileFailedException e) {
//            System.err.println(e.getMessage());
//        }

        final Map<String, ClassWriter> result = new HashMap<>();
        final GlangCompiler compiler;
        try {
            compiler = new GlangCompiler(
                "a.b.test", SOURCE,
                c -> result.computeIfAbsent(c, k ->
                    new ClassWriter(ClassWriter.COMPUTE_FRAMES)
                )
            );
            compiler.insertDebugPrints(true);
            compiler.compile();
            compiler.getErrorCollector().throwIfFailed();
        } catch (CompileFailedException e) {
            System.err.println(e.getMessage());
            return;
        }

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
        } catch (Throwable t) {
            System.err.println("Failed to load/run main class");
            t.printStackTrace();
        }
    }
}
